/**
 * Copyright (c) 2020-2023
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
 *   MCSE, KASTEL, KIT
 *      - Initial implementation
 */

package refs;

import java.util.Comparator;

public class FieldLambdaExpressions {
	private Comparator<String> l = Comparator.comparing(i -> i.toLowerCase());
	
	public void m(boolean b) {
		Comparator<Integer> iComp = Comparator.comparing(i -> i.doubleValue());
		Comparator<Double> dComp = b ? Comparator.comparing(d -> d.intValue())
				: Comparator.comparing(d -> d.intValue());
	}
}
