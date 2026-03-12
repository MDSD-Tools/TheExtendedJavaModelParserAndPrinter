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

package tools.mdsd.jamopp.test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

import tools.mdsd.jamopp.test.performance.PerformanceData;

public final class ChartUtility {
    public final static String DEFAULT_X_AXIS_TITLE = "# Measurement";
    private final static String UNIT_MILLISECONDS = "ms";
    private final static String UNIT_SECONDS = "s";
    private final static String UNIT_MINUTES = "min";
    private final static String UNIT_HOURS = "h";
    private final static double MILLISECONDS_OF_ONE_SECOND = 1000;
    private final static double SECONDS_OF_ONE_MINUTE = 60;
    private final static double MINUTES_OF_ONE_HOUR = 60;
    private final static double MILLISECONDS_OF_ONE_MINUTE = MILLISECONDS_OF_ONE_SECOND * SECONDS_OF_ONE_MINUTE;
    private final static double MILLISECONDS_OF_ONE_HOUR = MILLISECONDS_OF_ONE_MINUTE * MINUTES_OF_ONE_HOUR;

    private ChartUtility() {}

    public static void buildAndSaveChartsForPerformanceData(String dataName, PerformanceData data, Path outputDirectory) throws IOException {
        double[] parsingTimes = new double[data.getPoints().size()];
        double[] resolutionTimes = new double[parsingTimes.length];
        double[] recoveryTimes = new double[resolutionTimes.length];

        int index = 0;
        for (var dataPoint : data.getPoints()) {
            parsingTimes[index] = dataPoint.getParseTime();
            resolutionTimes[index] = dataPoint.getResolutionTime();
            recoveryTimes[index] = dataPoint.getRecoverTime();
            index++;
        }

        buildAndSaveChart(parsingTimes, dataName + " - Parsing",
            DEFAULT_X_AXIS_TITLE, "Parsing Time (" + adjustDataUnit(parsingTimes) + ")", outputDirectory.resolve(dataName + "-parsing.pdf"));
        buildAndSaveChart(resolutionTimes, dataName + " - Resolution",
            DEFAULT_X_AXIS_TITLE, "Resolution Time (" + adjustDataUnit(resolutionTimes) + ")", outputDirectory.resolve(dataName + "-resolution.pdf"));
        buildAndSaveChart(recoveryTimes, dataName + " - Recovery",
            DEFAULT_X_AXIS_TITLE, "Recovery Time (" + adjustDataUnit(recoveryTimes) + ")", outputDirectory.resolve(dataName + "-recovery.pdf"));
    }

    public static void buildAndSaveChart(double[] data, String title, String xAxisTitle, String yAxisTitle, Path chartFile) throws IOException {
        var chart = new XYChartBuilder().title(title).xAxisTitle(xAxisTitle).yAxisTitle(yAxisTitle).build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line).setLegendVisible(false);
        chart.addSeries(yAxisTitle, data);
        VectorGraphicsEncoder.saveVectorGraphic(chart, chartFile.toAbsolutePath().toString(), VectorGraphicsFormat.PDF);
    }

    private static String adjustDataUnit(double[] data) {
        var min = Arrays.stream(data).min().getAsDouble();
        var minMaxDiff = Arrays.stream(data).max().getAsDouble() - min;
        double unitAdjustingFactor = 0.0;
        String unitName = UNIT_MILLISECONDS;

        if (min > MILLISECONDS_OF_ONE_SECOND && minMaxDiff < MILLISECONDS_OF_ONE_MINUTE / 2) {
            unitAdjustingFactor = MILLISECONDS_OF_ONE_SECOND;
            unitName = UNIT_SECONDS;
        } else if (min > MILLISECONDS_OF_ONE_MINUTE && minMaxDiff < MILLISECONDS_OF_ONE_HOUR / 2) {
            unitAdjustingFactor = MILLISECONDS_OF_ONE_MINUTE;
            unitName = UNIT_MINUTES;
        } else if (minMaxDiff >= MILLISECONDS_OF_ONE_HOUR / 2) {
            unitAdjustingFactor = MILLISECONDS_OF_ONE_HOUR;
            unitName = UNIT_HOURS;
        } else {
            return unitName;
        }

        for (int index = 0; index < data.length; index++) {
            data[index] = data[index] / unitAdjustingFactor;
        }
        return unitName;
    }
}
