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
package tools.mdsd.jamopp.model.java.extensions.containers;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;

import tools.mdsd.jamopp.model.java.classifiers.Annotation;
import tools.mdsd.jamopp.model.java.classifiers.Class;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.classifiers.Enumeration;
import tools.mdsd.jamopp.model.java.classifiers.Interface;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.imports.ClassifierImport;
import tools.mdsd.jamopp.model.java.imports.ImportsFactory;
import tools.mdsd.jamopp.model.java.imports.PackageImport;

public class CompilationUnitExtension {
	
	/**
	 * Returns the first {@link ConcreteClassifier} that is contained in this
	 * {@link CompilationUnit} and which has the given name.
	 *
	 * @param me the context.
	 * @param name
	 *            the name of the classifier to search for
	 * @return the classifier if one is found, otherwise <code>null</code>
	 */
	public static ConcreteClassifier getContainedClassifier(CompilationUnit me, String name) {
		if (name == null) {
			return null;
		}
		
		for (ConcreteClassifier candidate : me.getClassifiers()) {
			if (name.equals(candidate.getName())) {
				return candidate;
			}
		}
		return null;
	}
	
	/**
	 * @param me the context.
	 * @return all classes in the same package imports
	 */
	public static EList<ConcreteClassifier> getClassifiersInSamePackage(CompilationUnit me) {
		EList<ConcreteClassifier> defaultImportList = new UniqueEList<ConcreteClassifier>();
		
		String packageName = me.getNamespacesAsString();

		//locally defined in this container
		defaultImportList.addAll(me.getClassifiers());
		
		defaultImportList.addAll(me.getConcreteClassifiers(packageName, "*"));

		return defaultImportList;
	}
	
	/**
	 * Returns the class that is directly contained in the compilation unit (if
	 * exactly one exists). If the {@link CompilationUnit} contains multiple
	 * classifiers or if the contained classifier is not a {@link Class},
	 * <code>null</code> is returned.
	 *
	 * @param me the compilation unit.
	 * @return the class directly contained in the compilation unit (if there is
	 *         exactly one contained classifier that is of type {@link Class})
	 */
	public static tools.mdsd.jamopp.model.java.classifiers.Class getContainedClass(CompilationUnit me) {
		List<ConcreteClassifier> classifiers = me.getClassifiers();
		if (classifiers.size() != 1) {
			return null;
		}
		
		ConcreteClassifier first = classifiers.get(0);
		if (first instanceof tools.mdsd.jamopp.model.java.classifiers.Class) {
			return (tools.mdsd.jamopp.model.java.classifiers.Class) first;
		}
		return null;
	}
	
	/**
	 * Returns the interface that is directly contained in the compilation unit
	 * (if exactly one exists). If the {@link CompilationUnit} contains multiple
	 * classifiers or if the contained classifier is not an {@link Interface},
	 * <code>null</code> is returned.
	 * 
	 * @param me the compilation unit.
	 * @return the interface directly contained in the compilation unit (if
	 *         there is exactly one contained classifier that is of type
	 *         {@link Interface})
	 */
	public static Interface getContainedInterface(CompilationUnit me) {
		List<ConcreteClassifier> classifiers = me.getClassifiers();
		if (classifiers.size() != 1) {
			return null;
		}
		
		ConcreteClassifier first = classifiers.get(0);
		if (first instanceof Interface) {
			return (Interface) first;
		}
		return null;
	}

	/**
	 * Returns the annotation that is directly contained in the compilation unit
	 * (if exactly one exists). If the {@link CompilationUnit} contains multiple
	 * classifiers or if the contained classifier is not an {@link Annotation},
	 * <code>null</code> is returned.
	 * 
	 * @param me the compilation unit.
	 * @return the annotation directly contained in the compilation unit (if
	 *         there is exactly one contained classifier that is of type
	 *         {@link Annotation})
	 */
	public static Annotation getContainedAnnotation(CompilationUnit me) {
		List<ConcreteClassifier> classifiers = me.getClassifiers();
		if (classifiers.size() != 1) {
			return null;
		}
		
		ConcreteClassifier first = classifiers.get(0);
		if (first instanceof Annotation) {
			return (Annotation) first;
		}
		return null;
	}
	
	/**
	 * Returns the enumeration that is directly contained in the compilation
	 * unit (if exactly one exists). If the {@link CompilationUnit} contains
	 * multiple classifiers or if the contained classifier is not an
	 * {@link Enumeration}, <code>null</code> is returned.
	 * 
	 * @param me the compilation unit.
	 * @return the enumeration directly contained in the compilation unit (if
	 *         there is exactly one contained classifier that is of type
	 *         {@link Enumeration})
	 */
	public static Enumeration getContainedEnumeration(CompilationUnit me) {
		List<ConcreteClassifier> classifiers = me.getClassifiers();
		if (classifiers.size() != 1) {
			return null;
		}
		
		ConcreteClassifier first = classifiers.get(0);
		if (first instanceof Enumeration) {
			return (Enumeration) first;
		}
		return null;
	}
	
	/**
	 * Adds an import of the given class to this compilation unit.
	 * 
	 * @param me the compilation unit.
	 * @param nameOfClassToImport name of the class to import.
	 */
	public static void addImport(CompilationUnit me, String nameOfClassToImport) {
		ClassifierImport importElement = ImportsFactory.eINSTANCE.createClassifierImport();
		ConcreteClassifier classToImport = me.getConcreteClassifier(nameOfClassToImport);
		importElement.setClassifier(classToImport);
		importElement.getNamespaces().addAll(classToImport.getContainingCompilationUnit().getNamespaces());
		me.getImports().add(importElement);
	}
	
	/**
	 * Adds an import of the given package to this compilation unit.
	 * 
	 * @param me the compilation unit.
	 * @param packageName name of the package to import.
	 */
	public static void addPackageImport(CompilationUnit me, String packageName) {
		PackageImport nsImport = ImportsFactory.eINSTANCE.createPackageImport();
		for (String name : packageName.split("\\.")) {
			nsImport.getNamespaces().add(name);
		}
		me.getImports().add(nsImport);
	}
}
