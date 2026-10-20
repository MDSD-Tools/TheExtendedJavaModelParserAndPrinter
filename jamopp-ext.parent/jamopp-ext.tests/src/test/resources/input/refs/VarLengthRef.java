/**
 * Copyright (c) 2020-2023
 * Modelling for Continuous Software Engineering (MCSE) group,
 *     Institute of Information Security and Dependability (KASTEL),
 *     Karlsruhe Institute of Technology (KIT).
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   MCSE, KASTEL, KIT
 *      - Initial implementation
 */

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
