package com.colcamex.www.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.excelleris.ExcellerisConfigurationBean;
import com.colcamex.www.excelleris.ExcellerisController;
import com.colcamex.www.http.ExpediusConnect;
import com.colcamex.www.http.AbstractConnectionController;
import com.colcamex.www.iha.IhaConfigurationBean;
import com.colcamex.www.iha.IhaController;
import com.colcamex.www.util.BeanRetrieval;
import com.colcamex.www.util.ExpediusProperties;

/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: Jan 2016.
 * 
 */
public class ExpediusControllerHandler {
	
	public static Logger logger = LogManager.getLogger(ExpediusControllerHandler.class);

	public static final Class<IhaConfigurationBean> IHA_CONFIGURATION_NAME = IhaConfigurationBean.class;
	public static final Class<ExcellerisConfigurationBean> EXCELLERIS_CONFIGURATION_NAME = ExcellerisConfigurationBean.class;
	public static final Class<ControllerBean> CONTROLLER_NAME = ControllerBean.class;
	
	private ControllerBean controllerBean;
	private HashMap<String, ConfigurationBeanInterface> configurationBeans;	// contain all the settings for each service.
	private ExpediusProperties properties;
	private ServiceExecuter serviceExecuter;
	private ExpediusMessageHandler messageHandler;
	private static ExpediusControllerHandler instance;
	private boolean excellerison;
	private boolean ihapoion;

	private ExpediusControllerHandler(ExpediusProperties properties) {
		_init(properties);	
	}
		
	public static ExpediusControllerHandler getInstance(ExpediusProperties properties) {
		if(instance == null) {
			instance = new ExpediusControllerHandler(properties);
		}
		return instance;
	}
	
	public static ExpediusControllerHandler getInstance() {
		return instance;
	}

