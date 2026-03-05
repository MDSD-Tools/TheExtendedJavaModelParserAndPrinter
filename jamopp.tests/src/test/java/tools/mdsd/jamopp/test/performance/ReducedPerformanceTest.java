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

package tools.mdsd.jamopp.test.performance;

/**
 * This class provides a reduced extent of the performance tests to save time and resources.
 * It acts more as a demonstration for the performance test execution.
 */
public class ReducedPerformanceTest extends PerformanceTest {
    @Override
    protected int getNumberOfRepetitions() {
        return 1;
    }
}
