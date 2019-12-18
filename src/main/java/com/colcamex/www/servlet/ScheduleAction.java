package com.colcamex.www.servlet;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.handler.PollTimer;


public class ScheduleAction extends HttpServlet  {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(ScheduleAction.class);

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
	
		String directive = "schedule";
		String action = request.getParameter("schedule");	
		String pollInterval = request.getParameter("pollInterval");
		String pollSetting = request.getParameter("pollSetting");
		String startWithServer = request.getParameter("startWithServer");
		String savedStatusMessage = "Settings Saved";
		
		ExpediusControllerHandler controllerHandler = ExpediusControllerHandler.getInstance();
		ControllerBean controllerBean = controllerHandler.getControllerBean();

		if((action != null)&&(action.equalsIgnoreCase("save"))) {
			
			controllerBean.setClientReady(false);
			
			controllerBean.setStartWithServer(Boolean.parseBoolean(startWithServer));
			
			if(pollSetting != null) {
				controllerBean.setPollSetting(Integer.parseInt(pollSetting));
			}
			
			if((pollInterval != null)&&(controllerBean.getPollSetting() == ControllerBean.FREQUENCY)) {
				controllerBean.setPollInterval(Integer.parseInt(pollInterval));
				controllerBean.setClientReady(true);
			}
			   		    		
			if(controllerBean.getPollSetting() == ControllerBean.TIME_OF_DAY) {
				
	    		String pollTime_OneHour = request.getParameter("pollTime_OneHour");
	    		String pollTime_OneMinute = request.getParameter("pollTime_OneMinute"); 
	    		String pollTime_TwoHour = request.getParameter("pollTime_TwoHour");
	    		String pollTime_TwoMinute = request.getParameter("pollTime_TwoMinute");
	    		String pollTime_ThreeHour = request.getParameter("pollTime_ThreeHour");
	    		String pollTime_ThreeMinute = request.getParameter("pollTime_ThreeMinute");
	    		String pollTime_FourHour = request.getParameter("pollTime_FourHour");
	    		String pollTime_FourMinute = request.getParameter("pollTime_FourMinute");
	    		
	    		controllerBean.setPollTimeOne(pollTime_OneHour, pollTime_OneMinute);
	    		controllerBean.setPollTimeTwo(pollTime_TwoHour, pollTime_TwoMinute);
	    		controllerBean.setPollTimeThree(pollTime_ThreeHour, pollTime_ThreeMinute);
	    		controllerBean.setPollTimeFour(pollTime_FourHour, pollTime_FourMinute);
	    		
	    		// verify times 
	    		boolean empty = true;
	    		Calendar[] pollTimes = controllerBean.getTimeArray();
	    		for(int i=0; pollTimes.length > i; i++) {
	    			if(pollTimes[i] != null) {
	    				empty = false; 
	    			}
	    		}
	    		
	    		if(empty){
	    			request.setAttribute("timeError", "Please set at least one time.");
	    			savedStatusMessage = null;
	    		} else {
	    			controllerBean.setClientReady(true);
	    		}
	
			}
			
			if(controllerHandler.persistBean()) {
				request.setAttribute("savedStatusMessage", savedStatusMessage);

	    		if(PollTimer.isRunning()) {	    			
	    			controllerHandler.stop();
					controllerHandler.start();
	    		}			
			}

		}
		
		request.setAttribute("controllerBean", controllerBean);

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
