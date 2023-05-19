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

import java.util.concurrent.Callable;

public interface SimpleInterfaceWithDefaultMethods<R> extends Runnable, Callable<R> {

	default boolean isActive() {
		return false;
	}
	
	public abstract String getName();
	
	@Override
	default void run() {
	}
	
	static void doMore(int i) {
		i++;
	}
	
	class Implementation implements SimpleInterfaceWithDefaultMethods<Integer> {
		@Override
		public String getName() {
			return "Test";
		}
		
		@Override
		public void run() {
		}
		
		@Override
		public Integer call() {
			return -1;
		}
	}
}
