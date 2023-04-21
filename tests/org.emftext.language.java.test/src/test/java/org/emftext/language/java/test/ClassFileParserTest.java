/*******************************************************************************
 * Copyright (c) 2021, Martin Armbruster
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.containers.CompilationUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jamopp.options.ParserOptions;

/**
 * A test class for the ClassFileParser.
 */
public class ClassFileParserTest extends AbstractJaMoPPTests {
	private static String BIN_FOLDER;
	private static final String CLASS_FILE_EXTENSION = ".class";
	
	@BeforeAll
	public static void firstSetup() {
		AbstractJaMoPPTests.initLogging();
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.FALSE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.FALSE);
		String binFolder = "bin";
		if (Files.notExists(Paths.get(".", binFolder))) {
			binFolder = "target" + File.separator + "classes";
		}
		BIN_FOLDER = binFolder;
	}
	
	@AfterAll
	public static void lastTearDown() {
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
	}
	
	@Test
	public void testImport1() throws Exception {
		String filename = "Import1.java";

		JavaClasspath.get().registerClassifier("", "Import2",
				createURI(BIN_FOLDER, "Import2" + CLASS_FILE_EXTENSION));

		CompilationUnit model = (CompilationUnit) parseResource(filename);
		assertNumberOfClassifiers(model, 1);
		
		parseAndReprint(filename);
	}
	
	@Test
	public void testExplicitGenericConstructorCalls() throws Exception {
		String classFile = "ConstructorCalls";
		JavaClasspath.get().registerClassifier("", classFile,
				createURI(BIN_FOLDER, classFile + CLASS_FILE_EXTENSION));
		
		String typename = "ExplicitGenericConstructorCalls";
		String filename = typename + ".java";
		org.emftext.language.java.classifiers.Class clazz = assertParsesToClass(typename);

		assertMemberCount(clazz, 4);

		parseAndReprint(filename);
	}
	
	@Test
	public void testResolving() throws Exception {
		String folder = "resolving";

		String registeredTypeName = "MethodCallsWithoutInheritance";
		JavaClasspath.get().registerClassifier(folder, registeredTypeName,
				createURI(BIN_FOLDER, folder, registeredTypeName + CLASS_FILE_EXTENSION));

		parseAndReprint(folder + File.separator + "MethodCalls.java");
		parseAndReprint(folder + File.separator + "MethodCallsWithLocalTypeReferences.java");
		parseAndReprint(folder + File.separator + "MethodCallsWithoutInheritance.java");
		parseAndReprint(folder + File.separator + "ReferenceToInheritedMethod.java");
		parseAndReprint(folder + File.separator + "VariableReferencing.java");
	}

	@Test
	public void testStaticCalls() throws Exception {
		String registeredType = "AClass";
		JavaClasspath.get().registerClassifier("", registeredType,
				createURI(BIN_FOLDER, registeredType + CLASS_FILE_EXTENSION));
		JavaClasspath.get().registerClassifier(registeredType + "$", "A",
				createURI(BIN_FOLDER, registeredType + "$A" + CLASS_FILE_EXTENSION));
		registeredType = "CallTargetsProvider";
		String pack = "staticcalltarget";
		JavaClasspath.get().registerClassifier(pack, registeredType,
				createURI(BIN_FOLDER, pack, registeredType + CLASS_FILE_EXTENSION));
		parseAndReprint("StaticCalls.java");
	}
	
	private URI createURI(String... relPath) {
		return URI.createFileURI(Paths.get(".", relPath).toAbsolutePath().toString());
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return false;
	}

	@Override
	protected String getTestInputFolder() {
		return "src-input";
	}
}
