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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.annotations.AnnotationAttributeSetting;
import tools.mdsd.jamopp.model.java.classifiers.Classifier;
import tools.mdsd.jamopp.model.java.commons.NamedElement;
import tools.mdsd.jamopp.model.java.members.InterfaceMethod;
import tools.mdsd.jamopp.model.java.members.MemberContainer;
import tools.mdsd.jamopp.model.java.members.MembersPackage;
import tools.mdsd.jamopp.model.java.members.Method;

/**
 * To resolve annotation attributes.
 */
public class InterfaceMethodDecider extends AbstractDecider {

	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference containingReference) {
		if (referenceContainer instanceof AnnotationAttributeSetting) {
			return true;
		}
		return false;
	}

	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container) {
		if (container instanceof Classifier) {
			return ((Classifier) container).getAllMembers((Classifier) container);
		}
		return null;
	}

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof InterfaceMethod) {
			Method method = (Method) element;
			if (id.equals(method.getName())) {
				NamedElement ne = (NamedElement) element;
				return id.equals(ne.getName());
			}
		}
		return false;
	}

	@Override
	public boolean containsCandidates(EObject container, EReference containingReference) {
		if (container instanceof MemberContainer) {
			if (MembersPackage.Literals.MEMBER_CONTAINER__MEMBERS.equals(containingReference)) {
				return true;
			}
		}
		return false;
	}
}
