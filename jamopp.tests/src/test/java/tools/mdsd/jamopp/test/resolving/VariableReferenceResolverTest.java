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
package tools.mdsd.jamopp.test.resolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.model.java.members.ClassMethod;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.statements.Block;
import tools.mdsd.jamopp.model.java.statements.ExpressionStatement;
import tools.mdsd.jamopp.model.java.statements.LocalVariableStatement;
import tools.mdsd.jamopp.model.java.statements.Statement;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;

public class VariableReferenceResolverTest extends AbstractResolverTestCase {

	@Test
	public void testReferencing() throws Exception {
		String typename = "VariableReferencing";
		tools.mdsd.jamopp.model.java.classifiers.Class clazz = assertParsesToClass(typename);
		assertNotNull(clazz);

		List<Member> members = clazz.getMembers();
		assertEquals(2, members.size());

		Field field = assertIsField(members.get(0), "var");
		ClassMethod method = assertIsMethod(members.get(1), "method");

		List<? extends Statement> statements = method.getStatements();

		assertEquals(6, statements.size());
		Statement statement1 = statements.get(0);
		assertType(statement1, ExpressionStatement.class);
		assertIsReferenceToField(statement1, field);

		Statement statement2 = statements.get(1);
		assertType(statement2, LocalVariableStatement.class);
		LocalVariable localVar = ((LocalVariableStatement) statement2).getVariable();

		Statement statement3 = statements.get(2);
		assertIsReferenceToLocalVariable(statement3, localVar);

		Statement statement4 = statements.get(3);
		assertType(statement4, Block.class);
	}
}
