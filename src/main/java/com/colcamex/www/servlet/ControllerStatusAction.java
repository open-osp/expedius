package com.colcamex.www.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.handler.ExpediusMessageHandler;
import com.colcamex.www.util.FilterUtility;

public class ControllerStatusAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ControllerStatusAction.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);	
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		ExpediusControllerHandler controllerHandler = ExpediusControllerHandler.getInstance();
		ExpediusMessageHandler messageHandler = controllerHandler.getMessageHandler();
		ControllerBean controllerBean = messageHandler.getControllerBean();
		
		if (request.getParameter("dismisserror") != null) {  
			
    		int removeindex = Integer.parseInt(FilterUtility.filter(request.getParameter("dismisserror")));
    		
    		logger.info("User removing dismissable error:"+controllerBean.getDismissableErrorMessages().get(removeindex));
    		
    		controllerBean.removeDismissableErrorMessage(removeindex);

    		messageHandler.persist();
    	}
		
		request.setAttribute("dismissableErrorMessages", controllerBean.getDismissableErrorMessages());
		request.setAttribute("controllerBean", controllerBean);
	
		RequestDispatcher dispatch = request.getRequestDispatcher("WEB-INF/pages/status.jsp");
	
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
