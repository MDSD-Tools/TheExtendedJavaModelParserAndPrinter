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

package jamopp.proxy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jdt.core.dom.IBinding;

public interface IJavaContextDependentURIFragmentCollector {
	IJavaContextDependentURIFragmentCollector GLOBAL_INSTANCE = new IJavaContextDependentURIFragmentCollectorImpl();
	
	void setBaseURI(URI uri);
	<ContainerType extends EObject> void registerContextDependentURIFragment(ContainerType container,
			EReference reference, String id, EObject proxyElement, int position);
	<ContainerType extends EObject> void registerContextDependentURIFragment(ContainerType container,
			EReference reference, String id, EObject proxyElement, int position, IBinding binding);
	Map<String, IJavaContextDependentURIFragment> getContextDependentURIFragmentMap();
	
	class IJavaContextDependentURIFragmentCollectorImpl implements IJavaContextDependentURIFragmentCollector {
		private URI altURI = URI.createURI("empty:/nothing.java");
		private URI baseURI;
		private int proxyCounter = 0;
		private Map<String, IJavaContextDependentURIFragment> internalURIFragmentMap = new LinkedHashMap<>();
		
		@Override
		public void setBaseURI(URI uri) {
			baseURI = uri;
		}
		
		@Override
		public <ContainerType extends EObject> void registerContextDependentURIFragment(ContainerType container,
				EReference reference, String id, EObject proxyElement, int position) {
			this.registerContextDependentURIFragment(container, reference, id, proxyElement, position, null);
		}
		
		@Override
		public <ContainerType extends EObject> void registerContextDependentURIFragment(ContainerType container,
				EReference reference, String id, EObject proxyElement, int position, IBinding binding) {
			InternalEObject proxy = (InternalEObject) proxyElement;
			String internalURIFragment = IJavaContextDependentURIFragment.INTERNAL_URI_FRAGMENT_PREFIX
					+ (proxyCounter++) + "_" + id;
			IJavaContextDependentURIFragment uriFragment = new JavaContextDependentURIFragment<ContainerType>(
					id, container, reference, position, proxy, binding);
			proxy.eSetProxyURI((baseURI == null ? altURI : baseURI).appendFragment(internalURIFragment));
			internalURIFragmentMap.put(internalURIFragment, uriFragment);
		}
		
		@Override
		public Map<String, IJavaContextDependentURIFragment> getContextDependentURIFragmentMap() {
			return internalURIFragmentMap;
		}
	}
}
