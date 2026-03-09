/*******************************************************************************
 * Copyright (c) 2023, Martin Armbruster
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

public class StoragePerformance {
	private String id;
	private long takenStorageByCodeFiles;
	private long takenStorage;
	private long overallFiles;
	private long codeFiles;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public long getTakenStorageByCodeFiles() {
		return takenStorageByCodeFiles;
	}
	
	public void setTakenStorageByCodeFiles(long takenStorageByCodeFiles) {
		this.takenStorageByCodeFiles = takenStorageByCodeFiles;
	}
	
	public long getTakenStorage() {
		return takenStorage;
	}
	
	public void setTakenStorage(long takenStorage) {
		this.takenStorage = takenStorage;
	}
	
	public long getOverallFiles() {
		return overallFiles;
	}
	
	public void setOverallFiles(long overallFiles) {
		this.overallFiles = overallFiles;
	}
	
	public long getCodeFiles() {
		return codeFiles;
	}
	
	public void setCodeFiles(long codeFiles) {
		this.codeFiles = codeFiles;
	}
}
