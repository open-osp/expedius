package com.colcamex.www.handler;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.colcamex.www.http.AbstractConnectionController;

/**
 * 
 * @author dennis warren, colcamex Resources.
 * copyright 2012 - 2014
 * The service executer executes an array of lab distribution 
 * clients. 
 */
public class ServiceExecuter implements Runnable {

	private static Logger logger = Logger.getLogger("ServiceExecuter");
	
	private AbstractConnectionController[] services;
	private static ServiceExecuter instance = null;
	private Date lastRunTime;
	private boolean serviceStatus;
	private ExpediusMessageHandler messageHandler;
	private ExpediusHL7LabHandler labHandler;
	
	private ServiceExecuter() {
		// default
	}
	
	public static ServiceExecuter getInstance() {
		if(ServiceExecuter.instance == null) {						
			instance = new ServiceExecuter();
			logger.info("Instantiating Service Executer.");
		}
		return instance;
	}
	
	private boolean execute() {
		return execute(0);
	}
	
	private boolean execute(int mode) {
		
		boolean status = true;
		int i = 0;
		
		if(services.length > 0) {
			
			// need to suspend the next service while the previous is
			// running.  Thread suspend?
			
			while( (status) && (i < services.length) ) {
				
				logger.info("Executing service " + services[i].getServiceName());
				
				status = execute(services[i], mode); 
				i++;
			}
		} else {
			status = Boolean.FALSE;
		}
		
		return status;
	}
	
	private boolean execute(AbstractConnectionController service, int mode) {
		
		boolean status = Boolean.FALSE;
		
		// set the connection
		if(service != null) {

			// inject the message handler
			if(getMessageHandler() != null) {
				service.setMessageHandler(getMessageHandler());
			}
			
			// inject the lab handler.
			if(getLabHandler() != null) {
				service.setLabHandler(getLabHandler());
			}
			
			if(mode > 0) {
				status = service.start(mode);	
			} else {
				status = service.start();
			}
		}
		
		return status;			 
	}

	/**
	 * Checks if the service host is available.
	 * @return
	 */
	public boolean checkHostStatus() {
		return execute(AbstractConnectionController.LOGIN_MODE);
	}
	
	/**
	 * Performs a login routine for each of the lab distribution services 
	 * to confirm connection status.
	 * @return
	 */
	public boolean checkClientStatus() {

		boolean status = Boolean.TRUE;
		
		for(int i = 0; i < services.length; i++) {

			if(! services[i].getConfigurationBean().isCertificateInstalled()) {
				
				logger.error("Missing security certificate for service " + services[i].getServiceName());
				
				if(getMessageHandler() != null) {
					getMessageHandler().addErrorMessage("Cannot start client for " + services[i].getServiceName() + 
							" - missing security certificate.");
				}
				status = Boolean.FALSE;
			} 
			
			if(! services[i].getConfigurationBean().isLoginInfoSet()) {
				
				logger.error("Missing login credentials for service " + services[i].getServiceName());
				
				if(getMessageHandler() != null) {
					getMessageHandler().addErrorMessage("Cannot start client for " + services[i].getServiceName() + 
							" - missing login information.");
				}
				status = Boolean.FALSE;
			}
		}
		
		return status;
	}

	
	/**
	 * Executes the lab retrieval sequence for each of the distribution services.
	 */
	@Override
	public void run() {
		Thread thread = Thread.currentThread();
		thread.setName("ServiceExecuter"+"["+thread.getId()+"]");
		logger.debug("Running ServiceExecuter [" + thread.getName() + "]");
		
		boolean status = Boolean.FALSE;
		
		// reset error messages for next run.
		getMessageHandler().setErrorMessages(new ArrayList<String>());
		
		// log run time.
		setLastRunTime(new Date());

		if(getServices().length > 0) {
			status = execute();
		} else {
			logger.error("No Services to execute");
		}
		
		logger.info("Service execution status is: " + status);

		// set the service status on every run to keep track.
		setServiceStatus(PollTimer.isRunning());
		
		// persist messaging and settings after each run.
		getMessageHandler().persist();
	}

	/**
	 * @return Array of ExpediusConnectionControllers.
	 */
	public AbstractConnectionController[] getServices() {
		return services;
	}

	/**
	 * Set an array of abstract lab service connection controllers.
	 * 
	 */
	public void setServices(AbstractConnectionController[] services) {
		this.services = services;
	}

	public Date getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(Date lastRunTime) {
		this.lastRunTime = lastRunTime;
		
		if(getMessageHandler() != null) {
			getMessageHandler().setLastDownload(this.lastRunTime);
		}
	}

	public ExpediusMessageHandler getMessageHandler() {
		return messageHandler;
	}

	/**
	 * Set an ExpediusMessageHandler that tracks and stores status 
	 * and error messages.
	 * @param messageHandler
	 */
	public void setMessageHandler(ExpediusMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public boolean isServiceStatus() {
		return serviceStatus;
	}

	private void setServiceStatus(boolean serviceStatus) {
		this.serviceStatus = serviceStatus;
		if(getMessageHandler() != null) {
			getMessageHandler().setServiceStatus(serviceStatus);
		}
	}

	public ExpediusHL7LabHandler getLabHandler() {
		return labHandler;
	}

	public void setLabHandler(ExpediusHL7LabHandler labHandler) {
		this.labHandler = labHandler;
	}

}
