package pkgJava14;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ClassWithReferences3 {
	class Person {
		private String id = "5";
		
		public String getId(Person this) {
			return this.id;
		}
		
		public double getRnd() {
			return Math.random();
		}
	}
	
	public boolean m() {
		var list = new ArrayList<Person>();
		var pred = (Predicate<Person>) p -> p.getRnd() <= 0.1;
		return list.stream().allMatch(pp -> pred.test(pp));
	}
}
