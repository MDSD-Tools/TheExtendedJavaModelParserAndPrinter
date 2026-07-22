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

package tools.mdsd.jamopp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.members.ClassMethod;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.statements.ExpressionStatement;
import tools.mdsd.jamopp.model.java.statements.Statement;
import tools.mdsd.jamopp.parser.jdt.JaMoPPJDTParser;

/**
 * Class for testing the Parser API
 */

public class JaMoPPParserAPITest extends AbstractJaMoPPTests {
	
	private JaMoPPJDTParser parser;
	private static final String JAVA_FILE_EXTENSION = ".java";
	protected static final String TEST_INPUT_FOLDER = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "input";
	
	@BeforeEach
	public void setUp() {
		super.initResourceFactory();
		parser = new JaMoPPJDTParser();
		
	}
	
	// 
	//Bekomme ich aus dem resource set irgendwie wieder classifier raus? 
	// oder die Assert Parser nutzen aus dem old JamoPP project? 
	
	@Disabled
	public void testIsClass() throws Exception {
		//ResourceSet set = parser.parseDirectory(Paths.get("scr-input/ClassA"));
		//set.getResources().get(index)
		String typename = "ClassA";
		//String filename = typename + JAVA_FILE_EXTENSION;
		tools.mdsd.jamopp.model.java.classifiers.Class clazz = assertParsesToClass(typename);
		
		this.assertIsClass(clazz);
		//this.assertParsesToType(typename, "Class")
	}
	
	@Disabled
	void testNameOfClass() {
		
		//this.assertClassifierName(declaration, expectedName);
	}
	
	@Test
	@Disabled
	public void testMethodOverwriting()throws Exception {
		//System.out.print("setup");
		String filenameParent = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "input" + File.separator + "ClassB" + JAVA_FILE_EXTENSION;
		String filenameChild = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "input" + File.separator + "ClassA" + JAVA_FILE_EXTENSION;
		CompilationUnit cu = (CompilationUnit) parseResource(filenameParent, filenameChild);
		System.out.print("setup");
		System.out.print(cu.getClassifiers().get(1).getName());
		System.out.print(cu.getClassifiers().get(2).getName());
		
		
		//assertEquals(clazz.getMembers().get(1), target);
		
		//ConcreteClassifier clazz = cu.getClassifiers().get(1);
		//assertEquals(clazz.getMembers().get(1), target);
		
		ConcreteClassifier clazz = cu.getClassifiers().get(2);
				
				Statement s = ((ClassMethod) clazz.getMembers().get(2)).getStatements().get(2);
		ClassMethod target = (ClassMethod) ((MethodCall) (
				(ExpressionStatement) s).getExpression()).getTarget();
		assertEquals(clazz.getMembers().get(1), target);
		
		
		//parseAndReprint(filename, getTestInputFolder(), TEST_OUTPUT_FOLDER);
		
		
		//ResourceSet set = parser.parseDirectory(Paths.get("scr-input/ClassA","scr-input/ClassB"));
		
	}
	
	
	@Disabled
	public void testSrcSevenAndUp() {
		ResourceSet set = parser.parseDirectory(Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "sevenandup"));
		this.assertModelValid(set);
	}

	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return true;
	}

	@Override
	protected String getTestInputFolder() {
		return "";
	}
}
