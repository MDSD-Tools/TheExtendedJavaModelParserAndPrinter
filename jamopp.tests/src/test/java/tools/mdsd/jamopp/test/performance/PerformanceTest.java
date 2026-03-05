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

package tools.mdsd.jamopp.test.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import tools.mdsd.jamopp.recovery.trivial.TrivialRecovery;
import tools.mdsd.jamopp.resource.JavaResource2;
import tools.mdsd.jamopp.test.AbstractJaMoPPTests;
import tools.mdsd.jamopp.test.OutputUtility;
import tools.mdsd.jamopp.test.OutputUtility.TransferResult;
import tools.mdsd.jamopp.test.bulk.SingleFileParserBulkTests;

/**
 * Class to perform performance tests and measurements.
 */
public class PerformanceTest extends AbstractJaMoPPTests {
	private static final Logger LOGGER = LogManager.getLogger("jamopp."
			+ SingleFileParserBulkTests.class.getSimpleName());
	private final String inputFolder = "target" + File.separator + "src-bulk" + File.separator + "TeaStore";
	private static final Path PARENT_OUTPUT = Paths.get("target", "tests", "output_performance");
	private static final Path JAVA_OUTPUT = PARENT_OUTPUT.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_JAVA);
	private static final Path XMI_OUTPUT = PARENT_OUTPUT.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_XMI);
	private static final Path JSON_OUTPUT = PARENT_OUTPUT.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_JSON);
	private static final Path SUMMARY_FILE_PATH = PARENT_OUTPUT.resolve("summary.md");
	
	@BeforeEach
	public void setup() throws IOException {
		super.initResourceFactory();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("json", new JsonResourceFactory());
		if (Files.exists(JAVA_OUTPUT)) {
			PathUtils.deleteDirectory(JAVA_OUTPUT);
			PathUtils.deleteDirectory(XMI_OUTPUT);
			PathUtils.deleteDirectory(JSON_OUTPUT);
		}
		try {
			Files.createDirectories(JAVA_OUTPUT);
			Files.createDirectories(XMI_OUTPUT);
			Files.createDirectories(JSON_OUTPUT);
		} catch (IOException e1) {
		}
	}
	
	@Test
	public void measureTeaStoreFullResolution() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
		measurePerformance("teastore-full-resolution", getNumberOfRepetitions(), true, false);
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
		measurePerformance("teastore-without-resolving-everything", getNumberOfRepetitions(), true, false);
	}
	
	private void prepareParserOptionsForOneLevelResolution() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
	}
	
	@Test
	public void measureTeaStoreWithOneLevelResolution() {
		prepareParserOptionsForOneLevelResolution();
		measurePerformance("teastore-one-level-resolution", getNumberOfRepetitions(), false, true);
	}
	
	@Disabled("Takes several hours.")
	@Test
	public void measureTeaStoreWithOneLevelResolutionAndFullResolution() {
		prepareParserOptionsForOneLevelResolution();
		measurePerformance("teastore-one-level-resolution-full", 1, true, false);
	}
	
	private void prepareParserOptionsForSecondVariant() {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
	}
	
	@Test
	public void measureTeaStoreSecondVariant() {
		prepareParserOptionsForSecondVariant();
		measurePerformance("teastore-second-variant", getNumberOfRepetitions(), false, true);
	}
	
	@Disabled("Takes several hours.")
	@Test
	public void measureTeaStoreSecondVariantAndFullResolution() {
		prepareParserOptionsForSecondVariant();
		measurePerformance("teastore-second-variant-resolution", 1, true, false);
	}

	@AfterAll
	public static void clean() throws IOException {
		calculateAndSaveAllStatistics();
	}
	
	private static void calculateAndSaveAllStatistics() throws IOException {
		StringBuilder builder = new StringBuilder();

		Files
			.walk(PARENT_OUTPUT, 1)
			.filter(path -> Files.isRegularFile(path))
			.forEach(path -> {
				builder.append("# Results for " + path.getFileName().toString());

				var data = PerformanceData.load(path);
				var stat = data.getStatistics();
				builder.append("\n\nAverage time (ms): " + stat.getMean() + " (with std. " + + stat.getStandardDeviation() + " ms)\n");

				stat = data.getStatisticsWithoutResolution();
				builder.append("Average time without resolution (ms): " + stat.getMean() + " (with std. " + + stat.getStandardDeviation() + " ms)\n");
				builder.append("Average parsing time (ms): " + data.getAverageParseTime());
				builder.append("\nAverage resolution time (ms): " + data.getAverageResolutionTime());
				builder.append("\nAverage recovery time (ms): " + data.getAverageRecoveryTime());

				for (var storage : data.getStorage()) {
					builder.append("\n\nStorage ("
							+ storage.getId()
							+ "): "
							+ storage.getCodeFiles()
							+ " code files of overall "
							+ storage.getOverallFiles()
							+ " files taking "
							+ storage.getTakenStorageByCodeFiles()
							+ " Bytes for code files of overall "
							+ storage.getTakenStorage()
							+ " Bytes.");
				}
				builder.append("\n\n");
			});

		Files.writeString(SUMMARY_FILE_PATH, builder.toString());
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return false;
	}

	@Override
	protected String getTestInputFolder() {
		return inputFolder;
	}

	protected int getNumberOfRepetitions() {
		return 100;
	}
	
	private void measurePerformance(String name, int max, boolean fullResolution, boolean recover) {
		String testInput = getTestInputFolder();
		LOGGER.debug("Executing performance measurements for " + name);
		Path target = Paths.get(testInput);
		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?src/test/.*?");
		
		Path outputMeasurement = PARENT_OUTPUT.resolve(name + ".json");
		PerformanceData result;
		if (Files.exists(outputMeasurement)) {
			result = PerformanceData.load(outputMeasurement);
		} else {
			result = new PerformanceData();
		}
		int actualMax = Math.min(max, max - result.getPoints().size());
		for (int i = 0; i < actualMax; i++) {
			System.out.println("Measurement " + i + " for " + name);
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
			
			if (recover) {
				millis = System.currentTimeMillis();
				new TrivialRecovery(set).recover();
				millis = System.currentTimeMillis() - millis;
				point.setRecoverTime(millis);
			}
			
			Set<Resource> parsedFiles = new HashSet<>(set.getResources());
			LOGGER.debug("Asserting the resolution of all proxy objects.");
			for (Resource res : parsedFiles) {
				if (res.getContents().size() == 0 || (!fullResolution && !res.getURI().isFile())) {
					continue;
				}
				this.assertResolveAllProxies(res);
			}
			
			LOGGER.debug("Reprinting.");
			for (Resource res : parsedFiles) {
				if (res.getContents().size() == 0 || !res.getURI().isFile()) {
					continue;
				}
				String oldUri = res.getURI().toString();
				try {
					this.testReprint((JavaResource2) res);
				} catch (Exception e) {
					fail(e);
				}
				res.setURI(URI.createURI(oldUri));
			}
			
			result.addPoint(point);
			PerformanceData.save(result, outputMeasurement);
			
			if (i == 0 && (fullResolution || recover)) {
				try {
					result.setStorage(measureStorage(set));
				} catch (IOException e) {
					fail(e);
				}
				PerformanceData.save(result, outputMeasurement);				
			}
			
			for (Resource res : parsedFiles) {
				res.unload();
			}
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE
				.getContextDependentURIFragmentMap().clear();
		}
		LOGGER.debug("Finished meausring " + name);
	}
	
	private List<StoragePerformance> measureStorage(ResourceSet resourceSet) throws IOException {
		StoragePerformance javaStorage = new StoragePerformance();
		javaStorage.setId(OutputUtility.SUPPORTED_FILE_EXTENSION_JAVA);
		var result = OutputUtility.transferToOutput(resourceSet, JAVA_OUTPUT.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_JAVA, true);
		fillStorageInformationFromTransfer(javaStorage, JAVA_OUTPUT, result);
		
		result.sourceTargetMapping().forEach((key, value) -> {
			key.getContents().addAll(value.getContents());
		});
		
		StoragePerformance xmiStorage = new StoragePerformance();
		xmiStorage.setId(OutputUtility.SUPPORTED_FILE_EXTENSION_XMI);
		result = OutputUtility.transferToOutput(resourceSet, XMI_OUTPUT.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_XMI, true);
		fillStorageInformationFromTransfer(xmiStorage, XMI_OUTPUT, result);
		
		result.sourceTargetMapping().forEach((key, value) -> {
			key.getContents().addAll(value.getContents());
		});
		
		StoragePerformance jsonStorage = new StoragePerformance();
		jsonStorage.setId(OutputUtility.SUPPORTED_FILE_EXTENSION_JSON);
		fillStorageInformationFromTransfer(jsonStorage, JSON_OUTPUT, OutputUtility.transferToOutput(resourceSet, JSON_OUTPUT.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_JSON, true));
		
		return List.of(javaStorage, xmiStorage, jsonStorage);
	}
	
	private void fillStorageInformationFromTransfer(StoragePerformance storage, Path outputDir, TransferResult outputResult) throws IOException {
		storage.setTakenStorage(PathUtils.sizeOfDirectory(outputDir));
		long codeFiles = 0;
		long codeSize = 0;
		for (var entry : outputResult.sourceTargetMapping().entrySet()) {
			if (entry.getKey().getURI().isFile()) {
				codeFiles++;
				codeSize += FileUtils.sizeOf(new File(entry.getValue().getURI().toFileString()));
			}
		}
		storage.setCodeFiles(codeFiles);
		storage.setTakenStorageByCodeFiles(codeSize);
		storage.setOverallFiles(outputResult.sourceTargetMapping().entrySet().size());
	}
}
