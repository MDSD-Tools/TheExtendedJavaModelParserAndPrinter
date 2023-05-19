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
package org.emftext.language.java.extensions.commons;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.statements.Statement;
import org.emftext.language.java.statements.StatementListContainer;

public class CommentableExtension {

	/**
	 * Adds the given statement before the statement that contains this element.
	 * 
	 * @param me the context in which the statement is added.
	 * @param statementToAdd the statement to add.
	 */
	public static void addBeforeContainingStatement(Commentable me, Statement statementToAdd) {
		EObject container = me.eContainer();
		EObject statement = me;
		while (container != null) {
			if (container instanceof StatementListContainer) {
				break;
			}
			container = container.eContainer();
			statement = statement.eContainer();
		}
		if (container == null) {
			throw new IllegalArgumentException("Element " + me + " is not contained in a StatementListContainer.");
		}
		StatementListContainer statementListContainer = (StatementListContainer) container;
		EList<Statement> statements = statementListContainer.getStatements();
		int index = statements.indexOf(statement);
		
		statements.add(index, statementToAdd);
	}

	/**
	 * Adds the given statement after the statement that contains this element.
	 * 
	 * @param me the context in which the statement is added.
	 * @param statementToAdd the statement to add.
	 */
	public static void addAfterContainingStatement(Commentable me, Statement statementToAdd) {
		EObject container = me.eContainer();
		EObject statement = me;
		while (container != null) {
			if (container instanceof StatementListContainer) {
				break;
			}
			container = container.eContainer();
			statement = statement.eContainer();
		}
		if (container == null) {
			throw new IllegalArgumentException("Element " + me + " is not contained in a StatementListContainer.");
		}
		StatementListContainer statementListContainer = (StatementListContainer) container;
		EList<Statement> statements = statementListContainer.getStatements();
		int index = statements.indexOf(statement);
		
		if (index == statements.size() - 1) {
			// statement is the last one
			statements.add(statementToAdd);
		} else {
			statements.add(index + 1, statementToAdd);
		}
	}

	/**
	 * Walks up the containment hierarchy and returns the first parent with the
	 * given type. If no such parent is found, null is returned.
	 * 
	 * @param me the start object.
	 * @param type the given type.
	 * @return the found parent or null.
	 */
	public static EObject getParentByEType(Commentable me, EClass type) {
		EObject container = me.eContainer();
		while (container != null) {
			if (type.isInstance(container)) {
				return container;
			}
			container = container.eContainer();
		}
		return null;
	}

	/**
	 * Walks up the containment hierarchy and returns the first parent with the
	 * given type. If no such parent is found, null is returned.
	 * 
	 * @param <T> the given type.
	 * @param me the start object.
	 * @param type class object of the given type.
	 * @return the found parent or null.
	 */
	public static <T> T getParentByType(Commentable me, Class<T> type) {
		EObject container = me.eContainer();
		while (container != null) {
			if (type.isInstance(container)) {
				return type.cast(container);
			}
			container = container.eContainer();
		}
		return null;
	}

