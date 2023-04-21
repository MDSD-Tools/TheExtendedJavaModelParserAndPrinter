package mbtp;

import java.util.TreeSet;

public class C2<F extends C3<G>, G> extends C1<TreeSet<F>, F> {
	public void m() {
		getCollection().pollFirst().getM();
	}
}
