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

package org.emftext.language.java.test.performance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * Represents multiple performance measurements.
 */
public class PerformanceData {
	private ArrayList<PerformanceDataPoint> points = new ArrayList<>();
	
	public ArrayList<PerformanceDataPoint> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<PerformanceDataPoint> points) {
		this.points = points;
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
