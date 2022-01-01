package com.colcamex.www.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.colcamex.www.bean.ConfigurationBeanInterface;
import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.security.Encryption;


/**
 * @author dennis Warren
 * @ Colcamex
 * www.colcamex.com
 * dwarren@colcamex.com
 * 
 * Serializes beans and writes them to a flat file.
 */
public class BeanRetrieval implements Serializable {

	public static Logger logger = LogManager.getLogger("BeanRetrieval");
	
	private static String SAVE_PATH = "/var/lib/expedius/.appdata/";
	private static final long serialVersionUID = 1L;

	
	
	public static void setSavePath(String path) {
		if(path != null){
			
			if(! path.equals(getSavePath())) {
				BeanRetrieval.SAVE_PATH = path;
			} 
		}			
	}

	public static String getSavePath() {
		return BeanRetrieval.SAVE_PATH;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public static Object getBean(String beanName) throws IOException, ClassNotFoundException {
		
		Object bean = null;
	
		if(checkBean(beanName)) {
			
			beanName = Encryption.md5(beanName);	
			
			try(ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(getSavePath() + beanName))
			){
				bean = ois.readObject();
			}
			
		} else {
			logger.error("Bean not found.");
		}	
		
		return bean;
	}
	
	public static boolean checkBean(String beanName) {		
		File file = new File(getSavePath() + Encryption.md5(beanName));
		return file.exists(); 
	}

	public static boolean setBean(ConfigurationBeanInterface configurationBean) throws IOException {
		return setBean((Object) configurationBean);
	}
	
	public static boolean setBean(ControllerBean controllerBean) throws IOException {
		return setBean((Object) controllerBean);
	}
	
	private static boolean setBean(Object bean) throws IOException {
		boolean success = false;		
		String simpleName;
		String beanName;
		
		if(bean != null) {
			
			simpleName = bean.getClass().getSimpleName();	
			beanName = Encryption.md5(simpleName);

			try(ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(getSavePath() + beanName))
			)
			{
				oos.writeObject(bean);
				success = true;
			}
		}
		return success;		
	}

}
