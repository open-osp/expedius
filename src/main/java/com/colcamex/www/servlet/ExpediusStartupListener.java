package com.colcamex.www.servlet;

import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.handler.PollTimer;
import com.colcamex.www.util.Email;
import com.colcamex.www.util.ExpediusProperties;

/**
 * Listens for server start ups.
 * If Expedius is set to recover from a server start up it
 * will start running from here.
 * 
 * @author dennis warren @ colcamex Resources
 * Copyright 2012 - 2014
 *
 */
public class ExpediusStartupListener implements ServletContextListener {

	public static Logger logger = Logger.getLogger("ExpediusStartupListener");

	private static ExpediusProperties properties;
	private static String vendor, version;
	
    /**
     * Default constructor. 
     */
    public ExpediusStartupListener() {
        // default constructor
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {
    	
    	_init(event.getServletContext().getInitParameter(ExpediusProperties.PROPERTIES_FILE_NAME), 
				event.getServletContext().getServletContextName());
		
		String emailMessage = "Expedius failed to recover from server restart. Please log-in to" + 
				" Expedius and start the service manually. Contact support immediatley after.";

		ExpediusControllerHandler controllerHandler = null;
		boolean runningStatus = Boolean.FALSE;
		boolean restartStatus = Boolean.FALSE;
		
		if(properties == null) {
			logger.error("Server restart failure. Missing properties file.");			
		} else {
			
			if(properties.containsKey("VERSION")) {
				ExpediusStartupListener.version = properties.getProperty("VERSION");
				logger.info("VERSION: "+ExpediusStartupListener.version);
			}
			
			if(properties.containsKey("VENDOR")) {
				ExpediusStartupListener.vendor = properties.getProperty("VENDOR");
				logger.info("VENDOR: "+ExpediusStartupListener.vendor);
			}
			
			controllerHandler = ExpediusControllerHandler.getInstance(properties);
		}

		if(controllerHandler != null) {
			
			runningStatus = controllerHandler.getControllerBean().getStatus();
			restartStatus = controllerHandler.getControllerBean().isStartWithServer();
			
			logger.info("Expedius was running at shutdown: " + runningStatus);
			logger.info("Expedius is set to restart: " + restartStatus);

		} else {
			logger.error("Error on server restart. Expedius failed to instantiate the controller object.");
		}
		
		if( (restartStatus) && (runningStatus) ) {		
			controllerHandler.start();
			emailMessage = null;
		} else {
			
			controllerHandler.getMessageHandler().setServiceStatus(Boolean.FALSE);
			controllerHandler.getMessageHandler().persist();
			
			logger.info("Expedius is not set to recover from a server restart.");
			emailMessage = "Expedius is not set to recover from a server restart. Running status = " +runningStatus+ 
				". Restart status = " +restartStatus+ " Please log-in to Expedius and start the service. " +
				". Note: For Expedius to recover from a server restart the option \"start Expedius when server boots\" "
						+ " on the schedule page must be checked. And Expedius needs to be in a running state prior to "
						+ "a server restart.";
		}

		if(emailMessage != null) {
			Email.sendEmail("Expedius", emailMessage, properties);
		}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {
    	ScheduledExecutorService scheduler = PollTimer.getScheduler();
		if(scheduler != null) {
			scheduler.shutdown();
		}
		
		logger.info("A server shutdown occurred. Stopping Expedius services.");
    }
    
    private void _init(String propertiesPath, String context) {	

		if(propertiesPath != null) {
			if(! propertiesPath.isEmpty()) {
				properties = ExpediusProperties.getProperties(propertiesPath);
				logger.info("Initializing Expedius from context path: " + propertiesPath);
				properties.setProperty("CONTEXT_PATH", context);
			} else {
				logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
			}
		} else {
			logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
		}
	}
	
}
