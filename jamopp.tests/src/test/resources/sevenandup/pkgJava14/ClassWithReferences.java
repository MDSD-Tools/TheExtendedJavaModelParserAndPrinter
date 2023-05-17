package pkgJava14;

import java.util.ArrayList;

public class ClassWithReferences {
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
		ArrayList<Person> list = new ArrayList<Person>();
		list.add(new Person());
		double a = list.get(0).getRnd();
		System.out.println(list.stream().filter(a >= 0.75 ? p -> false : (((p -> p.getId().equals("5")))))
			.anyMatch(p -> p.getRnd() < 0.5D));
	}
}
