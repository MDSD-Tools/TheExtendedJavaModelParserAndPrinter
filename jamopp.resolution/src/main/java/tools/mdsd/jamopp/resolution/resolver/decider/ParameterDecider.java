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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.commons.NamedElement;
import tools.mdsd.jamopp.model.java.expressions.LambdaExpression;
import tools.mdsd.jamopp.model.java.parameters.Parameter;
import tools.mdsd.jamopp.model.java.parameters.ParametersPackage;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.statements.StatementsPackage;

/**
 * A decider that looks for parameters.
 */
public class ParameterDecider extends AbstractDecider {

	@Override
	public boolean continueAfterReference() {
		return false;
	}
	
	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container) {
		if (container instanceof LambdaExpression) {
			return ((LambdaExpression) container).getParameters().getParameters();
		}
		return null;
	}

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof Parameter) {
			NamedElement ne = (NamedElement) element;
			return id.equals(ne.getName());
		}
		return false;
	}

	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {
		if (ParametersPackage.Literals.PARAMETRIZABLE__PARAMETERS.equals(containingReference)) {
			return true;
		}
		if (StatementsPackage.Literals.CATCH_BLOCK__PARAMETER.equals(containingReference)) {
			return true;
		}
		if (StatementsPackage.Literals.FOR_EACH_LOOP__NEXT.equals(containingReference)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean walkInto(EObject element) {
		return false;
	}

	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference containingReference) {
		return referenceContainer instanceof Reference && !(referenceContainer instanceof MethodCall);
	}
}
