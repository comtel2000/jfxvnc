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
package org.jfxvnc.ui.presentation.about;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.airhacks.afterburner.injection.Injector;

public class AboutViewTest {
  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AboutViewTest.class);
  // private static Stage stage;

  @BeforeClass
  public static void init() {
    Injector.setLogger((t) -> logger.info(t));
    // AboutView view = new AboutView();
    // stage = new Stage(StageStyle.UNDECORATED);
    // stage.setScene(new Scene(view.getView()));
    // stage.show();
  }

  public static void end() {
    Injector.forgetAll();
    // stage.close();
  }

  @Test
  public void aboutView() {

    AboutViewPresenter presenter = Injector.instantiatePresenter(AboutViewPresenter.class);
    assertNotNull(presenter);

  }

}
