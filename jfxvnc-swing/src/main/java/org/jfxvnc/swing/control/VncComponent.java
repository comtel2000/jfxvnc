/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package org.jfxvnc.swing.control;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import io.netty.buffer.ByteBuf;

public class VncComponent extends JComponent {

  private static final long serialVersionUID = 5723358407311608532L;
  protected BufferedImage currentImage = null;
  private final Lock bufferLock = new ReentrantLock();

  private final RenderComponent renderComponent;
  private boolean keepAspect = true;
  private int imgWidth, imgHeight;
  private final boolean resizable;
  private Rectangle fixBounds;

  private Timer resourceTimer;
  private VolatileImage volatileImage;
  private boolean frameRendered = false;
  private volatile boolean updatePending = false;
  private final boolean useVolatile;

  public VncComponent() {
    this(true, true);
  }

  public VncComponent(boolean useVolatile, boolean resizable) {

    this.useVolatile = useVolatile;
    this.resizable = resizable;
    this.imgWidth = 0;
    this.imgHeight = 0;

    if (resizable) {
      setLayout(null);
    }
    add(renderComponent = new RenderComponent());

    if (resizable) {
      renderComponent.addPropertyChangeListener("preferredSize", (evt) -> {
        scaleVideoOutput();
      });
    }

    if (resizable) {
      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent evt) {
          scaleVideoOutput();
        }
      });
    }
    // renderComponent.setBounds(getBounds());
    setOpaque(false);
  }

  public void setResourceTimerEnabled(boolean flag, int delay) {
    if (resourceTimer != null) {
      resourceTimer.stop();
      resourceTimer = null;
    }
    if (flag && useVolatile) {
      resourceTimer = new Timer(delay, resourceReaper);
    }
  }

  private void scaleVideoOutput() {
    if (fixBounds != null) {
      renderComponent.setBounds(fixBounds);
      return;
    }

    final Component child = renderComponent;
    final Dimension childSize = child.getPreferredSize();
    final int width = getWidth(), height = getHeight();

    double aspect = keepAspect ? (double) childSize.width / (double) childSize.height : 1.0f;

    int scaledHeight = (int) ((double) width / aspect);
    if (!keepAspect) {
      child.setBounds(child.getX(), child.getY(), width, height);
    } else if (scaledHeight < height) {
      final int y = (height - scaledHeight) / 2;
      child.setBounds(0, y, width, scaledHeight);
    } else {
      final int scaledWidth = (int) ((double) height * aspect);
      final int x = (width - scaledWidth) / 2;
      child.setBounds(x, 0, scaledWidth, height);
    }
  }

  public boolean isKeepAspect() {
    return keepAspect;
  }

  public boolean isResizable() {
    return resizable;
  }

  protected ActionListener resourceReaper = (t) -> {
    if (!frameRendered) {
      if (volatileImage != null) {
        volatileImage.flush();
        volatileImage = null;
      }
      resourceTimer.stop();
    }
    frameRendered = false;
  };


  public void setKeepAspect(boolean keepAspect) {
    this.keepAspect = keepAspect;
  }

  @Override
  public boolean isLightweight() {
    return true;
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (isOpaque()) {
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setColor(getBackground());
      g2d.fillRect(0, 0, getWidth(), getHeight());
      g2d.dispose();
    }
  }

  private void renderVolatileImage(BufferedImage bufferedImage, int x, int y, int w, int h) {
    do {
      final GraphicsConfiguration gc = getGraphicsConfiguration();
      if (volatileImage == null || volatileImage.getWidth() != imgWidth || volatileImage.getHeight() != imgHeight
          || volatileImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {

        if (volatileImage != null) {
          volatileImage.flush();
        }

        volatileImage = gc.createCompatibleVolatileImage(w, h);
        volatileImage.setAccelerationPriority(1.0f);
      }
      Graphics2D g = volatileImage.createGraphics();
      // g.drawImage(bufferedImage, 0,0, null);
      g.drawImage(bufferedImage, x, y, x + w, y + h, x, y, x + w, y + h, null);
      g.dispose();
    } while (volatileImage.contentsLost());
  }


  private void volatileRender(Graphics g, int x, int y, int w, int h) {
    do {
      if (updatePending || volatileImage == null || volatileImage.validate(getGraphicsConfiguration()) != VolatileImage.IMAGE_OK) {
        bufferLock.lock();
        try {
          updatePending = false;
          renderVolatileImage(currentImage, x, y, w, h);
        } finally {
          bufferLock.unlock();
        }
      }
      g.drawImage(volatileImage, x, y, w, h, null);
    } while (volatileImage.contentsLost());
  }

  private void heapRender(Graphics g, int x, int y, int w, int h) {
    updatePending = false;
    g.drawImage(currentImage, x, y, w, h, null);

  }

  private void render(Graphics g, int x, int y, int w, int h) {
    if (useVolatile) {
      volatileRender(g, x, y, w, h);
    } else {
      heapRender(g, x, y, w, h);
    }
    if (!frameRendered) {
      frameRendered = true;
      if (resourceTimer != null && !resourceTimer.isRunning()) {
        resourceTimer.restart();
      }
    }
  }


  protected final void update(final int x, final int y, final int width, final int height) {
    SwingUtilities.invokeLater(() -> {
      if (currentImage.getWidth() != imgWidth || currentImage.getHeight() != imgHeight) {
        imgWidth = currentImage.getWidth();
        imgHeight = currentImage.getHeight();

        setPreferredSize(new Dimension(imgWidth, imgHeight));
        renderComponent.setPreferredSize(getPreferredSize());
        if (!resizable) {
          setSize(getPreferredSize());
          setMinimumSize(getPreferredSize());
          setMaximumSize(getPreferredSize());
          renderComponent.setSize(getPreferredSize());
          renderComponent.setMinimumSize(getPreferredSize());
          renderComponent.setMaximumSize(getPreferredSize());
        }

      }
      if (renderComponent.isVisible()) {
        // renderComponent.paintImmediately(x, y, width, height);
        renderComponent.repaint(x, y, width, height);
      }
    });
  }

  protected BufferedImage getBufferedImage(int width, int height, int type) {
    if (currentImage != null && currentImage.getWidth() == width && currentImage.getHeight() == height && currentImage.getType() == type) {
      return currentImage;
    }
    if (currentImage != null) {
      currentImage.flush();
    }
    currentImage = new BufferedImage(width, height, type);
    currentImage.setAccelerationPriority(0.0f);
    return currentImage;
  }

  protected BufferedImage getBufferedImage(int width, int height, IndexColorModel cm) {
    if (currentImage != null && currentImage.getWidth() == width && currentImage.getHeight() == height
        && currentImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
      return currentImage;
    }
    if (currentImage != null) {
      currentImage.flush();
    }
    currentImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, cm);
    currentImage.setAccelerationPriority(0.0f);
    return currentImage;
  }


  protected void renderFrame(boolean isPrerollFrame, int x, int y, int width, int height, ByteBuf img) {

    if (updatePending && !isPrerollFrame) {
      return;
    }

    final WritableRaster raster = currentImage.getRaster();
    if (img.hasArray()) {
      raster.setDataElements(x, y, width, height, img.array());
    } else {
      byte[] pixels = new byte[img.readableBytes()];
      img.readBytes(pixels);
      raster.setDataElements(x, y, width, height, pixels);
    }
    updatePending = true;

    update(x, y, width, height);
  }


  public void clear() {
    renderComponent.removeAll();
  }

  public void setFixBounds(int x, int y, int width, int height) {
    fixBounds = new Rectangle(x, y, width, height);
  }

  private class RenderComponent extends JComponent {

    private static final long serialVersionUID = -863557731953632418L;

    @Override
    protected void paintComponent(Graphics g) {
      int width = getWidth(), height = getHeight();
      Graphics2D g2d = (Graphics2D) g;
      if (currentImage != null) {
        render(g2d, 0, 0, width, height);
      } else {
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);
      }
      g2d.dispose();
    }

    @Override
    public boolean isOpaque() {
      return VncComponent.this.isOpaque();
    }

    @Override
    public boolean isLightweight() {
      return true;
    }
  }

}
