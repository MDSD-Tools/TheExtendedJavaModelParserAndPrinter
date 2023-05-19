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
package org.emftext.language.java.resolver;

import java.util.HashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.resolver.result.IJavaElementMapping;
import org.emftext.language.java.resolver.result.IJavaReferenceMapping;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;
import org.emftext.language.java.resolver.result.IJavaURIMapping;
import org.emftext.language.java.resolver.result.JavaReferenceResolveResult;

import jamopp.proxy.IJavaContextDependentURIFragment;
import jamopp.resolution.bindings.CentralBindingBasedResolver;

/**
 * A central reference resolver that is capable of resolving references for IJavaContextDependentURIFragments.
 */
public class CentralReferenceResolver {
	public static final CentralReferenceResolver GLOBAL_INSTANCE = new CentralReferenceResolver();
	private IJavaReferenceResolver<EObject, EObject> resolver = new JavaReferenceResolverSwitch();
	private HashSet<IJavaContextDependentURIFragment> resolving = new HashSet<>();
	private CentralBindingBasedResolver bindingResolver;
	
	public void setBindingBasedResolver(CentralBindingBasedResolver resolver) {
		bindingResolver = resolver;
	}
	
	public IJavaReferenceResolveResult<EObject> resolve(IJavaContextDependentURIFragment context) {
		if (resolving.contains(context)) {
			return null;
		}
		resolving.add(context);
		IJavaReferenceResolveResult<EObject> result = new JavaReferenceResolveResult<>();
		// Set an initial default error message.
		result.setErrorMessage(getStdErrorMessage(context));
		
		// Do the actual resolving.
		if (bindingResolver != null) {
			EObject resolved = bindingResolver.resolve(context.getBinding());
			if (resolved == null) {
				resolving.remove(context);
				return null;
			}
			result.addMapping(context.getIdentifier(), resolved);
		} else {
			resolver.resolve(context.getIdentifier(), context.getContainer(), context.getReference(),
					context.getPositionInReference(), result);
		}
		
		// EMFText allows proxies to resolve to multiple objects. The first one is
		// returned, the others are added here to the reference.
		if (result.wasResolvedMultiple()) {
			handleMultipleResults(context, result);
		}
		resolving.remove(context);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void handleMultipleResults(IJavaContextDependentURIFragment context,
			IJavaReferenceResolveResult<EObject> result) {
		EList<EObject> list = null;
		Object temp = context.getContainer().eGet(context.getReference());
		if (temp instanceof EList<?>) {
			list = (EList<EObject>) temp;
		}
		
		boolean first = true;
		for (IJavaReferenceMapping<EObject> mapping : result.getMappings()) {
			if (first) {
				first = false;
			} else if (list != null) {
				addResultToList(mapping, context.getProxy(), list);
			} else {
				throw new IllegalArgumentException(context.getContainer().eClass().getName() + "."
						+ context.getReference().getName()
						+ " has multiplicity 1, but was resolved to multiple elements.", null);
			}
		}
	}
	
	private void addResultToList(IJavaReferenceMapping<? extends EObject> mapping, EObject proxy, EList<EObject> list) {
		EObject target = null;
		int proxyPosition = list.indexOf(proxy);
		
		if (mapping instanceof IJavaElementMapping<?>) {
			target = ((IJavaElementMapping<? extends EObject>) mapping).getTarget();
			// TODO
		// } else if (mapping instanceof IJavaURIMapping) {
		// 	target = EcoreUtil.copy(proxy);
		// 	URI uri = ((IJavaURIMapping) mapping).getTarget();
		// 	((InternalEObject) target).eSetProxyURI(uri);
		}
		try {
			// If target is in another proxy and list is "unique", add() will try to resolve
			// the new proxy to check for uniqueness. There seems to be no way to avoid that.
			// Until now, this does not cause any problems.
			if (proxyPosition + 1 == list.size()) {
				list.add(target);
			} else {
				list.add(proxyPosition + 1, target);
			}
		} catch (Exception e1) {
		}
	}
	
	private String getStdErrorMessage(IJavaContextDependentURIFragment context) {
		String typeName = context.getReference().getEType().getName();
		String msg = typeName + " '" + context.getIdentifier() + "' not declared";
		return msg;
	}
}
