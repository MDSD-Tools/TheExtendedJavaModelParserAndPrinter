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
 *   Martin Armbruster
 *      - Adaptation
 ******************************************************************************/
package jamopp.parser.jdt.singlefile;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.classifiers.Annotation;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.members.MembersFactory;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.parameters.ParametersFactory;
import org.emftext.language.java.statements.StatementsFactory;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.TypesFactory;

/**
 * Utility class that enhances and simplifies a Java model based on Java
 * language specifics.
 */
public class ParsePostProcessor {

	/**
	 * Main method to perform the completion and expression simplification for
	 * the given resource.
	 * 
	 * @param obj the resource to complete and simplify.
	 */
	public static void complete(EObject obj) {
		obj.eAllContents().forEachRemaining(element -> {
			if (element instanceof Class) {
				addDefaultSuperClass((Class) element);
			}
			if (element instanceof Interface) {
				addDefaultSuperInterface((Interface) element);
			}
			if (element instanceof Enumeration) {
				addMissingEnumerationMembers((Enumeration) element);
			}
			if (element instanceof Annotation) {
				addMissingAnnotationMembers((Annotation) element);
			}
			if (element instanceof ConcreteClassifier) {
				addMissingPackage((ConcreteClassifier) element);
			}
		});
	}

	/**
	 * Adds <code>java.lang.Object</code> as default super class to the given
	 * class if the class does not explicitly extend another class.
	 * 
	 * @param javaClass
	 *            the class to complete
	 */
	private static void addDefaultSuperClass(Class javaClass) {
		if (javaClass.getExtends() == null && javaClass.getDefaultExtends() == null
				&& !javaClass.isJavaLangObject(javaClass)) {
			Class objectClass = javaClass.getObjectClass();
			ClassifierReference classifierReference = TypesFactory.eINSTANCE.createClassifierReference();
			classifierReference.setTarget(objectClass);
			javaClass.setDefaultExtends(classifierReference);
		}
	}

	/**
	 * Adds <code>java.lang.Object</code> as default super interface to the
	 * given interface if that interface implements no other interface.
	 * 
	 * @param javaInterface
	 *            the interface to complete
	 */
	private static void addDefaultSuperInterface(Interface javaInterface) {
		if (javaInterface.getExtends().isEmpty() && javaInterface.getDefaultExtends().isEmpty()) {
			Class objectClass = javaInterface.getObjectClass();
			ClassifierReference classifierReference = TypesFactory.eINSTANCE.createClassifierReference();
			classifierReference.setTarget(objectClass);
			javaInterface.getDefaultExtends().add(classifierReference);
		}
	}

	/**
	 * Adds the additional method <code>value()</code> to the given annotation.
	 * 
	 * @param annotation
	 *            the annotation to complete
	 */
	private static void addMissingAnnotationMembers(Annotation annotation) {
		String valueMethodName = "value";
		Method valueMethod = annotation.getContainedMethod(valueMethodName);
		if (valueMethod == null) {
			valueMethod = MembersFactory.eINSTANCE.createInterfaceMethod();
			valueMethod.setName(valueMethodName);
			valueMethod.setStatement(StatementsFactory.eINSTANCE.createEmptyStatement());
			ClassifierReference type = TypesFactory.eINSTANCE.createClassifierReference();
			type.setTarget(JavaClasspath.get().getConcreteClassifier("java.lang.String"));
			valueMethod.setTypeReference(type);
			annotation.getDefaultMembers().add(valueMethod);
		}
	}

	/**
	 * Adds the additional methods <code>value()</code> and
	 * <code>valueOf()</code> to the given enumeration.
	 * 
	 * @param enumeration
	 *            the enumeration to complete
	 */
	private static void addMissingEnumerationMembers(Enumeration enumeration) {

		// Add the values() method
		String valuesMethodName = "values";
		Method valuesMethod = enumeration.getContainedMethod(valuesMethodName);

		if (valuesMethod == null) {
			valuesMethod = MembersFactory.eINSTANCE.createInterfaceMethod();
			valuesMethod.setName(valuesMethodName);
			valuesMethod.setStatement(StatementsFactory.eINSTANCE.createEmptyStatement());

			ClassifierReference type = TypesFactory.eINSTANCE.createClassifierReference();
			type.setTarget(enumeration);
			valuesMethod.setTypeReference(type);
			enumeration.getDefaultMembers().add(valuesMethod);
		}

		// Add the valueOf() method
		String valueOfMethodName = "valueOf";
		Method valueOfMethod = enumeration.getContainedMethod(valueOfMethodName);

		if (valueOfMethod == null) {
			valueOfMethod = MembersFactory.eINSTANCE.createInterfaceMethod();
			valueOfMethod.setName(valueOfMethodName);
			valueOfMethod.setStatement(StatementsFactory.eINSTANCE.createEmptyStatement());

			ClassifierReference type = TypesFactory.eINSTANCE.createClassifierReference();
			type.setTarget(enumeration);
			valueOfMethod.setTypeReference(type);

			Parameter strParameter = ParametersFactory.eINSTANCE.createOrdinaryParameter();
			strParameter.setName("str");
			type = TypesFactory.eINSTANCE.createClassifierReference();
			type.setTarget(JavaClasspath.get().getConcreteClassifier("java.lang.String"));
			strParameter.setTypeReference(type);

			valueOfMethod.getParameters().add(strParameter);
			enumeration.getDefaultMembers().add(valueOfMethod);
		}
	}
	
	private static void addMissingPackage(ConcreteClassifier clazz) {
		if (clazz.getPackage() == null) {
			String packageName = clazz.getContainingCompilationUnit().getNamespacesAsString();
			org.emftext.language.java.containers.Package pack = JavaClasspath.get().getPackage(packageName);
			if (pack != null) {
				clazz.setPackage(pack);
			}
		}
	}
}
