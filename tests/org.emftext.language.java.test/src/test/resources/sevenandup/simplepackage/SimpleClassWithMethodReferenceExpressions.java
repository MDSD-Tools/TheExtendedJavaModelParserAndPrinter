/*******************************************************************************
 * Copyright (c) 2019-2020, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package simplepackage;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class SimpleClassWithMethodReferenceExpressions {

	public static void someMethod() {
		Function<String, String> concat = "s"::concat;
		Supplier s = SimpleClassWithMethodReferenceExpressions::new;
		s = Inner<String, Integer>::new;
		s = Inner<SimpleClassWithMethodReferenceExpressions, String>::<Integer, Long>new;
		s = ((SimpleClassWithMethodReferenceExpressions.Inner) s.get()).r()::getClass;
		Function<String, Long> decode = Long::decode;
		IntFunction f = Long[]::new;
		f = IntFunction[]::new;
	}
	
	static class Inner<R, T> {
		
		public <U, V> Inner() {
		}
		
		Object r() {
			return this;
		}
	}
}
