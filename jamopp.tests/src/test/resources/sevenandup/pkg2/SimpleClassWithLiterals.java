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

package pkg2;

public class SimpleClassWithLiterals {

	public void literals() {
		int i = 0b010101010101010101001;
		i = 0B0100101010101;
		i = 0b0_______________101010_01___10_10_0001_01010_01;
		long l = 0B0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0L;
		l = 0b010101001010101010010101010111111110000001001010_1l;
	}
}
