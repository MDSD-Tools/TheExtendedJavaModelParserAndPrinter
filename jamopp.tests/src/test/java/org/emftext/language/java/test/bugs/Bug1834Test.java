/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.java.test.bugs;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class Bug1834Test extends AbstractBugTestCase {

	private static final String NL = System.getProperty("line.separator");

	@Disabled("This test is deactivated because the respective feature is not implemented yet")
	@Test
	public void testDefaultMethodPrinting() throws IOException {
		
		ResourceSet rs = createResourceSet();
		Resource resource = rs.createResource(URI.createURI("Test.java"));
		String sourceCode = "public class Test {public void m(){}}";
		InputStream inputStream = new ByteArrayInputStream(sourceCode.getBytes());
		resource.load(inputStream, getLoadOptions());
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		resource.save(outputStream, getLoadOptions());
		String printResult = outputStream.toString();
		System.out.println("printResult =>" + printResult + "<=");
		
		String expected = 
			"public class Test {" + NL
			+ "\tpublic void m() {" + NL
			+ "\t}" + NL
			+ "}";
		assertTrue(printResult.contains(expected), "Print result must contain expected code.");
	}
}
