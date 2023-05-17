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

package jamopp.resolution.bindings;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.dom.IBinding;

abstract class AbstractBindingResolver<T extends IBinding> {
	private CentralBindingBasedResolver resolver;
	
	protected AbstractBindingResolver(CentralBindingBasedResolver parentResolver) {
		resolver = parentResolver;
	}
	
	protected CentralBindingBasedResolver getParentResolver() {
		return resolver;
	}
	
	protected abstract EObject resolve(T binding);
}
