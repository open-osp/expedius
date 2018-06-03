package com.colcamex.www.util;


import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
//import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ExpediusLog {

	private static String LOG_DIR = "/var/lib/expedius/log/";
	private static String HTML_LOG_DIR = "/Users/denniswarren/Documents/colcamex/workspace/Servers/Tomcat v6.0 Server at localhost-config/wtpwebapps/Expedius/";
	
	private static final String LOG_NAME = "expedius.log";
	private static String HTML_LOG_NAME = "expediusLog.html";
	private static Logger log;
	
	private static FileHandler fileTxt;
	private static SimpleFormatter formatterTxt;	
	private static FileHandler fileHTML;
	private static ExcellerisLogHTMLFormatter formatterHTML;	

//	static
//    {
//        synchronized( SimpleFormatter.class ) {}
//        synchronized( SimpleDateFormat.class ) {}
//        synchronized( Date.class ) {}
//        synchronized( LogRecord.class ) {}
//        synchronized( Formatter.class ) {}
//    }
	
	public static void setLogPath(String path) {
		if(path != null) {
			ExpediusLog.LOG_DIR = path;
		}
	}
	
	public static void sethtmlLogPath(String path) {
		if(path != null) {
			ExpediusLog.HTML_LOG_DIR = path;
		}
	}
	
	public static String getHtmlLogName() {
		return HTML_LOG_NAME;
	}

	public static void setHtmlLogName(String htmlLogName) {
		if(htmlLogName != null) {
			ExpediusLog.HTML_LOG_NAME = htmlLogName;
		}
	}

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		ExpediusLog.log = log;
	}

	/**
	 * Open the log file handlers and formatters.
	 * @throws SecurityException 
	 * @throws IOException
	 */
	public static void setup(Level level, Logger log) throws SecurityException, IOException {
		
		if(level == null) {
			level = Level.ALL;
		}
		
		if(log == null) {
			setLog(Logger.getLogger(""));
		} else {
			setLog(log);
		}
		
		boolean append = true;		

		ExpediusLog.log.setLevel(level);
		
		fileTxt = new FileHandler(LOG_DIR+LOG_NAME, append);
		//fileHTML = new FileHandler(HTML_LOG_DIR+HTML_LOG_NAME, 1024000, 3, append);
		fileHTML = new FileHandler(HTML_LOG_DIR+HTML_LOG_NAME, append);


		// Create txt Formatter
		formatterTxt = new SimpleFormatter();
		fileTxt.setFormatter(formatterTxt);
		ExpediusLog.log.addHandler(fileTxt);
		

		// Create HTML Formatter
		formatterHTML = new ExcellerisLogHTMLFormatter();
		fileHTML.setFormatter(formatterHTML);
		ExpediusLog.log.addHandler(fileHTML);

	}

	/**
	 * Shut down the log file handlers.
	 * @throws IOException
	 */
	static public void close() throws IOException {
		
		if(fileHTML != null) {
			fileHTML.flush();
			fileHTML.close();
		}
		
		if(fileTxt != null) {
			fileTxt.flush();
			fileTxt.close();
		}
	}
}

