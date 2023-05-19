package refs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedLambdaExpressions {
	public void m() {
		Map<String, List<String>> map = new HashMap<>();
		map.forEach((s, t) -> t.removeIf(u -> u.length() > 0));
	}
}
