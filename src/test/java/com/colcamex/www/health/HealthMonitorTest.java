package com.colcamex.www.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class HealthMonitorTest {

	@Before
	public void setUp() {
		HealthMonitor.reset();
	}

	@Test
	public void timestampsStartNull() {
		assertNull(HealthMonitor.getLastOscarTransfer());
		assertNull(HealthMonitor.getLastExcellerisConnection());
	}

	@Test
	public void recordsOscarTransfer() {
		long before = System.currentTimeMillis();
		HealthMonitor.recordOscarTransfer();
		Date recorded = HealthMonitor.getLastOscarTransfer();
		assertNotNull(recorded);
		assertTrue(recorded.getTime() >= before);
		assertNull(HealthMonitor.getLastExcellerisConnection());
	}

	@Test
	public void recordsExcellerisConnection() {
		HealthMonitor.recordExcellerisConnection();
		assertNotNull(HealthMonitor.getLastExcellerisConnection());
		assertNull(HealthMonitor.getLastOscarTransfer());
	}
}
