
package org.oscarehr.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.oscarehr.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Login_QNAME = new QName("http://ws.oscarehr.org/", "login");
	private final static QName _LoginResponse_QNAME = new QName("http://ws.oscarehr.org/", "loginResponse");
	private final static QName _NotAuthorisedException_QNAME = new QName("http://ws.oscarehr.org/", "NotAuthorisedException");
	private final static QName _ParseHL7_QNAME = new QName("http://ws.oscarehr.org/", "parseHL7");
    private final static QName _ParseHL7Response_QNAME = new QName("http://ws.oscarehr.org/", "parseHL7Response");
    private final static QName _SaveHL7_QNAME = new QName("http://ws.oscarehr.org/", "saveHL7");
    private final static QName _SaveHL7Response_QNAME = new QName("http://ws.oscarehr.org/", "saveHL7Response");
    private final static QName _Exception_QNAME = new QName("http://ws.oscarehr.org/", "Exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.oscarehr.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Login }
     * 
     */
    public Login createLogin() {
        return new Login();
    }

	/**
     * Create an instance of {@link LoginResponse }
     * 
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

	/**
     * Create an instance of {@link NotAuthorisedException }
     * 
     */
    public NotAuthorisedException createNotAuthorisedException() {
        return new NotAuthorisedException();
    }

	/**
     * Create an instance of {@link LoginResultTransfer }
     * 
     */
    public LoginResultTransfer createLoginResultTransfer() {
        return new LoginResultTransfer();
    }

	/**
     * Create an instance of {@link JAXBElement }{@code <}{@link Login }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "login")
    public JAXBElement<Login> createLogin(Login value) {
        return new JAXBElement<Login>(_Login_QNAME, Login.class, null, value);
    }

	/**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "loginResponse")
    public JAXBElement<LoginResponse> createLoginResponse(LoginResponse value) {
        return new JAXBElement<LoginResponse>(_LoginResponse_QNAME, LoginResponse.class, null, value);
    }

	/**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotAuthorisedException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "NotAuthorisedException")
    public JAXBElement<NotAuthorisedException> createNotAuthorisedException(NotAuthorisedException value) {
        return new JAXBElement<NotAuthorisedException>(_NotAuthorisedException_QNAME, NotAuthorisedException.class, null, value);
    }

	/**
     * Create an instance of {@link ParseHL7 }
     * 
     */
    public ParseHL7 createParseHL7() {
        return new ParseHL7();
    }

    /**
     * Create an instance of {@link ParseHL7Response }
     * 
     */
    public ParseHL7Response createParseHL7Response() {
        return new ParseHL7Response();
    }

    /**
     * Create an instance of {@link SaveHL7 }
     * 
     */
    public SaveHL7 createSaveHL7() {
        return new SaveHL7();
    }

    /**
     * Create an instance of {@link SaveHL7Response }
     * 
     */
    public SaveHL7Response createSaveHL7Response() {
        return new SaveHL7Response();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParseHL7 }{@code >}}
     * 
     */
//    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "parseHL7")
    public JAXBElement<ParseHL7> createParseHL7(ParseHL7 value) {
        return new JAXBElement<ParseHL7>(_ParseHL7_QNAME, ParseHL7 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParseHL7Response }{@code >}}
     * 
     */
//    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "parseHL7Response")
    public JAXBElement<ParseHL7Response> createParseHL7Response(ParseHL7Response value) {
        return new JAXBElement<ParseHL7Response>(_ParseHL7Response_QNAME, ParseHL7Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveHL7 }{@code >}}
     * 
     */
//    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "saveHL7")
    public JAXBElement<SaveHL7> createSaveHL7(SaveHL7 value) {
        return new JAXBElement<SaveHL7>(_SaveHL7_QNAME, SaveHL7 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveHL7Response }{@code >}}
     * 
     */
//    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "saveHL7Response")
    public JAXBElement<SaveHL7Response> createSaveHL7Response(SaveHL7Response value) {
        return new JAXBElement<SaveHL7Response>(_SaveHL7Response_QNAME, SaveHL7Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
//    @XmlElementDecl(namespace = "http://ws.oscarehr.org/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

}
