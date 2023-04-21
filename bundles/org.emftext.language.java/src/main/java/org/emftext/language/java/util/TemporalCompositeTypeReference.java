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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.impl.TypeReferenceImpl;

/**
 * A temporal type reference that combines several type references.
 */
public class TemporalCompositeTypeReference extends TypeReferenceImpl {
	
	private EList<TypeReference> references = new UniqueEList<>();

	public EList<TypeReference> getTypeReferences() {
		return references;
	}
	
	public Type asType() {
		if (references.size() == 0) {
			return null;
		} else if (references.size() == 1) {
			return references.get(0).getTarget();
		} else {
			TemporalCompositeClassifier result = new TemporalCompositeClassifier(references.get(0));
			for (TypeReference ref : references) {
				result.getSuperTypes().add(ref.getTarget());
			}
			return result;
		}
	}
}
