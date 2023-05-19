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
package org.emftext.language.java.extensions.classifiers;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.InternalEObject;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.TypesFactory;

public class ClassExtension {

	/**
	 * Recursively collects all super types (extended classes and implemented
	 * interfaces) of the given class.
	 * 
	 * @param me the given class.
	 * @return the collected super types.
	 */
	public static EList<ConcreteClassifier> getAllSuperClassifiers(org.emftext.language.java.classifiers.Class me) {

		EList<ConcreteClassifier> result = new UniqueEList<ConcreteClassifier>();

		// Collects all super classes first
		org.emftext.language.java.classifiers.Class superClass = me;
		while (superClass != null && !superClass.eIsProxy() && !me.isJavaLangObject(superClass)) {
			superClass = superClass.getSuperClass();
			if (superClass != null) {
				result.add(superClass);
			}
		}

		// Collect all implemented interfaces
		for (TypeReference typeArg : me.getImplements()) {
			ConcreteClassifier superInterface = (ConcreteClassifier) typeArg.getTarget();
			if (superInterface != null) {
				result.add(superInterface);
				if (superInterface instanceof Interface) {
					result.addAll(((Interface) superInterface).getAllSuperClassifiers());
				}
			}
		}

		// Collect all implemented interfaces of super classes
		superClass = me.getSuperClass();
		if (superClass != null && !superClass.eIsProxy() && !me.isJavaLangObject(superClass)) {
			result.addAll(superClass.getAllSuperClassifiers());
		}

		return result;
	}

	/**
	 * @param me the given class.
	 * @return the direct super class
	 */
	public static org.emftext.language.java.classifiers.Class getSuperClass(
			org.emftext.language.java.classifiers.Class me) {

		TypeReference superClassReference = me.getExtends();
		if (superClassReference == null) {
			superClassReference = me.getDefaultExtends();
		}

		if (superClassReference == null) {
			return null;
		}

		Type result = superClassReference.getTarget();
		if (result instanceof org.emftext.language.java.classifiers.Class) {
			return (org.emftext.language.java.classifiers.Class) result;
		}
		return null;
	}

	/**
	 * @param me the given class.
	 * @return primitive type, if the class can be wrapped
	 */
	public static PrimitiveType unWrapPrimitiveType(org.emftext.language.java.classifiers.Class me) {

		String type = me.eIsProxy() ? ((InternalEObject) me).eProxyURI().toString() : me.getQualifiedName();

		if (type.contains("java.lang.Boolean")) {
			return TypesFactory.eINSTANCE.createBoolean();
		}
		if (type.contains("java.lang.Byte")) {
			return TypesFactory.eINSTANCE.createByte();
		}
		if (type.contains("java.lang.Character")) {
			return TypesFactory.eINSTANCE.createChar();
		}
		if (type.contains("java.lang.Float")) {
			return TypesFactory.eINSTANCE.createFloat();
		}
		if (type.contains("java.lang.Double")) {
			return TypesFactory.eINSTANCE.createDouble();
		}
		if (type.contains("java.lang.Integer")) {
			return TypesFactory.eINSTANCE.createInt();
		}
		if (type.contains("java.lang.Long")) {
			return TypesFactory.eINSTANCE.createLong();
		}
		if (type.contains("java.lang.Short")) {
			return TypesFactory.eINSTANCE.createShort();
		}
		if (type.contains("java.lang.Void")) {
			return TypesFactory.eINSTANCE.createVoid();
		}
		return null;
	}
}
