package org.jfxvnc.ui.presentation.about;

/*
 * #%L
 * jfxvnc-ui
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.airhacks.afterburner.injection.Injector;

public class AboutViewTest {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AboutViewTest.class);
    private static Stage stage;

    @BeforeClass
    public static void init() {
	Injector.setLogger((t) -> logger.info(t));
//	AboutView view = new AboutView();
//	stage = new Stage(StageStyle.UNDECORATED);
//	stage.setScene(new Scene(view.getView()));
//	stage.show();
    }

    public static void end() {
	Injector.forgetAll();
//	stage.close();
    }

    @Test
    public void aboutView() {
	
	AboutViewPresenter presenter = (AboutViewPresenter) Injector.instantiatePresenter(AboutViewPresenter.class);
	assertNotNull(presenter);

    }

}
