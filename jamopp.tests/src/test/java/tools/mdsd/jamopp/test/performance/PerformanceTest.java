/*******************************************************************************
 * Copyright (c) 2021-2026, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package tools.mdsd.jamopp.test.performance;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.mdsd.jamopp.test.AbstractJaMoPPTests;

/**
 * Class to perform performance tests and measurements.
 */
public class PerformanceTest extends AbstractJaMoPPTests {
	private static final Logger LOGGER = LogManager.getLogger("jamopp."
			+ PerformanceTest.class.getSimpleName());
	protected static PerformanceTestExecutor TEST_EXECUTOR;

	@BeforeAll
	public static void setupEverything() {
		PerformanceTestStandaloneMain.setupRegistries();
		TEST_EXECUTOR = new PerformanceTestExecutor(PerformanceTestStandaloneMain.DEFAULT_INPUT_PATH.toAbsolutePath(),
			PerformanceTestStandaloneMain.DEFAULT_OUTPUT_PATH.toAbsolutePath());
	}
	
	@BeforeEach
	public void setup() throws IOException {
		TEST_EXECUTOR.setupTestEnvironment();
	}
	
	@Test
	public void measureTeaStoreFullResolution() {
		TEST_EXECUTOR.measureTeaStoreFullResolution();
	}
	
	@Test
	public void measureTeaStoreWithOneLevelResolution() {
		TEST_EXECUTOR.measureTeaStoreWithOneLevelResolution();
	}
	
	@Test
	public void measureTeaStoreSecondVariant() {
		TEST_EXECUTOR.measureTeaStoreSecondVariant();
	}
	
	@AfterAll
	public static void clean() throws IOException {
		TEST_EXECUTOR.cleanEverything();
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return true;
	}

	@Override
	protected String getTestInputFolder() {
		return PerformanceTestStandaloneMain.DEFAULT_INPUT_PATH.toString();
	}
}
