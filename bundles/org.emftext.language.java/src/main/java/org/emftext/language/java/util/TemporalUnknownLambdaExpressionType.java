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

package org.emftext.language.java.util;

import org.emftext.language.java.expressions.LambdaExpression;

/**
 * This class represents the actual type of a LambdaExpression which is temporarily unknown.
 */
public class TemporalUnknownLambdaExpressionType extends TemporalUnknownType {
	public TemporalUnknownLambdaExpressionType(LambdaExpression creator) {
		super(creator);
	}

	public LambdaExpression getLambdaExpression() {
		return (LambdaExpression) getCreator();
	}
}
