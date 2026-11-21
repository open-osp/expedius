package com.colcamex.www.health;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe, in-memory record of key activity timestamps used by the
 * health check endpoint.
 *
 * Values are held in memory only and reset when the application restarts.
 */
public final class HealthMonitor {

	private static final AtomicReference<Date> lastOscarTransfer = new AtomicReference<Date>();
	private static final AtomicReference<Date> lastExcellerisConnection = new AtomicReference<Date>();

	private HealthMonitor() {
		// static utility
	}

	/**
	 * Record a lab file successfully transferred to Oscar through the OscarWSHandler.
	 */
	public static void recordOscarTransfer() {
		lastOscarTransfer.set(new Date());
	}

	/**
	 * Record a successful connection (login granted) to the Excelleris server.
	 */
	public static void recordExcellerisConnection() {
		lastExcellerisConnection.set(new Date());
	}

	public static Date getLastOscarTransfer() {
		return copy(lastOscarTransfer.get());
	}

	public static Date getLastExcellerisConnection() {
		return copy(lastExcellerisConnection.get());
	}

	/** For unit tests only. */
	static void reset() {
		lastOscarTransfer.set(null);
		lastExcellerisConnection.set(null);
	}

	private static Date copy(Date date) {
		return date == null ? null : new Date(date.getTime());
	}
}
