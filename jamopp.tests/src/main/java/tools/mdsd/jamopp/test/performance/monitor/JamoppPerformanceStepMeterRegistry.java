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

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JamoppPerformanceStepMeterRegistry extends StepMeterRegistry {
    private static final Logger LOGGER = LogManager.getLogger(JamoppPerformanceStepMeterRegistry.class);
    private Path outputFile;
    private StepRegistryConfig config;

    public JamoppPerformanceStepMeterRegistry(StepRegistryConfig config, Clock clock, Path output) {
        super(config, clock);
        this.outputFile = output.toAbsolutePath();
        this.config = config;
        this.start(new NamedThreadFactory("jamopp-monitor"));
    }

    @Override
    protected void publish() {
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            writer.append(MonitorConstants.PUBLISHED_TIME_LINE_PREFIX + System.currentTimeMillis());
            writer.append("\n");

            for (var meter : getMeters()) {
                if (meter.getId().getName().equals(MonitorConstants.JVM_METER_USED_MEMORY)) {
                    writeMeter(writer, meter);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not write metrics because: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Writes a meter and its data to the output in a JSON format. The JSON format is directly created instead of using a library
     * to avoid overhead.
     * 
     * @param writer the output to write to.
     * @param meter the meter to write.
     * @throws IOException if an IO error occurs.
     */
    private void writeMeter(BufferedWriter writer, Meter meter) throws IOException {
        writer.append("{\"name\":\"");
        writer.append(meter.getId().getName());

        writer.append("\",\"baseUnit:\":\"");
        writer.append(meter.getId().getBaseUnit());

        writer.append("\",\"tags\":[");
        boolean firstItem = true;
        for (var tag : meter.getId().getTags()) {
            if (!firstItem) {
                writer.append(",");
            }
            writer.append("{\"key\":\"");
            writer.append(tag.getKey());
            writer.append("\",\"value\":\"");
            writer.append(tag.getValue());
            writer.append("\"}");
            firstItem = false;
        }

        writer.append("],\"values\":[");
        firstItem = true;
        for (var measurement : meter.measure()) {
            if (!firstItem) {
                writer.append(",");
            }
            writer.append("{\"value\":" + measurement.getValue());
            writer.append(",\"statistic\":\"");
            writer.append(measurement.getStatistic().name());
            writer.append("\"}");
            firstItem = false;
        }

        writer.append("]}\n");
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
      return TimeUnit.MILLISECONDS;
    }
}
