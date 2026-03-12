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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import tools.mdsd.jamopp.recovery.trivial.TrivialRecovery;
import tools.mdsd.jamopp.resource.JavaResource2Factory;
import tools.mdsd.jamopp.test.ChartUtility;
import tools.mdsd.jamopp.test.OutputUtility;
import tools.mdsd.jamopp.test.OutputUtility.TransferResult;

/**
 * Class to perform performance tests and measurements.
 */
public class PerformanceTestExecutor {
	private static final Logger LOGGER = LogManager.getLogger("jamopp."
			+ PerformanceTestExecutor.class.getSimpleName());
	private final Path inputFolder;
	private final Path outputFolder;
	private final Path javaOutput;
	private final Path xmiOutput;
	private final Path jsonOutput;
	private final Path summaryFile;

	public PerformanceTestExecutor(Path inputFolder, Path outputFolder) {
		this.inputFolder = inputFolder;
		this.outputFolder = outputFolder;
		this.javaOutput = outputFolder.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_JAVA);
		this.xmiOutput = outputFolder.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_XMI);
		this.jsonOutput = outputFolder.resolve(OutputUtility.SUPPORTED_FILE_EXTENSION_JSON);
		this.summaryFile = outputFolder.resolve("summary.md");
	}
	
	public void setupTestEnvironment() throws IOException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		JavaClasspath.get().clear();
		JavaClasspath.get().registerStdLib();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("json", new JsonResourceFactory());
		if (Files.exists(javaOutput)) {
			PathUtils.deleteDirectory(javaOutput);
			PathUtils.deleteDirectory(xmiOutput);
			PathUtils.deleteDirectory(jsonOutput);
		}
		try {
			Files.createDirectories(javaOutput);
			Files.createDirectories(xmiOutput);
			Files.createDirectories(jsonOutput);
		} catch (IOException e1) {
		}
	}
	
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
	
	/**
	 * Currently, this method is protected since it probably does not provide further valuable insights in addition to the other tests.
	 */
	protected void measureTeaStoreWithoutResolvingEverything() {
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
	
	public void measureTeaStoreWithOneLevelResolution() {
		prepareParserOptionsForOneLevelResolution();
		measurePerformance("teastore-one-level-resolution", getNumberOfRepetitions(), false, true);
	}
	
	/**
	 * Currently, this method is protected since it takes several hours to complete.
	 */
	protected void measureTeaStoreWithOneLevelResolutionAndFullResolution() {
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
	
	public void measureTeaStoreSecondVariant() {
		prepareParserOptionsForSecondVariant();
		measurePerformance("teastore-second-variant", getNumberOfRepetitions(), false, true);
	}
	
	/**
	 * Currently, this method is protected since it takes several hours to complete.
	 */
	protected void measureTeaStoreSecondVariantAndFullResolution() {
		prepareParserOptionsForSecondVariant();
		measurePerformance("teastore-second-variant-resolution", 1, true, false);
	}

	public void cleanEverything() throws IOException {
		calculateAndSaveAllStatistics();
	}
	
	private void calculateAndSaveAllStatistics() throws IOException {
		StringBuilder builder = new StringBuilder();

		Files
			.walk(outputFolder, 1)
			.filter(Files::isRegularFile)
			.filter(path -> path.toString().endsWith(OutputUtility.SUPPORTED_FILE_EXTENSION_JSON))
			.forEach(path -> {
				String name = path.getFileName().toString();
				builder.append("# Results for " + name);

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

				try {
					ChartUtility.buildAndSaveChartsForPerformanceData(name, data, outputFolder);
				} catch (IOException e) {
					LOGGER.info("Could not create and store charts for: " + name);
				}
			});

		Files.writeString(summaryFile, builder.toString());
	}

	protected int getNumberOfRepetitions() {
		return 100;
	}
	
	private void measurePerformance(String name, int max, boolean fullResolution, boolean recover) {
		LOGGER.debug("Executing performance measurements for " + name);
		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?src/test/.*?");
		
		Path outputMeasurement = outputFolder.resolve(name + ".json");
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
			ResourceSet set = parser.parseDirectory(inputFolder);
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
			
			for (Resource res : set.getResources()) {
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
		var result = OutputUtility.transferToOutput(resourceSet, javaOutput.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_JAVA, true);
		fillStorageInformationFromTransfer(javaStorage, javaOutput, result);
		
		result.sourceTargetMapping().forEach((key, value) -> {
			key.getContents().addAll(value.getContents());
		});
		
		StoragePerformance xmiStorage = new StoragePerformance();
		xmiStorage.setId(OutputUtility.SUPPORTED_FILE_EXTENSION_XMI);
		result = OutputUtility.transferToOutput(resourceSet, xmiOutput.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_XMI, true);
		fillStorageInformationFromTransfer(xmiStorage, xmiOutput, result);
		
		result.sourceTargetMapping().forEach((key, value) -> {
			key.getContents().addAll(value.getContents());
		});
		
		StoragePerformance jsonStorage = new StoragePerformance();
		jsonStorage.setId(OutputUtility.SUPPORTED_FILE_EXTENSION_JSON);
		fillStorageInformationFromTransfer(jsonStorage, jsonOutput, OutputUtility.transferToOutput(resourceSet, jsonOutput.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_JSON, true));
		
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