	private void _init(ExpediusProperties properties) {
		Thread thread = Thread.currentThread();
		thread.setName("ExpediusControllerHandler"+"["+thread.getId()+"]");
		logger.debug("Initializing ExpediusControllerHandler. Current Thread is: " + thread.getName());
		
		setMessageHandler(new ExpediusMessageHandler());
		
		AbstractConnectionController[] connectionControllers = null;
		int arraysize = 0;
		int index = 0;
		ExpediusHL7LabHandler labHandler = null;
		ExpediusConnect connection = null;
		ExpediusW3CDocumentHandler documentHandler = null;

		if(properties != null) {
			
			setProperties(properties);
			
			if(! properties.containsKey("EMR_SSL_ENABLED")) {
				logger.error("Missing emr ssl enabled in properties.");
				return;
			} 
			
			if(! properties.containsKey("EMR_HOST_NAME")) {
				logger.error("Missing webservice hostname in properties.");
				return;
			} 
			
			if(! properties.containsKey("EMR_CONTEXT_PATH")) {	
				logger.error("Missing webservice context path in properties.");
				return;
			} 
			
			if(! properties.containsKey("EMR_SERVICE_ENDPOINT")) {	
				logger.error("Missing webservice endpoint in properties");
				return;
			} 
			
			if(properties.containsKey("EXCELLERIS")) {
				setExcellerison( Boolean.parseBoolean(properties.getProperty("EXCELLERIS")) );
			} 
			
			if(properties.containsKey("IHAPOI")) {
				setIhapoion( Boolean.parseBoolean(properties.getProperty("IHAPOI")) );
			}
									
			checkFirstRun(properties);			

			setControllerBean( CONTROLLER_NAME );

			// pass the ExpediusControllerBean pointer to the ExpediusMessageHandler.
			if(getMessageHandler() != null) {
				getMessageHandler().setControllerBean(getControllerBean());
			}
			
			// createExpediusLog(properties);

		} else {
			logger.error("Properties file not provided.");
			return;
		}

		setConfigurationBeans( new HashMap<String, ConfigurationBeanInterface>() );			

		if(excellerison) {
			arraysize = arraysize + 1;
			getConfigurationBeans().put(EXCELLERIS_CONFIGURATION_NAME.getSimpleName(), getBean(EXCELLERIS_CONFIGURATION_NAME) );
		}
		if(ihapoion) {
			arraysize = arraysize + 1;
			getConfigurationBeans().put(IHA_CONFIGURATION_NAME.getSimpleName(), getBean(IHA_CONFIGURATION_NAME) );
		}

		// don't forget to increase the array size when adding new services.
		if(arraysize > 0) {
			connectionControllers = new AbstractConnectionController[arraysize];
		} else {
			logger.error("No services are set to run");
			return;
		}
		
		/*
		 *  Lab handler parses the downloaded results and then connects to Oscar's web services.
		 *  instantiated here so that incomplete webservice connections to Oscar can be caught prior 
		 *  to going further.
		 */		
		OscarWSHandler webserviceHandler = new OscarWSHandler(properties.getProperty( "EMR_WS_USERNAME" ).trim(), properties.getProperty( "EMR_WS_PASSWORD" ).trim());
		labHandler = new ExpediusHL7LabHandler(properties);
		labHandler.setWebserviceHandler(webserviceHandler);
		
		documentHandler = new ExpediusW3CDocumentHandler();
		connection = ExpediusConnect.getInstance(documentHandler);
	
		if( isExcellerison() ){
			logger.info("Setting Excelleris service ");	

			ConfigurationBeanInterface excellerisConfigurationBean = getConfigurationBean( EXCELLERIS_CONFIGURATION_NAME );
			AbstractConnectionController excelleris = new ExcellerisController(excellerisConfigurationBean, properties);
			excelleris.setServiceName(excellerisConfigurationBean.getServiceName());
			excelleris.setDocumentHandler(documentHandler);
			excelleris.setConnection(connection);
			connectionControllers[index] = excelleris;
			index = index + 1;
		}
		
		if( isIhapoion() ) {
			logger.info("Setting IHAPOI service ");
			/*
			 *  in case it was forgotten during the first run - the certificate status
			 *  needs to be changed to true.
			 */
			ConfigurationBeanInterface ihaConfiguration = getConfigurationBean( IHA_CONFIGURATION_NAME );
			if(! ihaConfiguration.isCertificateInstalled()) {
				ihaConfiguration.setCertificateInstalled(Boolean.TRUE);
			}
			
			AbstractConnectionController ihapoi = new IhaController(ihaConfiguration, properties);
			ihapoi.setServiceName(ihaConfiguration.getServiceName());
			ihapoi.setDocumentHandler(documentHandler);
			ihapoi.setConnection(connection);
			connectionControllers[index] = ihapoi;	
			index = index + 1;
		}
		
		if (connectionControllers.length > 0) {

			serviceExecuter = ServiceExecuter.getInstance();
			serviceExecuter.setServices(connectionControllers);
			if(this.getMessageHandler() != null) {
				serviceExecuter.setMessageHandler(this.getMessageHandler());
			}
			if(labHandler != null) {
				serviceExecuter.setLabHandler(labHandler);	
			}
		}
			

		getMessageHandler().persist();
		persistConfigurationBeans();
	}
	
	/**
	 * Stop the timer
	 */
	@SuppressWarnings("rawtypes")
	public void stop() {
		if(PollTimer.isRunning()) {	
			List<Runnable> stopStatus = PollTimer.stop();
			
			for(Runnable status : stopStatus) {
				if(status instanceof ScheduledFuture) {
					logger.debug("Stopping thread(s): "+status.toString());
					((ScheduledFuture) status).cancel(true);				
				}
						
			}

			getMessageHandler().setServiceStatus(Boolean.FALSE);
			
		}
	}
    
	/**
	 * Start the timer to execute timed lab downloads
	 */
    public void start() {
    	
    	Object pollFrequency = null;
    	int pollSetting = 0;
    	logger.info("<-- STARTING AUTO DOWNLOAD -->");
    	
    	boolean clientStatus = isClientsReady(); 
    	
    	logger.info("Client status is: " + clientStatus);
    	
		//boolean hostStatus = isHostStatus();				
		//logger.info("Host status is: "+hostStatus);		
    	
    	if( clientStatus){// && hostStatus ) {
    		
    		pollSetting = controllerBean.getPollSetting();
	    	
    		if(pollSetting > 0) {
    			
		    	if(pollSetting == ControllerBean.FREQUENCY) {
		    		pollFrequency = controllerBean.getPollInterval();
		    	}
		    	
		    	if(pollSetting == ControllerBean.TIME_OF_DAY) {
		    		pollFrequency = controllerBean.getTimeArray();
		    	}
		    	
		    	if(PollTimer.isRunning()) {
		    		stop();
		    	}
		    	
		    	if(! PollTimer.isRunning()) {		    		
					PollTimer.start(serviceExecuter, pollSetting, pollFrequency);					
					getMessageHandler().setServiceStatus(PollTimer.isRunning());
					getMessageHandler().setLastStartup(new Date());
		    	}
		    	
		    	getMessageHandler().persist();
    		}    	
    	}
    }
    
