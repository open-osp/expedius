package com.colcamex.www.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.security.Encryption;
import com.colcamex.www.security.KeyCutter;
import com.colcamex.www.util.ExpediusProperties;


/**
 * 
 * @author Dennis Warren
 * @Company Colcamex Resources
 * @Date 
 * @Comment 
 * 
 */
public class CertificateInstallAction extends HttpServlet {
	
    private static Logger logger = Logger.getLogger("FileUpload");
	
    private static final String ALLOWED_FILE_TYPE = "application/x-pkcs12";
    private static final String ERROR = "WEB-INF/pages/error/error.jsp";
	private static final long serialVersionUID = 1L;
	private static final String UPLOAD_DIRECTORY = "upload";
    private static final int THRESHOLD_SIZE = 1024 * 1024 * 3; // 3MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 1; // 1MB
    private static final int REQUEST_SIZE = 1024 * 1024 * 50; // 50MB  
    
    private ConfigurationBeanInterface configurationBean = null;
    private ExpediusProperties properties;
    private File storeFile;
    private KeyCutter keyCutter;
 
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CertificateInstallAction() {
        super();
        // default constructor.
    }
    
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

        String propertiesPath = config.getServletContext().getInitParameter("ExpediusProperties");
        properties = ExpediusProperties.getProperties(propertiesPath);// + "/" + ControllerAction.getPropertiesFileName());
        //configurationBean = (ExcellerisConfigurationBean) BeanRetrieval.getBean("ExcellerisConfigurationBean");
	}
    
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		
		ExpediusControllerHandler controllerHandler = null;
		
		if(properties != null) {

			controllerHandler = ExpediusControllerHandler.getInstance(properties);
		}
		
		String certPass = null;
		String certPassConfirm = null;
		String message = " ";
		FileItem item = null;
		String fieldName = null;

        if (!ServletFileUpload.isMultipartContent(request)) {
        	forward(request, response, ERROR);
        	return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(THRESHOLD_SIZE);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
         
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(REQUEST_SIZE);
        upload.setFileSizeMax(MAX_FILE_SIZE);
         
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
 
        try {
            Iterator iterator = upload.parseRequest(request).iterator();

            while (iterator.hasNext()) {
            	
            	String contentType = null;
            	String fileName = null;
            	String mimeType = null;
            	String filePath = null;
            	
                item = (FileItem) iterator.next();
                fieldName = item.getFieldName();
                
                if(fieldName.equalsIgnoreCase("password")) {
                	certPass = item.getString();
                }
                if(fieldName.equalsIgnoreCase("passwordconfirm")) {
                	certPassConfirm = item.getString();
                }
                if(fieldName.equalsIgnoreCase("certificate")) {
    				configurationBean = controllerHandler.getConfigurationBean(item.getString() + "Bean");
                }
                // processes fields that are not form fields
                if (!item.isFormField()) {
                	
                	contentType = item.getContentType();                	
                	fileName = FilenameUtils.getName(item.getName());               	
                	mimeType = getServletContext().getMimeType(fileName);
                	
                	logger.debug("Upload Mimetype is: "+mimeType);               	
                	logger.debug("Upload content Type: "+contentType);
                    logger.debug("Upload field Name: "+item.getFieldName());
                    logger.debug("Upload file name: "+fileName);
                	
                    if( (contentType.equalsIgnoreCase(ALLOWED_FILE_TYPE)) ||
                    	(mimeType.equalsIgnoreCase(ALLOWED_FILE_TYPE)) ) {

	                    filePath = uploadPath + File.separator + fileName;
	                    storeFile = new File(filePath);
	                    item.write(storeFile);
	                    
                    } else {   
                    	
                    	logger.error("Certificate store must be .pfx format");
                    	
                    	message = "Error: Certificate is not a .pfx file type.";
                    	// request.setAttribute("message", "Error: Certificate is not a .pfx file type.");
                    	
                    }                                     
                } 
                
                item.delete();
            }

        } catch (Exception ex) {
        	logger.error("Exception: ",ex);
        	
        	message = "File upload error: " + ex.getMessage();
        }
         
        if((storeFile != null)&&(Encryption.testPassword(certPass, certPassConfirm))) {
        	
	        if(cutKey(certPass)) { 
	        	
	        	configurationBean.setCertPath(new File(properties.getProperty("TRUSTSTORE_URL")));
	        	logger.debug("Truststore saved");
	        	
	        	configurationBean.setKeyPath(new File(properties.getProperty("KEYSTORE_URL")));
	        	logger.debug("Keystore saved");

	        } else {
	        	
	        	configurationBean.setCertificateInstalled(false);
	        	
	        	message = keyCutter.getError();
	
	        }
	        

	        // delete key from temp directory
	        if(storeFile.delete()) {    
	        	logger.debug("Temp cache file removed");
	        }
	        
        } else {
        	
        	logger.debug("Temporary cache store or certificate passwords have failed.");

        }
        
        // save bean.
        controllerHandler.persistBean();

        request.setAttribute("message", message);
        
        RequestDispatcher dispatch = request.getRequestDispatcher("configuration");
		
		try {
			dispatch.forward(request, response);
		} catch (ServletException e) {
			logger.error("Exception: ",e);
		} catch (IOException e) {
			logger.error("Exception: ",e);
		}
    }
	
	private boolean cutKey(String certPass) {
		
		if(properties == null) {
			logger.error("Cannot cut key, properties file is missing");
			return false;
		}

        // pass over to the key cutter. 
        keyCutter = KeyCutter.getInstance();
        keyCutter.setSourcePath(storeFile);
        keyCutter.setCertificatePassword(certPass);
        keyCutter.setTrustStorePath(properties.getProperty("TRUSTSTORE_URL"));
        keyCutter.setKeyStorePath(properties.getProperty("KEYSTORE_URL"));
        keyCutter.setKeyStorePassword(properties.getProperty("STORE_PASS"));
        keyCutter.setKeyStoreAlias(properties.getProperty("KEYSTORE_ALIAS"));
        keyCutter.setTrustStoreAlias(properties.getProperty("TRUSTSTORE_ALIAS"));
        keyCutter.setStoreType(properties.getProperty("STORE_TYPE"));
        return keyCutter.cutKey();
	
	}
	
	/**
	 * forward method which can be invoked at anytime during the script
	 */
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) {
		
        RequestDispatcher dispatch = request.getRequestDispatcher(path);
        
        try {
			dispatch.forward(request, response);
		} catch (ServletException e) {
			logger.error("Exception: ",e);
		} catch (IOException e) {
			logger.error("Exception: ",e);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
