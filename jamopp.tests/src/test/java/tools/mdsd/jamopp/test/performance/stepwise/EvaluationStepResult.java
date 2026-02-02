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

import java.util.List;

public class EvaluationStepResult {
	private int step;
	private int totalDuration;
	private int totalFiles;
	private int totalSize;
	private int totalProxies;
	private List<EvaluationStepFileChange> changedFiles;

	public int getStep() {
		return step;
	}
	
	public void setStep(int step) {
		this.step = step;
	}
	
	public int getTotalDuration() {
		return totalDuration;
	}
	
	public void setTotalDuration(int totalDuration) {
		this.totalDuration = totalDuration;
	}
	
	public int getTotalFiles() {
		return totalFiles;
	}
	
	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}
	
	public int getTotalSize() {
		return totalSize;
	}
	
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
	
	public int getTotalProxies() {
		return totalProxies;
	}
	
	public void setTotalProxies(int totalProxies) {
		this.totalProxies = totalProxies;
	}
	
	public List<EvaluationStepFileChange> getChangedFiles() {
		return changedFiles;
	}
	
	public void addChangedFiles(EvaluationStepFileChange changedFile) {
		this.changedFiles.add(changedFile);
	}
}
