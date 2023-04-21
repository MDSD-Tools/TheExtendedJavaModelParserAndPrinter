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
 * Standard implementation of <code>IContextDependentURIFragment</code>.
 * 
 * @param <ContainerType> the type of the object that contains the reference which
 * shall be resolved.
 */
public class JavaContextDependentURIFragment<ContainerType extends EObject>
	implements IJavaContextDependentURIFragment {
	private String identifier;
	private ContainerType container;
	private EReference reference;
	private int positionInReference;
	private EObject proxy;
	private IBinding binding;
	
	public JavaContextDependentURIFragment(String identifier, ContainerType container, EReference reference,
			int positionInReference, EObject proxy) {
		this.identifier = identifier;
		this.container = container;
		this.reference = reference;
		this.positionInReference = positionInReference;
		this.proxy = proxy;
	}
	
	public JavaContextDependentURIFragment(String identifier, ContainerType container, EReference reference,
			int positionInReference, EObject proxy, IBinding binding) {
		this(identifier, container, reference, positionInReference, proxy);
		this.binding = binding;
	}
	
	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public ContainerType getContainer() {
		return container;
	}
	
	@Override
	public EReference getReference() {
		return reference;
	}
	
	@Override
	public int getPositionInReference() {
		return positionInReference;
	}
	
	@Override
	public EObject getProxy() {
		return proxy;
	}
	
	@Override
	public IBinding getBinding() {
		return binding;
	}
	
	@Override
	public void setBinding(IBinding newBinding) {
		binding = newBinding;
	}
}
