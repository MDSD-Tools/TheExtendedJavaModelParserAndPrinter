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

package org.emftext.language.java.extensions.generics;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.emftext.language.java.generics.ExtendsTypeArgument;
import org.emftext.language.java.types.TypeReference;

public class ExtendsTypeArgumentExtension {
	
	/**
	 * Returns the upper bound of an ExtendsTypeArgument wrapped in a list.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the ExtendsTypeArgument which upper bound will be wrapped in a list.
	 * @return the list wrapping the upper bound.
	 * @deprecated Use getExtendType() to receive the upper bound directly.
	 */
	@Deprecated
	public static EList<TypeReference> getExtendTypes(ExtendsTypeArgument me) {
		EList<TypeReference> result = new BasicEList<>();
		result.add(me.getExtendType());
		return result;
	}
}
