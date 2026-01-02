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

import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.model.java.members.ClassMethod;
import tools.mdsd.jamopp.model.java.members.MembersFactory;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.ReferencesFactory;
import tools.mdsd.jamopp.model.java.statements.Return;
import tools.mdsd.jamopp.model.java.statements.StatementsFactory;

public class Bug1818Test extends AbstractBugTestCase {
	@Test
	public void testPrintReturn() {
	    Return newReturnStatement = StatementsFactory.eINSTANCE.createReturn();
	    ClassMethod method = MembersFactory.eINSTANCE.createClassMethod();
	    method.setName("methodCall");
	    MethodCall methodCall = ReferencesFactory.eINSTANCE.createMethodCall();
	    methodCall.setTarget(method);
		method.getStatements().add(newReturnStatement);
		newReturnStatement.setReturnValue(methodCall);
//		String text = JavaResourceUtil.getText(newReturnStatement);
//		assertEquals("return methodCall();", text);
	}
}
