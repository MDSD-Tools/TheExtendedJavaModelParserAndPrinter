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

import java.time.Duration;

import io.micrometer.core.instrument.step.StepRegistryConfig;

/**
 * Configuration for the JaMoPP step meter registry.
 */
public class JamoppPerformanceStepRegistryConfig implements StepRegistryConfig {
    @Override
    public String get(String arg0) {
        return null;
    }

    @Override
    public Duration step() {
        return Duration.ofSeconds(15);
    }

    @Override
    public String prefix() {
        return "jamopp-performance-step-config";
    }
}