package staticcalltarget;

import java.util.function.Function;

public class CallTargetsProvider {
	public static String anotherStaticMethod() {
		return "";
	}
	
	public static void staticWithParameter(int i) {
	}
	
	public static void staticWithParameter(double d) {
	}
	
	public static void staticWithParameter(int i, int j) {
	}
	
	public static void staticWithParameter(int i, Runnable r) {
		r.run();
	}
	
	public static <T,R> void staticWithParameter(T i, Function<T, R> s) {
		s.apply(i);
	}
}
