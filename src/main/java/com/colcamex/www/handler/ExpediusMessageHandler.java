package com.colcamex.www.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.util.BeanRetrieval;

/**
 * 
 * @author dennis warren @ Colcamex Resources.
 * Copy Right 2012 - 2014
 *
 */
public class ExpediusMessageHandler {
	public static Logger logger = LogManager.getLogger(ExpediusMessageHandler.class);
	private String statusMessage;
	private ControllerBean controllerBean;
	private ArrayList<String> errorMessages;
	private Date lastDownload;
	private boolean serviceStatus;
	private Date lastStartup;
	
	public ExpediusMessageHandler() {
		// default constructor.
	}
	
	public ExpediusMessageHandler(ControllerBean controllerBean) {
		this.controllerBean = controllerBean;
	}
	
	/**
	 * Adds an error message for immediate display to the user.
	 * @param message
	 */
	public void addErrorMessage(String message) {
		if(errorMessages != null) {
			errorMessages.add(message);
		}
	}
	
	/**
	 * persists an error message to the controllerBean until 
	 * dismissed by the user. Reflects controllerBean method.
	 * @param message
	 */
	public void addDismissableErrorMessage(String message) {
		
		if(message != null) {
			message = message + " Error date: " + new Date();
		}
		
		if(controllerBean != null) {
			// persist these messages.
			controllerBean.addDismissableErrorMessage(message);
		}
	}

	public ArrayList<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(ArrayList<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Sets a status feedback message to the user. 
	 * @param statusMessage
	 */
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public ControllerBean getControllerBean() {
		return controllerBean;
	}

	/**
	 * The controller bean contains all persisted settings and 
	 * messages for Expedius.
	 * @param controllerBean
	 */
	public void setControllerBean(ControllerBean controllerBean) {
		this.controllerBean = controllerBean;
	}

	public Date getLastDownload() {
		return lastDownload;
	}

	public void setLastDownload(Date lastDownload) {
		this.lastDownload = lastDownload;
		
		if(controllerBean != null) {
			// persist this download time.
			controllerBean.setLastDownLoad(this.lastDownload);
		}
	}

	/**
	 * Indicates that the serviceExecuter completed correctly and is
	 * still running.
	 * @return
	 */
	public boolean isServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(boolean serviceStatus) {
		this.serviceStatus = serviceStatus;
		if(controllerBean != null) {
			controllerBean.setStatus(serviceStatus);	
		}
	}

	public Date getLastStartup() {
		return lastStartup;
	}

	public void setLastStartup(Date lastStartup) {
		this.lastStartup = lastStartup;
		if(controllerBean != null) {
			controllerBean.setLastStartup(this.lastStartup);
		}
	}

	public void fatalErrorMessage(String message) {
		
	}

	/**
	 * persist all status data. 
	 */
	public void persist() {
		try {
			BeanRetrieval.setBean(this.controllerBean);
		} catch (FileNotFoundException e) {
			logger.fatal("Bean retrieval error.", e);
		} catch (IOException e) {
			logger.fatal("Bean retrieval error.", e);
		}
	}

}
