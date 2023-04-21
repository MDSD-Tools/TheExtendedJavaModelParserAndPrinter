package refs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LambdaExpressionRefs4 {
	public void m(Map<Character, List<Long>> list) {
		list.entrySet().stream().flatMap(e -> e.getValue().stream())
		        .collect(Collectors.toList());
	}
}
