/*******************************************************************************
 * Copyright (c) 2026
 * Modelling for Continuous Software Engineering (MCSE) group,
 *     Institute of Information Security and Dependability (KASTEL),
 *     Karlsruhe Institute of Technology (KIT).
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster (MCSE)
 *      - Initial implementation
 ******************************************************************************/

package tools.mdsd.jamopp.test.performance.stepwise;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.google.gson.Gson;

import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import tools.mdsd.jamopp.test.ChartUtility;
import tools.mdsd.jamopp.test.OutputUtility;

public class StepwisePerformanceExecutor {
	private static final Logger LOGGER = LogManager.getLogger("jamopp." + StepwisePerformanceExecutor.class.getSimpleName());
	private static final String GIT_AUTHOR_NAME = "Extended JaMoPP - Stepwise Performance Test";
	private static final String GIT_AUTHOR_MAIL = "noreply@null.localhost";
	private static final String GIT_DIRECTORY_NAME = "models";
	private static final String RESULTS_FILE_NAME = "results.json";
	private static final String CHARTS_DEFAULT_X_AXIS_NAME = "# Iteration";
	private static final String CHARTS_PROXY_COUNT_Y_AXIS_NAME = "# Proxy Objects";
	private static final String CHARTS_PROXY_COUNT_TITLE = "Proxy Objects";
	private static final String CHARTS_PROXY_COUNT_FILE_NAME = "chart-proxies.pdf";
	private static final String CHARTS_DEFAULT_TIME_Y_AXIS_NAME = "Execution Time (%s)";
	private static final String CHARTS_RESOLUTION_TIME_TITLE = "Resolution Time";
	private static final String CHARTS_RESOLUTION_TIME_FILE_NAME = "chart-resolution-time.pdf";
	private static final String CHARTS_MODEL_SAVING_TIME_TITLE = "Model Saving Time";
	private static final String CHARTS_MODEL_SAVING_TIME_FILE_NAME = "chart-model-saving-time.pdf";
	private static final String CHARTS_MODEL_COUNT_TITLE = "Models";
	private static final String CHARTS_MODEL_COUNT_Y_AXIS_NAME = "# Models";
	private static final String CHARTS_MODEL_COUNT_FILE_NAME = "chart-model-count.pdf";
	private static final String CHARTS_MODEL_SIZE_TITLE = "Model Size";
	private static final String CHARTS_MODEL_SIZE_Y_AXIS_NAME = "Model Size";
	private static final String CHARTS_MODEL_SIZE_FILE_NAME = "chart-model-size.pdf";
	private Git git;
	private RevCommit lastCommit;
	
	public void measurePerformance(String name, Path srcDirectory, Path outputDirectory) throws IOException, GitAPIException {
		if (Files.notExists(srcDirectory) || !Files.isDirectory(srcDirectory)) {
			throw new IllegalStateException("Given input directory '" + srcDirectory.toString() + "' does not exist or is not a directory.");
		}

		if (Files.exists(outputDirectory)) {
			if (!Files.isDirectory(outputDirectory)) {
				throw new IllegalStateException("The given output directory '" + outputDirectory.toString() + "' exists and is not a directory.");
			}
		} else {
			Files.createDirectories(outputDirectory);
		}

		LOGGER.info("Executing performance measurements for: " + name);
        StepwiseEvaluationResult result = new StepwiseEvaluationResult();
		result.setName(name);

		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?src/test/.*?");

        long millis = System.currentTimeMillis();
        ResourceSet set = parser.parseDirectory(srcDirectory);
        result.setParsingTime(System.currentTimeMillis() - millis);

        EvaluationStepResult stepResult = new EvaluationStepResult();
        stepResult.setStep(0);
        stepResult.setTimeResolution(0);
        stepResult.setTotalProxies(IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.getContextDependentURIFragmentMap().size());
        result.addStep(stepResult);

		var resultFile = outputDirectory.resolve(RESULTS_FILE_NAME);
		var gitDir = outputDirectory.resolve(GIT_DIRECTORY_NAME);
		this.git = Git.init().setDirectory(gitDir.toFile()).call();

		var outputResult = this.storeModelsAndCalculateSizeChanges(set, gitDir);
		stepResult.setTimeModelSaving(outputResult.getRight());
		outputResult.getLeft().forEach(stepResult::addChangedFiles);
		this.saveResults(result, resultFile);
			
		List<Resource> oldResources = List.of();
		int iteration = 1;
		do {
			oldResources = new ArrayList<>(set.getResources());

			for (Resource resource : oldResources) {
				if (EcoreUtil.ProxyCrossReferencer.find(resource).size() == 0) {
					continue;
				}

				System.out.println(resource.getURI().toString());

				millis = System.currentTimeMillis();
				EcoreUtil.resolveAll(resource);
				millis = System.currentTimeMillis() - millis;

				stepResult = new EvaluationStepResult();
				stepResult.setStep(iteration);
				stepResult.setTimeResolution(millis);
				stepResult.setTotalProxies(IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.getContextDependentURIFragmentMap().size());

				outputResult = this.storeModelsAndCalculateSizeChanges(set, gitDir);
				stepResult.setTimeModelSaving(outputResult.getRight());
				outputResult.getLeft().forEach(stepResult::addChangedFiles);

				result.addStep(stepResult);
				this.saveResults(result, resultFile);
				iteration++;
			}
		} while (oldResources.size() != set.getResources().size());
		
		this.lastCommit = null;
		
		this.git.getRepository().close();
		this.git.close();
		this.git = null;
		for (Resource res : set.getResources()) {
			res.unload();
		}
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.getContextDependentURIFragmentMap().clear();

		buildAndSaveCharts(result, outputDirectory);
		LOGGER.info("Finished measuring: " + name);
	}
	
