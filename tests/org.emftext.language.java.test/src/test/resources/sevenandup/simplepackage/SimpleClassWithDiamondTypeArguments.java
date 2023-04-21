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

public class SimpleClassWithDiamondTypeArguments {

	public void someMethod() {
		ArrayList<String> list = new ArrayList<>();
		new ArrayList<>();
		InnerClass<Integer, Byte, String> f1 = new InnerClass<>();
		InnerClass<Byte, String, Integer> f2 = new InnerClass<Byte, String, Integer>();
		InnerClass<String, Integer, Byte> f3 = new InnerClass<>();
		InnerClass<?, ?, ? extends Byte> f4 = new InnerClass<>();
	}
	
	class InnerClass<R, S, T> {
	}
}
