/*******************************************************************************
 * Copyright (c) 2023-2026
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

import java.util.ArrayList;
import java.util.List;

public class EvaluationStepResult {
	private int step;
	private long timeResolution;
	private long timeModelSaving;
	private long totalProxies;
	private List<EvaluationStepFileChange> changedFiles = new ArrayList<>();

	public int getStep() {
		return step;
	}
	
	public void setStep(int step) {
		this.step = step;
	}
	
	public long getTimeResolution() {
		return timeResolution;
	}
	
	public void setTimeResolution(long totalDuration) {
		this.timeResolution = totalDuration;
	}

	public long getTimeModelSaving() {
		return timeModelSaving;
	}

	public void setTimeModelSaving(long timeModelSaving) {
		this.timeModelSaving = timeModelSaving;
	}
	
	public long getTotalProxies() {
		return totalProxies;
	}
	
	public void setTotalProxies(long totalProxies) {
		this.totalProxies = totalProxies;
	}
	
	public List<EvaluationStepFileChange> getChangedFiles() {
		return changedFiles;
	}
	
	public void addChangedFiles(EvaluationStepFileChange changedFile) {
		this.changedFiles.add(changedFile);
	}
}
