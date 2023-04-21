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
package jamopp.proxy;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * An <code>IContextDependentURIFragment</code> points at an element referenced
 * from another element through an <code>identifier</code>. In contrast to a
 * normal EMF URI fragment (<code>URI.fragment</code>), which is a String that can
 * be resolved to an element within a <code>Resource</code>, the
 * <code>identifier</code> of a <code>IContextDependentURIFragment</code> does not
 * have to be globally unique.
 * <p>
 * An <code>IContextDependentURIFragment</code> is registered for a proxy object for which it defines the URI
 * fragment.
 */
public interface IJavaContextDependentURIFragment {
	/**
	 * A prefix that can be used in a <code>URI.fragment</code> String of a proxy to
	 * indicate the existence of an <code>IContextDependentURIFragment</code>.
	 */
	String INTERNAL_URI_FRAGMENT_PREFIX = "EMFTEXT_INTERNAL_URI_FRAGMENT_";
	
	/**
	 * Returns the proxy object.
	 * 
	 * @return The proxy object.
	 */
	public EObject getProxy();
	
	/**
	 * Returns an identifier identifying the elements at which the proxy object points to.
	 * 
	 * @return the identifier in this context.
	 */
	public String getIdentifier();
	
	/**
	 * Returns the element containing the proxy object.
	 * 
	 * @return The element that references the proxy.
	 */
	public EObject getContainer();
	
	/**
	 * Returns the reference in the container's <code>EClass</code> that contains the proxy object.
	 * 
	 * @return The reference.
	 */
	public EReference getReference();
	
	/**
	 * Returns the position within the reference if the reference contains multiple elements.
	 * 
	 * @return The position or -1 otherwise.
	 */
	public int getPositionInReference();
	
	/**
	 * Returns the binding associated with the proxy element provided by an Eclipse JDT Parser.
	 * 
	 * @return the binding.
	 */
	public IBinding getBinding();
	
	/**
	 * Sets the binding associated with the proxy element.
	 * 
	 * @param newBinding the new binding.
	 */
	public void setBinding(IBinding newBinding);
}
