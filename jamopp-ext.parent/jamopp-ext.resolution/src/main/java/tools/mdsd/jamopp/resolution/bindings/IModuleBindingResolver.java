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

package tools.mdsd.jamopp.resolution.bindings;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.dom.IModuleBinding;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class IModuleBindingResolver extends AbstractBindingResolver<IModuleBinding> {
	protected IModuleBindingResolver(CentralBindingBasedResolver parentResolver) {
		super(parentResolver);
	}

	@Override
	protected EObject resolve(IModuleBinding binding) {
		URI uri = LogicalJavaURIGenerator.getModuleURI(binding.getName());
		Resource modContainer = this.getParentResolver().findModuleResourceInResourceSet(binding.getName());
		if (modContainer == null) {
			modContainer = this.getParentResolver().findResourceInResourceSet(uri);
		}
		if (modContainer == null) {
			if (ParserOptions.PREFER_BINDING_CONVERSION.isTrue()) {
				return convertBindingToModule(binding, uri);
			}
			try {
				modContainer = this.getParentResolver().getResourceSet().getResource(uri, true);
				if (modContainer != null) {
					return (tools.mdsd.jamopp.model.java.containers.Module) modContainer.getContents().get(0);
				}
			} catch (RuntimeException e) {
			}
		} else {
			return (tools.mdsd.jamopp.model.java.containers.Module) modContainer.getContents().get(0);
		}
		return convertBindingToModule(binding, uri);
	}
	
	private tools.mdsd.jamopp.model.java.containers.Module convertBindingToModule(IModuleBinding binding, URI uri) {
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.setBaseURI(uri);
		tools.mdsd.jamopp.model.java.containers.Module result =
				JDTBindingConverterUtility.convertToModule(binding);
		// Logical URI of the module is used to create the corresponding resource.
		Resource modContainer = this.getParentResolver().getResourceSet().createResource(uri);
		modContainer.getContents().add(result);
		// For the registration, the physical URI is used.
		uri = JavaClasspath.get(this.getParentResolver().getResourceSet()).getURIMap().get(uri);
		JavaClasspath.get(this.getParentResolver().getResourceSet()).registerJavaRoot(result, uri);
		return result;
	}
}
