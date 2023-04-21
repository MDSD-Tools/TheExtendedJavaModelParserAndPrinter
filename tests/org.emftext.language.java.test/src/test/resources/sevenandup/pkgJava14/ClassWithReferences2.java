package pkgJava14;

import java.util.ArrayList;

public class ClassWithReferences2 {
	class Person {
		private String id = "5";
		
		public String getId(Person this) {
			return this.id;
		}
		
		public double getRnd() {
			return Math.random();
		}
	}
	
	public void m() {
		var list = new ArrayList<Person>();
		System.out.println(list.get(0).getId());
		list.parallelStream().limit(2).map(p -> p.getId()).reduce("", (r, s) -> r + s);
	}
}
