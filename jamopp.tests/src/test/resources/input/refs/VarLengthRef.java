package refs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class VarLengthRef {
	public void m() {
		List<String> list = Arrays.asList("A", "B", "C");
		try {
			Files.write(Paths.get(""), new byte[] {}, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE);
		} catch (IOException e) {
		}
	}
}
