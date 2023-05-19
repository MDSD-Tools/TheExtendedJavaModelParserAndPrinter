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

import org.eclipse.emf.common.util.EList;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.statements.Block;
import org.emftext.language.java.statements.Return;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypesFactory;

public class LambdaExpressionExtension {
	
	public static boolean doesLambdaMatchFunctionalInterface(LambdaExpression expr, Interface functionalInterface) {
		Method m = functionalInterface.getAbstractMethodOfFunctionalInterface();
		if (m.getParameters().size() == expr.getParameters().getParameters().size()) {
			for (int index = 0; index < m.getParameters().size(); index++) {
				Parameter lambdaParam = expr.getParameters().getParameters().get(index);
				if (!(lambdaParam.getTypeReference() instanceof InferableType)) {
					Parameter methodParameter = m.getParameters().get(index);
					if (!lambdaParam.getTypeReference().getTarget()
							.isSuperType(lambdaParam.getTypeReference().getArrayDimension(),
								methodParameter.getTypeReference().getTarget(),
								methodParameter.getTypeReference())) {
						return false;
					}
				}
			}
			Type methReturn = m.getTypeReference().getTarget();
			Type lambdaReturn = getReturnType(expr, methReturn);
			if (lambdaReturn == null) {
				return true;
			}
			return lambdaReturn.isSuperType(expr.getArrayDimension(), methReturn, m.getTypeReference());
		}
		return false;
	}
	
	public static Type getReturnType(LambdaExpression me, Type potentialReturnType) {
		if (me.getBody() instanceof LambdaExpression) {
			if (!(potentialReturnType instanceof Interface)) {
				return null;
			}
			if (((LambdaExpression) me.getBody())
					.doesLambdaMatchFunctionalInterface((Interface) potentialReturnType)) {
				return potentialReturnType;
			}
		} else if (me.getBody() instanceof Expression) {
			return ((Expression) me.getBody()).getType();
		} else {
			Block b = (Block) me.getBody();
			EList<Return> list = b.getChildrenByType(Return.class);
			if (list.isEmpty() || list.get(0).getReturnValue() != null) {
				return TypesFactory.eINSTANCE.createVoid();
			}
			return list.get(0).getReturnValue().getType();
		}
		return null;
	}
}
