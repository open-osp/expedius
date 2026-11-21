package com.colcamex.www.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class HealthCheckActionTest {

	@Test
	public void formatsDatesAsIso8601() {
		String formatted = (String) HealthCheckAction.format(new Date(0));
		assertTrue(formatted, formatted.matches("19(69|70)-\\d\\d-\\d\\dT\\d\\d:\\d\\d(:\\d\\d)?([+-]\\d\\d:\\d\\d|Z)"));
	}

	@Test
	public void formatsNullDateAsJsonNull() {
		assertEquals(JSONNull.getInstance(), HealthCheckAction.format(null));
	}

	@Test
	public void reportsDownWhenControllerNotInitialized() {
		// No ExpediusControllerHandler has been created in this JVM.
		JSONObject health = new HealthCheckAction().buildHealth();
		assertEquals(HealthCheckAction.STATUS_DOWN, health.getString("status"));
		assertTrue(health.has("version"));
		assertTrue(health.has("lastOscarTransfer"));
		assertTrue(health.has("lastExcellerisConnection"));
		assertTrue(health.has("timestamp"));
	}
}
