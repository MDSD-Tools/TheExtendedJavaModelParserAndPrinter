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

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.commons.NamespaceAwareElement;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.TypeReference;

public class ConcreteClassifierExtension {
	
	public static EList<ConcreteClassifier> getAllInnerClassifiers(ConcreteClassifier me) {	
		EList<ConcreteClassifier> innerClassifierList = new UniqueEList<ConcreteClassifier>();

		innerClassifierList.addAll(me.getInnerClassifiers());

		for (ConcreteClassifier superClassifier : me.getAllSuperClassifiers()) {
			if (superClassifier == null) {
				continue;
			}
			List<ConcreteClassifier> superInnerList = superClassifier
					.getInnerClassifiers();

			for (ConcreteClassifier superInner : superInnerList) {
				if (superInner.eIsProxy()) {
					EObject resolved = EcoreUtil.resolve(superInner, me);
					superInner = (ConcreteClassifier) resolved;
				}
				
				boolean isVisible = !superInner.isHidden(me);
				if (isVisible) {
					innerClassifierList.add(superInner);
				}
			}
		}	
		return innerClassifierList;
	}
	
	public static EList<ConcreteClassifier> getInnerClassifiers(ConcreteClassifier me) {
		if (me.eIsProxy()) {
			String uriString = ((InternalEObject) me).eProxyURI().trimFragment().toString();
			String fullName = uriString.substring(LogicalJavaURIGenerator.JAVA_CLASSIFIER_PATHMAP.length(), 
				uriString.length() - LogicalJavaURIGenerator.JAVA_FILE_EXTENSION.length());
			EList<ConcreteClassifier> result = new UniqueEList<>();
			result.add(JavaClasspath.get(me).getConcreteClassifier(fullName));
			return result;
		}

		// For classes declared locally inside methods that are not registered
		// in the class path
		EList<ConcreteClassifier> result = new UniqueEList<ConcreteClassifier>();
		// Can not call ClassifierUtil.getAllMembers, because it will try to
		// call this method!
		for (Member member : me.getMembers()) {
			if (member instanceof ConcreteClassifier) {
				result.add((ConcreteClassifier) member);
				result.addAll(((ConcreteClassifier) member).getInnerClassifiers());
			}
		}
		
		return result;
	}
	
	public static EList<ClassifierReference> getSuperTypeReferences(ConcreteClassifier me) {
		EList<ClassifierReference> typeReferenceList = new UniqueEList<ClassifierReference>();
		if (me instanceof org.emftext.language.java.classifiers.Class) {
			org.emftext.language.java.classifiers.Class javaClass = (org.emftext.language.java.classifiers.Class) me;
			
			// Add super type of class to super type list
			TypeReference superClass = javaClass.getExtends();
			if (superClass != null) {
				ClassifierReference classifierReference = superClass.getPureClassifierReference();
				typeReferenceList.add(classifierReference);
				Classifier target = classifierReference.getTarget();
				ConcreteClassifier concreteTarget = (ConcreteClassifier) target;
				if (!me.isJavaLangObject(concreteTarget)) {
					typeReferenceList.addAll(concreteTarget.getSuperTypeReferences());
				}
			}
			
			// Add all implemented interfaces to super type list
			addSuperTypes(javaClass.getImplements(), typeReferenceList);
			
		} else if (me instanceof Interface) {
			Interface javaInterface = (Interface) me;
			
			// Add all super interfaces to super type list
			addSuperTypes(javaInterface.getExtends(), typeReferenceList);
		}
		return typeReferenceList;
	}

	private static void addSuperTypes(List<TypeReference> typeReferences,
			List<ClassifierReference> superTypeReferences) {
		
		for (TypeReference interfaceReference : typeReferences) {
			addSuperType(interfaceReference, superTypeReferences);
		}
	}

	private static void addSuperType(TypeReference typeReference,
			List<ClassifierReference> superTypeReferences) {
		
		ClassifierReference classifierReference = typeReference.getPureClassifierReference();
		superTypeReferences.add(classifierReference);
		Classifier target = classifierReference.getTarget();
		ConcreteClassifier concreteTarget = (ConcreteClassifier) target;
		superTypeReferences.addAll(concreteTarget.getSuperTypeReferences());
	}
	
	/**
	 * Returns all members of the given classifier including inner classes and 
	 * all members of super types (extended classes and implemented interfaces).
	 * 
	 * @param me the given classifier.
	 * @param context to check protected visibility
	 * @return member list
	 */
	public static EList<Member> getAllMembers(ConcreteClassifier me, Commentable context) {
		EList<Member> memberList = new UniqueEList<Member>();

		ConcreteClassifier concreteClassifier = (ConcreteClassifier) me;
		memberList.addAll(concreteClassifier.getMembers());
		memberList.addAll(concreteClassifier.getDefaultMembers());
		// Because inner classes are found in separate class files
		memberList.addAll(concreteClassifier.getAllInnerClassifiers());
		
		for (ConcreteClassifier superClassifier : me.getAllSuperClassifiers()) {
			if (superClassifier == null) {
				continue;
			}
			if (superClassifier.eIsProxy()) {
				superClassifier = (ConcreteClassifier) EcoreUtil.resolve(superClassifier, context);
			}
			for (Member member : superClassifier.getMembers()) {
				if (member instanceof AnnotableAndModifiable) {
					AnnotableAndModifiable modifiable = (AnnotableAndModifiable) member;

					boolean isVisible = !modifiable.isHidden(context);
					if (isVisible) {
						memberList.add(member);
					}
				} else {
					memberList.add(member);
				}
			}
			memberList.addAll(superClassifier.getDefaultMembers());
		}
		return memberList;
	}

	/**
	 * Returns the qualified name of this concrete classifier.
	 * 
	 * @param me the classifier to return the qualified name for.
	 * @return the qualified name.
	 */
	public static String getQualifiedName(ConcreteClassifier me) {
		StringBuilder qualifiedName = new StringBuilder();
		if (me.eContainer() instanceof ConcreteClassifier) {
			qualifiedName.append(((ConcreteClassifier) me.eContainer()).getQualifiedName());
			qualifiedName.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
		} else if (me.eContainer() instanceof NamespaceAwareElement) {
			qualifiedName.append(((NamespaceAwareElement) me.eContainer()).getNamespacesAsString());
		} else {
			List<String> packageParts = me.getContainingPackageName();
			if (packageParts != null) {
				for (String packagePart : packageParts) {
					qualifiedName.append(packagePart);
					qualifiedName.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
				}
			}
		}
		qualifiedName.append(me.getName());
		return qualifiedName.toString();
	}

	/**
	 * Returns <code>true</code> if the given {@link ConcreteClassifier} is
	 * <code>java.lang.Object</code>. Attention: This method does not take the
	 * {@link ConcreteClassifier} on which the method is called (<code>me</code>
	 * ) as argument as this is not used in the methods implementation.
	 * 
	 * @param clazz
	 *            the class to check
	 * @return <code>true</code> if <code>clazz</code> represents
	 *         <code>java.lang.Object</code>, otherwise <code>false</code>
	 */
	public static boolean isJavaLangObject(ConcreteClassifier clazz) {
		String name = clazz.getName();
		if (!"Object".equals(name)) {
			return false;
		}
		
		List<String> packageName = clazz.getContainingPackageName();
		if (packageName.size() != 2) {
			return false;
		}
		if (!("java".equals(packageName.get(0)))) {
			return false;
		}
		if (!("lang".equals(packageName.get(1)))) {
			return false;
		}
		return true;
	}
}
