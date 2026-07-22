/*******************************************************************************
 * Copyright (c) 2006-2012
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
package tools.mdsd.jamopp.test.bugs;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.model.java.classifiers.Class;
import tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.ContainersFactory;
import tools.mdsd.jamopp.model.java.members.ClassMethod;
import tools.mdsd.jamopp.model.java.members.MembersFactory;
import tools.mdsd.jamopp.model.java.statements.Block;
import tools.mdsd.jamopp.model.java.statements.Return;
import tools.mdsd.jamopp.model.java.statements.StatementsFactory;
import tools.mdsd.jamopp.model.java.types.TypesFactory;

public class Bug1541Test extends AbstractBugTestCase {

	@Test
	public void testPrinting() throws IOException {
		CompilationUnit cu = ContainersFactory.eINSTANCE.createCompilationUnit();
		Class classA = ClassifiersFactory.eINSTANCE.createClass();
		cu.getClassifiers().add(classA);
		
		cu.getNamespaces().add("org");
		cu.getNamespaces().add("my");
		cu.getNamespaces().add("namespace1");
		classA.setName("ClassA");
		
		ClassMethod m1 = MembersFactory.eINSTANCE.createClassMethod();
		m1.setName("m1");
		m1.setTypeReference(TypesFactory.eINSTANCE.createVoid());
		m1.makePublic();
		classA.getMembers().add(m1);
		
		Block cont = StatementsFactory.eINSTANCE.createBlock();
		cont.setName("");
		
		Return returnStatement = StatementsFactory.eINSTANCE.createReturn();
		cont.getStatements().add(returnStatement);
		m1.setStatement(cont);
		
		Resource r = createResourceSet().createResource(URI.createURI("ClassA.java"));
		
		r.getContents().add(cu);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		r.save(out, null);
		System.out.println("Test1541.testPrinting() " + out.toString());
		assertTrue(out.toString().matches(// \\u003b is ; \\u007b is { \\u007d is }
				"package\\s++org\\.my\\.namespace1\\u003b\\s++class\\s++ClassA\\s++\\u007b"
				+ "\\s++public\\s++void\\s++m1\\(\\)\\s++\\u007b"
				+ "\\s++return\\u003b"
				+ "\\s++\\u007d"
				+ "\\s++\\u007d\\s++"));
	}
}
