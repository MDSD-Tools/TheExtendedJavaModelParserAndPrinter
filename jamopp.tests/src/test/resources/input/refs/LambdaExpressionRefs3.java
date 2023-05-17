package refs;

import java.util.HashMap;
import java.util.Map;

public class LambdaExpressionRefs3 {
	public void m(HashMap<String, Map<Long, Integer>> map) {
		Map.Entry<String, Map<Long, Integer>> img = map.entrySet().stream()
		        .filter(entry -> entry.getValue().containsKey(6L)).findFirst().orElse(null);
	}
}
