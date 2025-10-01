package com.colcamex.www.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ExpediusProperties extends Properties {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger("ExpediusConnect");
	public static final String PROPERTIES_FILE_NAME = "ExpediusProperties";
	
	private static ExpediusProperties instance = null;	

	protected ExpediusProperties(String propertiesPath) {
		super();
		
		if( propertiesPath == null ) {
			propertiesPath = getClass().getClassLoader().getResource("expedius.properties").getPath();
		}
		
		try (FileInputStream inputStream = new FileInputStream(propertiesPath)) {
				load(inputStream);
		} catch (IOException e) {
			logger.error("Failed to load properties file", e);
		} finally {
			
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
