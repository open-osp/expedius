package com.colcamex.www.handler;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;
import org.oscarehr.ws.LoginResultTransfer2;


/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: Jan 2016.
 * 
 * Handler class that overrides handleMessage to modify the message data 
 * such as in this case add the security header.
 * 
 * This needs to be added to the service port handler chain before invoking
 * an endpoint.
 * 
 */
public class OscarWSAuthHandler implements SOAPHandler<SOAPMessageContext> {

	public static Logger logger = Logger.getLogger( OscarWSAuthHandler.class );

	private LoginResultTransfer2 loginResultTransfer;

	@PostConstruct
	public void init() {
		logger.debug( "OscarWSAuthHandler SOAP Message Interceptor Initialized." );
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {

		logger.debug("Handle Message Called.");

		Boolean result = Boolean.TRUE;
		Boolean outbound = (Boolean) context.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );
		SOAPMessage message = context.getMessage();

		logger.debug( "Is Outbound Message: " + outbound );

		// inject security header to all chained web service clients
		if ( outbound ) {   
			try {	        	
				if( message != null && loginResultTransfer != null ) {
					result = injectSecurityHeader( message );	
				}
			} catch (SOAPException e) {
				logger.error( "Failed to inject security header. Was the login WS invoked prior?" , e );
			}
		} 

		return result;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		logger.debug("handleFault has been invoked.");
		return true;
	}

	@Override
	public void close(MessageContext context) {
		logger.debug("close has been invoked.");
	}

	@Override
	public Set<QName> getHeaders() {
		logger.debug("get headers has been invoked.");
		return null;
	}

	private Boolean injectSecurityHeader( SOAPMessage message ) throws SOAPException {

		String prefix = "wsse";
		String uri = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

		SOAPHeader header = message.getSOAPHeader();				
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		if (header == null) {
			header = envelope.addHeader();
		}

		SOAPFactory factory = SOAPFactory.newInstance();
		SOAPElement securityElem = factory.createElement("Security", prefix, uri);
		SOAPElement tokenElem = factory.createElement("UsernameToken", prefix, uri);

		tokenElem.addAttribute( QName.valueOf("wsu:Id"), "UsernameToken-1" );
		tokenElem.addAttribute( QName.valueOf("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );

		// user name
		SOAPElement userElem = factory.createElement( "Username", prefix, uri );
		userElem.addTextNode( getLoginResultTransfer().getSecurityId() + "" );

		// password
		SOAPElement pwdElem = factory.createElement( "Password", prefix, uri );
		pwdElem.addTextNode( getLoginResultTransfer().getSecurityTokenKey() );
		pwdElem.addAttribute( QName.valueOf("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText" );

		// add to header.
		tokenElem.addChildElement(userElem);
		tokenElem.addChildElement(pwdElem);

		securityElem.addChildElement(tokenElem);
		header.addChildElement(securityElem);

		message.saveChanges();
	
		return Boolean.TRUE;
	}

	private LoginResultTransfer2 getLoginResultTransfer() {
		return loginResultTransfer;
	}

	public void setLoginResultTransfer( LoginResultTransfer2 loginResultTransfer ) {
		logger.debug("Login Result Transfer Token Object set");
		this.loginResultTransfer = loginResultTransfer;
	}

}
