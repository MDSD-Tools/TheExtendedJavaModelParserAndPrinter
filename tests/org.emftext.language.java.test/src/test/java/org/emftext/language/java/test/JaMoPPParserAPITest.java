package org.emftext.language.java.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.statements.ExpressionStatement;
import org.emftext.language.java.statements.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jamopp.parser.jdt.JaMoPPJDTParser;

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
		org.emftext.language.java.classifiers.Class clazz = assertParsesToClass(typename);
		
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
