package com.colcamex.www.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The ExpediusProperties class is a singleton utility class that extends the Java Properties class
 * to provide application-level configuration management for the Expedius system. This class allows
 * loading properties from a default properties file located in the classpath or an override file
 * provided at runtime via a file system path. Properties loaded from the override file will take precedence.
 *
 * This class supports lazy initialization for retrieving the singleton instance and offers flexibility
 * to load configuration from different locations. It also provides logging to indicate the success
 * or failure of property loading operations.
 *
 * Thread-safe behavior is not explicitly guaranteed; if used in a multi-threaded context, additional
 * synchronization may be necessary when accessing or modifying properties.
 */
public class ExpediusProperties extends Properties {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger("ExpediusConnect");
	public static final String PROPERTIES_FILE_NAME = "ExpediusProperties";
	
	private static ExpediusProperties instance = null;

	/**
	 * Loads the application properties file from either the classpath or a specified filesystem location.
	 * The constructor first attempts to load the "expedius.properties" file from the classpath.
	 * If it cannot locate the file, it logs a warning and attempts to load an override properties file
	 * from the provided filesystem path. If the properties file cannot be loaded from either location,
	 * it throws a RuntimeException.
	 *
	 * @param propertiesPath the absolute or relative file path of the override properties file.
	 *                       If this parameter is null, only the classpath is used to search for
	 *                       the properties file.
	 */
	protected ExpediusProperties(String propertiesPath) {
		super();

		// Try to load from classpath first
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("expedius.properties")) {
			if (inputStream != null) {
				load(inputStream);
				logger.info("Properties loaded from classpath: expedius.properties");
			} else {
				logger.warn("expedius.properties not found in classpath, trying default path");
			}
		} catch (IOException e) {
			logger.error("Failed to load properties file from classpath", e);
		}

		if (propertiesPath != null) {
			// load override properties file from filesystem
			try (FileInputStream inputStream = new FileInputStream(propertiesPath)) {
				load(inputStream);
				logger.info("Override properties loaded from file: " + propertiesPath);
			} catch (FileNotFoundException e) {
				logger.error("Properties file not found: " + propertiesPath, e);
				throw new RuntimeException("Critical: Properties file not found: " + propertiesPath, e);
			} catch (IOException e) {
				logger.error("Failed to load properties file: " + propertiesPath, e);
				throw new RuntimeException("Critical: Failed to load properties file: " + propertiesPath, e);
			}
		}
	}
	
	/**
	 * Provides a pointer to the Expedius Properties instance IF it has
	 * already been instantiated. 
	 * IF not instantiated, an instance will be created with the default
	 * properties file path.
	 */
	public static ExpediusProperties getProperties() {
		if(instance == null)
		{
			instance = new ExpediusProperties(null);
		}
		return instance;
	}
	
	/**
	 * Get an Expedius Properties file instance from the provided properties path 
	 * parameter.  If the property path is not found, an instance based on default
	 * parameters will be provided.
	 */
	public static ExpediusProperties getProperties(String propertiesPath) {		
		return new ExpediusProperties(propertiesPath);
	}

}
