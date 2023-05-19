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

import java.util.HashMap;
import java.util.Map;

public class LambdaExpressionRefs3 {
	public void m(HashMap<String, Map<Long, Integer>> map) {
		Map.Entry<String, Map<Long, Integer>> img = map.entrySet().stream()
		        .filter(entry -> entry.getValue().containsKey(6L)).findFirst().orElse(null);
	}
}
