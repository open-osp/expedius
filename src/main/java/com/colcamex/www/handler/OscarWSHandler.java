package com.colcamex.www.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;


import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;

import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.log4j.Logger;
import org.oscarehr.ws.LabUploadWs;
import org.oscarehr.ws.LabUploadWsService;
import org.oscarehr.ws.LoginResultTransfer2;
import org.oscarehr.ws.LoginWs;
import org.oscarehr.ws.LoginWsService;

import com.colcamex.www.security.SSLSocket;
import com.colcamex.www.util.ExpediusProperties;

/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: Jan 2016.
 * 
 */
public class OscarWSHandler {

	public static Logger logger = Logger.getLogger( OscarWSHandler.class );

	private final static String EMR_WEB_SERVICE = "http://localhost:8080/oscar-SNAPSHOT/ws/";
	private final static String LOGIN_SERVICE = ExpediusProperties.getInstance().getProperty("EMR_LOGIN_ENDPOINT");
	private final static String LAB_UPLOAD_SERVICE = ExpediusProperties.getInstance().getProperty("EMR_LAB_UPLOAD_ENDPOINT");
	private final static URL LOGIN_WSDL = OscarWSHandler.class.getResource("LoginService.wsdl");
	private final static URL LAB_UPLOAD_WSDL = OscarWSHandler.class.getResource("LabUploadService.wsdl");
	private final static String LOGIN_SERVICE_URL = EMR_WEB_SERVICE + LOGIN_SERVICE;
	private final static String LAB_UPLOAD_SERVICE_URL = EMR_WEB_SERVICE + LAB_UPLOAD_SERVICE;

	private LabUploadWs hL7LabUploadWs;
	private LoginWs loginWs;
	private String username;
	private String password;

	@SuppressWarnings("rawtypes")
	public OscarWSHandler() {

		System.setProperty( "com.sun.net.ssl.enableECC", "false");
		
		LoginWsService loginWsService = new LoginWsService(LOGIN_WSDL);
		loginWs = loginWsService.getLoginWsPort();
		BindingProvider provider = (BindingProvider) loginWs;
		provider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, LOGIN_SERVICE_URL  );
		LabUploadWsService labUploadWsService = new LabUploadWsService(LAB_UPLOAD_WSDL);	
		hL7LabUploadWs = labUploadWsService.getLabUploadWsPort();		
		provider = (BindingProvider)hL7LabUploadWs;
		List<Handler> handlerChain = provider.getBinding().getHandlerChain();
		handlerChain.add(new OscarWSAuthHandler());
		provider.getBinding().setHandlerChain(handlerChain);		
		provider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, LAB_UPLOAD_SERVICE_URL  );	
	}

	public OscarWSHandler(String username, String password ) {
		this();
		setUsername( username ); 
		setPassword( password );
	}

	public LoginResultTransfer2 login() {
		return login( getUsername(), getPassword() );
	}
	
	/**
	 * Authenticate and then add the returned token to the SOAP handler chain.
	 * @param user - full login username
	 * @param pass - full user password
	 */
	private LoginResultTransfer2 login( String user, String pass ) {

		if( loginWs != null ) {	
			try {
				logger.debug("Logging In");
				configureSSLConduit( loginWs );
				return loginWs.login2(user, pass);
			} catch (Exception e) {
				logger.error("Unathorized access ", e);
			}		
		}

		return null;
	}

	public String saveHL7( String savedFilePath, String providerNumber) throws IOException {
		Path path = Paths.get(savedFilePath);			
		String fileContent = new String(Files.readAllBytes(path));
		return saveHL7( path.getFileName().toString(), fileContent, providerNumber );
	}

	public String saveHL7( String savedFileName, String fileContent, String providerNumber ) {
		logger.debug("SaveHL7 " + savedFileName);
		configureSSLConduit( hL7LabUploadWs );
		injectAuthenticationToken( hL7LabUploadWs );
		return hL7LabUploadWs.uploadExcelleris(savedFileName, fileContent, providerNumber);
	}

	/**
	 * Get the message handler; OscarWSAuthHandler and put the authentication token into 
	 * the header of this call. 
	 * 
	 * @param hL7LabUploadWs
	 */
	@SuppressWarnings("rawtypes")
	private void injectAuthenticationToken( LabUploadWs hL7LabUploadWs ){
		LoginResultTransfer2 loginResultTransfer = login( getUsername(), getPassword() );
		BindingProvider bindingProvider = (BindingProvider) hL7LabUploadWs;
		List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
		for( Handler handler : handlerChain ){		
			if( handler instanceof OscarWSAuthHandler ) {
				( ( OscarWSAuthHandler ) handler).setLoginResultTransfer( loginResultTransfer );
			}
		}
	}
	
	private void configureSSLConduit( Object port ) {
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();	
		TLSClientParameters tlsCP = new TLSClientParameters();
		try {
			// configure with the Expedius custom Socket Factory.
			tlsCP.setSSLSocketFactory( SSLSocket.getInstance().getSSlContext().getSocketFactory() );
			tlsCP.setSslCacheTimeout(300000);
			tlsCP.setDisableCNCheck(Boolean.TRUE);
			httpConduit.setTlsClientParameters(tlsCP);			
		} catch (UnrecoverableKeyException e) {
			logger.error("", e);
		} catch (KeyManagementException e) {
			logger.error("", e);
		} catch (KeyStoreException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		} catch (CertificateException e) {
			logger.error("", e);
		} catch (FileNotFoundException e) {
			logger.error("", e);
		} catch (NoSuchProviderException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	private String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
