/*******************************************************************************
 * Copyright (c) 2021, Martin Armbruster
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

package org.emftext.language.java.test.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.test.AbstractJaMoPPTests;
import org.emftext.language.java.test.bulk.SingleFileParserBulkTests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jamopp.options.ParserOptions;
import jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import jamopp.resource.JavaResource2;

/**
 * Class to perform performance tests and measurements.
 */
@Disabled
public class PerformanceTest extends AbstractJaMoPPTests {
	private static final Logger LOGGER = Logger.getLogger("jamopp."
			+ SingleFileParserBulkTests.class.getSimpleName());
	private final String inputFolder = "TeaStore";
	private final Path parentOutput = Paths.get("output_performance");
	
	@Test
	public void measureTeaStoreFullResolution() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
		measurePerformance("teastore-full-resolution", 100, true);
	}
	
	@Test
	public void measureTeaStoreWithoutResolvingEverything() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
		measurePerformance("teastore-without-resolving-everything", 20, true);
	}
	
	@Test
	public void measureTeaStoreWithOneLevelResolution() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
		measurePerformance("teastore-one-level-resolution", 20, false);
	}
	
	@Test
	public void measureTeaStoreSecondVariant() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
		measurePerformance("teastore-second-variant", 20, false);
	}
	
	@Test
	public void printAllAverageTimes() {
		try {
			Files.walk(parentOutput).forEach(path -> {
				var data = PerformanceData.load(path);
				System.out.println(path.getFileName().toString());
				System.out.println("Average parsing time: " + data.getAverageParseTime());
				System.out.println("Average resolution time: " + data.getAverageResolutionTime());
			});
		} catch (IOException e) {
		}
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return false;
	}

	@Override
	protected String getTestInputFolder() {
		return inputFolder;
	}
	
	private void measurePerformance(String name, int max, boolean fullResolution) {
		String testInput = getTestInputFolder();
		LOGGER.debug("Executing performance measurements for " + name);
		Path target = Paths.get(testInput);
		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?src/test/.*?");
		try {
			Files.createDirectories(parentOutput);
		} catch (IOException e1) {
		}
		Path outputMeasurement = parentOutput.resolve(name + ".json");
		PerformanceData result;
		if (Files.exists(outputMeasurement)) {
			result = PerformanceData.load(outputMeasurement);
		} else {
			result = new PerformanceData();
		}
		int actualMax = Math.min(max, max - result.getPoints().size());
		for (int i = 0; i < actualMax; i++) {
			PerformanceDataPoint point = new PerformanceDataPoint();
			long millis = System.currentTimeMillis();
			ResourceSet set = parser.parseDirectory(target);
			millis = System.currentTimeMillis() - millis;
			point.setParseTime(millis);
			if (fullResolution) {
				millis = System.currentTimeMillis();
				EcoreUtil.resolveAll(set);
				millis = System.currentTimeMillis() - millis;
			} else {
				var ress = new HashSet<>(set.getResources());
				millis = System.currentTimeMillis();
				for (Resource r : ress) {
					EcoreUtil.resolveAll(r);
				}
				millis = System.currentTimeMillis() - millis;
			}
			point.setResolutionTime(millis);
			Set<Resource> parsedFiles = new HashSet<>(set.getResources());
			LOGGER.debug("Asserting the resolution of all proxy objects.");
			for (Resource res : parsedFiles) {
				if (res.getContents().size() == 0 || !(fullResolution && res.getURI().isFile())) {
					continue;
				}
				this.assertResolveAllProxies(res);
			}
			LOGGER.debug("Reprinting.");
			for (Resource res : parsedFiles) {
				if (res.getContents().size() == 0 || !(fullResolution && res.getURI().isFile())) {
					continue;
				}
				try {
					this.testReprint((JavaResource2) res);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
			result.getPoints().add(point);
			PerformanceData.save(result, outputMeasurement);
			for (Resource res : parsedFiles) {
				res.unload();
			}
		}
		LOGGER.debug("Finished meausring " + name);
	}
}
