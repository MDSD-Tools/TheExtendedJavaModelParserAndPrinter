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

package tools.mdsd.jamopp.model.java.extensions.expressions;

import org.eclipse.emf.ecore.EObject;

import tools.mdsd.jamopp.model.java.expressions.AssignmentExpression;
import tools.mdsd.jamopp.model.java.expressions.MethodReferenceExpression;
import tools.mdsd.jamopp.model.java.extensions.types.TypeReferenceExtension;
import tools.mdsd.jamopp.model.java.members.AdditionalField;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.Method;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.statements.Return;
import tools.mdsd.jamopp.model.java.types.Type;
import tools.mdsd.jamopp.model.java.types.TypeReference;
import tools.mdsd.jamopp.model.java.util.TemporalCompositeTypeReference;
import tools.mdsd.jamopp.model.java.util.TemporalUnknownType;
import tools.mdsd.jamopp.model.java.variables.AdditionalLocalVariable;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;

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
