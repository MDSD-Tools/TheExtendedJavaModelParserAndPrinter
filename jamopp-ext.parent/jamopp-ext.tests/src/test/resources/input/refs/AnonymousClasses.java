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

package refs;

public class AnonymousClasses {
	public static class A {
		public void m() {
			class D {
				public void e() {
				}
			}
			D d = new D();
			d.e();
		}
	}
	
	static {
		class C {
			public void m() {
			}
		}
		C c = new C();
		A a = new A() {
			private void n() {
			}
			
			@Override
			public void m() {
				n();
				c.m();
			}
		};
	}
	
	public void s() {
		A b = new A() {
			private void o() {
			}
			
			@Override
			public void m() {
				o();
			}
		};
		b.m();
	}
	
	public void t() {
		A c = new A() {
			class K {
				public void p() {
					A d = new A() {
						@Override
						public void m() {
							m();
						}
					};
					d.m();
				}
			}
			
			@Override
			public void m() {
				K k = new K();
				k.p();
			}
		};
		c.m();
	}
}
