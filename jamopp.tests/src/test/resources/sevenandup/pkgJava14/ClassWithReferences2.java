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

package pkgJava14;

import java.util.ArrayList;

public class ClassWithReferences2 {
	class Person {
		private String id = "5";
		
		public String getId(Person this) {
			return this.id;
		}
		
		public double getRnd() {
			return Math.random();
		}
	}
	
	public void m() {
		var list = new ArrayList<Person>();
		System.out.println(list.get(0).getId());
		list.parallelStream().limit(2).map(p -> p.getId()).reduce("", (r, s) -> r + s);
	}
}