	private Pair<List<EvaluationStepFileChange>, Long> storeModelsAndCalculateSizeChanges(ResourceSet resourceSet, Path output) throws GitAPIException, IOException {
		// Store all model, and reset the source models.
		long storingTime = System.currentTimeMillis();
		var result = OutputUtility.transferToOutput(resourceSet, output.toString(), OutputUtility.SUPPORTED_FILE_EXTENSION_XMI, true);
		storingTime = System.currentTimeMillis() - storingTime;
		result.sourceTargetMapping().forEach((key, value) -> {
			key.getContents().addAll(value.getContents());
		});

		// Add all generated files to the Git repository to save them.
		this.git.add().addFilepattern(".").call();
		var recentCommit = this.git.commit().setAuthor(GIT_AUTHOR_NAME, GIT_AUTHOR_MAIL).setMessage("").call();

		// Calculate the size of every changed file.
		List<EvaluationStepFileChange> fileChanges = new ArrayList<>();
		AtomicLong totalChangeSize = new AtomicLong();

		// Get all changed files.
		var gitObjReader = this.git.getRepository().newObjectReader();
		CanonicalTreeParser oldTree = null;
		if (this.lastCommit != null) {
			oldTree = new CanonicalTreeParser(null, gitObjReader, this.lastCommit);
		}
		CanonicalTreeParser newTree = new CanonicalTreeParser(null, gitObjReader, recentCommit);
		var diffEntries = this.git.diff().setShowNameOnly(true).setOldTree(oldTree).setNewTree(newTree).call();

		// Calculate the size for every changed file.
		diffEntries.forEach(entry -> {
			var affectedFile = output.resolve(entry.getNewPath());
			var size = FileUtils.sizeOf(affectedFile.toFile());
			totalChangeSize.addAndGet(size);

			var stepFileChange = new EvaluationStepFileChange();
			stepFileChange.setNewSize(size);
			stepFileChange.setPath(entry.getNewPath());
			fileChanges.add(stepFileChange);
		});

		this.lastCommit = recentCommit;
		return Pair.of(fileChanges, storingTime);
	}

	private void saveResults(StepwiseEvaluationResult results, Path file) throws IOException {
		Gson gson = new Gson();
		Files.writeString(file, gson.toJson(results));
	}

	private void buildAndSaveCharts(StepwiseEvaluationResult result, Path outputDir) throws IOException {
		// Planned graphs: iteration number (x axis) vs. following numbers on y axis
		// memory consumption
		var currentData = result.getSteps().stream().mapToDouble(EvaluationStepResult::getTotalProxies).toArray();
		ChartUtility.buildAndSaveChartWithDiff(currentData, CHARTS_PROXY_COUNT_TITLE, CHARTS_DEFAULT_X_AXIS_NAME,
			CHARTS_PROXY_COUNT_Y_AXIS_NAME, outputDir.resolve(CHARTS_PROXY_COUNT_FILE_NAME));

		currentData = result.getSteps().stream().mapToDouble(EvaluationStepResult::getTimeResolution).toArray();
		ChartUtility.buildAndSaveChart(currentData, CHARTS_RESOLUTION_TIME_TITLE, CHARTS_DEFAULT_X_AXIS_NAME,
			String.format(CHARTS_DEFAULT_TIME_Y_AXIS_NAME, ChartUtility.adjustTimeMillisecondsUnit(currentData)),
			outputDir.resolve(CHARTS_RESOLUTION_TIME_FILE_NAME));

		currentData = result.getSteps().stream().mapToDouble(EvaluationStepResult::getTimeModelSaving).toArray();
		ChartUtility.buildAndSaveChart(currentData, CHARTS_MODEL_SAVING_TIME_TITLE, CHARTS_DEFAULT_X_AXIS_NAME,
			String.format(CHARTS_DEFAULT_TIME_Y_AXIS_NAME, ChartUtility.adjustTimeMillisecondsUnit(currentData)),
			outputDir.resolve(CHARTS_MODEL_SAVING_TIME_FILE_NAME));

		double[] sizes = new double[currentData.length];
		Map<String, Long> filesToSizes = new HashMap<>();

		for (var index = 0; index < currentData.length; index++) {
			var step = result.getSteps().get(index);
			step.getChangedFiles().forEach(file -> {
				filesToSizes.put(file.getPath(), file.getNewSize());
			});

			currentData[index] = filesToSizes.size();
			sizes[index] = filesToSizes.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
		}

		ChartUtility.buildAndSaveChartWithDiff(currentData, CHARTS_MODEL_COUNT_TITLE, CHARTS_DEFAULT_X_AXIS_NAME,
			CHARTS_MODEL_COUNT_Y_AXIS_NAME, outputDir.resolve(CHARTS_MODEL_COUNT_FILE_NAME));
		ChartUtility.buildAndSaveChartWithDiff(sizes, CHARTS_MODEL_SIZE_TITLE, CHARTS_DEFAULT_X_AXIS_NAME,
			CHARTS_MODEL_SIZE_Y_AXIS_NAME, outputDir.resolve(CHARTS_MODEL_SIZE_FILE_NAME));
	}
}
