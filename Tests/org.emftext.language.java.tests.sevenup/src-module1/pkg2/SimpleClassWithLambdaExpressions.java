package pkg2;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SimpleClassWithLambdaExpressions {

	public void lambdas() {
		Predicate p = o -> {
			return true;
		};
		p = (o) -> {
			return false;
		};
		p = (o) -> false;
		p = (Object o) -> true;
		BinaryOperator<Integer> bo = (Integer i, Integer j) -> i+j;
		BinaryOperator equ = (o, m) -> {
			return o.equals(m);
		};
		Runnable run = () -> {};
		Consumer<Void> cons = (Void l) -> {};
		I i1 = (k, l, m, n, s, t, u, lo, d, f, expr) -> {
			this.lambdas();
		};
		I i2 =
			(int k, int l, int m, int n, String s, String t, String[] u, long lo, double d, float f, SimpleClassWithLambdaExpressions expr) -> this.lambdas();
	}
	
	@FunctionalInterface
	interface I {
		void r(int k, int l, int m, int n, String s, String t, String[] u, long lo, double d, float f, SimpleClassWithLambdaExpressions expr);
	}
}
