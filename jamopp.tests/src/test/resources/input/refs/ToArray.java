package refs;

import java.util.HashSet;
import java.util.Set;

public class ToArray {
	public void m() {
		Set<String> set = new HashSet<>();
		String[] setArray = set.toArray(new String[0]);
	}
}
