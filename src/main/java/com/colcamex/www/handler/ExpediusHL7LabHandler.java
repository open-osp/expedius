package com.colcamex.www.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.colcamex.www.util.ExpediusProperties;

import net.sf.json.JSONObject;


/**
 * This handler class is written to be an extensible
 * solution for handling Excelleris generated HL7 
 * labs.
 * 
 * Currently it acts as a buffer between the Excelleris process and 
 * the lab handling classes in Oscar McMaster.
 * 
 * @author Dennis Warren
 * Colcamex Resources
 * Date: April 2012
 *
 */
public class ExpediusHL7LabHandler {

	public static Logger logger = Logger.getLogger(ExpediusHL7LabHandler.class);
	
	private static final String DEFAULT_SAVE_PATH = "/var/lib/expedius/hl7/";
	private static final String DOCUMENT_FORMAT = "UTF-8";
	public static final int HTTP_WEBSERVICE_ERROR = 100;
	public static final int OK = HttpsURLConnection.HTTP_OK;
	
	private Document hl7labs;
	private String fileName;
	private String providerNumber;
	private String savePath;
	private String serviceName;
	private String labType;
	private int responseCode;
	private OscarWSHandler webserviceHandler;

	public ExpediusHL7LabHandler(){
		// default
	}
	
