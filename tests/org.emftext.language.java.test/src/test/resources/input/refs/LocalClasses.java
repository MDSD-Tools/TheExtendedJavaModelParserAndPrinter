package refs;

public class LocalClasses {
	{
		class C {
			public void m() {
			}
		}
		C c = new C();
		c.m();
	}
	
	{
		class C {
			public void n() {
			}
		}
		C c = new C();
		c.n();
	}
	
	public void s() {
		class C {
			public void o() {
			}
		}
		C c = new C();
		c.o();
	}
	
	public void t() {
		try {
			class C {
				public void p() {
				}
			}
			C c = new C();
			c.p();
		} catch (Exception e) {
			class C {
				public void q() {
				}
			}
			C c = new C();
			c.q();
		}
		Runnable r = () -> {
			class C {
				public void r() {
				}
			}
			C c = new C();
			c.r();
		};
		class C {
			public void u() {
			}
		}
		C c = new C();
		c.u();
	}
}
