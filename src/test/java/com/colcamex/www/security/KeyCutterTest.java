package com.colcamex.www.security;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Test class for KeyCutter.
 * The KeyCutter class is responsible for generating keystores and truststores
 * by processing certificates using various configuration parameters.
 * The cutKey method is the primary focus of these tests,
 * as it executes the key cutting process and handles various errors.
 */
public class KeyCutterTest {

	@Test
	public void testCutKey_SuccessfulExecution() {
		KeyCutter keyCutter = KeyCutter.getInstance();
		File dummySource = new File("test-cert.pfx");
		File dummyKeyStore = new File("test-keystore.jks");
		File dummyTrustStore = new File("test-truststore.jks");

		// Setup KeyCutter
		keyCutter.setSourcePath(dummySource);
		keyCutter.setKeyStorePath(dummyKeyStore);
		keyCutter.setTrustStorePath(dummyTrustStore);
		keyCutter.setCertificatePassword("test-password");
		keyCutter.setKeyStorePassword("key-store-password");
		keyCutter.setKeyStoreAlias("test-alias");
		keyCutter.setTrustStoreAlias("trust-alias");

		// Test cutKey
		KeyCutter result = keyCutter.cutKey();

		assertNotNull("KeyCutter should return an instance", result);
		assertNull("No error should be set on successful execution", result.getError());
	}

	@Test
	public void testCutKey_FileNotFound() {
		KeyCutter keyCutter = KeyCutter.getInstance();
		File nonExistentSource = new File("non-existent.pfx");
		File dummyKeyStore = new File("test-keystore.jks");
		File dummyTrustStore = new File("test-truststore.jks");

		// Setup KeyCutter
		keyCutter.setSourcePath(nonExistentSource);
		keyCutter.setKeyStorePath(dummyKeyStore);
		keyCutter.setTrustStorePath(dummyTrustStore);
		keyCutter.setCertificatePassword("test-password");
		keyCutter.setKeyStorePassword("key-store-password");
		keyCutter.setKeyStoreAlias("test-alias");
		keyCutter.setTrustStoreAlias("trust-alias");

		// Test cutKey
		KeyCutter result = keyCutter.cutKey();

		assertNotNull("Error message should be set", result.getError());
		assertEquals("Certificate storage directory not found. Contact support.", result.getError());
	}

	@Test
	public void testCutKey_InvalidPassword() {
		KeyCutter keyCutter = KeyCutter.getInstance();
		File dummySource = new File("test-cert.pfx");
		File dummyKeyStore = new File("test-keystore.jks");
		File dummyTrustStore = new File("test-truststore.jks");

		// Setup KeyCutter
		keyCutter.setSourcePath(dummySource);
		keyCutter.setKeyStorePath(dummyKeyStore);
		keyCutter.setTrustStorePath(dummyTrustStore);
		keyCutter.setCertificatePassword("wrong-password");
		keyCutter.setKeyStorePassword("key-store-password");
		keyCutter.setKeyStoreAlias("test-alias");
		keyCutter.setTrustStoreAlias("trust-alias");

		// Test cutKey
		KeyCutter result = keyCutter.cutKey();

		assertNotNull("Error message should be set", result.getError());
		assertTrue("Error should mention incorrect certificate password",
				result.getError().contains("Certificate password incorrect"));
	}

	@Test
	public void testCutKey_InvalidCertificate() {
		KeyCutter keyCutter = KeyCutter.getInstance();
		File dummySource = new File("invalid-cert.pfx");
		File dummyKeyStore = new File("test-keystore.jks");
		File dummyTrustStore = new File("test-truststore.jks");

		// Setup KeyCutter
		keyCutter.setSourcePath(dummySource);
		keyCutter.setKeyStorePath(dummyKeyStore);
		keyCutter.setTrustStorePath(dummyTrustStore);
		keyCutter.setCertificatePassword("test-password");
		keyCutter.setKeyStorePassword("key-store-password");
		keyCutter.setKeyStoreAlias("test-alias");
		keyCutter.setTrustStoreAlias("trust-alias");

		// Test cutKey
		KeyCutter result = keyCutter.cutKey();

		assertNotNull("Error message should be set", result.getError());
		assertTrue("Error should mention invalid or corrupt certificate",
				result.getError().contains("Not a Certificate or Certificate is corrupt"));
	}

	@Test
	public void testCutKey_NoSuchProvider() {
		KeyCutter keyCutter = KeyCutter.getInstance();
		File dummySource = new File("test-cert.pfx");
		File dummyKeyStore = new File("test-keystore.jks");
		File dummyTrustStore = new File("test-truststore.jks");

		// Setup KeyCutter
		keyCutter.setSourcePath(dummySource);
		keyCutter.setKeyStorePath(dummyKeyStore);
		keyCutter.setTrustStorePath(dummyTrustStore);
		keyCutter.setCertificatePassword("test-password");
		keyCutter.setKeyStorePassword("key-store-password");
		keyCutter.setKeyStoreAlias("test-alias");
		keyCutter.setTrustStoreAlias("trust-alias");
		keyCutter.setImportStoreType("non-existent-provider");

		// Test cutKey
		KeyCutter result = keyCutter.cutKey();

		assertNotNull("Error message should be set", result.getError());
		assertTrue("Error should mention unknown provider", result.getError().contains("Certificate provider unknown"));
	}
}