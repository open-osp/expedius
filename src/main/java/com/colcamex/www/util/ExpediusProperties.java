package com.colcamex.www.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ExpediusProperties extends Properties {

	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTIES_FILE_NAME = "ExpediusProperties";
	
	private static ExpediusProperties instance = null;	

	protected ExpediusProperties(String propertiesPath) {
		super();
		
		if( propertiesPath == null ) {
			propertiesPath = getClass().getClassLoader().getResource("expedius.properties").getPath();
		}
		
		try (FileInputStream inputStream = new FileInputStream(propertiesPath)) {

			if(inputStream != null) {			
				load(inputStream);
				inputStream.close();				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();			
		} finally {
			
		}
	}
	
	/**
	 * Provides a pointer to the Expedius Properties instance IF it has
	 * already been instantiated. 
	 * IF not instantiated an instance will be created with the default 
	 * properties file path. 
	 * @return
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
	 * parameter.  If the properties path is not found, an instance based on default
	 * parameters will be provided. 
	 * @param propertiesPath
	 */
	public static ExpediusProperties getProperties(String propertiesPath) {		
		instance = new ExpediusProperties(propertiesPath);
		return instance;
	}
	

}
