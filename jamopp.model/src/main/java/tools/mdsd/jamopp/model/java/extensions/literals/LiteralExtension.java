/*******************************************************************************
 * Copyright (c) 2006-2014
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
package tools.mdsd.jamopp.model.java.extensions.literals;

import tools.mdsd.jamopp.model.java.literals.BooleanLiteral;
import tools.mdsd.jamopp.model.java.literals.CharacterLiteral;
import tools.mdsd.jamopp.model.java.literals.DoubleLiteral;
import tools.mdsd.jamopp.model.java.literals.FloatLiteral;
import tools.mdsd.jamopp.model.java.literals.IntegerLiteral;
import tools.mdsd.jamopp.model.java.literals.Literal;
import tools.mdsd.jamopp.model.java.literals.LongLiteral;
import tools.mdsd.jamopp.model.java.literals.NullLiteral;

public class LiteralExtension {

	/**
	 * @param me the literal.
	 * @return type of the literal
	 */
	public static tools.mdsd.jamopp.model.java.classifiers.Class getOneType(Literal me) {
		// Overrides implementation in Expression
		tools.mdsd.jamopp.model.java.classifiers.Class javaClass = null;

		if (me instanceof NullLiteral) {
			javaClass = me.getLibClass("Void");
		} else if (me instanceof BooleanLiteral) {
			javaClass = me.getLibClass("Boolean");
		} else if (me instanceof DoubleLiteral) {
			javaClass = me.getLibClass("Double");
		} else if (me instanceof FloatLiteral) {
			javaClass = me.getLibClass("Float");
		} else if (me instanceof IntegerLiteral) {
			javaClass = me.getLibClass("Integer");
		} else if (me instanceof LongLiteral) {
			javaClass = me.getLibClass("Long");
		} else if (me instanceof CharacterLiteral) {
			javaClass = me.getLibClass("Character");
		}

		return javaClass;
	}
}
