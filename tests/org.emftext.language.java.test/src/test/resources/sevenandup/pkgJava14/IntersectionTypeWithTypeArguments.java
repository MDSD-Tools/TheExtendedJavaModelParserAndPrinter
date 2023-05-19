/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
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

package pkgJava14;

public class IntersectionTypeWithTypeArguments {
	void someMethod() {
		var kl = (D1<R> & D2<R>) (new R());
		kl.r();
	}
	
	class R implements D2<R> {
		@Override
		public void r() {
		}

		@Override
		public void t() {
		}
	}
	
	interface D1<T> {
		void r();
	}
	
	interface D2<S extends D1<S>> extends D1<S> {
		void t();
	}
}
