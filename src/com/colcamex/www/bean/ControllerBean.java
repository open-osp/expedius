package com.colcamex.www.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author dennis warren @ Colcamex
 * Copy right 2012 - 2014
 * 
 * ControllerBean
 * A serializable bean that stores configuration and status
 * information for Expedius.
 * 
 */
public class ControllerBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final int FREQUENCY = 1;
	public static final int TIME_OF_DAY = 2;
	
	private boolean status;
	private boolean startWithServer;
	private boolean clientReady;
	private Date lastDownLoad;
	private Date nextDownLoad;
	private String method;
	private String methodDetail;
	private Date lastStartup;
	private String htmlLogPath;
	private int downloadCycles;
	private int lastDownloadFileCount;
	
	private int pollSetting;
	private int pollFrequency;
	
	private Calendar pollTimeOne;
	private Calendar pollTimeTwo;
	private Calendar pollTimeThree;
	private Calendar pollTimeFour;
	
	private Integer pollTimeOneHour;
	private Integer pollTimeTwoHour;
	private Integer pollTimeThreeHour;
	private Integer pollTimeFourHour;
	private Integer pollTimeOneMin;
	private Integer pollTimeTwoMin;
	private Integer pollTimeThreeMin;
	private Integer pollTimeFourMin;
	
	private Integer[] hourArray;	
	private Calendar[] timeArray;
	
	private String message;
	private ArrayList<String> errorMessages;
	private ArrayList<String> dismissableErrorMessages;
	

	public ControllerBean() {

		hourArray = new Integer[25];
		for(Integer i = 0; hourArray.length > i; i++) {
			this.hourArray[i] = i;
		}

		dismissableErrorMessages = new ArrayList<String>();
		message = null;
	}

	public boolean isClientReady() {
		return clientReady;
	}

	public void setClientReady(boolean clientReady) {
		this.clientReady = clientReady;
	}

	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getLastDownLoad() {
		return lastDownLoad;
	}
	public void setLastDownLoad(Date lastDownLoad) {
		this.lastDownLoad = lastDownLoad;
		if( (getPollSetting() == FREQUENCY) && (getPollInterval() > 0) ) {
			setNextDownLoad(new Date(this.lastDownLoad.getTime() + (getPollInterval() * 1000)));
		}
	}
	public Date getNextDownLoad() {
		Date now = getLastStartup();
		
		if(now == null) {
			setNextDownLoad(null);
		} else {
			now = new Date(now.getTime() + (getPollInterval() * 1000));
			if(now.after(this.nextDownLoad)) {
				setNextDownLoad(now);
			}
		}
		
		return nextDownLoad;
	}
	public void setNextDownLoad(Date nextDownLoad) {
		this.nextDownLoad = nextDownLoad;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getMethodDetail() {
		return methodDetail;
	}
	public void setMethodDetail(String methodDetail) {
		this.methodDetail = methodDetail;
	}

	public String getHtmlLogPath() {
		return htmlLogPath;
	}
	public void setHtmlLogPath(String htmlLogPath) {
		this.htmlLogPath = htmlLogPath;
	}

	public ArrayList<String> getErrorMessages() {
		return errorMessages;
	}
	
	public void setErrorMessages(ArrayList<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public void addErrorMessages(ArrayList<String> errorMessages) {
		if(errorMessages.size() > 0) {
			this.errorMessages = errorMessages;
		}
	}
	
	public void addErrorMessage(String message) {
		this.errorMessages.add(message);
	}

	public ArrayList<String> getDismissableErrorMessages() {
		return dismissableErrorMessages;
	}

	public void setDismissableErrorMessages(
			ArrayList<String> dismissableErrorMessages) {
		this.dismissableErrorMessages = dismissableErrorMessages;
	}
	
	public void addDismissableErrorMessage(String message) {
		this.dismissableErrorMessages.add(message);
	}
	
	public void removeDismissableErrorMessage(int index) {
		this.dismissableErrorMessages.remove(index);
	}

	public int getPollSetting() {	
		return pollSetting;
	}

	public void setPollSetting(int pollSetting) {	
		this.pollSetting = pollSetting;
	}

	public int getPollInterval() {		
		return this.pollFrequency;
	}
	
	public void setPollInterval (int pollInterval) {
		this.pollFrequency = pollInterval;		
	}

	public Calendar getPollTimeOne() {
		return pollTimeOne;
	}
	
	public void setPollTimeOne(String hour, String minute) {
		Integer intHour = null;
		Integer intMin = null;
		
		if((hour != null)&&(minute != null)) {
			if((! hour.isEmpty())&&(! minute.isEmpty())) {
				intHour = Integer.parseInt(hour); 						
				intMin = Integer.parseInt(minute);
			} 
		}
		
		setPollTimeOne(intHour,intMin);
	}

	public void setPollTimeOne(Integer hour, Integer minute) {
		setPollTimeOneHour(hour);
		setPollTimeOneMin(minute);
		
		if((hour == null)&&(minute == null)) {
			this.pollTimeOne = null;
		} else {		
			this.pollTimeOne = Calendar.getInstance();
			pollTimeOne.set(Calendar.HOUR_OF_DAY, hour);
			pollTimeOne.set(Calendar.MINUTE, minute);
		}
		
	}

	public Calendar getPollTimeTwo() {
		return pollTimeTwo;
	}
	
	public void setPollTimeTwo(String hour, String minute) {
		Integer intHour = null;
		Integer intMin = null;
				
		if((hour != null)&&(minute != null)) {
			if((! hour.isEmpty())&&(! minute.isEmpty())) {
				intHour = Integer.parseInt(hour); 						
				intMin = Integer.parseInt(minute);
			}
		}
		
		setPollTimeTwo(intHour,intMin);
	}

	public void setPollTimeTwo(Integer hour, Integer minute) {
		setPollTimeTwoHour(hour);
		setPollTimeTwoMin(minute);
		
		if((hour == null)&&(minute == null)) {
			this.pollTimeTwo = null;
		} else {
			this.pollTimeTwo = Calendar.getInstance();
			pollTimeTwo.set(Calendar.HOUR_OF_DAY, hour);
			pollTimeTwo.set(Calendar.MINUTE, minute);
		}	
	}

	public Calendar getPollTimeThree() {
		return pollTimeThree;
	}

	public void setPollTimeThree(String hour, String minute) {
		Integer intHour = null;
		Integer intMin = null;
		
		if((hour != null)&&(minute != null)) {
			if((! hour.isEmpty())&&(! minute.isEmpty())) {
				intHour = Integer.parseInt(hour); 						
				intMin = Integer.parseInt(minute);
			}
		}
		
		setPollTimeThree(intHour,intMin);
	}
	
	public void setPollTimeThree(Integer hour, Integer minute) {
		setPollTimeThreeHour(hour);
		setPollTimeThreeMin(minute);
		
		if((hour == null)&&(minute == null)) {
			this.pollTimeThree = null;
		} else {
			this.pollTimeThree = Calendar.getInstance();
			pollTimeThree.set(Calendar.HOUR_OF_DAY, hour);
			pollTimeThree.set(Calendar.MINUTE, minute);
			
		}	
	}

	public Calendar getPollTimeFour() {
		return pollTimeFour;
	}

	public void setPollTimeFour(String hour, String minute) {
		Integer intHour = null;
		Integer intMin = null;
		
		if((hour != null)&&(minute != null)) {
			if((! hour.isEmpty())&&(! minute.isEmpty())) {
				intHour = Integer.parseInt(hour); 						
				intMin = Integer.parseInt(minute);
			}
		}
		
		setPollTimeFour(intHour,intMin);
	}
	
	public void setPollTimeFour(Integer hour, Integer minute) {
		setPollTimeFourHour(hour);
		setPollTimeFourMin(minute);
		
		if((hour == null)&&(minute == null)) {
			this.pollTimeFour = null;
		} else {
			this.pollTimeFour = Calendar.getInstance();
			pollTimeFour.set(Calendar.HOUR_OF_DAY, hour);
			pollTimeFour.set(Calendar.MINUTE, minute);
		}	
	}

	public boolean isStartWithServer() {
		return startWithServer;
	}

	public void setStartWithServer(boolean startWithServer) {
		this.startWithServer = startWithServer;
	}

	public Integer[] getHourArray() {
		return hourArray;
	}

	public Calendar[] getTimeArray() {
		timeArray = new Calendar[4];
		timeArray[0] = getPollTimeOne();
		timeArray[1] = getPollTimeTwo();
		timeArray[2] = getPollTimeThree();
		timeArray[3] = getPollTimeFour();
		return timeArray;
	}

	public Integer getPollTimeOneHour() {
		return pollTimeOneHour;
	}

	private void setPollTimeOneHour(Integer pollTimeOneHour) {
		this.pollTimeOneHour = pollTimeOneHour;
	}

	public Integer getPollTimeTwoHour() {
		return pollTimeTwoHour;
	}

	private void setPollTimeTwoHour(Integer pollTimeTwoHour) {
		this.pollTimeTwoHour = pollTimeTwoHour;
	}

	public Integer getPollTimeThreeHour() {
		return pollTimeThreeHour;
	}

	private void setPollTimeThreeHour(Integer pollTimeThreeHour) {
		this.pollTimeThreeHour = pollTimeThreeHour;
	}

	public Integer getPollTimeFourHour() {
		return pollTimeFourHour;
	}

	private void setPollTimeFourHour(Integer pollTimeFourHour) {
		this.pollTimeFourHour = pollTimeFourHour;
	}

	public Integer getPollTimeOneMin() {
		return pollTimeOneMin;
	}

	private void setPollTimeOneMin(Integer pollTimeOneMin) {
		this.pollTimeOneMin = pollTimeOneMin;
	}

	public Integer getPollTimeTwoMin() {
		return pollTimeTwoMin;
	}

	private void setPollTimeTwoMin(Integer pollTimeTwoMin) {
		this.pollTimeTwoMin = pollTimeTwoMin;
	}

	public Integer getPollTimeThreeMin() {
		return pollTimeThreeMin;
	}

	private void setPollTimeThreeMin(Integer pollTimeThreeMin) {
		this.pollTimeThreeMin = pollTimeThreeMin;
	}

	public Integer getPollTimeFourMin() {
		return pollTimeFourMin;
	}

	private void setPollTimeFourMin(Integer pollTimeFourMin) {
		this.pollTimeFourMin = pollTimeFourMin;
	}

	public void setPollTimeOne(Calendar pollTimeOne) {
		this.pollTimeOne = pollTimeOne;
	}

	public void setPollTimeTwo(Calendar pollTimeTwo) {
		this.pollTimeTwo = pollTimeTwo;
	}

	public void setPollTimeThree(Calendar pollTimeThree) {
		this.pollTimeThree = pollTimeThree;
	}

	public void setPollTimeFour(Calendar pollTimeFour) {
		this.pollTimeFour = pollTimeFour;
	}

	public int getDownloadCycles() {
		return downloadCycles;
	}

	public void setDownloadCycles(int downloadCycles) {
		this.downloadCycles = downloadCycles;
	}

	public int getLastDownloadFileCount() {
		return lastDownloadFileCount;
	}

	public void setLastDownloadFileCount(int lastDownloadFileCount) {
		this.lastDownloadFileCount = lastDownloadFileCount;
	}

	public Date getLastStartup() {
		return lastStartup;
	}

	public void setLastStartup(Date lastStartup) {
		this.lastStartup = lastStartup;
	}

	@Override
	public String toString() {
		
		StringBuilder out = new StringBuilder();
		/*Field[] fields = this.getClass().getDeclaredFields();

		for(int i = 0; fields.length > i; i++) {
			
			Method method;
			String methodName;
			try {
				
				methodName = fields[i].getName();				
				method = getClass().getMethod(( "get" + 
						 methodName.replaceFirst(methodName.substring(0, 1), methodName.substring(0, 1).toUpperCase())));
				out += " " + method.getName() + "()";
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} 
			
		}*/
		
		out.append(" Status = " + getStatus()+";  ");
		out.append("LastDownload = " + getLastDownLoad()+";  ");
		out.append("NextDownload = " + getNextDownLoad()+";  ");
		out.append("Method = " + getMethod()+";  ");
		out.append("MethodDetail = " + getMethodDetail()+";  ");
		out.append("DownloadCycles = " + getDownloadCycles()+";  ");
		out.append("FileCount = " + getLastDownloadFileCount()+";  ");
		out.append("PollSetting = " + getPollSetting()+";  ");
		out.append("Message = " + getMessage()+";  ");
		out.append("ErrorMessage = " + getErrorMessages()+";  ");
		out.append("DismissableErrorMessage = " + getDismissableErrorMessages()+";  ");
		return out.toString();
	}
	
}
