public class AClass {
	public static class A {
		private String name;
		
		A(String s) {
			name = s;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static A createA(String s) {
		return new A(s);
	}
}
