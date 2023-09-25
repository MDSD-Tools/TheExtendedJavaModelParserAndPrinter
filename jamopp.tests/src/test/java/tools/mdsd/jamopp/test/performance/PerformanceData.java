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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * Represents multiple performance measurements.
 */
public class PerformanceData {
	private ArrayList<PerformanceDataPoint> points = new ArrayList<>();
	private ArrayList<StoragePerformance> storage = new ArrayList<>();
	
	public List<PerformanceDataPoint> getPoints() {
		return (List<PerformanceDataPoint>) points.clone();
	}
	
	public void addPoint(PerformanceDataPoint newPoint) {
		this.points.add(newPoint);
	}

	public void setPoints(List<PerformanceDataPoint> points) {
		this.points.clear();
		this.points.addAll(points);
	}
	
	public List<StoragePerformance> getStorage() {
		return (List<StoragePerformance>) storage.clone();
	}
	
	public void setStorage(List<StoragePerformance> storage) {
		this.storage.clear();
		this.storage.addAll(storage);
	}
	
	public double getAverageParseTime() {
		return (double) points.stream().mapToLong(p -> p.getParseTime()).sum() / points.size();
	}
	
	public double getAverageResolutionTime() {
		return (double) points.stream().mapToLong(p -> p.getResolutionTime()).sum() / points.size();
	}
	
	public static PerformanceData load(Path file) {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			Gson gson = new Gson();
			PerformanceData result = gson.fromJson(reader, PerformanceData.class);
			return result;
		} catch (IOException e) {
			return new PerformanceData();
		}
	}
	
	public static void save(PerformanceData data, Path file) {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			Gson gson = new Gson();
			gson.toJson(data, writer);
		} catch (IOException e) {
		}
	}
}
