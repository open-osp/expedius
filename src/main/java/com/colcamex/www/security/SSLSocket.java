package com.colcamex.www.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.colcamex.www.util.ExpediusProperties;


/**
 * @author Dennis Warren @ Colcamex Resources 
 * Date: May 2012.
 * 
 */
public class SSLSocket {
	
	private final Logger logger = LogManager.getLogger("SSLSocket");
	
	protected final static String  TRUSTSTORE_URL = ExpediusProperties.getProperties().getProperty("TRUSTSTORE_URL").trim();
	protected final static String  STORE_TYPE = ExpediusProperties.getProperties().getProperty("STORE_TYPE").trim();
	protected final static String  STORE_PASS = ExpediusProperties.getProperties().getProperty("STORE_PASS").trim();
	protected final static String  HTTPS_PROTOCOL = ExpediusProperties.getProperties().getProperty("HTTPS_PROTOCOL").trim();
	protected final static String  KEYSTORE_URL = ExpediusProperties.getProperties().getProperty("KEYSTORE_URL").trim();
	
	private final static String DEFAULT_PROVIDER = "SunJSSE";
	private final static String DEFAULT_FACTORY_TYPE = "SunX509";
	private final static String DEFAULT_SSL_TYPE = "TLSv1";
	private final static String DEFAULT_STORE_TYPE = "JKS";
	
	private String storeType; 
	private String pass; 
	private String keySource;
	private String trustSource;
	private String httpsProtocol;
	private String trustFactoryType;
	private static SSLSocket instance = null;
	
	/**
	 * Default constructor.
	 */
	protected SSLSocket(){
		// default constructor.
	}
	
	/**
	 * Constructor.
	 */
	protected SSLSocket(
			String trustSource, 
			String storeType, 
			String pass, 
			String httpsProtocol,
			String keySource
	){
		
		this.setStoreType(storeType);
		logger.info("Setting SSLSocket with security protocol: " + httpsProtocol);
		this.setHttpsProtocol(httpsProtocol);
		this.setPass(pass);
		logger.debug("Setting SSLSocket with key store source: " + keySource);
		this.setKeySource(keySource);
		logger.debug("Setting SSLSocket with trust store source: " + trustSource);
		this.setTrustSource(trustSource);
	
	}
	
	/**
	 * Returns a SSLSocket Factory with the default settings in the 
	 * properties file.
	 */
	public static synchronized SSLSocket getInstance() {
		if(instance == null) {		
			instance = new SSLSocket(TRUSTSTORE_URL, 
					STORE_TYPE, 
					STORE_PASS, 
					HTTPS_PROTOCOL, 
					KEYSTORE_URL);
		} else {
			instance = null;
			instance = new SSLSocket(TRUSTSTORE_URL, 
					STORE_TYPE, 
					STORE_PASS, 
					HTTPS_PROTOCOL, 
					KEYSTORE_URL);
		}
		return instance;
	}
	
	public static synchronized SSLSocket getInstance(
			String trustSource, 
			String storeType, 
			String pass, 
			String httpsProtocol,
			String keySource) { 
		// instantiate
		if(instance == null) {		
			instance = new SSLSocket(trustSource, 
					storeType, 
					pass, 
					httpsProtocol,
					keySource);
		} else {
			instance = null;
			instance = new SSLSocket(trustSource, 
					storeType, 
					pass, 
					httpsProtocol,
					keySource);
		}
		return instance;
	}
	
	
	/**
	 * Creates a socket for binding to the HTTPS connection.
	 * @return SSLContext
	 */
	public synchronized SSLContext getSSlContext() throws UnrecoverableKeyException, 
		KeyStoreException, 
		NoSuchAlgorithmException, 
		CertificateException,
		IOException, 
		KeyManagementException, NoSuchProviderException {
		
			if( (this.getHttpsProtocol() == null) || (this.getHttpsProtocol().isEmpty()) ) {
				this.setHttpsProtocol(DEFAULT_SSL_TYPE);
			}

			SSLContext sslContext = SSLContext.getInstance( this.getHttpsProtocol(), DEFAULT_PROVIDER ); 

            sslContext.init(getKeyManagers(), getTrustManagers(), new SecureRandom());

			return sslContext;		
	}
	
