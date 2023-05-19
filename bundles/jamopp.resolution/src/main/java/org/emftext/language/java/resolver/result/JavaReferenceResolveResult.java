/*******************************************************************************
 * Copyright (c) 2006-2014
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
 *   Martin Armbruster
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/
package org.emftext.language.java.resolver.result;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

/**
 * A basic implementation of the
 * org.emftext.language.java.resource.java.IJavaReferenceResolveResult interface
 * that collects mappings in a list.
 * 
 * @param <ReferenceType> the type of the references that can be contained in this
 * result.
 */
public class JavaReferenceResolveResult<ReferenceType extends EObject> implements IJavaReferenceResolveResult<ReferenceType> {
	private Collection<IJavaReferenceMapping<ReferenceType>> mappings;
	private String errorMessage;
	
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public Collection<IJavaReferenceMapping<ReferenceType>> getMappings() {
		return mappings;
	}
	
	@Override
	public boolean wasResolved() {
		return mappings != null;
	}
	
	@Override
	public boolean wasResolvedMultiple() {
		return mappings != null && mappings.size() > 1;
	}
	
	@Override
	public boolean wasResolvedUniquely() {
		return mappings != null && mappings.size() == 1;
	}
	
	@Override
	public void setErrorMessage(String message) {
		errorMessage = message;
	}
	
	@Override
	public void addMapping(String identifier, ReferenceType target) {
		if (target == null) {
			throw new IllegalArgumentException("Mapping references to null is only allowed for fuzzy resolution.");
		}
		if (mappings == null) {
			mappings = new ArrayList<IJavaReferenceMapping<ReferenceType>>(1);
		}
		mappings.add(new JavaElementMapping<ReferenceType>(identifier, target, null));
		errorMessage = null;
	}
}
