package com.colcamex.www.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.colcamex.www.bean.ControllerBean;
import com.colcamex.www.handler.ExpediusControllerHandler;
import com.colcamex.www.handler.PollTimer;
import com.colcamex.www.health.HealthMonitor;
import com.colcamex.www.util.ExpediusProperties;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * Health check endpoint. Returns a JSON document describing the current
 * state of Expedius.
 *
 * HTTP 200 when status is UP, HTTP 503 otherwise.
 *
 * Example:
 * {
 *   "status": "UP",
 *   "version": "4.12.19",
 *   "serviceRunning": true,
 *   "lastOscarTransfer": "2026-11-20T10:15:02-08:00",
 *   "lastExcellerisConnection": "2026-11-20T10:15:00-08:00",
 *   "lastDownload": "2026-11-20T10:15:00-08:00",
 *   "nextDownload": "2026-11-20T12:15:00-08:00",
 *   "errorCount": 0,
 *   "timestamp": "2026-11-20T10:20:00-08:00"
 * }
 */
public class HealthCheckAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(HealthCheckAction.class);

	public static final String STATUS_UP = "UP";
	public static final String STATUS_DOWN = "DOWN";

	private static final String VERSION_PREFIX = "version";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		JSONObject health = buildHealth();

		response.setStatus(STATUS_UP.equals(health.getString("status"))
				? HttpServletResponse.SC_OK
				: HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

		PrintWriter writer = response.getWriter();
		writer.write(health.toString(2));
		writer.flush();
	}

	JSONObject buildHealth() {

		JSONObject health = new JSONObject();
		ExpediusControllerHandler controllerHandler = ExpediusControllerHandler.getInstance();
		ControllerBean controllerBean = null;
		ExpediusProperties properties = null;
		String reason = null;

		if (controllerHandler == null) {
			reason = "Expedius controller is not initialized.";
		} else {
			controllerBean = controllerHandler.getControllerBean();
			properties = controllerHandler.getProperties();
			if (controllerBean == null) {
				reason = "Expedius configuration could not be loaded.";
			}
		}

		boolean pollTimerRunning = false;
		try {
			pollTimerRunning = PollTimer.isRunning();
		} catch (RuntimeException e) {
			logger.warn("Unable to determine poll timer state.", e);
		}

		if (reason == null && !pollTimerRunning) {
			reason = "Expedius auto download service is stopped.";
		}

		health.put("status", reason == null ? STATUS_UP : STATUS_DOWN);
		if (reason != null) {
			health.put("reason", reason);
		}
		health.put("version", getVersion(properties));
		health.put("serviceRunning", pollTimerRunning);
		health.put("lastOscarTransfer", format(HealthMonitor.getLastOscarTransfer()));
		health.put("lastExcellerisConnection", format(HealthMonitor.getLastExcellerisConnection()));

		if (controllerBean != null) {
			health.put("lastDownload", format(controllerBean.getLastDownLoad()));
			health.put("lastStartup", format(controllerBean.getLastStartup()));
			health.put("errorCount", controllerBean.getDismissableErrorMessages() == null
					? 0 : controllerBean.getDismissableErrorMessages().size());
		}

		if (pollTimerRunning) {
			health.put("nextDownload", format(PollTimer.getNextRun()));
		}

		health.put("timestamp", format(new Date()));

		return health;
	}

	/**
	 * VERSION property is in the form "Version 4.12.19"; report just the number.
	 */
	static Object getVersion(ExpediusProperties properties) {
		if (properties == null) {
			properties = ExpediusProperties.getProperties();
		}
		String version = properties == null ? null : properties.getProperty("VERSION");
		if (version == null || version.trim().isEmpty()) {
			return JSONNull.getInstance();
		}
		version = version.trim();
		if (version.toLowerCase().startsWith(VERSION_PREFIX)) {
			version = version.substring(VERSION_PREFIX.length()).trim();
		}
		return version;
	}

	static Object format(Date date) {
		if (date == null) {
			return JSONNull.getInstance();
		}
		return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
