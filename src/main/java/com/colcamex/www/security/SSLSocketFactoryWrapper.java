package com.colcamex.www.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;  
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Dennis Warren 
 * @ Colcamex Resources
 * dwarren@colcamex.com
 * www.colcamex.com 
 * Date: October 2014.
 * 
 */
public class SSLSocketFactoryWrapper extends SSLSocketFactory {

	private final SSLSocketFactory socketFactory;
	private final String[] protocols;
	private final String[] cipherSuites;

	
	public SSLSocketFactoryWrapper(final SSLSocketFactory socketFactory, 
			final String[] protocols, final String[] cipherSuites) {
		this.socketFactory = socketFactory;
		this.protocols = protocols;
		this.cipherSuites = cipherSuites;
	}
	
	@Override
	public Socket createSocket(Socket s, String host, int port,
			boolean autoClose) throws IOException {
		final Socket socket = socketFactory.createSocket(s, host, port, autoClose);  
        return override(socket);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return this.socketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return this.socketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		final Socket socket = socketFactory.createSocket( host, port);  
        return override(socket);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		final Socket socket = socketFactory.createSocket(host, port);  
        return override(socket);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {
		final Socket socket = socketFactory.createSocket(host, port);  
        return override(socket);
	}

	@Override
	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		final Socket socket = socketFactory.createSocket(address, port, localAddress, localPort);  
        return override(socket);
	}
	
	private Socket override(final Socket socket) {  
        if (socket instanceof SSLSocket) { 
        	
            if (protocols != null && protocols.length > 0) {  
                ((SSLSocket) socket).setEnabledProtocols(protocols);  
            }  
            
            if( (cipherSuites != null) && (cipherSuites.length > 0) ) {
            	((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);
            }
            
        }  
        
        return socket;  
    } 

}
