/*******************************************************************************
 * Copyright (c) 2006-2013
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/

package org.emftext.language.java.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import jamopp.resource.JavaResource2;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;

/**
 * A separate test case for the input files that contain Unicode escape
 * sequences.
 */
public class UnicodeTest extends AbstractJaMoPPTests {

	@Test
	public void testUnicodeInput() {
		try {
			assertParsesToClass("ControlZ");
			assertParsesToClass("Unicode");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUnicodeConverterDeactivated() {
		try {
			Map<String, Object> loadOptions = Collections.emptyMap();
			
			assertParsesWithoutErrors("ControlZ", loadOptions);
			assertParsesWithoutErrors("Unicode", loadOptions);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	protected void assertParsesWithoutErrors(String typename, 
			Map<?, ?> loadOptions) throws Exception {
		String filename = File.separator + typename + ".java";
		File inputFolder = new File("./" + getTestInputFolder());
		File file = new File(inputFolder, filename);
		assertTrue(file.exists(), "File " + file + " should exist.");
		URI fileURI = URI.createFileURI(file.getAbsolutePath());
		JavaResource2 resource = (JavaResource2) getResourceSet().createResource(fileURI);
		resource.load(loadOptions);
		
		assertTrue(resource.getErrors().isEmpty());
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return true;
	}

	@Override
	protected String getTestInputFolder() {
		return "src-input" + File.separator + "unicode";
	}

}
