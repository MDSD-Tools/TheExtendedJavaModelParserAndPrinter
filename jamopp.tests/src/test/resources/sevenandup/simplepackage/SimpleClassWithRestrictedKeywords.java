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

public class SimpleClassWithRestrictedKeywords {

	private int module;
	private long transitive;
	
	public void opens(String exports) {
		char requires;
		String with;
		float uses;
		double provides;
	}
	
	public interface open {
		public static void to(byte to) {
		}
	}
}
