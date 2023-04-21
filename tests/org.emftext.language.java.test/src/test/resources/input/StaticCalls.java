import static staticcalltarget.CallTargetsProvider.anotherStaticMethod;
import static staticcalltarget.CallTargetsProvider.staticWithParameter;

public class StaticCalls {
	public void m() {
		String s = AClass.createA("empty").getName();
		System.out.println(s);
		System.out.println(anotherStaticMethod());
		staticWithParameter(1);
		staticWithParameter(0.0);
		staticWithParameter(0, 0);
		staticWithParameter(0, () -> s.chars());
		staticWithParameter(0, (i) -> i.byteValue());
	}
}