    /**
     * Initiate a single manual download of labs from all configured services.
     * @return
     */
    public boolean downloadNow() {
    	
    	logger.info("<-- STARTING MANUAL DOWNLOAD -->"); 
    	
		boolean clientStatus = isClientsReady();		
		logger.info("Client status is: "+clientStatus); 
		
		// can be switched off for troubleshooting.
		// boolean downloadWindow = checkDownloadWindow(new Date());	
		boolean downloadWindow = Boolean.TRUE;
		
		if(! downloadWindow) {
			logger.info("Request too soon. Download not allowed.");
		} 	
		
		if( (clientStatus) && (downloadWindow) ) { 
			
    		serviceExecuter.run();
 		
    		logger.debug("Inspecting current thread: "+Thread.currentThread().getName());
    		
    		if(getMessageHandler().getErrorMessages().size() == 0) {
    			return Boolean.TRUE;
    		}
		}
		
		return Boolean.FALSE;
    }
    
    /**
     * 
     * @return
     */
    public boolean isHostStatus() {    		
    	return serviceExecuter.checkHostStatus();
	}


	/**
	 * Ensure that client configuration is set.
	 * @return
	 */
	public boolean isClientsReady() {   	   	
    	return serviceExecuter.checkClientStatus();
    }
	
	/**
	 * Hosts require at least 25 mins between downloads to minimize concurrency and 
	 * reduce bandwidth.
	 * @return
	 */
	public boolean checkDownloadWindow(Date now) {
		
		Date lastDownload = getMessageHandler().getControllerBean().getLastDownLoad();
		
		logger.info("Last download time: "+lastDownload);
		
    	// is the server being "over - polled"?
    	if(lastDownload != null) {    		
    		Date allowedTime = new Date((lastDownload.getTime()) + (25 * 60 * 1000));
    		
    		logger.info("Next time allowed: "+allowedTime);
    		logger.info("Time now is: "+now);
    		
	    	if(now.before(allowedTime)) {
	    		getMessageHandler().addErrorMessage("Lab fetch request sooner than 25 minutes from last request on: "+lastDownload);
	    		return Boolean.FALSE;
	    	}
    	}
    	
    	return Boolean.TRUE;
	}

	private boolean checkFirstRun(ExpediusProperties properties) {
		
		boolean status = true;
		String dataSavePath;
		ArrayList<Object> beans = new ArrayList<Object>();

		if(properties.containsKey("DATA_PATH")) {		
			dataSavePath = properties.getProperty("DATA_PATH");
			BeanRetrieval.setSavePath(dataSavePath);
		} 
		

		if( ! BeanRetrieval.checkBean(CONTROLLER_NAME.getSimpleName()) ) {
			logger.info("First run detected. Creating new ControllerBean.");
			
			ControllerBean controllerBean = new ControllerBean();			
			controllerBean.setPollInterval(PollTimer.DEFAULT_POLL_INTERVAL);
			controllerBean.setPollSetting(ControllerBean.FREQUENCY);
			controllerBean.setStartWithServer(true);
			
			beans.add(controllerBean);

		}

		if(isExcellerison()) {
			try {
				ConfigurationBeanInterface excellerisConfigurationBean;
				if (!BeanRetrieval.checkBean(EXCELLERIS_CONFIGURATION_NAME.getSimpleName())) {
					logger.info("First run detected. Creating new ExcellerisConfigurationBean.");

					excellerisConfigurationBean = new ExcellerisConfigurationBean();
				} else {
					excellerisConfigurationBean = BeanRetrieval.getBean(EXCELLERIS_CONFIGURATION_NAME);
				}
				excellerisConfigurationBean.initialize(
						properties.getProperty("EXCELLERIS_URI"),
						properties.getProperty("REQUEST_NEW"),
						properties.getProperty("LOGIN"),
						properties.getProperty("LOGOUT"),
						properties.getProperty("ACK_POSITIVE")
				);
				excellerisConfigurationBean.setServiceName("excelleris");

				beans.add(excellerisConfigurationBean);
			} catch (Exception e) {

			}
		}
		
		if( ! BeanRetrieval.checkBean(IHA_CONFIGURATION_NAME.getSimpleName())  && isIhapoion() ) {
			logger.info("First run detected. Creating new IhaConfigurationBean.");
			
			ConfigurationBeanInterface ihaConfigurationBean = new IhaConfigurationBean();		
			ihaConfigurationBean.initialize(
				properties.getProperty("IHA_URI"),	
				properties.getProperty("IHA_NEW"),
				properties.getProperty("IHA_LOGIN"),
				null,
				properties.getProperty("IHA_ACK")
			);
			
			ihaConfigurationBean.setServiceName("ihapoi");
			
			// IHA POI does not require a certificate exchange. A public certificate 
			// should have been created during install.
			ihaConfigurationBean.setCertificateInstalled(Boolean.TRUE);
			
			beans.add(ihaConfigurationBean);

		}
		
		if(beans.size() > 0) {
			status = persistBeans(beans);
		}
		
		return status;
	}

