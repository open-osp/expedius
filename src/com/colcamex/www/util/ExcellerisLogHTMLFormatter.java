/**
 * @author Dennis Warren
 * @Company Colcamex Resources
 * @Date Jun 3, 2012
 * @Filename ExcellerisLogHTMLFormatter.java
 * @Comment Copy Right Dennis Warren o/a Colcamex Resources
 */
package com.colcamex.www.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dennis Warren
 * @Revised Jun 3, 2012
 * @Comment 
 *
 */
public class ExcellerisLogHTMLFormatter extends Formatter{

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord record) {

		String time = calcTime(record.getMillis());	
		StringBuffer buf = new StringBuffer(1000);
		int warningLevel = record.getLevel().intValue();
		String warning = record.getLevel().toString();

		if( warningLevel > Level.CONFIG.intValue()) {
			buf.append("<tr>");		
			buf.append("<td>");
	
			if ( warningLevel >= Level.WARNING.intValue())
			{
				buf.append("<span style=\"color:red;\">");
				buf.append(warning);
				buf.append("</span>");
			} else{
				buf.append(warning);
			}
			buf.append("</td>");
			
			buf.append("<td>");
			buf.append(time);
			buf.append("</td>");
			buf.append("<td>");
			buf.append(formatMessage(record));
			buf.append('\n');
			buf.append("<td>");
			
			buf.append("</tr>\n");
		}
		
		return buf.toString();
	}
	
	private String calcDate(Date date) {
		return calcDate(date.getTime());
	}
	
	private String calcDate(long millisecs)
	{
		return formatTime(new SimpleDateFormat("MMM dd,yyyy"), millisecs);
	}
	
	private String calcTime(long millisecs) {
		return formatTime(new SimpleDateFormat("HH:mm"), millisecs);
	} 
	
	private String formatTime(SimpleDateFormat date_format, long millisecs) {
		Date resultdate = new Date(millisecs);
		return date_format.format(resultdate);
	}

	// This method is called just after the handler using this
	// formatter is created
	public String getHead(Handler h)
	{
		return ("<span class=\"anchor\">" + calcDate(new Date())) + "</span>"
				+ "<table id=\"htmlLog\">\n "
				+ "<tr><th>&nbsp;</th><th>Time</th><th>Log Message</th></tr>\n";
	}

	// This method is called just after the handler using this
	// formatter is closed
	public String getTail(Handler h)
	{
		return "</table>";
	}

}
