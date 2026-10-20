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
