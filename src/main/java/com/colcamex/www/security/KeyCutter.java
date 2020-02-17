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
import org.apache.log4j.Logger;

/**
 * 
 * @author Dennis Warren
 * @Company Colcamex Resources
 * @Date Jun 3, 2012
 */
public class KeyCutter {
	
	private static Logger logger = Logger.getLogger(KeyCutter.class);
	
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

	/**
	 * Default Constructor
	 */
	protected KeyCutter() {
		// default constructor.
		setImportStoreType(DEFAULT_IMPORT_STORE_TYPE);
		setStoreType(DEFAULT_IMPORT_STORE_TYPE);
		setError(null);
	}
	
	/**
	 * Static constructor. Returns an instance of ExcellerisConnect.
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

	public KeyCutter cutKey() {
		
		KeyStore keystore = null;
		KeyStore truststore = null;
		
		try(FileOutputStream keyStoreOut = new FileOutputStream(getKeyStorePath());
				FileOutputStream trustStoreOut = new FileOutputStream(getTrustStorePath())) {
			
			keystore = keyStore();
			truststore = trustStore();
		
			if(keystore != null) {
				keystore.store(keyStoreOut, getKeyStorePassword());
			} 

			if(truststore != null) {
				truststore.store(trustStoreOut, getKeyStorePassword());
			} 
			
		} catch (FileNotFoundException e) {
			logger.error("Exception: ",e);
			setError("Certificate storage directory not found. Contact support.");
		} catch (KeyStoreException e) {
			logger.error("Exception: ",e);
			setError("Contact support." + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception: ",e);
			setError("Contact support." + e.getMessage());
		} catch (CertificateException e) {
			logger.error("Exception: ",e);
			setError("Contact support." + e.getMessage());
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
			setError("Contact support." + e.getMessage());
		} 

		return instance;
	}
	
	private KeyStore keyStore() throws KeyStoreException, 
			NoSuchAlgorithmException, CertificateException, 
			IOException, UnrecoverableKeyException, NoSuchProviderException {
		
		KeyStore keystore = KeyStore.getInstance(getImportStoreType());
		KeyStore jksKeystore = KeyStore.getInstance(getStoreType());
		
		try(FileInputStream fileInputStream = new FileInputStream(getSourcePath())) {
			jksKeystore.load(null, null);
			keystore.load(fileInputStream, getCertificatePassword());
			Enumeration<String> aliases = keystore.aliases();
			String alias = aliases.nextElement();
			
			if(aliases.hasMoreElements()) {
				logger.debug("Found more than one alias: "+alias);
			}

			Key key = keystore.getKey(alias, getCertificatePassword());
			Certificate[] chain = keystore.getCertificateChain(alias);
			
			if(logger.isDebugEnabled())
			{
				Certificate cert = null;
				for(int i = 0; chain.length > i; i++) {			
					cert = chain[i];
					logger.debug("Certificate type for number:" + i + " in chain " + cert.getType());
					logger.debug("Public key algorithm for number:" + i + " in chain " +cert.getPublicKey().getAlgorithm());
				}
			}
			if(key instanceof PrivateKey) {			
				logger.debug("Installing private key");
				jksKeystore.setKeyEntry(getKeyStoreAlias(), key, getKeyStorePassword(), chain);
			}
		}

		return jksKeystore;
	}
	
	private KeyStore trustStore() throws KeyStoreException, NoSuchProviderException, 
			NoSuchAlgorithmException, CertificateException, IOException {
		
		KeyStore keyStore = KeyStore.getInstance(getImportStoreType());
		KeyStore jksTrustStore = KeyStore.getInstance(getStoreType());
		
		try(FileInputStream fileInputStream = new FileInputStream (getSourcePath())){
			keyStore.load(fileInputStream, getCertificatePassword());	
			logger.debug("Installing CACert into trust store");
		}
		
		/*
		 * It's assumed here that a custom truststore was created during installation 
		 * in order to trust the Oscar EMR cert. 
		 */
		try(FileInputStream fileInputStream = new FileInputStream (getTrustStorePath())){
			jksTrustStore.load(fileInputStream , getCertificatePassword());

		} catch(Exception e) {
			jksTrustStore.load(null, null);
		}
		
		Enumeration<String> aliases = keyStore.aliases();
		String alias = aliases.nextElement();
		Certificate[] cert = keyStore.getCertificateChain(alias);
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

	public File getTrustStorePath() {
		return trustStorePath;
	}
	
	public void setTrustStorePath(String trustStorePath) {
		setTrustStorePath(new File(trustStorePath + "expedius_truststore." + getStoreType()));
	}

	public void setTrustStorePath(File trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public File getKeyStorePath() {
		return keyStorePath;
	}
	
	public void setKeyStorePath(String keyStorePath) {
		setKeyStorePath(new File(keyStorePath + "expedius_keystore." + getStoreType()));
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
		return this.error;
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
