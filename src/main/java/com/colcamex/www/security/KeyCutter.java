/**
 * @author Dennis Warren
 * @Company OSCARprn by Treatment
 * @Date Jun 3, 2012
 * @Comment Copy Right OSCARprn by Treatment
 * 
 */
package com.colcamex.www.security;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Dennis Warren
 * @Company Colcamex Resources
 * @Date Jun 3, 2012
 */
public class KeyCutter {

	private static final Logger logger = LogManager.getLogger("KeyCutter");
	private static final String DEFAULT_IMPORT_STORE_TYPE = "pkcs12";
	private static final String DEFAULT_KEYSTORE_NAME = "expedius_key.jks";
	private static final String DEFAULT_TRUSTSTORE_NAME = "expedius_trust.jks";
	private static final String DEFAULT_INTERMEDIATE_CERT_URL = "https://cacerts.digicert.com/DigiCertGlobalG2TLSRSASHA2562020CA1-1.crt.pem";

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
		
		KeyStore keystore;
		KeyStore truststore;
		
		try(FileOutputStream keyStoreOut = new FileOutputStream(getKeyStorePath());
				FileOutputStream trustStoreOut = new FileOutputStream(getTrustStorePath())) {
			
			keystore = keyStore();
			truststore = trustStore();
			keystore.store(keyStoreOut, getKeyStorePassword());
			truststore.store(trustStoreOut, getKeyStorePassword());

		} catch (FileNotFoundException e) {
			logger.error("Exception: ",e);
			setError("Certificate storage directory not found. Contact support.");
		} catch (KeyStoreException e) {
			logger.error("Exception: ",e);
			setError("Contact support." + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception: ",e);
			setError("The certificate algorithm is missing" + e.getMessage());
		} catch (CertificateException e) {
			logger.error("Exception: ",e);
			setError("There was an error with the certificate" + e.getMessage());
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
			setError("Certificate provider unknown." + e.getMessage());
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
				Certificate cert;
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

		X509Certificate intermediateCert = fetchIntermediateCertificate(DEFAULT_INTERMEDIATE_CERT_URL);
		if(intermediateCert != null) {
			jksTrustStore.setCertificateEntry(getTrustStoreAlias() + "_intermediate", intermediateCert);
		}
		
		return jksTrustStore;
	}

	/**
	 * Downloads an intermediate certificate from DigiCert and installs it into the trust store
	 * @param certificateUrl The URL of the certificate to download
	 */
	public X509Certificate fetchIntermediateCertificate(String certificateUrl) {
		try {
			// Download the certificate
			logger.info("Downloading certificate from: " + certificateUrl);
			byte[] certificateBytes = downloadCertificate(certificateUrl);

			if (certificateBytes == null) {
				setError("Failed to download certificate from: " + certificateUrl);
				return null;
			}

			// Parse the certificate
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			return (X509Certificate) certificateFactory.generateCertificate(
					new ByteArrayInputStream(certificateBytes)
			);

		} catch (Exception e) {
			logger.error("Failed to download and install certificate: ", e);
			setError("Failed to download and install certificate: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Downloads certificate data from the given URL
	 * @param certificateUrl The URL to download the certificate from
	 * @return The certificate bytes, or null if download failed
	 */
	private byte[] downloadCertificate(String certificateUrl) {
		try {
			URL url = new URL(certificateUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(10000); // 10 seconds
			connection.setReadTimeout(30000); // 30 seconds
			connection.setRequestProperty("User-Agent", "Expedius Certificate Downloader");

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				logger.error("HTTP error code: " + responseCode + " when downloading certificate");
				return null;
			}

			try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

				while ((bytesRead = in.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesRead);
				}

				return baos.toByteArray();
			}

		} catch (Exception e) {
			logger.error("Error downloading certificate: ", e);
			return null;
		}
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
		Path trustPath = Paths.get(trustStorePath);
		if(Files.isDirectory(trustPath)){
			trustPath = Paths.get(trustPath.toString(), DEFAULT_TRUSTSTORE_NAME);
		}
		setTrustStorePath(trustPath.toFile());
	}

	public void setTrustStorePath(File trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public File getKeyStorePath() {
		return keyStorePath;
	}
	
	public void setKeyStorePath(String keyStorePath) {
		Path keyPath = Paths.get(keyStorePath);
		if(Files.isDirectory(keyPath)){
			keyPath = Paths.get(keyPath.toString(), DEFAULT_KEYSTORE_NAME);
		}
		setKeyStorePath(keyPath.toFile());
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
		return storeType.toLowerCase();
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
