/*******************************************************************************
 * Copyright (c) 2021, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package org.emftext.language.java.extensions.expressions;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.expressions.AssignmentExpression;
import org.emftext.language.java.expressions.MethodReferenceExpression;
import org.emftext.language.java.extensions.types.TypeReferenceExtension;
import org.emftext.language.java.members.AdditionalField;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.statements.Return;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.util.TemporalCompositeTypeReference;
import org.emftext.language.java.util.TemporalUnknownType;
import org.emftext.language.java.variables.AdditionalLocalVariable;
import org.emftext.language.java.variables.LocalVariable;

public class MethodReferenceExpressionExtension {
	public static Type getTargetType(MethodReferenceExpression me) {
		TypeReference ref = getTargetTypeReference(me);
		if (ref instanceof TemporalCompositeTypeReference) {
			return ((TemporalCompositeTypeReference) ref).asType();
		}
		return ref == null ? null : ref.getTarget();
	}
	
	public static TypeReference getTargetTypeReference(MethodReferenceExpression me) {
		TypeReference targetType = null;
		EObject parentContainer = me;
		while (!(parentContainer.eContainer() instanceof MethodCall
				|| parentContainer.eContainer() instanceof LocalVariable
				|| parentContainer.eContainer() instanceof AdditionalLocalVariable
				|| parentContainer.eContainer() instanceof AssignmentExpression
				|| parentContainer.eContainer() instanceof Return
				|| parentContainer.eContainer() instanceof Field
				|| parentContainer.eContainer() instanceof AdditionalField)) {
			parentContainer = parentContainer.eContainer();
		}
		if (parentContainer.eContainer() instanceof MethodCall) {
			MethodCall call = (MethodCall) parentContainer.eContainer();
			Method m = (Method) call.getTarget();
			if (!m.eIsProxy()) {
				targetType = m.getParameters().get(
					call.getArguments().indexOf(parentContainer))
					.getTypeReference();
			}
		} else if (parentContainer.eContainer() instanceof LocalVariable) {
			targetType = ((LocalVariable) parentContainer.eContainer()).getTypeReference();
		} else if (parentContainer.eContainer() instanceof AdditionalLocalVariable) {
			targetType = ((AdditionalLocalVariable) parentContainer.eContainer()).getTypeReference();
		} else if (parentContainer.eContainer() instanceof Field) {
			targetType = ((Field) parentContainer.eContainer()).getTypeReference();
		} else if (parentContainer.eContainer() instanceof AdditionalField) {
			targetType = ((AdditionalField) parentContainer.eContainer().eContainer()).getTypeReference();
		} else if (parentContainer.eContainer() instanceof AssignmentExpression) {
			AssignmentExpression assExpr = (AssignmentExpression) parentContainer.eContainer();
			targetType = assExpr.getChild().getOneTypeReference(false);
		} else if (parentContainer.eContainer() instanceof Return) {
			while (!(parentContainer instanceof Method)) {
				parentContainer = parentContainer.eContainer();
			}
			targetType = ((Method) parentContainer).getTypeReference();
		}
		if (targetType == null || targetType.eIsProxy()) {
			targetType = TypeReferenceExtension.convertToTypeReference(new TemporalUnknownType(me));
		}
		return targetType;
	}
}
