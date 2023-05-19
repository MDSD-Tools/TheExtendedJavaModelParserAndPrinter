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

import static staticcalltarget.CallTargetsProvider.anotherStaticMethod;
import static staticcalltarget.CallTargetsProvider.staticWithParameter;

public class StaticCalls {
	public void m() {
		String s = AClass.createA("empty").getName();
		System.out.println(s);
		System.out.println(anotherStaticMethod());
		staticWithParameter(1);
		staticWithParameter(0.0);
		staticWithParameter(0, 0);
		staticWithParameter(0, () -> s.chars());
		staticWithParameter(0, (i) -> i.byteValue());
	}
}
