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
package org.jfxvnc.app.presentation.about;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class AboutViewPresenter implements Initializable {

  @FXML
  private TextArea build;
  @FXML
  private TextArea license;
  @FXML
  private TextArea thirdLicense;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    String version = AboutViewPresenter.class.getPackage().getImplementationVersion();
    appendBuildLine(String.format("JavaFX VNC (%s)", version != null ? version : "DEV"));
    appendBuildLine("Copyright © 2015 - comtel2000");
    appendBuildLine(null);
    appendBuildLine(System.getProperty("java.runtime.name"));
    appendBuildLine(String.format("Version:\t%s (%s)", System.getProperty("java.runtime.version"), System.getProperty("java.vendor")));
    appendBuildLine(String.format("OS: \t%s (%s) %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version")));

  }

  private void appendBuildLine(String text) {
    if (text != null) {
      build.appendText(text);
    }
    build.appendText(System.lineSeparator());
  }
}
