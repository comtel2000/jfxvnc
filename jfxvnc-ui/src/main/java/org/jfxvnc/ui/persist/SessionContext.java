package org.jfxvnc.ui.persist;

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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

public class SessionContext {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SessionContext.class);

    private String name;
    private Path propPath;

    private final ObservableMap<String, Property<?>> bindings = FXCollections.observableHashMap();
    private final Properties props = new Properties();

    public SessionContext() {
	setSession(SessionContext.class.getName());
    }

    public void setSession(String name) {
	this.name = name;
	propPath = FileSystems.getDefault().getPath(System.getProperty("user.home"), "." + name + ".properties");
    }

    public Properties getProperties() {
	return props;
    }

    public void loadSession() {

	if (Files.exists(propPath, LinkOption.NOFOLLOW_LINKS)) {
	    try (InputStream is = Files.newInputStream(propPath, StandardOpenOption.READ)) {
		props.load(is);
	    } catch (IOException ex) {
		logger.error(ex.getMessage(), ex);
	    }
	}
    }

    @PreDestroy
    public void saveSession() {
	logger.debug("save session");
	try (OutputStream outStream = Files.newOutputStream(propPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
	    props.store(outStream, name + " session properties");
	} catch (IOException ex) {
	    logger.error(ex.getMessage(), ex);
	}

    }

    public void bind(final BooleanProperty property, final String propertyName) {
	String value = props.getProperty(propertyName);
	if (value != null) {
	    property.set(Boolean.valueOf(value));
	}
	property.addListener(o -> {
	    props.setProperty(propertyName, property.getValue().toString());
	});
    }

    @SuppressWarnings("unchecked")
    public void bind(final ObjectProperty<?> property, final String propertyName, Class<?> type) {
	String value = props.getProperty(propertyName);
	if (value != null) {
	    if (type.getName().equals(Color.class.getName())) {
		((ObjectProperty<Color>) property).set(Color.valueOf(value));
	    } else if (type.getName().equals(String.class.getName())) {
		((ObjectProperty<String>) property).set(value);
	    } else {
		((ObjectProperty<Object>) property).set(value);
	    }
	}
	property.addListener(o -> {
	    props.setProperty(propertyName, property.getValue().toString());
	});
    }

    public void bind(final DoubleProperty property, final String propertyName) {
	String value = props.getProperty(propertyName);
	if (value != null) {
	    property.set(Double.valueOf(value));
	}
	property.addListener(o -> {
	    props.setProperty(propertyName, property.getValue().toString());
	});
    }

    public void bind(final ToggleGroup toggleGroup, final String propertyName) {
	try {
	    String value = props.getProperty(propertyName);
	    if (value != null) {
		int selectedToggleIndex = Integer.parseInt(value);
		toggleGroup.selectToggle(toggleGroup.getToggles().get(selectedToggleIndex));
	    }
	} catch (Exception ignored) {
	}
	toggleGroup.selectedToggleProperty().addListener(o -> {
	    if (toggleGroup.getSelectedToggle() == null) {
		props.remove(propertyName);
	    } else {
		props.setProperty(propertyName, Integer.toString(toggleGroup.getToggles().indexOf(toggleGroup.getSelectedToggle())));
	    }
	});
    }

    public void bind(final Accordion accordion, final String propertyName) {
	Object selectedPane = props.getProperty(propertyName);
	for (TitledPane tp : accordion.getPanes()) {
	    if (tp.getText() != null && tp.getText().equals(selectedPane)) {
		accordion.setExpandedPane(tp);
		break;
	    }
	}
	accordion.expandedPaneProperty().addListener((ov, t, expandedPane) -> {
	    if (expandedPane != null) {
		props.setProperty(propertyName, expandedPane.getText());
	    }
	});
    }

    public void bind(final ComboBox<?> combo, final String propertyName) {
	try {
	    String value = props.getProperty(propertyName);
	    if (value != null) {
		int index = Integer.parseInt(value);
		combo.getSelectionModel().select(index);
	    }
	} catch (Exception ignored) {
	}
	combo.getSelectionModel().selectedIndexProperty().addListener(o -> {
	    props.setProperty(propertyName, Integer.toString(combo.getSelectionModel().getSelectedIndex()));
	});
    }
    
    public void bind(final StringProperty property, final String propertyName) {
	String value = props.getProperty(propertyName);
	if (value != null) {
	    property.set(value);
	}

	property.addListener(o -> {
	    props.setProperty(propertyName, property.getValue());
	});
    }

    /**
     * session scope bindings
     * 
     * @return
     */
    public ObservableMap<String, Property<?>> getBindings() {
	return bindings;
    }

    /**
     * add session scope binding (Property.getName() required)
     * 
     * @param value
     */
    public void addBinding(Property<?> value) {
	if (value.getName() == null || value.getName().isEmpty()){
	    throw new IllegalArgumentException("property name must not be empty");
	}
	bindings.put(value.getName(), value);
    }
    
    public Optional<Property<?>> getBinding(String key) {
	return Optional.ofNullable(bindings.get(key));
    }

    public Optional<ObjectProperty<?>> getObjectBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !ObjectProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((ObjectProperty<?>) b.get());
    }

    public Optional<BooleanProperty> getBooleanBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !BooleanProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((BooleanProperty) b.get());
    }

    public Optional<IntegerProperty> getIntegerBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !IntegerProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((IntegerProperty) b.get());
    }

    public Optional<StringProperty> getStringBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !StringProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((StringProperty) b.get());
    }

    public Optional<DoubleProperty> getDoubleBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !DoubleProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((DoubleProperty) b.get());
    }

    public Optional<FloatProperty> getFloatBinding(String key) {
	Optional<Property<?>> b = getBinding(key);
	if (!b.isPresent() || !FloatProperty.class.isInstance(b.get())) {
	    return Optional.empty();
	}
	return Optional.of((FloatProperty) b.get());
    }

}
