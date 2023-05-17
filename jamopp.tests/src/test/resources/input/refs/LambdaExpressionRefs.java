package refs;

import java.util.Random;
import java.util.stream.Stream;

public class LambdaExpressionRefs {
	public void m() {
		Random rand = new Random();
		int textLength = rand.nextInt(30);
	    String str = Stream.generate(() -> rand.nextInt(30)).limit(textLength)
	        .map(i -> (char) i.intValue())
	        .map(c -> c.charValue())
	        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	    System.out.println(str);
	}
}
