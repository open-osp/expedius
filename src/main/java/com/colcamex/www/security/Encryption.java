package com.colcamex.www.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author dennis warren
 * www.colcamex.com
 * dwarren@colcamex.com
 *
 */
public final class Encryption {

	private static final Logger logger = LogManager.getLogger("ExpediusConnect");
	private static final MessageDigest messageDigest = initMessageDigest("SHA-1");
	private static final MessageDigest messageDigestMd5 = initMessageDigest("MD5");
	private static final QueueCache<String, byte[]> sha1Cache = new QueueCache<String,byte[]>(4, 2048);
	private static final int MAX_SHA_KEY_CACHE_SIZE = 2048;
	private static final String OBSCURE = "/KJHkjh#@323hgfREW:L8764";

	private static MessageDigest initMessageDigest(String format) {
		try
		{
			return(MessageDigest.getInstance(format));
		}
		catch (NoSuchAlgorithmException e)
		{
			return(null);
		}
	}

	public static byte[] getSha1(String s) 
	{
		byte[] b=sha1Cache.get(s);

		if (b==null)
		{
			b=getSha1NoCache(s);
			if (s.length()<MAX_SHA_KEY_CACHE_SIZE) sha1Cache.put(s, b);
		}
		
		return(b);
	}
	

	protected static byte[] getSha1NoCache(String s) {
		if (s == null) return(null);

		try
		{
			synchronized (messageDigest)
			{
				return(messageDigest.digest(s.getBytes()));
			}
		}
		catch (Exception e)
		{
			return(null);
		}
	}

	public static byte[] generateEncryptionKey() 
			throws NoSuchAlgorithmException {
		
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);

		SecretKey secretKey = keyGenerator.generateKey();
		return(secretKey.getEncoded());
	}

	public static byte[] encrypt(SecretKeySpec secretKeySpec, byte[] plainData) {
		
		if (secretKeySpec == null) return(plainData);

		Cipher cipher;
		byte[] results = null;
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			results = cipher.doFinal(plainData);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
		         BadPaddingException e) {
			logger.error("Failed to set encryption cipher", e);
		}

		return  results;
	}

	public static byte[] decrypt(SecretKeySpec secretKeySpec, byte[] encryptedData) {
			
		if (secretKeySpec == null) return(encryptedData);

		Cipher cipher;
		byte[] results = null;
		
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			results = cipher.doFinal(encryptedData);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
		         BadPaddingException e) {
			logger.error("Failed to set decryption cipher", e);
		}

		return results;
	}
	
	
	public static String md5(String input) {
		String output = input + OBSCURE;
		
		if(messageDigestMd5 != null) {
			messageDigestMd5.update(input.getBytes(), 0, input.length());
	        output = new BigInteger(1, messageDigestMd5.digest()).toString(16);
		} else {
			return input;
		}
		
		return output;
	}
	
	public static boolean testPassword(String pass, String passConfirm) {

        if((pass != null)&&(passConfirm != null)) {
	        if(pass.equals(passConfirm)) {
	        	return true;
	        }
        }
		return false;
	}

}
