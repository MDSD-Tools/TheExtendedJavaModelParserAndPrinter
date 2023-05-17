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

public class SimpleClassWithReceiverParameters {
	
	public SimpleClassWithReceiverParameters() { // No ReceiverParameter allowed.
	}
	
	public SimpleClassWithReceiverParameters(int i) { // No ReceiverParameter allowed.
	}
	
	public void someMethod(SimpleClassWithReceiverParameters this) {
		this.someMethod();
	}
	
	public class SomeInnerClass {
		
		public SomeInnerClass(SimpleClassWithReceiverParameters SimpleClassWithReceiverParameters.this) {
		}
		
		public boolean anotherMethod(SimpleClassWithReceiverParameters.SomeInnerClass this) {
			return true;
		}
		
		public boolean anotherMethod2(SomeInnerClass this) {
			return false;
		}
	}
	
	SomeInnerClass get(SimpleClassWithReceiverParameters this) {
		return this.new SomeInnerClass();
	}
	
	void method1(SimpleClassWithReceiverParameters this, int i, String k) {
	}
	
	void method2(SimpleClassWithReceiverParameters this, SomeInnerClass f, byte... b) {
	}
}
