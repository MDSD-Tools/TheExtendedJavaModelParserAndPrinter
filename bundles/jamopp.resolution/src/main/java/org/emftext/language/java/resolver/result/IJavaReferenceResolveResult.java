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
 * The result of a single attempt to resolve an identifier. The result can either
 * be successful (identifier was resolved to one or more objects) or failed
 * (identifier was not resolved). In the case of failure, the result provides an
 * error message.
 * This interface must not be implemented by clients.
 * 
 * @param <ReferenceType> the type of the references that can be contained in this
 * result.
 */
public interface IJavaReferenceResolveResult<ReferenceType extends EObject> {
	
	/**
	 * Returns the error message that describes what went wrong while resolving a
	 * reference.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage();
	
	/**
	 * Sets the error message that describes what went wrong while resolving a
	 * reference. If a mapping for the reference was already found (i.e., addMapping()
	 * was called before), the call to this method is ignored. If addMapping() is
	 * called afterwards, the error message is also discarded.
	 * 
	 * @param message the error that prevented resolving the reference.
	 */
	public void setErrorMessage(String message);
	
	/**
	 * Adds a mapping from the given identifier to the given object. Adding such a
	 * mapping means that the identifier was resolved to reference the target object.
	 * Previous errors as well as future ones will be discarded. Once a mapping is
	 * found, resolve errors have no meaning anymore.
	 * The target must not be null and implementations of this method can throw an
	 * IllegalArgumentException if this rule is violated.
	 * 
	 * @param identifier the given identifier.
	 * @param target the resolved target object for the identifier.
	 */
	public void addMapping(String identifier, ReferenceType target);
	
	/**
	 * Indicates the type of the result. Depending on the type of the result, different
	 * information is available (e.g., the error message is only set if the resolve
	 * operation failed).
	 * 
	 * @return true if the reference was successfully resolved.
	 */
	public boolean wasResolved();
	
	/**
	 * Indicates the type of the result. Depending on the type of the result, different
	 * information is available (e.g., the unique mapping is only set if the resolve
	 * operation returned a unique result).
	 * 
	 * @return true if the reference was resolved to exactly one target object.
	 */
	public boolean wasResolvedUniquely();
	
	/**
	 * Indicates the type of the result. Depending on the type of the result, different
	 * information is available (e.g., the multiple mappings are only set if the
	 * resolve operation returned multiple result).
	 * 
	 * @return true the reference was resolved to more than one target object.
	 */
	public boolean wasResolvedMultiple();
	
	/**
	 * Returns all mappings that were found while resolving an identifier.
	 * 
	 * @return the mappings.
	 */
	public Collection<IJavaReferenceMapping<ReferenceType>> getMappings();
}