	/**
	 * Construct by feeding Expedius Properties class. Warning, this does not set the 
	 * web service end point on instantiation.
	 * @param properties
	 */
	public ExpediusHL7LabHandler(ExpediusProperties properties){
		
		if(properties != null) {
			
			if(properties.containsKey("SERVICE_NUMBER")) {
				providerNumber = properties.getProperty("SERVICE_NUMBER").trim();
			}
			
			if(properties.containsKey("SERVICE_NAME")) {
				serviceName = properties.getProperty("SERVICE_NAME").trim();
			}
			
			if(properties.containsKey("LAB_TYPE")) {
				labType = properties.getProperty("LAB_TYPE").trim();
			}
			
			if(properties.containsKey("HL7_SAVE_PATH")) {
				savePath = properties.getProperty("HL7_SAVE_PATH").trim();
			} 
			
			if(savePath == null || savePath.isEmpty()) 
			{
				savePath = DEFAULT_SAVE_PATH;
			}
			
			if(! savePath.endsWith(File.separator))
			{
				savePath = savePath + File.separator;
			}
			
			savePath = savePath + labType + File.separator;
			
			if(! confirmDirectory(savePath, null))
			{
				createDirectory(savePath);
			}
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	private void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public Document getHl7labs() {
		return hl7labs;
	}

	public void setHl7labs(Document hl7labs) {
		this.hl7labs = hl7labs;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		
		String incomingFileName = new String( fileName.trim() );
		
		if(incomingFileName != null) {			
			incomingFileName = incomingFileName.trim();
			if(incomingFileName.contains(File.separator)) {
				incomingFileName = fileName.replace(File.separator, "").trim();
			} 			
			incomingFileName = ( serviceName.toLowerCase() ) + "_" + ( incomingFileName.replaceAll(".enc", "") ).toLowerCase();
		}
		
		this.fileName = incomingFileName;
	}

	public void setProviderNumber(String providerNumber) {
		this.providerNumber = providerNumber;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getSavePath() {
		return savePath;
	}

	public String getLabType() {
		return labType;
	}

	public void setLabType(String labType) {
		this.labType = labType;
	}

	public OscarWSHandler getWebserviceHandler() {
		return webserviceHandler;
	}

	public void setWebserviceHandler(OscarWSHandler webserviceHandler) {
		this.webserviceHandler = webserviceHandler;
	}

	public void reset() {	
		setHl7labs(null);
		setFileName(null);
		setProviderNumber(null);
	}

	/**
	 * Parse and persist HL7 file to Oscar database for viewing in 
	 * Oscars inbox.
	 * 
	 * @return False if file failed save. 
	 * @throws IOException 
	 * @throws Exception 
	 */
	public void saveHL7() throws IOException {
		setResponseCode(HTTP_WEBSERVICE_ERROR);
		
		String fileName = this.getFileName();
		String filePath = this.getSavePath();

		if( (fileName != null) && (this.confirmDirectory(filePath, fileName)) ) {

			String savedFilePath = new String(filePath + fileName);
			
			logger.debug("HL7 lab file Path: " + savedFilePath);
			logger.debug("Service Number (provider)" + providerNumber);
			
			String result = getWebserviceHandler().saveHL7(savedFilePath, providerNumber);

			/*
			 *  {"success":0,"message":"Failed insert lab into DB (Likely duplicate lab): 
			 *  LabUpload.excelleris_qa.xml.1575872768479 of type: PATHL7", "audit":""}
			 */
			JSONObject jsonResponse = JSONObject.fromObject(result);
			
			Integer status = jsonResponse.getInt("success");
			String message = jsonResponse.getString("message");
			String audit = jsonResponse.getString("audit");

			if(status > 0 && "success".equalsIgnoreCase(audit)) {				
				setResponseCode(OK);
			} else if(status == 0 && ("NULL".equalsIgnoreCase(message) || message.isEmpty()) && audit.isEmpty()) {
				setResponseCode(OK);
				logger.warn("Lab uploaded completed, but with an error in the process. File " + fileName);
			} else {					
				logger.error("Oscar Web Service Error while persisting HL7 lab files. Server response File " + fileName + " Error Message: " + message);
			}
			

		} else { 
			logger.debug("Failed to locate saved file: " + fileName);				
		}
	}
	
	/**
	 * Save whatever is in the Hl7labs parameter
	 * @return
	 * @throws TransformerException 
	 * @throws IOException 
	 */
	public boolean saveFile() throws IOException, TransformerException {
		return saveFile(this.getHl7labs());
	} 
	
	/**
	 * Save downloaded HL7 to local disk space. 
	 * Save path is determined in the properties file.
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws TransformerException 
	 */
	public boolean saveFile(Document document) throws IOException, TransformerException {

		setResponseCode(HTTP_WEBSERVICE_ERROR);

		// convert Document into input stream for use with oscar utilities.
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		DOMSource domSource = new DOMSource(document);

		try(StringWriter xmlAsWriter = new StringWriter()){
			StreamResult streamResult = new StreamResult(xmlAsWriter);  
			transformer.transform(domSource, streamResult); 
			return saveFile(xmlAsWriter.toString());
		}
	}  
	
	/**
	 * Save the contents of the given string.
	 * @param contents
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private boolean saveFile(String contents) throws UnsupportedEncodingException, IOException {

		setResponseCode(HTTP_WEBSERVICE_ERROR);
		String savePath = getSavePath() + getFileName();
		
		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents.getBytes(DOCUMENT_FORMAT));
				OutputStream outputStream = new FileOutputStream(savePath))
		{
			logger.info("Saving lab files to " + savePath);
			
			int bytesRead = 0;				
			while ((bytesRead = byteArrayInputStream.read()) != -1){
				outputStream.write(bytesRead);
			}
		}

		// confirm the file was written.
		if( confirmDirectory(getSavePath(), getFileName()) ) {
			setResponseCode(OK);
			
			if(getResponseCode() == OK) {
				logger.info("New lab file saved: success");
			}
			
			return Boolean.TRUE;
			
		} else {
			logger.error("New lab file saved: failed");
			this.setFileName(null);
		}
		
		return Boolean.FALSE;
	}
		
	/** 
	 * Complementary to save path.
	 * @param directoryPath
	 */
	private void createDirectory(String directoryPath) {
		File newDirectory = new File(directoryPath);
		
		if( newDirectory.mkdir() ) {
			logger.info("New lab save directory created at " + newDirectory.getAbsolutePath());
		} else {
			logger.log(Level.ERROR, "Failed to create new save directory.");
		}
	}
	

	/**
	 * Make sure the directory exists and is writable.
	 * 
	 * @param filePath
	 * @return
	 */
	private boolean confirmDirectory(String filePath, String savedFile) {		
		
		boolean allGood = Boolean.TRUE;
		
		if(filePath == null) {
			return Boolean.FALSE;
		}
		
		if(savedFile == null) {
			savedFile = "";
		}
	
		File directoryCheck = new File(filePath + savedFile);		
		if(directoryCheck.exists()) {
	
			if(! directoryCheck.canWrite()) {
				allGood = Boolean.FALSE;
			}
			
			if(! directoryCheck.canRead()) {
				allGood = Boolean.FALSE;
			}
			
		} else {
			allGood = Boolean.FALSE;
		}

		return allGood;
	}

}