	/**
	 * Searches for the first child with the given type. If no such child is
	 * found, <code>null</code> is returned.
	 * 
	 * @param me the start object.
	 * @param type the given type.
	 * @return the found child or null.
	 */
	public static EObject getFirstChildByEType(Commentable me, EClass type) {
		Iterator<EObject> it = me.eAllContents();
		while (it.hasNext()) {
			EObject next = it.next();
			if (type.isInstance(next)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * Searches for the first child with the given type. If no such child is
	 * found, <code>null</code> is returned.
	 * 
	 * @param <T> the given type.
	 * @param me the start object.
	 * @param type class object of the given type.
	 * @return the found child or null.
	 */
	public static <T> T getFirstChildByType(Commentable me, Class<T> type) {
		Iterator<EObject> it = me.eAllContents();
		while (it.hasNext()) {
			EObject next = it.next();
			if (type.isInstance(next)) {
				return type.cast(next);
			}
		}
		return null;
	}

	/**
	 * Returns all children of the given type.
	 * 
	 * @param me the start object.
	 * @param type the given type.
	 * @return all children.
	 */
	public static EList<EObject> getChildrenByEType(Commentable me, EClass type) {
		EList<EObject> children = new BasicEList<EObject>();
		Iterator<EObject> it = me.eAllContents();
		while (it.hasNext()) {
			EObject next = it.next();
			if (type.isInstance(next)) {
				children.add(next);
			}
		}
		return children;
	}

	/**
	 * Returns all children of the given type.
	 * 
	 * @param <T> the given type.
	 * @param me the start object.
	 * @param type class object of the type.
	 * @return the children.
	 */
	public static <T> EList<T> getChildrenByType(Commentable me, Class<T> type) {
		EList<T> children = new BasicEList<T>();
		Iterator<EObject> it = me.eAllContents();
		while (it.hasNext()) {
			EObject next = it.next();
			if (type.isInstance(next)) {
				children.add(type.cast(next));
			}
		}
		return children;
	}

	/**
	 * Finds the {@link ConcreteClassifier} representing the class with the
	 * given classified name.
	 * 
	 * @param me the context.
	 * @param name
	 *            classified name of the ConcreteClassifier
	 * @return the classifier.
	 */
	public static ConcreteClassifier getConcreteClassifier(Commentable me, String name) {
		return (ConcreteClassifier) EcoreUtil.resolve(JavaClasspath.get(me).getConcreteClassifier(name), me);
	}
	
	/**
	 * Finds all {@link ConcreteClassifier} representing the classes in the
	 * given package or a single class from that package.
	 * 
	 * @param me the context.
	 * @param packageName
	 *            name of the package
	 * @param classifierQuery
	 *            * for all classifiers or name of a single classifier
	 * @return the classifier.
	 */
	public static EList<ConcreteClassifier> getConcreteClassifiers(
			Commentable me, String packageName, String classifierQuery) {
		if (!packageName.endsWith(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)) {
			packageName += LogicalJavaURIGenerator.PACKAGE_SEPARATOR;
		}
		EList<ConcreteClassifier> result = new UniqueEList<ConcreteClassifier>();
		if (classifierQuery.equals("*")) {
			Collection<ConcreteClassifier> classi = JavaClasspath.get(me).getConcreteClassifiers(packageName);
			if (classi != null) {
				for (ConcreteClassifier clazz : classi) {
					result.add(clazz);
				}
			}
		} else {
			ConcreteClassifier classifier = JavaClasspath.get(me)
					.getConcreteClassifier(packageName + classifierQuery);
			if (classifier != null) {
				result.add(classifier);
			}
		}
		return result;
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Class}
	 * representing the class with the given name located in
	 * <code>java.lang</code>.
	 *
	 * @param me the context.
	 * @param name
	 *            name of the Class.
	 * @return the Class.
	 */
	public static org.emftext.language.java.classifiers.Class getLibClass(Commentable me, String name) {
		ConcreteClassifier result = JavaClasspath.get(me).getConcreteClassifier("java.lang." + name);
		result = (ConcreteClassifier) EcoreUtil.resolve(result, me);
		if (result != null && result instanceof org.emftext.language.java.classifiers.Class) {
			return (org.emftext.language.java.classifiers.Class) result;
		}
		return null;
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Interface}
	 * representing the interface with the given name located in
	 * <code>java.lang</code>.
	 * 
	 * @param me the context.
	 * @param name
	 *            name of the Interface.
	 * @return the interface.
	 */
	public static Interface getLibInterface(Commentable me, String name) {
		ConcreteClassifier interfaceClass = JavaClasspath.get(me).getConcreteClassifier("java.lang." + name);
		interfaceClass = (ConcreteClassifier) EcoreUtil.resolve(interfaceClass, me);
		if (interfaceClass != null && interfaceClass instanceof Interface) {
			return (Interface) interfaceClass;
		}
		return null;
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Class}
	 * representing <code>java.lang.Class</code>.
	 * 
	 * @param me the context.
	 * @return the Class.
	 */
	public static org.emftext.language.java.classifiers.Class getClassClass(Commentable me)  {
		return me.getLibClass("Class");
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Class}
	 * representing <code>java.lang.Object</code>.
	 * 
	 * @param me the context.
	 * @return the Class.
	 */
	public static org.emftext.language.java.classifiers.Class getObjectClass(Commentable me)  {
		return me.getLibClass("Object");
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Class}
	 * representing <code>java.lang.String</code>.
	 * 
	 * @param me the context.
	 * @return the Class.
	 */
	public static org.emftext.language.java.classifiers.Class getStringClass(Commentable me) {
		return me.getLibClass("String");
	}
	
	/**
	 * Finds the {@link org.emftext.language.java.classifiers.Interface}
	 * representing <code>java.lang.annotation.Annotation</code>.
	 * 
	 * @param me the context.
	 * @return the Class.
	 */
	public static Interface getAnnotationInterface(Commentable me) {
		ConcreteClassifier proxy = JavaClasspath.get(me).getConcreteClassifier("java.lang.annotation.Annotation");
		proxy = (ConcreteClassifier) EcoreUtil.resolve(proxy, me);
		if (proxy != null && proxy instanceof Interface) {
			return (Interface) proxy;
		}
		return null;
	}
	
	//===== Container look up =====
	
	/**
	 * Finds the containing classifier for the given element.
	 * 
	 * @param me the context.
	 * @return containing classifier
	 */
	public static ConcreteClassifier getContainingConcreteClassifier(Commentable me) {
		EObject value = me;
		while (!(value instanceof ConcreteClassifier) && value != null) {
			value = value.eContainer();
		}
		return (ConcreteClassifier) value;
	}
	
	/**
	 * Finds the classifier that is the parent of this element. If this element
	 * is an inner classifier the parent classifier does not necessarily contain
	 * this element, since it can reside in a different compilation unit when
	 * stored in byte code.
	 * 
	 * @param me the context.
	 * @return containing classifier
	 */
	public static ConcreteClassifier getParentConcreteClassifier(Commentable me) {
		return me.getContainingConcreteClassifier();
	}
	
	/**
	 * Finds the containing anonymous class for the given element.
	 * 
	 * @param me the context.
	 * @return containing anonymous class 
	 */
	public static AnonymousClass getContainingAnonymousClass(Commentable me) {
		EObject value = me;
		while (!(value instanceof AnonymousClass) 
				&& !(value instanceof ConcreteClassifier) // Do not jump over other classifiers 
				&& value != null) {
			value = value.eContainer();
		}
		if (!(value instanceof AnonymousClass)) {
			return null;
		}
		return (AnonymousClass) value;
	}
	
	/**
	 * Finds the containing compilation unit for the given element.
	 * 
	 * @param me the given element.
	 * @return containing compilation unit
	 */
	public static CompilationUnit getContainingCompilationUnit(Commentable me) {
		EObject value = me;
		while (!(value instanceof CompilationUnit) && value != null) {
			value = value.eContainer();
		}
		return (CompilationUnit) value;
	}

	/** 
	 * Finds the containing annotation instance for the given element.
	 * 
	 * @param me the given element.
	 * @return containing annotation instance
	 */
	public static AnnotationInstance getContainingAnnotationInstance(Commentable me) {
		EObject value = me;
		while (!(value instanceof AnnotationInstance) && value != null) {
			value = value.eContainer();
		}
		return (AnnotationInstance) value;
	}
	
	public static EList<String> getContainingPackageName(Commentable me) {
		CompilationUnit cu = me.getContainingCompilationUnit();
		if (cu == null) {
			return null;
		}
		
		int idx = cu.getNamespaces().size();
		if (cu.getName() != null) {
			char[] fullName = cu.getName().toCharArray();
			for (int i = 0; i < fullName.length; i++) {
				if (fullName[i] == '$') {
					idx--;
				}
			}
		}
		List<String> packageNameParts = cu.getNamespaces().subList(0, idx);
		BasicEList<String> packageNameList = new BasicEList<String>(
				packageNameParts);
		return ECollections.unmodifiableEList(packageNameList);
	}
	
	public static EList<String> getContainingContainerName(Commentable me) {
		CompilationUnit cu = me.getContainingCompilationUnit();
		if (cu == null) {
			return null;
		}
		return ECollections.unmodifiableEList(cu.getNamespaces());
	}
}
