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

public class StepwiseEvaluationResult {
	private String name;
	private List<EvaluationStepResult> steps = new ArrayList<>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<EvaluationStepResult> getSteps() {
		return steps;
	}
	
	public void addStep(EvaluationStepResult step) {
		this.steps.add(step);
	}
}
