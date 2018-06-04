package com.colcamex.www.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.util.ExpediusProperties;
import com.colcamex.www.util.FilterUtility;

public class ControllerAction extends HttpServlet {//implements ServletContextListener {
	
	public static Logger logger = Logger.getLogger(ControllerAction.class);
	private static final long serialVersionUID = 1L;
	private static ExpediusProperties properties;
	private String vendor, version;
	

	@Override
	public void init(ServletConfig config) throws ServletException {

		String propertiesPath = config.getServletContext().getInitParameter(ExpediusProperties.PROPERTIES_FILE_NAME); 
		String context = config.getServletContext().getServletContextName();
		
		if(propertiesPath != null) {
			if(! propertiesPath.isEmpty()) {
				properties = ExpediusProperties.getProperties(propertiesPath);
				
				logger.info("Initializing Expedius from context path: " + context);
				
				properties.setProperty("CONTEXT_PATH", context);
				//checkFirstRun(properties);
			} else {
				logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
			}
		} else {
			logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
		}
	}

	/**
	 * User interface Action.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException { 
		
		String directive = " ";				 
		String dispatch = "status";
		ExpediusControllerHandler controllerHandler = null;
		
		if(ControllerAction.properties != null) {

			controllerHandler = ExpediusControllerHandler.getInstance(ControllerAction.properties);
			
		}
		
		if (request.getParameter("control") != null) {
    		directive = FilterUtility.filter(request.getParameter("control").toLowerCase());
    	}
		
		if(controllerHandler != null) {
			
			controllerHandler.getMessageHandler().setErrorMessages(new ArrayList<String>());
			controllerHandler.getMessageHandler().setStatusMessage(null);

			if (directive.equalsIgnoreCase("start")) {
	
				controllerHandler.start();
	
	    	} else if (directive.equalsIgnoreCase("stop")) {
	    		
	    		controllerHandler.stop();  
	   		
	    	} else if (directive.equalsIgnoreCase("get labs now")) {
	
	    		if(controllerHandler.downloadNow()) {	        			
	        		controllerHandler.getMessageHandler().setStatusMessage("Download Complete");
	    		}
	    		
	    	} else {	    		
	    		logger.error("There was a problem with the web request.");	
	    	}
		
			controllerHandler.getMessageHandler().persist();
				
			request.setAttribute("errorMessages", controllerHandler.getMessageHandler().getErrorMessages());
			request.setAttribute("message", controllerHandler.getMessageHandler().getStatusMessage());	

			request.setAttribute("vendor", this.vendor);
			request.setAttribute("version", this.version);
			
		} else {
			logger.error("Expedius failed to initialize correctly.");
			dispatch = "error";
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(dispatch);

		try {
			dispatcher.forward(request, response);
		} catch (ServletException e) {
			logger.error("Dispatch error" + e);
		} catch (IOException e) {
			logger.error("Dispatch error" + e);
		} 

	}
	       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
//	private boolean checkFirstRun(ExpediusProperties properties) {
//		
//		boolean status = true;
//		String dataSavePath = null;
//		
//		if(properties.containsKey("DATA_PATH")) {		
//			dataSavePath = properties.getProperty("DATA_PATH");
//		} 
//		
//		if(dataSavePath != null) {
//			if(! BeanRetrieval.getSavePath().equalsIgnoreCase(dataSavePath)) {
//				BeanRetrieval.setSavePath(dataSavePath);
//			}
//			
//			if(! BeanRetrieval.checkBean("ControllerBean")) {
//				logger.info("First run detected. Creating new ControllerBean.");
//				
//				ControllerBean controllerBean = new ControllerBean();			
//				controllerBean.setPollInterval(PollTimer.DEFAULT_POLL_INTERVAL);
//				controllerBean.setPollSetting(ControllerBean.FREQUENCY);
//				controllerBean.setStartWithServer(true);
//				
//				BeanRetrieval.setBean(controllerBean);
//			}
//			if(! BeanRetrieval.checkBean("ExcellerisConfigurationBean")) {
//				logger.info("First run detected. Creating new ExcellerisConfigurationBean.");
//				
//				ConfigurationBean excellerisConfigurationBean = new ExcellerisConfigurationBean();		
//				excellerisConfigurationBean.initialize(
//					properties.getProperty("EXCELLERIS_URI"),	
//					properties.getProperty("REQUEST_NEW"),
//					properties.getProperty("LOGIN"),
//					properties.getProperty("LOGOUT"),
//					properties.getProperty("ACK_POSITIVE")
//				);	
//				
//				BeanRetrieval.setBean(excellerisConfigurationBean);
//			}
//			if(! BeanRetrieval.checkBean("IhaConfigurationBean")) {
//				logger.info("First run detected. Creating new IhaConfigurationBean.");
//				
//				ConfigurationBean ihaConfigurationBean = new IhaConfigurationBean();		
//				ihaConfigurationBean.initialize(
//					properties.getProperty("EXCELLERIS_URI"),	
//					properties.getProperty("REQUEST_NEW"),
//					properties.getProperty("LOGIN"),
//					properties.getProperty("LOGOUT"),
//					properties.getProperty("ACK_POSITIVE")
//				);	
//				
//				BeanRetrieval.setBean(ihaConfigurationBean);
//			}
//		} else {
//			logger.error("Missing data save path. Cannot execute Expedius on first run");
//			status = false;
//		}
//		
//		return status;
//	}
	
}