/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
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

package org.emftext.language.java.extensions.arrays;

import org.emftext.language.java.arrays.ArrayTypeable;
import org.emftext.language.java.parameters.VariableLengthParameter;

public class ArrayTypeableExtension {
	
	public static long getArrayDimension(ArrayTypeable me) {
		long size = me.getArrayDimensionsBefore().size() + me.getArrayDimensionsAfter().size();
		if (me instanceof VariableLengthParameter
				|| me.eContainer() instanceof VariableLengthParameter) {
			size++;
		}
		return size;
	}
}
