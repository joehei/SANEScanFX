package de.heimbuchner.sanescanfx.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class FileProperties {

	Path propertiesFile;

	Properties properties;

	public FileProperties(Path propertiesFile) throws IOException {
		this.propertiesFile = propertiesFile;
		properties = new Properties();
		loadProperties();
	}

	public Properties getProperties() {
		return properties;
	}

	public Path getPropertiesFile() {
		return propertiesFile;
	}

	void loadProperties() throws IOException {
		try (FileInputStream fi = new FileInputStream(propertiesFile.toFile())) {
			properties.load(fi);
		}
	}

	void saveProperties() throws IOException {
		try (FileOutputStream fr = new FileOutputStream(propertiesFile.toFile())) {
			properties.store(fr, "Properties");
		}
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setPropertiesFile(Path propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public void writeProperty(String key, String value) throws IOException {
		properties.put(key, value);
		saveProperties();
	}

}
