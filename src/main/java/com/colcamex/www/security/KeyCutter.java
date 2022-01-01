/**
 * @author Dennis Warren
 * @Company OSCARprn by Treatment
 * @Date Jun 3, 2012
 * @Comment Copy Right OSCARprn by Treatment
 * 
 */
package com.colcamex.www.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Dennis Warren
 * @Company Colcamex Resources
 * @Date Jun 3, 2012
 * @Comment 
 * 		If you prefer to do this at the command line via java's keytool
 *		keytool -importkeystore -srckeystore mypfxfile.pxf -srcstoretype pkcs12 
 *		-destkeystore clientcert.jks -deststoretype JKS
 *		
 */
public class KeyCutter {
	
	private static Logger logger = LogManager.getLogger("KeyCutter");
	
	private static final String DEFAULT_IMPORT_STORE_TYPE = "pkcs12";
	
	private static KeyCutter instance = null;
	private String error;
	private File sourcePath;
	private File trustStorePath;
	private File keyStorePath;
	private String certificatePassword;
	private String keyStorePassword;
	private String keyStoreAlias;
	private String trustStoreAlias;
	private String storeType;
	private String importStoreType;
	private FileInputStream fis = null;
	private FileOutputStream out = null;
		
	/**
	 * Default Constructor.
	 * 
	 */
	protected KeyCutter() {
		// default constructor.
		setImportStoreType(DEFAULT_IMPORT_STORE_TYPE);
	}
	
	/**
	 * Static constructor. Returns an instance of ExcellerisConnect.
	 * 
	 * @return
	 */
	public static KeyCutter getInstance() { 
		// instantiate
		if(instance == null) {		
			instance = new KeyCutter();
		} else {
			instance = null;
			instance = new KeyCutter();
		}
		return instance;
	}

	public boolean cutKey() {
		
		boolean success = false;
		
		try {
			
			KeyStore keystore = keyStore();
			KeyStore truststore = trustStore();
		
			if(keystore != null) {
				out = new FileOutputStream(getKeyStorePath());
				keystore.store(out, getKeyStorePassword());
				out.close();
				success = true;
			} 

			if(truststore != null) {
				out = new FileOutputStream(getTrustStorePath());
				truststore.store(out, getKeyStorePassword());
				out.close();
				success = true;
			} 
			
		} catch (FileNotFoundException e) {
			logger.error("Exception: ",e);
			setError("Certificate storage directory not found. Contact support.");
		} catch (KeyStoreException e) {
			logger.error("Exception: ",e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception: ",e);
		} catch (CertificateException e) {
			logger.error("Exception: ",e);
		} catch (IOException e) {
			
			if(e.getCause() instanceof javax.crypto.BadPaddingException) {
				setError("Error: Certificate password incorrect.");
				logger.error("Exception caused by incorrect certificate password.",e);
			}
			
			logger.error("Not a Certificate or Certificate is corrupt.",e);
			setError("Error: Not a Certificate or Certificate is corrupt.");
			
		} catch (UnrecoverableKeyException e) {
			setError("Certificate password incorrect. "+e);
			logger.error("Exception unknown. Could be incorrect certificate password: ",e);
			
		} catch (NoSuchProviderException e) {
			logger.error("Exception: ",e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("Exception: ",e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Exception: ",e);
				}
			}
		}
		
		return success;
	}
	
	private KeyStore keyStore() 
			throws KeyStoreException, 
			NoSuchAlgorithmException, CertificateException, 
			IOException, UnrecoverableKeyException, NoSuchProviderException {
		
		fis = new FileInputStream(getSourcePath());
		
		KeyStore keystore = KeyStore.getInstance(getImportStoreType());
		KeyStore jksKeystore = KeyStore.getInstance(getStoreType());

		jksKeystore.load(null, null);
		keystore.load(fis, getCertificatePassword());

		fis.close();

		Enumeration<String> aliases = keystore.aliases();
		String alias = aliases.nextElement();
			
		if(aliases.hasMoreElements()) {
			logger.debug("Found more than one alias: "+alias);
		}

		Key key = keystore.getKey(alias, getCertificatePassword());
		
		Certificate[] chain = keystore.getCertificateChain(alias);
		Certificate cert = null;
		for(int i = 0; chain.length > i; i++) {			
			cert = chain[i];
			logger.debug("Certificate type for number:" + i + " in chain " + cert.getType());
			logger.debug("Public key algorithm for number:" + i + " in chain " +cert.getPublicKey().getAlgorithm());
		}
		
		if(key instanceof PrivateKey) {			
			logger.debug("Installing private key");
			jksKeystore.setKeyEntry(getKeyStoreAlias(), key, getKeyStorePassword(), chain);
		}
		
		return jksKeystore;
	}
	
	private KeyStore trustStore() 
			throws KeyStoreException, NoSuchProviderException, 
			NoSuchAlgorithmException, CertificateException, 
			IOException {
		
		KeyStore keyStore = KeyStore.getInstance(getImportStoreType());
		KeyStore jksTrustStore = KeyStore.getInstance(getStoreType());
		keyStore.load(new FileInputStream (getSourcePath()), getCertificatePassword());
		jksTrustStore.load( null , null);

		Enumeration<String> aliases = keyStore.aliases();
		String alias = aliases.nextElement();
		Certificate[] cert = keyStore.getCertificateChain(alias);

		logger.debug("Installing CACert into trust store");
		
		for(int i = 0; cert.length > i; i++) {
			jksTrustStore.setCertificateEntry(getTrustStoreAlias()+i, cert[i]);
		}
		
		return jksTrustStore;
	}

	private File getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(File sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	public void setSourcePath(String sourcePath) {
		setSourcePath(new File(sourcePath));
	}

	private char[] getCertificatePassword() {
		return certificatePassword.toCharArray();
	}

	public void setCertificatePassword(String certificatePassword) {
		this.certificatePassword = certificatePassword;
	}

	private File getTrustStorePath() {
		return trustStorePath;
	}
	
	public void setTrustStorePath(String trustStorePath) {
		setTrustStorePath(new File(trustStorePath));
	}

	public void setTrustStorePath(File trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	private File getKeyStorePath() {
		return keyStorePath;
	}
	
	public void setKeyStorePath(String keyStorePath) {
		setKeyStorePath(new File(keyStorePath));
	}

	public void setKeyStorePath(File keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	private char[] getKeyStorePassword() {
		return keyStorePassword.toCharArray();
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	private String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}

	private String getTrustStoreAlias() {
		return trustStoreAlias;
	}

	public void setTrustStoreAlias(String trustStoreAlias) {
		this.trustStoreAlias = trustStoreAlias;
	}

	private String getStoreType() {
		return storeType;
	}

	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private String getImportStoreType() {
		return importStoreType;
	}

	public void setImportStoreType(String importStoreType) {
		this.importStoreType = importStoreType;
	}
	
}
