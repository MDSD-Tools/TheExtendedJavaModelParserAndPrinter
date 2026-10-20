/*******************************************************************************
 * Copyright (c) 2020-2023
 * Modelling for Continuous Software Engineering (MCSE) group,
 *     Institute of Information Security and Dependability (KASTEL),
 *     Karlsruhe Institute of Technology (KIT).
 * 
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
package tools.mdsd.jamopp.resolution.resolver.decider;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.classifiers.AnonymousClass;
import tools.mdsd.jamopp.model.java.classifiers.Classifier;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.commons.Commentable;
import tools.mdsd.jamopp.model.java.commons.NamedElement;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.imports.Import;
import tools.mdsd.jamopp.model.java.imports.ImportingElement;
import tools.mdsd.jamopp.model.java.imports.StaticClassifierImport;
import tools.mdsd.jamopp.model.java.imports.StaticMemberImport;
import tools.mdsd.jamopp.model.java.members.AdditionalField;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.members.MembersFactory;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.references.ReflectiveClassReference;
import tools.mdsd.jamopp.model.java.references.SelfReference;
import tools.mdsd.jamopp.model.java.types.ClassifierReference;
import tools.mdsd.jamopp.model.java.types.TypesFactory;

/**
 * A decider that looks for fields declared in a classifier.
 */
public class FieldDecider extends AbstractDecider {

	private Field standardArrayLengthField = null;

	private Reference fieldReference = null;

	public Field getArrayLengthFiled(Commentable objectContext) {
		if (standardArrayLengthField  == null) {
			standardArrayLengthField = MembersFactory.eINSTANCE.createField();
			standardArrayLengthField.setName("length");
			ClassifierReference typeReference = TypesFactory.eINSTANCE.createClassifierReference();
			typeReference.setTarget(objectContext.getLibClass("Integer"));
			standardArrayLengthField.setTypeReference(typeReference);
		}
		return standardArrayLengthField;
	}

	private boolean insideDefiningClassifier = true;
	private boolean isStatic = false;
	
	@Override
	public void reset() {
		insideDefiningClassifier = true;
	}

	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container) {
		EList<EObject> resultList = new BasicEList<EObject>();
		if (container instanceof Classifier) {
			if (container instanceof ConcreteClassifier && insideDefiningClassifier) {
				EList<Member> memberList = ((Classifier) container).getAllMembers(fieldReference);
				for (Member member : memberList) {
					if (member instanceof Field) {
						resultList.add(member);
						resultList.addAll(((Field) member).getAdditionalFields());
					}
				}
				insideDefiningClassifier = false;
				isStatic = ((ConcreteClassifier) container).isStatic();
			} else {
				EList<Member> memberList = ((Classifier) container).getAllMembers(fieldReference);
				for (Member member : memberList) {
					if (member instanceof Field) {
						// If isStatic is true, the defining classifier is static and objects of
						// the defining classifier have no access to non-static fields of the
						// classifiers in which the defining
						// classifier is located. Nevertheless, static fields are included.
						if (!isStatic || ((Field) member).isStatic()) {
							resultList.add(member);
							resultList.addAll(((Field) member).getAdditionalFields());
						}
					}
				}
			}
		}

		if (container instanceof AnonymousClass) {
			resultList.addAll(((AnonymousClass) container).getMembers());

			EList<Member> memberList = ((AnonymousClass) container).getAllMembers(fieldReference);
			for (Member member : memberList) {
				if (member instanceof Field) {
					resultList.add(member);
					resultList.addAll(((Field) member).getAdditionalFields());
				}
			}
			return resultList;
		}

		if (container instanceof CompilationUnit) {
			addImports(container, resultList);
			addArrayLengthField(resultList, (CompilationUnit) container);
		}

		return resultList;
	}

	private void addArrayLengthField(EList<EObject> resultList, Commentable objectContext) {
		//Arrays have the additional member field "length"
		//We always add the field since we do not know if we have an array or not
		resultList.add(getArrayLengthFiled(objectContext));
	}

	private void addImports(EObject container, EList<EObject> resultList) {
		if (container instanceof ImportingElement) {
			for (Import aImport : ((ImportingElement) container).getImports()) {
				if (aImport instanceof StaticMemberImport) {
					resultList.addAll(((StaticMemberImport) aImport).getStaticMembers());
				} else if (aImport instanceof StaticClassifierImport) {
					resultList.addAll(aImport.getImportedMembers());
				}
			}
		}
	}

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof Field || element instanceof AdditionalField) {
			NamedElement ne = (NamedElement) element;
			return id.equals(ne.getName());
		}
		return false;
	}

	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {
		return false;
	}

	@Override
	public boolean walkInto(EObject element) {
		return false;
	}

	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference containingReference) {
		if (referenceContainer instanceof MethodCall) {
			return false;
		}
		if (!(referenceContainer instanceof Reference)) {
			return false;
		}
		Reference reference = (Reference) referenceContainer;
		if (reference.getNext() instanceof ReflectiveClassReference) {
			return false;
		}
		if (reference.getNext() instanceof SelfReference) {
			return false;
		}
		fieldReference = (Reference) referenceContainer;
		return true;
	}
}
