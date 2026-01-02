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
package tools.mdsd.jamopp.model.java.extensions.types;

import org.eclipse.emf.common.util.EList;

import tools.mdsd.jamopp.model.java.commons.Commentable;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.types.PrimitiveType;

public class PrimitiveTypeExtension {

	/**
	 * @param me the primitive type.
	 * @param context to check protected visibility
	 * @return all members (including super type members)
	 */
	public static EList<Member> getAllMembers(PrimitiveType me, Commentable context) {
		tools.mdsd.jamopp.model.java.classifiers.Class javaClass = me.wrapPrimitiveType();
		return javaClass.getAllMembers(context);
	}
	
	/**
	 * @param me the type to wrap.
	 * @return primitive type as a class representation
	 */
	public static tools.mdsd.jamopp.model.java.classifiers.Class wrapPrimitiveType(PrimitiveType me) {
		tools.mdsd.jamopp.model.java.classifiers.Class javaClass = null;
		
		if (me instanceof tools.mdsd.jamopp.model.java.types.Boolean) {
			javaClass = me.getLibClass("Boolean");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Byte) {
			javaClass = me.getLibClass("Byte");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Char) {
			javaClass = me.getLibClass("Character");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Double) {
			javaClass = me.getLibClass("Double");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Float) {
			javaClass = me.getLibClass("Float");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Int) {
			javaClass = me.getLibClass("Integer");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Long) {
			javaClass = me.getLibClass("Long");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Short) {
			javaClass = me.getLibClass("Short");
		}
		if (me instanceof tools.mdsd.jamopp.model.java.types.Void) {
			javaClass = me.getLibClass("Void");
		}
		return javaClass;
	}
}
