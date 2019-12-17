package com.colcamex.www.excelleris;

import java.io.File;

import com.colcamex.www.bean.ConfigurationBeanInterface;


/**
 * @author dennis warren @ Colcamex Resources 
 *
 */
public class ExcellerisConfigurationBean implements ConfigurationBeanInterface {

	private static final long serialVersionUID = 1L;
	
	protected String userName;
	protected String password;
	protected String secretKey;
	
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
	
	public ExcellerisConfigurationBean() {
		// default constructor
	}
	
	public void initialize(
			String URI,
			String REQUEST,
			String LOGIN,
			String LOGOUT,
			String ACKNOWLEDGE) {
		
		setServicePath(URI);
		setLoginPath(LOGIN);
		setLogoutPath(LOGOUT);
		setFetchPath(REQUEST);
		setAcknowledgePath(ACKNOWLEDGE);
		
	}
	
	
	public String getSecretKey() {
		return new String(secretKey);
	}

	
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	@Override
	public String getPassword () {	
		
		if(password == null) {
			return "";
		} 
		
		return new String(password);
		
	}
	
	@Override
	public void setPassword (String password) {		
		this.password = password;				
	}
	
	@Override
	public String getUserName() {
		
		if(userName == null) {
			return "";
		}
		return userName;
	}
	
	@Override
	public void setUserName (String userName) {
		this.userName = userName;

	}
	
	@Override
	public String getServicePath() {
		return servicePath;
	}

	@Override
	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	@Override
	public String getLoginPath() {
		return loginPath;
	}

	@Override
	public void setLoginPath(String loginPath) {
		this.loginPath = loginPath;
	}

	@Override
	public String getAcknowledgePath() {
		return acknowledgePath;
	}

	@Override
	public void setAcknowledgePath(String acknowledgePath) {
		this.acknowledgePath = acknowledgePath;
	}

	@Override
	public String getLogoutPath() {
		return logoutPath;
	}

	@Override
	public void setLogoutPath(String logoutPath) {
		this.logoutPath = logoutPath;
	}

	@Override
	public String getFetchPath() {
		return fetchPath;
	}

	@Override
	public void setFetchPath(String fetchPath) {
		this.fetchPath = fetchPath;
	}

	@Override
	public boolean isCertificateInstalled() {
		certificateInstalled = true;
		
		File certPath = getCertPath();
		File keyPath = getKeyPath();
		
		if(certPath != null) {
			if(! certPath.isFile()) {
				certificateInstalled = false;
			}
		} else {
			certificateInstalled = false;
		}
		if(keyPath != null) {
			if(! keyPath.isFile()) {
				certificateInstalled = false;
			}
		} else {
			certificateInstalled = false;
		}
		return certificateInstalled;
	}
	
	@Override
	public File getCertPath() {
		return certPath;
	}

	@Override
	public void setCertPath(File certPath) {
		this.certPath = certPath;
	}

	@Override
	public File getKeyPath() {
		return keyPath;
	}

	@Override
	public void setKeyPath(File keyPath) {
		this.keyPath = keyPath;
	}

	@Override
	public void setCertificateInstalled(boolean certificateInstalled) {
		this.certificateInstalled = certificateInstalled;
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
	public String getUserLogPath() {
		if(this.userLogPath == null) {
			return new String(getServiceName() + "-Log" + ".html");
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
