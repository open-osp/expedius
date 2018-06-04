package com.colcamex.www.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ExpediusProperties extends Properties {

	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTIES_FILE_NAME = "ExpediusProperties";
	public static final String DEFAULT_PATH = "/var/lib/expedius/expedius.properties";
	
	private static ExpediusProperties instance = null;	
	private FileInputStream inputStream;

	protected ExpediusProperties(String propertiesPath) {
		super();		
		
		try {
			
			if( propertiesPath == null ) {
				propertiesPath = DEFAULT_PATH;
			}

			inputStream = new FileInputStream(propertiesPath);
						
			if(inputStream != null) {			
				load(inputStream);
				inputStream.close();				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();			
		} finally {
			// paranoid stream closing
			if(inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Provides a pointer to the Expedius Properties instance IF it has
	 * already been instantiated. 
	 * IF not instantiated an instance will be created with the default 
	 * properties file path. 
	 * @return
	 */
	public static ExpediusProperties getInstance() {
		return ExpediusProperties.getProperties( null );
	}
	
	/**
	 * Get an Expedius Properties file instance from the provided properties path 
	 * parameter.  If the properties path is not found, an instance based on default
	 * parameters will be provided. 
	 * @param propertiesPath
	 */
	public static ExpediusProperties getProperties(String propertiesPath) {
		
		if(instance == null) {
			instance = new ExpediusProperties(propertiesPath);
		}
		return instance;
	}
	

}
