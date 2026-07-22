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
package tools.mdsd.jamopp.resolution.resolver.decider;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.classifiers.ClassifiersPackage;
import tools.mdsd.jamopp.model.java.classifiers.Enumeration;
import tools.mdsd.jamopp.model.java.commons.NamedElement;
import tools.mdsd.jamopp.model.java.expressions.AssignmentExpression;
import tools.mdsd.jamopp.model.java.imports.ClassifierImport;
import tools.mdsd.jamopp.model.java.imports.Import;
import tools.mdsd.jamopp.model.java.imports.ImportingElement;
import tools.mdsd.jamopp.model.java.imports.StaticClassifierImport;
import tools.mdsd.jamopp.model.java.imports.StaticMemberImport;
import tools.mdsd.jamopp.model.java.members.EnumConstant;
import tools.mdsd.jamopp.model.java.modifiers.AnnotableAndModifiable;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.statements.StatementsPackage;
import tools.mdsd.jamopp.model.java.statements.Switch;
import tools.mdsd.jamopp.model.java.statements.SwitchCase;
import tools.mdsd.jamopp.model.java.types.Type;
import tools.mdsd.jamopp.model.java.util.TemporalCompositeClassifier;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;

/**
 * A decider that looks for enumeration constants.
 */
public class EnumConstantDecider extends AbstractDecider {

	private EObject reference = null;

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof EnumConstant) {
			NamedElement ne = (NamedElement) element;
			return id.equals(ne.getName());
		}
		return false;
	}

	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container) {
		if (container instanceof Switch
				&& ((reference.eContainmentFeature().equals(StatementsPackage.Literals.CONDITIONAL__CONDITION)
					&& reference.eContainer() instanceof SwitchCase)
				|| ((reference.eContainmentFeature().equals(
						StatementsPackage.Literals.NORMAL_SWITCH_CASE__ADDITIONAL_CONDITIONS)
					|| reference.eContainmentFeature().equals(
							StatementsPackage.Literals.NORMAL_SWITCH_RULE__ADDITIONAL_CONDITIONS))
					&& reference.eContainer() instanceof SwitchCase))) {
			Switch aSwitch = (Switch) container;
			Type variableType = aSwitch.getVariable().getType();
			if (variableType instanceof Enumeration) {
				return ((Enumeration) variableType).getConstants();
			}
			if (variableType instanceof TemporalCompositeClassifier) {
				for (EObject superType : ((TemporalCompositeClassifier) variableType).getSuperTypes()) {
					if (superType instanceof Enumeration) {
						return ((Enumeration) superType).getConstants();
					}
				}
			}
		}
		if (container instanceof AssignmentExpression) {
			AssignmentExpression assignmentExpression = (AssignmentExpression) container;
			Type assignmentExpressionType = assignmentExpression.getType();
			if (assignmentExpressionType instanceof Enumeration) {
				return ((Enumeration) assignmentExpressionType).getConstants();
			}
		}
		if (container instanceof LocalVariable) {
			LocalVariable localVariable = (LocalVariable) container;
			Type assignmentExpressionType = localVariable.getTypeReference().getTarget();
			if (assignmentExpressionType instanceof Enumeration) {
				return ((Enumeration) assignmentExpressionType).getConstants();
			}
		}

		EList<EObject> resultList = addImports(container);

		return resultList;
	}

	private EList<EObject> addImports(EObject container) {
		if (container instanceof ImportingElement) {
			EList<EObject> resultList = new BasicEList<EObject>();
			for (Import aImport : ((ImportingElement) container).getImports()) {
				if (aImport instanceof StaticMemberImport) {
					resultList.addAll(((StaticMemberImport) aImport).getStaticMembers());
				} else if (aImport instanceof StaticClassifierImport) {
					resultList.addAll(aImport.getImportedMembers());
				} else if (aImport instanceof ClassifierImport) {
					for (EObject member : ((ClassifierImport) aImport).getClassifier().getMembers()) {
						if (member instanceof AnnotableAndModifiable) {
							if (((AnnotableAndModifiable) member).isStatic()) {
								resultList.add(member);
							}
						}
					}
				}
			}
			return resultList;
		}
		return null;
	}

	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {
		if (ClassifiersPackage.Literals.ENUMERATION__CONSTANTS.equals(containingReference)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference containingReference) {
		reference = referenceContainer;
		return referenceContainer instanceof Reference && !(referenceContainer instanceof MethodCall);
	}
}
