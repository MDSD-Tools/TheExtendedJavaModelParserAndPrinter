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
package tools.mdsd.jamopp.resolution.resolver.decider;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.commons.NamedElement;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.references.ReflectiveClassReference;
import tools.mdsd.jamopp.model.java.references.SelfReference;
import tools.mdsd.jamopp.model.java.statements.ForLoop;
import tools.mdsd.jamopp.model.java.statements.LocalVariableStatement;
import tools.mdsd.jamopp.model.java.statements.StatementsPackage;
import tools.mdsd.jamopp.model.java.statements.Switch;
import tools.mdsd.jamopp.model.java.statements.SwitchCase;
import tools.mdsd.jamopp.model.java.variables.AdditionalLocalVariable;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;
import tools.mdsd.jamopp.model.java.variables.VariablesPackage;

/**
 * A decider that looks for local variable declarations.
 */
public class LocalVariableDecider extends AbstractDecider {

	@Override
	public boolean continueAfterReference() {
		return false;
	}

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof LocalVariable || element instanceof AdditionalLocalVariable) {
			NamedElement ne = (NamedElement) element;
			return id.equals(ne.getName());
		}
		return false;
	}

	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {
		if (StatementsPackage.Literals.LOCAL_VARIABLE_STATEMENT__VARIABLE.equals(containingReference)) {
			return true;
		}
		if (VariablesPackage.Literals.LOCAL_VARIABLE__ADDITIONAL_LOCAL_VARIABLES.equals(containingReference)) {
			return true;
		}
		if (StatementsPackage.Literals.FOR_LOOP__INIT.equals(containingReference)) {
			return true;
		}
		if (StatementsPackage.Literals.TRY_BLOCK__RESOURCES.equals(containingReference)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean walkInto(EObject element) {
		if (element instanceof LocalVariableStatement) {
			if (StatementsPackage.Literals.STATEMENT_CONTAINER__STATEMENT.equals(element.eContainmentFeature())) {
				return true;
			}
			if (StatementsPackage.Literals.BLOCK__STATEMENTS.equals(element.eContainmentFeature())
					|| StatementsPackage.Literals.SWITCH_CASE__STATEMENTS
					.equals(element.eContainmentFeature())) {
				return true;
			}
		}
		if (element instanceof LocalVariable) {
			if (StatementsPackage.Literals.LOCAL_VARIABLE_STATEMENT__VARIABLE.equals(element.eContainmentFeature())
					|| StatementsPackage.Literals.FOR_LOOP__INIT.equals(element.eContainmentFeature())) {
				return true;
			}
		}
		if (element instanceof ForLoop) {
			return true;
		}
		if (element instanceof Switch) {
			return true;
		}
		if (element instanceof SwitchCase) {
			return true;
		}
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
		return true;
	}
}
