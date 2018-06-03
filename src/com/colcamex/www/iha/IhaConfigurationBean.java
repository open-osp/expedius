package com.colcamex.www.iha;

import java.io.File;

import com.colcamex.www.bean.ConfigurationBeanInterface;

public class IhaConfigurationBean implements ConfigurationBeanInterface {

	private static final long serialVersionUID = 1L;
	
	private String userName;
	private String password;
	// protected String secretKey;
	
	private String serviceName;
	private String servicePath;
	private String loginPath;
	private String acknowledgePath;
	private String logoutPath;
	private String fetchPath;
	private File certPath;
	private File keyPath;
	private String userLogPath;
	
	private boolean certificateInstalled;

	public IhaConfigurationBean() {
		// default constructor.
	}
	
	@Override
	public void initialize(
			String URI,
			String REQUEST,
			String LOGIN,
			String LOGOUT,
			String ACKNOWLEDGE) {
		
		setServicePath(URI);
		setLoginPath(LOGIN);
		setFetchPath(REQUEST);
		setAcknowledgePath(ACKNOWLEDGE);
		setLogoutPath(LOGOUT);
	}
	
/*	
	public String getSecretKey() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setSecretKey(String secretKey) {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;		
	}

	@Override
	public String getUserName() {
		return this.userName;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;		
	}

	@Override
	public boolean isCertificateInstalled() {
		return certificateInstalled;
	}

	@Override
	public boolean isLoginInfoSet() {
		String password = getPassword();
		String login = getUserName();

		if((password != null)&&(login != null)) {
			if( (! password.isEmpty())&&(! login.isEmpty()) ){
				return true;
			}		
		}
		return false;
	}

	@Override
	public String getServicePath() {
		return this.servicePath;
	}

	@Override
	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
		
	}

	@Override
	public String getLoginPath() {
		return this.loginPath;
	}

	@Override
	public void setLoginPath(String loginPath) {
		this.loginPath = loginPath;
	}

	@Override
	public String getAcknowledgePath() {
		return this.acknowledgePath;
	}

	@Override
	public void setAcknowledgePath(String acknowledgePath) {
		this.acknowledgePath = acknowledgePath;
		
	}

	@Override
	public String getLogoutPath() {
		return this.logoutPath;
	}

	@Override
	public void setLogoutPath(String logoutPath) {
		this.logoutPath = logoutPath;
		
	}

	@Override
	public String getFetchPath() {
		return this.fetchPath;
	}

	@Override
	public void setFetchPath(String fetchPath) {
		this.fetchPath = fetchPath;
	}

	@Override
	public File getCertPath() {
		return this.certPath;
	}

	@Override
	public void setCertPath(File certPath) {
		this.certPath = certPath;
		
	}

	@Override
	public File getKeyPath() {
		return this.keyPath;
	}

	@Override
	public void setKeyPath(File keyPath) {
		this.keyPath = keyPath;
		
	}

	@Override
	public void setCertificateInstalled(boolean certificateInstalled) {
		this.certificateInstalled = Boolean.TRUE;		
	}

	@Override
	public String getUserLogPath() {
		if(this.userLogPath == null) {
			return new String(getServiceName() + "Log" + ".html");
		}
		
		return userLogPath;
	}

	@Override
	public void setUserLogPath(String userLogPath) {
		this.userLogPath = userLogPath;		
	}

	@Override
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;		
	}

	@Override
	public String getServiceName() {
		return this.serviceName;
	}

}
