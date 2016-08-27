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
package org.jfxvnc.ui;

import java.util.stream.IntStream;

import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.PlusMinusSlider;
import org.jfxvnc.ui.presentation.about.AboutView;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DemoApp extends Application {

  private final SimpleDoubleProperty sceneWidthProperty = new SimpleDoubleProperty(1024);
  private final SimpleDoubleProperty sceneHeightProperty = new SimpleDoubleProperty(768);

  private int w = 19;
  private int h = 19;

  // CursorImageRect [hotspotX=9, hotspotY=9, width=19, height=19,
  // bitmask.length=57, pixels.length=361]
  private final byte[] bitmask = new byte[] { 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, -16, -31, -32, 0, 64, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0 };
  private final int[] pixels = new int[] { -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -1, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -1, -16777216, -1, -16777216, -16777216, -16777216, -16777216, -1,
      -16777216, -1, -16777216, -16777216, -16777216, -16777216, -1, -16777216, -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -1, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216,
      -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216 };

  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("Demo (" + System.getProperty("javafx.runtime.version") + ")");

    // remove transparent pixels
    int maskBytesPerRow = Math.floorDiv((w + 7), 8);
    IntStream.range(0, h).forEach(y -> IntStream.range(0, w)
        .filter(x -> (bitmask[(y * maskBytesPerRow) + Math.floorDiv(x, 8)] & (1 << 7 - Math.floorMod(x, 8))) < 1).forEach(x -> pixels[y * w + x] = 0));
    // for (int y = 0; y < h; y++) {
    // for (int x = 0; x < w; x++) {
    // int bit = 7 - Math.floorMod(x, 8);
    // if ((bitmask[(y * maskBytesPerRow) + Math.floorDiv(x, 8)] & (1 <<
    // bit)) < 1) {
    // pixels[y * w + x] = 0;
    // }
    // }
    // }

    Dimension2D dim = ImageCursor.getBestSize(w, h);

    WritableImage cImage = new WritableImage((int) dim.getWidth(), (int) dim.getHeight());
    cImage.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);

    MasterDetailPane mdPane = new MasterDetailPane(Side.RIGHT);

    ImageView imgView = new ImageView(new Image(VncClientApp.class.getResourceAsStream("icon.png")));
    // imgView.setFitHeight(imgView.getImage().getHeight());
    // imgView.setFitWidth(imgView.getImage().getWidth());
    imgView.setPreserveRatio(true);

    imgView.setCursor(new ImageCursor(cImage, 0, 0));
    ScrollPane scrollPane = new ScrollPane(imgView);
    scrollPane.setFitToHeight(true);
    scrollPane.setFitToWidth(true);

    BorderPane mainPane = new BorderPane();

    PlusMinusSlider slider = new PlusMinusSlider();

    mdPane.setMasterNode(scrollPane);

    // TitledPane tp = new TitledPane("controlsfx css bug #457", new
    // ComboBox<Object>());

    AboutView about = new AboutView();
    mdPane.setDetailNode(about.getView());

    mdPane.setShowDetailNode(true);

    slider.setOnValueChanged((e) -> {

      double scaleFactor = e.getValue() + 1;

      // imgView.getTransforms().clear();
      // imgView.getTransforms().add(new Scale(scaleFactor, scaleFactor));
      // imgView.fitWidthProperty().set(imgView.fitWidthProperty().get() *
      // scaleFactor);

      imgView.setFitHeight(imgView.getImage().getHeight() * scaleFactor);

      // imgView.setFitHeight(imgView.getImage().getHeight() *
      // scaleFactor);
      // imgView.setFitWidth(imgView.getImage().getWidth() * scaleFactor);
      // System.out.println(imgView.getBoundsInParent());
      // System.out.println(imgView.getBoundsInLocal());

      // scrollPane.setViewportBounds(imgView.getBoundsInLocal());
      // imgView.setFitHeight(imgView.getFitHeight() * scaleFactor);
    });

    mainPane.setCenter(mdPane);
    mainPane.setBottom(new ToolBar(slider));
    Scene scene = new Scene(mainPane, sceneWidthProperty.get(), sceneHeightProperty.get());

    stage.setScene(scene);
    stage.show();

  }

  public static void main(String[] args) {
    launch(args);
  }

}
