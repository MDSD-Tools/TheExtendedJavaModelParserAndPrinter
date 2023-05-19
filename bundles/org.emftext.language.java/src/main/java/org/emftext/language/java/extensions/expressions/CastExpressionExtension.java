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

import org.emftext.language.java.expressions.CastExpression;
import org.emftext.language.java.expressions.MultiplicativeExpressionChild;

public class CastExpressionExtension {

	/**
	 * Gets the child of an CastExpression.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the CastExpression for which the child is returned.
	 * @return the child.
	 * @deprecated Use getGeneralChild().
	 */
	@Deprecated
	public static MultiplicativeExpressionChild getChild(CastExpression me) {
		if (me.getGeneralChild() instanceof MultiplicativeExpressionChild) {
			return (MultiplicativeExpressionChild) me.getGeneralChild();
		}
		return null;
	}
	
	/**
	 * Sets the child of an CastExpression.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the CastExpression for which the child is set.
	 * @param newChild the new child.
	 * @deprecated Use setGeneralChild(Expression).
	 */
	@Deprecated
	public static void setChild(CastExpression me, MultiplicativeExpressionChild newChild) {
		me.setGeneralChild(newChild);
	}
}
