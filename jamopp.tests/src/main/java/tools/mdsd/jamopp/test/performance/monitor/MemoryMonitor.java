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

package tools.mdsd.jamopp.test.performance.monitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import tools.mdsd.jamopp.test.ChartUtility;

public class MemoryMonitor {
    private boolean initialized = false;
    private Path outputFile;

    public MemoryMonitor(Path outputFile) {
        this.outputFile = outputFile;
    }

    public void initialize() {
        if (this.initialized) {
            return;
        }

        Metrics.globalRegistry.add(
            new JamoppPerformanceStepMeterRegistry(
                new JamoppPerformanceStepRegistryConfig(),
                Clock.SYSTEM,
                this.outputFile
            )
        );
        new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
        this.initialized = true;
    }

    public void stop() {
        Metrics.globalRegistry.close();
    }

    public void readDataAndCreateChart() throws IOException {
        var data = readAndParseMonitorData();
        createMemoryChart(data);
    }

    private List<MeasurementFrame> readAndParseMonitorData() throws IOException {
        List<MeasurementFrame> frames = new ArrayList<>();
        MeasurementFrame currentFrame = new MeasurementFrame();
        Gson gson = new Gson();

        try (var reader = Files.newBufferedReader(outputFile)) {
            String readLine;

            while ((readLine = reader.readLine()) != null) {
                if (readLine.startsWith(MonitorConstants.PUBLISHED_TIME_LINE_PREFIX)) {
                    currentFrame = new MeasurementFrame();
                    frames.add(currentFrame);
                    long time = Long.parseLong(readLine.substring(MonitorConstants.PUBLISHED_TIME_LINE_PREFIX.length()));
                    currentFrame.measurementTime = time;
                } else {
                    var measurement = gson.fromJson(readLine, Measurement.class);
                    currentFrame.measurements.add(measurement);
                }
            }
        }

        return frames;
    }

    private void createMemoryChart(List<MeasurementFrame> data) throws IOException {
        double[] xValues = new double[data.size()];
        double[] yUsedMemory = new double[xValues.length];
        long lastTime = 0;

        for (var index = 0; index < xValues.length; index++) {
            var frame = data.get(index);
            if (index != 0) {
                xValues[index] = (frame.measurementTime - lastTime) + xValues[index - 1];
            } else {
                xValues[0] = 0;
            }

            lastTime = frame.measurementTime;
            
            yUsedMemory[index] = frame
                .measurements
                .stream()
                .filter(measurement -> {
                    for (var tag : measurement.tags) {
                        if (tag.key.equals(MonitorConstants.JVM_METER_TAG_KEY_AREA)
                                && tag.value.equals(MonitorConstants.JVM_METER_TAG_VALUE_HEAP)) {
                            return true;
                        }
                    }
                    return false;
                })
                .flatMap(measurement -> Arrays.stream(measurement.values))
                .mapToDouble(measurement -> measurement.value)
                .sum();
        }

        ChartUtility.buildAndSaveChart(xValues, yUsedMemory,
            "Used Memory", "Time (ms)", "Used Memory (bytes)", outputFile.resolveSibling("memory.pdf"));
    }

    private static class MeasurementFrame {
        private long measurementTime;
        private List<Measurement> measurements = new ArrayList<>();
    }

    private static class Measurement {
        private String name;
        private String baseUnit;
        private Tag[] tags;
        private MeasurementValue[] values;
    }

    private static class Tag {
        private String key;
        private String value;
    }

    private static class MeasurementValue {
        private double value;
        private Statistic statistic;
    }
}
