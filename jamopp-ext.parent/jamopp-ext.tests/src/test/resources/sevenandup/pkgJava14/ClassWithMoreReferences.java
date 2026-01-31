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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassWithMoreReferences {
	void get() {}
	
	Runnable getRunnable() {
		return this::get;
	}
	
	Runnable getRunnableAsLambda() {
		return () -> {};
	}
	
	boolean test(Object a) {
		return false;
	}
	
	Predicate<Boolean> somePredicate() {
		return b -> b;
	}
	
	void m() {
		var k = getRunnable();
		k.run();
		Predicate<Object> p = k==null ? (o) -> true : (1-2 == 0 ? (o) -> o.equals(k) : this::test);
		p.test(k);
		List<Integer> list = new ArrayList<>();
		list = list.parallelStream().map(i -> (Object) i).filter(p).map(o -> (int) o).collect(Collectors.toList());
		var l = switch(1) {
			case 2 -> 1;
			default -> 0;
		};
		var m = switch(list.get(0)) {
			case 94309: {
				yield 2;
			}
			default: yield 0;
		};
		var u = l+m;
		var a = switch (50) {
			case 42 -> "";
			case 76 -> {
				yield "76";
			}
			default -> "1";
		};
		a.charAt(5);
	}
}
