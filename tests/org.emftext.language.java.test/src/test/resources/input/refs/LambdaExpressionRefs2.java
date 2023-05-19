package refs;

import java.util.HashMap;
import java.util.stream.Collectors;

public class LambdaExpressionRefs2 {
	public void m(HashMap<Long, String> images) {
		images.entrySet().parallelStream().collect(
				Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}
}
