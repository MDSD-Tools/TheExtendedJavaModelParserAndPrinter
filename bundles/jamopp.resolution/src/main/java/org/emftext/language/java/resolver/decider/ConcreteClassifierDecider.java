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
 *   Martin Armbruster
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/
package org.emftext.language.java.resolver.decider;

import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.commons.NamespaceAwareElement;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.imports.ClassifierImport;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.imports.ImportingElement;
import org.emftext.language.java.imports.PackageImport;
import org.emftext.language.java.imports.StaticClassifierImport;
import org.emftext.language.java.imports.StaticMemberImport;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.references.IdentifierReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.references.PackageReference;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.statements.StatementsPackage;
import org.emftext.language.java.types.ClassifierReference;

/**
 * A decider that looks for concrete classifiers.
 */
public class ConcreteClassifierDecider extends AbstractDecider {

	private EList<ConcreteClassifier> innerTypeSuperTypeList = new BasicEList<ConcreteClassifier>();

	private Commentable reference = null;
	
	@Override
	public void reset() {
		innerTypeSuperTypeList.clear();
	}

	/**
	 * @param container
	 * @param containingReference
	 * @return true for statements lists
	 */
	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {

		if (StatementsPackage.Literals.STATEMENT_CONTAINER__STATEMENT.equals(containingReference)) {
			return true;
		}
		if (StatementsPackage.Literals.BLOCK__STATEMENTS.equals(containingReference)
				|| StatementsPackage.Literals.SWITCH_CASE__STATEMENTS.equals(containingReference)) {
			return true;
		}
		return false;
	}

