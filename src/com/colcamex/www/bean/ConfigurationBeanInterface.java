package com.colcamex.www.bean;

import java.io.File;
import java.io.Serializable;


public interface ConfigurationBeanInterface extends Serializable {

	public void initialize(
			String URI,
			String REQUEST,
			String LOGIN,
			String LOGOUT,
			String ACKNOWLEDGE);
	
	public void setServiceName(String serviceName);
	
	public String getServiceName();
	
	public String getPassword();
	
	public void setPassword(String password); 
	
	public String getUserName();
	
	public void setUserName(String userName);
	
	public boolean isCertificateInstalled();
	
	public boolean isLoginInfoSet();
	
	public String getServicePath();

	public void setServicePath(String servicePath);

	public String getLoginPath();

	public void setLoginPath(String loginPath);

	public String getAcknowledgePath();

	public void setAcknowledgePath(String acknowledgePath);

	public String getLogoutPath();

	public void setLogoutPath(String logoutPath);

	public String getFetchPath();

	public void setFetchPath(String fetchPath);

	public File getCertPath();

	public void setCertPath(File certPath);

	public File getKeyPath();

	public void setKeyPath(File keyPath);

	public void setCertificateInstalled(boolean certificateInstalled);
	
	public String getUserLogPath();
	
	public void setUserLogPath(String userLogPath);

	
}
