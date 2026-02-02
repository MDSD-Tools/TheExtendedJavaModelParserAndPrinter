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

public class EvaluationStepFileChange {
	private String path;
	private int newSize;

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public int getNewSize() {
		return newSize;
	}
	
	public void setNewSize(int newSize) {
		this.newSize = newSize;
	}
}
