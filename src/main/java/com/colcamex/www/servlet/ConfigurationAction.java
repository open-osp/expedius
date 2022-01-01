package com.colcamex.www.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.security.Encryption;
import com.colcamex.www.util.ExpediusProperties;
import com.colcamex.www.util.FilterUtility;

public class ConfigurationAction extends HttpServlet {
	
	public static Logger logger = LogManager.getLogger("ConfigurationAction");
	private static final long serialVersionUID = 1L;
	private static ExpediusProperties properties; 
	
	@Override
	public void init(ServletConfig config) throws ServletException {

		String propertiesPath = config.getServletContext().getInitParameter(ExpediusProperties.PROPERTIES_FILE_NAME); 
	
		if(propertiesPath != null) {
			if(! propertiesPath.isEmpty()) {
				properties = ExpediusProperties.getProperties(propertiesPath);
			} else {
				logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
			}
		} else {
			logger.error("Properties file path is missing. Ensure that it is set in web.xml.");
		}
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) {

		ExpediusControllerHandler controllerHandler = ExpediusControllerHandler.getInstance();			
	
		ConfigurationBeanInterface configurationBean = null;
    	String directive = "configuration";
    	boolean excellerisOn = Boolean.FALSE;
    	boolean ihapoiOn = Boolean.FALSE;
    	String excellerisLoginStatus = "";
    	String ihaLoginStatus = "";
    	String excellerisCertStatus = "";
    	String message = " "; 
    	String userName = null;
		String password = null;
		String passwordConfirm = null; 
//		String servicePath = "";
//		String loginPath = "";
//		String fetchPath = "";
//		String acknowledgePath = "";
//		String logoutPath = "";
				
    	String loginForm = FilterUtility.filter(request.getParameter("login"));
    	String config = FilterUtility.filter(request.getParameter("config"));

    	if(properties != null) {
    		excellerisOn = Boolean.parseBoolean(properties.getProperty("EXCELLERIS"));
    		ihapoiOn = Boolean.parseBoolean(properties.getProperty("IHAPOI"));
    	}
    	
    	if( (! excellerisOn) && (! ihapoiOn) ) {
    		message = "No services are set. Choose which services to run in the Expedius Properties file.";
    	}
    	
    	if( loginForm != null) {

    		configurationBean = controllerHandler.getConfigurationBean(loginForm + "Bean");
    		
    		userName = FilterUtility.filter(request.getParameter("userName"));
    		password = FilterUtility.filter(request.getParameter("password"));
    		passwordConfirm = FilterUtility.filter(request.getParameter("passwordConfirm"));

    		if(userName != null) {
    			if(! userName.isEmpty()) {
    				configurationBean.setUserName(userName.trim());
    			}
    		}

    		if( (passwordConfirm != null)&&(password != null) ) {
    			if( (! passwordConfirm.isEmpty())&&(! password.isEmpty()) ) {
    				if(Encryption.testPassword(password, passwordConfirm)) {
    					configurationBean.setPassword(password.trim());
    	    		}
    			} else {
    				message = "Passwords are empty";
    			}
    		} else {
    			message = "Passwords are null";
    		}  		
    	} 
    	
//
//    	if( config != null) {
//
//    		configurationBean = controllerHandler.getConfigurationBean(config + "Bean");
//    		if(request.getParameterMap().containsKey("path")) {
//	    		servicePath = request.getParameter("path");
//	    		if(! configurationBean.getServicePath().equalsIgnoreCase(servicePath)) {
//	    			configurationBean.setServicePath(servicePath.trim());
//	    		}
//    		}
//
//    		if(request.getParameterMap().containsKey("loginPath")) {
//	    		loginPath = request.getParameter("loginPath");
//	    		if(! configurationBean.getLoginPath().equalsIgnoreCase(loginPath)) {
//	    			configurationBean.setLoginPath(loginPath.trim());
//	    		}
//    		}
//
//    		if(request.getParameterMap().containsKey("fetchPath")) {
//	    		fetchPath = request.getParameter("fetchPath");
//				if(! configurationBean.getFetchPath().equalsIgnoreCase(fetchPath)) {
//					configurationBean.setFetchPath(fetchPath.trim());
//				}
//    		}
//
//    		if(request.getParameterMap().containsKey("acknowledgePath")) {
//	    		acknowledgePath = request.getParameter("acknowledgePath");
//				if(! configurationBean.getAcknowledgePath().equalsIgnoreCase(acknowledgePath)) {
//					configurationBean.setAcknowledgePath(acknowledgePath.trim());
//				}
//    		}
//
//    		if(request.getParameterMap().containsKey("logoutPath")) {
//	    		logoutPath = request.getParameter("logoutPath");
//				if(! configurationBean.getLogoutPath().equalsIgnoreCase(logoutPath)) {
//					configurationBean.setLogoutPath(logoutPath.trim());
//				}
//    		}
//
//    		request.setAttribute(config + "_linkActionMessage", "Links updated successfully");
//
//    	}
    	

    	controllerHandler.persistConfigurationBeans();

    	// get the client status of each service. But only if they are set to run.
    	if(excellerisOn) {
    		configurationBean = controllerHandler.getConfigurationBean(ExpediusControllerHandler.EXCELLERIS_CONFIGURATION_NAME);
    		
    		if(configurationBean.isCertificateInstalled()) {
    			excellerisCertStatus = "Security Certificate Installed";
    		}
    		
    		if(configurationBean.isLoginInfoSet()) {
    			excellerisLoginStatus = "Login Info Set";
    		}
        	request.setAttribute("excellerisConfigurationBean", configurationBean);
    	}
    	  	
    	if(ihapoiOn) {
    		configurationBean = controllerHandler.getConfigurationBean(ExpediusControllerHandler.IHA_CONFIGURATION_NAME);
    		
    		if(configurationBean.isLoginInfoSet()) {
    			ihaLoginStatus = "Login Info Set";
    		}
    		request.setAttribute("ihaConfigurationBean", configurationBean);
    	}

    	request.setAttribute("ihapoiLoginStatusMessage", ihaLoginStatus);

    	request.setAttribute("excellerisLoginStatusMessage", excellerisLoginStatus);
    	request.setAttribute("excellerisCertStatusMessage", excellerisCertStatus);
    	
    	request.setAttribute("excellerisOn", excellerisOn);
    	request.setAttribute("ihapoiOn", ihapoiOn);
  	
		RequestDispatcher dispatch = request.getRequestDispatcher("WEB-INF/pages/"+directive+".jsp");
		
		try {
			dispatch.forward(request, response);
		} catch (ServletException e) {
			logger.error("Servlet Exception",e);
		} catch (IOException e) {
			logger.error("IO Exception",e);
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
	
}