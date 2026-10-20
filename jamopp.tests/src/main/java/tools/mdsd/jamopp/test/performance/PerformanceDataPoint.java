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

/**
 * Represents a performance measurement.
 */
public class PerformanceDataPoint {
	private long parseTime;
	private long resolutionTime;
	private long recoverTime;
	
	public long getParseTime() {
		return parseTime;
	}
	
	public void setParseTime(long parseTime) {
		this.parseTime = parseTime;
	}
	
	public long getResolutionTime() {
		return resolutionTime;
	}
	
	public void setResolutionTime(long resolutionTime) {
		this.resolutionTime = resolutionTime;
	}
	
	public long getRecoverTime() {
		return recoverTime;
	}
	
	public void setRecoverTime(long recoverTime) {
		this.recoverTime = recoverTime;
	}
}
