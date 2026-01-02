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
import java.util.List;
import java.util.Map;

public class NestedLambdaExpressions {
	public void m() {
		Map<String, List<String>> map = new HashMap<>();
		map.forEach((s, t) -> t.removeIf(u -> u.length() > 0));
	}
}
