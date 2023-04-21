/*******************************************************************************
 * Copyright 2021 Marvin Meller
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marvin Meller
 *      - initial implementation
 *  Yves Kirschner
 *      - parameterize implementation
 *  Manar Mazkatli
 *      - fix the test and let it runs as expected
 *  Martin Armbruster
 *      - optimization
 ******************************************************************************/
package org.emftext.language.java.test.standalone;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.test.xmi.JavaXMISerializationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import jamopp.options.ParserOptions;
import jamopp.options.ParserOptionsValueContainer;
import jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;

/**
 * Parameterized test for standalone use of JDT-based conversion of Java source
 * code to EMF-based models. Apart from JUint, no dependencies to other tools
 * were newly introduced. For this purpose, the existing code was taken almost
 * identically and copied into this test. This standalone implementation worked
 * as expected in the first version we developed together. A comparable
 * standalone call is also included in the original JaMoPP from DevBoost.
 *
 * @author Marvin Meller
 * @author Yves Kirschner
 * @author Manar Mazkatli
 * @author Martin Armbruster
 *
 * @version 1.2
 */
public class StandaloneTest extends JavaXMISerializationTest {

	/**
	 * This test generates corresponding EMF resources for the Java files in a
	 * project. JUinit passes the folder name of the projects {@code <project>} to
	 * be tested as a parameter. The EMF sources are then stored as XMI files in the
	 * folder {@code ./standalone_output/<project>/}.
	 *
	 * This {@code ./standalone_output/} folder is not yet automatically deleted
	 * after test execution. Likewise, these saved EMF resources would have to be
	 * reloaded and then tested even further.
	 *
	 * As before, the projects do not need to be built. The projects are integrated
	 * with Git submodules, so with {@code git submodule update --init} all projects
	 * must be updated before test execution.
	 *
	 *
	 * @param input Name of the project folder to test.
	 * @throws Exception 
	 *
	 * @see jamopp.standalone.JaMoPPStandalone
	 * @see org.emftext.language.java.test.AbstractJaMoPPTests
	 */
	@ParameterizedTest
	@ValueSource(strings = { "src-standalone", "acmeair", "piggymetrics", "petclinic", "TeaStore", "src-sevenandup",
			"teammates", "esda", "microservice", "src-input" })
	// src-input will be further examined, because it includes special symbols. One
	// of them is allowed in java but not allowed in xml.
	public void testProject(String input) throws Exception {

		final Path directory = Path.of(input).toAbsolutePath();
		assertTrue(Files.exists(directory), "Path " + directory + " must exist.");
		// The two lines have been replaced by equivalent calls to simplify
		// parameterization.
		// JaMoPPParserAPI parser = new JaMoPPJDTParser();
		// ResourceSet rs = parser.parseUri(URI.createURI(INPUT));
		final JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?/src/test/.*?", ".*?UnicodeIdentifiers.java");
		final ResourceSet rs = parser.parseDirectory(directory);

		TEST_OUTPUT_FOLDER_NAME = "standalone_output_ResolveAll_Active" + File.separator + input;
		File outputDir = new File(TEST_OUTPUT_FOLDER_NAME);
		outputDir.mkdirs();
		
		ResourceSet target = transferToXMI(rs, false);
		// Ignored because the stored proxy objects cannot be resolved.
//		this.compare(target);
	}

	/*
	 * TODO For further testing, the saved EMF resources must be reloaded. These
	 * loaded resources must then be tested further. Here, existing EMF
	 * functionality is called, comparable to the AbstractJaMoPPTests.
	 */

	/**
	 * Set the parser options explicitly. The default setting of the parser options
	 * should be fine for parsing non-compilable code; i.e. theoretically, nothing
	 * would need to be adjusted. However, this has been added explicitly to allow
	 * easy option changes for testing, among other things.
	 *
	 * @see jamopp.options.ParserOptionsValueContainer
	 * @see jamopp.options.ParserOptions
	 */
	@BeforeEach
	public void setUpParserOptions() {
		final ParserOptionsValueContainer instance = ParserOptionsValueContainer.getInstance();
//		instance.setValue(ParserOptions.RESOLVE_ALL_BINDINGS, Boolean.TRUE);
		instance.setValue(ParserOptions.RESOLVE_BINDINGS, Boolean.TRUE);
		instance.setValue(ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES, Boolean.TRUE);
		instance.setValue(ParserOptions.CREATE_LAYOUT_INFORMATION, Boolean.TRUE);
		instance.setValue(ParserOptions.PREFER_BINDING_CONVERSION, Boolean.TRUE);
		// instance.setValue(ParserOptions.RESOLVE_EVERYTHING, Boolean.FALSE);
		// instance.setValue(ParserOptions.REGISTER_LOCAL, Boolean.FALSE);
	}
}
