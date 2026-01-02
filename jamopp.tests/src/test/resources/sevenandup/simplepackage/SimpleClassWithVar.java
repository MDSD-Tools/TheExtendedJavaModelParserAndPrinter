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

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleClassWithVar {

	void someMethod() {
		var l = 3;
		var list = new ArrayList<String>();
		list.add("Test");
		C1 c = new C2();
		var k = (A1 & A2 & A3) c;
		
		for (var index = 0; index < 1000; ) {
			System.out.println(index);
			index++;
		}
		
		Function<String, String> f = (var s) -> {
				return s;
			};
		BiConsumer<Integer, Long> cons = (var j, var i) -> i = i+j;
	}
	
	interface A1 {
		void m1();
	}
	
	interface A2 {
		void m2();
	}
	
	interface A3 {
		void m3();
	}
	
	class C1 implements A1, A2 {
		@Override
		public void m1() {
		}
		
		@Override
		public void m2() {
		}
	}
	
	class C2 extends C1 implements A3 {
		@Override
		public void m3() {
		}
	}
}
