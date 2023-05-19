/*******************************************************************************
 * Copyright (c) 2019-2020, Martin Armbruster
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

import org.emftext.language.java.expressions.AssignmentExpressionChild;
import org.emftext.language.java.expressions.ConditionalExpression;

public class ConditionalExpressionExtension {

	/**
	 * Gets the expression of the else case.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the ConditionalExpression for which the else expression is returned.
	 * @return the expression of the else case.
	 * @deprecated Use getGeneralExpressionElse().
	 */
	@Deprecated
	public static AssignmentExpressionChild getExpressionElse(ConditionalExpression me) {
		if (me.getGeneralExpressionElse() instanceof AssignmentExpressionChild) {
			return (AssignmentExpressionChild) me.getGeneralExpressionElse();
		}
		return null;
	}
	
	/**
	 * Sets the expression for the else case.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the ConditionalExpression for which the else case is set.
	 * @param newChild the new child for the else case.
	 * @deprecated Use setGeneralExpressionElse(Expression).
	 */
	@Deprecated
	public static void setExpressionElse(ConditionalExpression me, AssignmentExpressionChild newChild) {
		me.setGeneralExpressionElse(newChild);
	}
}
