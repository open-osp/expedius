package com.colcamex.www.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.colcamex.www.security.SSLSocket;

public class SSLSocketTest {

	private static SSLSocket socket;
	
	@BeforeClass
	public static void setUp() throws Exception {
		socket = SSLSocket.getInstance();
	}
	
	@Test
	public void testGetSocketFactory() {
		try {
			System.out.println("testGetSocketFactory() " + socket.getSocketFactory());
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetSSLContext() {
		try {
			System.out.println("testGetSSLContext() " + socket.getSSlContext().getProvider().getName() );
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testGetPass() {		
		System.out.println("testGetPass() " + socket.getPass());

	}

	@Test
	public void testGetKeySource() {
		System.out.println("testGetKeySource() " + socket.getKeySource());
	}

	@Test
	public void testGetTrustSource() {
		System.out.println("testGetTrustSource() " + socket.getTrustSource());
	}

	@Test
	public void testGetHttpsProtocol() {
		System.out.println("testGetHttpsProtocol() " + socket.getHttpsProtocol());
	}

	@Test
	public void testGetTrustFactoryType() {
		System.out.println("testGetTrustFactoryType() " + socket.getTrustFactoryType());
	}

}
