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

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

/**
 * An implementation of the ResolveResult interface that delegates all method
 * calls to another ResolveResult. Client may subclass this class to easily create
 * custom ResolveResults.
 * 
 * @param <ReferenceType> the type of the references that can be contained in the wrapped result.
 * @param <T> the type of the references that can be contained in this result.
 */
public class JavaDelegatingResolveResult<ReferenceType extends EObject, T extends ReferenceType>
		implements IJavaReferenceResolveResult<T> {
	private IJavaReferenceResolveResult<ReferenceType> delegate;
	
	public JavaDelegatingResolveResult(IJavaReferenceResolveResult<ReferenceType> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public String getErrorMessage() {
		return delegate.getErrorMessage();
	}
	
	@Override
	public Collection<IJavaReferenceMapping<T>> getMappings() {
		return null;
	}
	
	@Override
	public boolean wasResolved() {
		return delegate.wasResolved();
	}
	
	@Override
	public boolean wasResolvedMultiple() {
		return delegate.wasResolvedMultiple();
	}
	
	@Override
	public boolean wasResolvedUniquely() {
		return delegate.wasResolvedUniquely();
	}
	
	@Override
	public void setErrorMessage(String message) {
		delegate.setErrorMessage(message);
	}
	
	@Override
	public void addMapping(String identifier, T target) {
		delegate.addMapping(identifier, target);
	}
}
