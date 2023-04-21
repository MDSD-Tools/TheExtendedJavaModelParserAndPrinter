/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package org.emftext.language.java.test;

import java.nio.file.Paths;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jamopp.options.ParserOptions;
import jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;

/**
 * Class for testing the parseDirectory method of the JaMoPPJDTParser.
 */
public class JaMoPPJDTParserDirectoryTest extends AbstractJaMoPPTests {
	private JaMoPPJDTSingleFileParser parser;
	
	@BeforeAll
	public static void firstSetup() {
		AbstractJaMoPPTests.initLogging();
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.TRUE);
	}
	
	@AfterAll
	public static void lastTearDown() {
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
	}
	
	@BeforeEach
	public void setUp() {
		super.initResourceFactory();
		parser = new JaMoPPJDTSingleFileParser();
	}
	
	@Test
	public void testSrcInputDirectory() throws Exception {
		ResourceSet set = parser.parseDirectory(Paths.get("src-input"));
		this.assertModelValid(set);
		this.testReprint(set);
	}
	
	@Test
	@Disabled("JDT puts break statements into Blocks on the right side of a SwitchRule.")
	public void testSrcSevenAndUp() throws Exception {
		ResourceSet set = parser.parseDirectory(Paths.get("src-sevenandup"));
		this.assertModelValid(set);
		this.testReprint(set);
	}

	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return false;
	}

	@Override
	protected String getTestInputFolder() {
		return "";
	}
}
