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
package org.jfxvnc.swing;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.security.SecurityType;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.swing.control.SwingVncImageView;
import org.jfxvnc.swing.service.VncRenderService;

public class SwingDemo implements InternalFrameListener {

  private static final String IP = "127.0.0.1";
  private static final int PORT = 5902;
  private static final String PWD = "comtel";

  private VncRenderService vncService;
  private JInternalFrame iframe;
  private SwingVncImageView vncView;

  public SwingDemo(boolean singleFrame) {
    JFrame frame = new JFrame("SwingDemo");
    frame.setSize(900, 700);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    manager.addKeyEventDispatcher(new KeyDispatcher());

    if (singleFrame) {
      frame.setContentPane(createVncView());
      frame.setBackground(Color.GRAY);
      frame.setVisible(true);
      return;
    }

    JDesktopPane pane = new JDesktopPane();
    frame.setContentPane(pane);

    iframe = new JInternalFrame("VNC", true, true, true, true);
    iframe.setSize(850, 650);
    iframe.setOpaque(false);
    iframe.addInternalFrameListener(this);
    iframe.setContentPane(createVncView());
    pane.add(iframe);
    frame.setVisible(true);
    iframe.setVisible(true);

  }

  private JComponent createVncView() {

    vncService = new VncRenderService();
    vncView = new SwingVncImageView(true, false);
    // vncView.setFixBounds(0, 0, 800, 600);
    vncService.setEventConsumer(vncView);
    vncService.inputEventListenerProperty().addListener(l -> vncView.registerInputEventListener(vncService.inputEventListenerProperty().get()));
    return vncView;
  }

  @Override
  public void internalFrameIconified(InternalFrameEvent e) {
    disconnect();
  }

  @Override
  public void internalFrameDeiconified(InternalFrameEvent e) {
    connect();

  }

  @Override
  public void internalFrameOpened(InternalFrameEvent e) {
    System.err.println("opended");
  }

  @Override
  public void internalFrameClosing(InternalFrameEvent e) {}


  @Override
  public void internalFrameClosed(InternalFrameEvent e) {
    disconnect();
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e) {}

  @Override
  public void internalFrameDeactivated(InternalFrameEvent e) {}

  private void disconnect() {
    if (vncService != null) {
      System.err.println("disconnecting..");
      vncView.setEnabled(false);
      vncService.disconnect();
    }
  }

  class KeyDispatcher implements KeyEventDispatcher {
    public boolean dispatchKeyEvent(KeyEvent e) {
      if (e.getID() == KeyEvent.KEY_TYPED) {
        if (e.getKeyChar() == 'c') {
          connect();
        }
        if (e.getKeyChar() == 'd') {
          disconnect();
        }
      }
      return false;
    }
  }

  private void connect() {
    if (vncService == null) {
      throw new IllegalStateException("service not initialized");
    }
    System.setSecurityManager(null);
    // ResourceLeakDetector.setLevel(Level.ADVANCED);
    ProtocolConfiguration prop = vncService.getConfiguration();
    prop.hostProperty().set(IP);
    prop.portProperty().set(PORT);
    prop.clientPixelFormatProperty().set(PixelFormat.RGB_555);
    prop.rawEncProperty().set(true);
    prop.hextileEncProperty().set(true);
    prop.zlibEncProperty().set(true);
    prop.securityProperty().set(PWD != null && !PWD.isEmpty() ? SecurityType.VNC_Auth : SecurityType.NONE);
    prop.passwordProperty().set(PWD);
    vncView.setEnabled(true);
    vncService.connect();
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    System.err.println("connect:\tpress 'c' key");
    System.err.println("disconnect:\tpress 'd' key");
    SwingUtilities.invokeLater(() -> new SwingDemo(true));
  }

}