	/**
	 * @param identifier
	 * @param container
	 * @return inner classifiers (also of super classes) and imported classifiers
	 */
	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container) {
		EList<EObject> resultList = new BasicEList<EObject>();

		if (container instanceof IdentifierReference) {
			IdentifierReference p = (IdentifierReference) container;
			String packageName = packageName(p);
			resultList.addAll(JavaClasspath.get(container).getConcreteClassifiers(packageName));
		}

		if (container instanceof Classifier) {
			Classifier classifier = (Classifier) container;

			//classifier itself has first priority
			resultList.add(classifier);

			if (!classifier.eIsProxy()) {
				for (Member member : classifier.getAllMembers(reference)) {
					if (member instanceof ConcreteClassifier) {
						innerTypeSuperTypeList.add((ConcreteClassifier) member);
					}
				}
				if (classifier instanceof ConcreteClassifier) {
					innerTypeSuperTypeList.addAll(
						((ConcreteClassifier) classifier).getAllInnerClassifiers());
					String cqualifiedName = ((ConcreteClassifier) classifier).getQualifiedName();
					if (JavaClasspath.get(container).isPackageRegistered(cqualifiedName
							+ LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR)) {
						cqualifiedName += LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR + identifier;
					} else {
						cqualifiedName += LogicalJavaURIGenerator.PACKAGE_SEPARATOR + identifier;
					}
					ConcreteClassifier cc = JavaClasspath.get(container).getConcreteClassifier(
							cqualifiedName);
					if (cc.eIsProxy()) {
						cc = (ConcreteClassifier) EcoreUtil.resolve(cc, classifier);
						if (!cc.eIsProxy()) {
							resultList.add(cc);
						}
					} else {
						resultList.add(cc);
					}
				}
			}
		}

		if (container instanceof AnonymousClass) {
			for (Member member : ((AnonymousClass) container).getAllMembers(reference)) {
				if (member instanceof ConcreteClassifier) {
					innerTypeSuperTypeList.add((ConcreteClassifier) member);
				}
			}
			ConcreteClassifier superClassifier = ((AnonymousClass) container).getSuperClassifier();
			if (superClassifier != null) {
				innerTypeSuperTypeList.addAll(superClassifier.getAllInnerClassifiers());
			}
		}

		addImportsAndInnerClasses(container, resultList);

		return resultList;
	}

	private String packageName(IdentifierReference p) {
		Reference previous = p.getPrevious();
		String s = "";
		if (previous instanceof IdentifierReference) {
			p = (IdentifierReference) previous;
		} else {
			return s;
		}
		while (p != null) {
			if (p.getTarget() instanceof org.emftext.language.java.containers.Package) {
				s = ((org.emftext.language.java.containers.Package) p.getTarget()).getNamespacesAsString();
				break;
			} else if (p.getTarget() instanceof PackageReference) {
				PackageReference ref = (PackageReference) p.getTarget();
				s = ref.getNamespacesAsString() + ref.getName() + "." + s;
				break;
			} else {
				s = p.getTarget().getName() + "." + s;
			}
			EObject container = p.eContainer();
			if (container instanceof IdentifierReference) {
				p = (IdentifierReference) container;
			} else {
				p = null;
			}
		}
		return s;
	}

	private void addImportsAndInnerClasses(EObject container, EList<EObject> resultList) {
		//1) Inner classifiers of superclasses
		if (container instanceof JavaRoot) {
			resultList.addAll(innerTypeSuperTypeList);
		}
		if (container instanceof ImportingElement) {
			//2) direct classifier imports
			for (Import aImport : ((ImportingElement) container).getImports()) {
				if (aImport instanceof ClassifierImport) {
					resultList.add(((ClassifierImport) aImport).getClassifier());
				} else if (aImport instanceof StaticMemberImport) {
					StaticMemberImport staticMemberImport = (StaticMemberImport) aImport;
					if (!staticMemberImport.getStaticMembers().isEmpty()) {
						// access first element to trigger proxy resolution
						// and avoid ConcurrentModificationException
						staticMemberImport.getStaticMembers().get(0);
					}
					resultList.addAll(staticMemberImport.getStaticMembers());
				} else if (aImport instanceof StaticClassifierImport) {
					resultList.addAll(aImport.getImportedMembers());
				}
			}
		}

		//3) same package
		if (container instanceof JavaRoot) {
			resultList.addAll(((JavaRoot) container).getClassifiersInSamePackage());
		}
		if (container instanceof ImportingElement) {
			//4) other packages
			EList<EObject> packageImports = new BasicEList<EObject>();
			for (Import aImport : ((ImportingElement) container).getImports()) {
				if (aImport instanceof PackageImport) {
					packageImports.addAll(aImport.getImportedClassifiers());
				}
			}
			resultList.addAll(packageImports);
		}
		
		//5) java.lang
		if (container instanceof JavaRoot || container.eContainer() == null) {
			resultList.addAll(JavaClasspath.get(container).getConcreteClassifiers("java.lang"));
		}
	}

	/**
	 * @param id
	 * @param element
	 * @return true if the element is a concrete classifier with the correct name
	 */
	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof ConcreteClassifier) {
			ConcreteClassifier concreteClassifier = (ConcreteClassifier) element;
			if (id.equals(concreteClassifier.getName())) {
				return true;
			}
			if (element.eIsProxy()) {
				String lastSegment = ((InternalEObject) element).eProxyURI().lastSegment();
				if (lastSegment.endsWith("." + id + LogicalJavaURIGenerator.JAVA_FILE_EXTENSION)
						|| lastSegment.equals(id + LogicalJavaURIGenerator.JAVA_FILE_EXTENSION)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param referenceContainer
	 * @param crossReference
	 * @return true for classifier references and references that are not method calls
	 */
	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference crossReference) {
		if (!(referenceContainer instanceof Commentable)) {
			return false;
		}

		if (referenceContainer instanceof MethodCall) {
			return false;
		}

		reference = (Commentable) referenceContainer;
		return (referenceContainer instanceof Reference || referenceContainer instanceof ClassifierReference);
	}
	
	private static ScopedTreeWalker relativeNamespaceResolutionWalker = new ScopedTreeWalker(
			List.of(new ConcreteClassifierDecider(), new TypeParameterDecider()));

	/**
	 * This method assumes that the namespace of the given namespace aware element
	 * is relative to the scope given by the starting point element. That is,
	 * each element of the namespace points to a classifier, where the first element
	 * points to a classifier available in the scope given by the starting point
	 * (i.e., define locally or imported).
	 *
	 * @param nsaElement the namespace aware element.
	 * @param idx index into the namespaces to resolve.
	 * @param startingPoint the starting point element.
	 * @param referenceContainer
	 * @param crossReference
	 * @return the classifier to which the last part of the namespace points, or null, if any
	 *         part of the namespace can not be resolved to a classifier in the given scope
	 */
	public static EObject resolveRelativeNamespace(NamespaceAwareElement nsaElement, int idx,
			EObject startingPoint, EObject referenceContainer, EReference crossReference) {

		if (idx < nsaElement.getNamespaces().size()) {
			String identifier = nsaElement.getNamespaces().get(idx);
			EObject target = null;
			if (idx == 0) {
				target = relativeNamespaceResolutionWalker.walk(startingPoint,
						identifier, referenceContainer, crossReference);
			} else {
				for (ConcreteClassifier cand : ((ConcreteClassifier) startingPoint).getAllInnerClassifiers()) {
					if (identifier.equals(cand.getName())) {
						target = cand;
						break;
					}
				}
			}

			if (target != null) {
				if (target.eIsProxy()) {
					target = EcoreUtil.resolve(target, referenceContainer);
				}
				return resolveRelativeNamespace(nsaElement, idx + 1, target, referenceContainer, crossReference);
			} else {
				return null;
			}
		}

		return startingPoint;
	}
}
