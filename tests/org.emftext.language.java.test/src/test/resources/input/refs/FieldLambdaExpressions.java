package refs;

import java.util.Comparator;

public class FieldLambdaExpressions {
	private Comparator<String> l = Comparator.comparing(i -> i.toLowerCase());
	
	public void m(boolean b) {
		Comparator<Integer> iComp = Comparator.comparing(i -> i.doubleValue());
		Comparator<Double> dComp = b ? Comparator.comparing(d -> d.intValue())
				: Comparator.comparing(d -> d.intValue());
	}
}
