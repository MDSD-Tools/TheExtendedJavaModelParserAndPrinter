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