	public synchronized SSLSocketFactory getSocketFactory() throws UnrecoverableKeyException, 
		KeyManagementException, 
		KeyStoreException, 
		NoSuchAlgorithmException, 
		CertificateException,
		NoSuchProviderException, 
		IOException {

		// remove all SSL cipher suites. Excelleris prefers TLSv1.
		SSLSocketFactory socketFactory = this.getSSlContext().getSocketFactory();
		String[] cipherSuites = socketFactory.getDefaultCipherSuites();
		ArrayList<String> cipherSuiteList = new ArrayList<String>();
		for(int i = 0; i < cipherSuites.length; i++) {
			if(! cipherSuites[i].contains("SSL")) {
				logger.debug("Adding cipher suite: " + cipherSuites[i]);
				cipherSuiteList.add(cipherSuites[i]);
			}
		}
		cipherSuites = new String[cipherSuiteList.size()];
		cipherSuites = cipherSuiteList.toArray(cipherSuites);		

		return new SSLSocketFactoryWrapper(socketFactory, new String[]{this.getHttpsProtocol()}, cipherSuites);
	}
	
	private KeyManager[] getKeyManagers() throws KeyStoreException, 
		NoSuchAlgorithmException, 
		CertificateException, IOException, 
		UnrecoverableKeyException {
		
		// keys
		KeyStore ks = KeyStore.getInstance(this.getStoreType());  					
		InputStream keySourceStream = new FileInputStream(this.getKeySource());		
		ks.load(keySourceStream, this.getPass().toCharArray());
		
		logger.debug("Setting SSLSocket with key store source: " + this.getKeySource());

		keySourceStream.close();
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(this.getTrustFactoryType());           
		kmf.init(ks, this.getPass().toCharArray());
		
		return kmf.getKeyManagers();
	}
	
	/**
	 * Create a custom trust store
	 */
	private TrustManager[] getTrustManagers() throws KeyStoreException, 
		NoSuchAlgorithmException, CertificateException, 
		IOException {
		
		//trust 
		KeyStore trustKeyStore = KeyStore.getInstance(this.getStoreType());
		trustKeyStore.load(new FileInputStream(this.getTrustSource()), this.getPass().toCharArray());
        
        logger.debug("Setting SSLSocket with trust store source: " + this.getTrustSource());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(this.getTrustFactoryType());
        tmf.init(trustKeyStore);
        
        return tmf.getTrustManagers();
	}

	public String getStoreType() {
		if(this.storeType == null) {
			return DEFAULT_STORE_TYPE;
		}
		return storeType;
	}

	private void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	public String getPass() {
		return pass;
	}

	private void setPass(String pass) {
		this.pass = pass;
	}

	public String getKeySource() {
		return keySource;
	}

	private void setKeySource(String keySource) {
		this.keySource = keySource;
	}

	public String getTrustSource() {
		return trustSource;
	}

	private void setTrustSource(String trustSource) {
		this.trustSource = trustSource;
	}

	public String getHttpsProtocol() {
		if(this.httpsProtocol == null) {
			return DEFAULT_SSL_TYPE;
		}
		return httpsProtocol;
	}

	private void setHttpsProtocol(String httpsProtocol) {
		this.httpsProtocol = httpsProtocol;
	}

	public String getTrustFactoryType() {
		if(this.trustFactoryType == null) {
			return DEFAULT_FACTORY_TYPE;
		}
		return trustFactoryType;
	}

	public void setTrustFactoryType(String trustFactoryType) {
		this.trustFactoryType = trustFactoryType;
	}

}
