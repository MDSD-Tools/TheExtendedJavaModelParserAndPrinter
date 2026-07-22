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

package staticcalltarget;

import java.util.function.Function;

public class CallTargetsProvider {
	public static String anotherStaticMethod() {
		return "";
	}
	
	public static void staticWithParameter(int i) {
	}
	
	public static void staticWithParameter(double d) {
	}
	
	public static void staticWithParameter(int i, int j) {
	}
	
	public static void staticWithParameter(int i, Runnable r) {
		r.run();
	}
	
	public static <T,R> void staticWithParameter(T i, Function<T, R> s) {
		s.apply(i);
	}
}