	private <T> T getBean(Class<?> beanName) {
		T bean = null;

		try {
			bean = (T) BeanRetrieval.getBean(beanName);
		} catch (FileNotFoundException e) {
			logger.fatal("Bean retrieval error.", e);
		} catch (IOException e) {
			logger.fatal("Bean retrieval error.", e);
		} catch (ClassNotFoundException e) {
			logger.fatal("Bean retrieval error.", e);
		}

		return bean;
	}
	
	public ControllerBean getControllerBean() {
		return controllerBean;
	}
	
	/**
	 * Depends on utility BeanRetrieval.
	 * @param beanName
	 */
	public void setControllerBean(Class<?> beanName) {
		setControllerBean( (ControllerBean) getBean(beanName));		
	}

	private void setControllerBean(ControllerBean controllerBean) {
		this.controllerBean = controllerBean;
	}

	public ExpediusProperties getProperties() {
		return properties;
	}

	public void setProperties(ExpediusProperties properties) {
		this.properties = properties;
	}

	public ConfigurationBeanInterface getConfigurationBean(Class<?> beanName) {
		return getConfigurationBean(beanName.getSimpleName());
	}

	public ConfigurationBeanInterface getConfigurationBean(String beanName) {
		return getConfigurationBeans().get(beanName);
	}

	public HashMap<String, ConfigurationBeanInterface> getConfigurationBeans() {
		return configurationBeans;
	}

	public void setConfigurationBeans(HashMap<String, ConfigurationBeanInterface> configurationBeans) {
		this.configurationBeans = configurationBeans;
	}
	
	public boolean persistConfigurationBeans() {
		boolean status = Boolean.TRUE;
		if(this.configurationBeans.size() > 0) {
			Iterator<String> keys = configurationBeans.keySet().iterator();
			while(keys.hasNext() && status) {
				status = persistBean( configurationBeans.get(keys.next()) );
			}
		}
		return status;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean persistBeans(ArrayList<Object> beans) {
		boolean status = Boolean.TRUE;
		
		Iterator it = beans.iterator();
		while ( (status) && (it.hasNext()) ) {
			status = persistBean( it.next() );	
		}
		
		return status;
	}
	
	/**
	 * Will persist any beans instantiated in this class.
	 * @return
	 */
	public boolean persistBean() {
		boolean status = Boolean.TRUE;
		
		if(! persistBean(controllerBean)) {
			status = Boolean.FALSE;
		}
		if(! persistConfigurationBeans()) {
			status = Boolean.FALSE;
		}
		
		return status;
		
	}

	/**
	 * Depends on BeanRetrieval utility.
	 * @param bean
	 */
	private boolean persistBean(Object bean) {
		
		boolean status = Boolean.TRUE;
		
		try {
			if(bean instanceof ConfigurationBeanInterface) {				
				BeanRetrieval.setBean( (ConfigurationBeanInterface) bean);			
			} else if (bean instanceof ControllerBean) {
				BeanRetrieval.setBean( (ControllerBean) bean );
			}
		} catch (FileNotFoundException e) {
			logger.fatal("Bean retrieval error.", e);
		} catch (IOException e) {
			logger.fatal("Bean retrieval error.", e);
		}
		
		return status;
		
	}

	public ExpediusMessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(ExpediusMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public boolean isExcellerison() {
		return excellerison;
	}

	private void setExcellerison(boolean excellerison) {
		this.excellerison = excellerison;
	}

	public boolean isIhapoion() {
		return ihapoion;
	}

	private void setIhapoion(boolean ihapoion) {
		this.ihapoion = ihapoion;
	}

}
