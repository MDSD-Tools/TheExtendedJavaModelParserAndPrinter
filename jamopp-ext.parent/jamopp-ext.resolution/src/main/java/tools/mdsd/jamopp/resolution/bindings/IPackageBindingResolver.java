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
import org.eclipse.jdt.core.dom.IPackageBinding;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class IPackageBindingResolver extends AbstractBindingResolver<IPackageBinding> {
	protected IPackageBindingResolver(CentralBindingBasedResolver parentResolver) {
		super(parentResolver);
	}

	@Override
	protected EObject resolve(IPackageBinding binding) {
		URI uri = LogicalJavaURIGenerator.getPackageURI(binding.getName());
		Resource packContainer = this.getParentResolver().findPackageResourceInResourceSet(binding.getName());
		if (packContainer == null) {
			packContainer = this.getParentResolver().findResourceInResourceSet(uri);
		}
		if (packContainer == null) {
			if (ParserOptions.PREFER_BINDING_CONVERSION.isTrue()) {
				return convertBindingToPackage(binding, uri);
			}
			try {
				packContainer = this.getParentResolver().getResourceSet().getResource(uri, true);
				if (packContainer != null) {
					return (tools.mdsd.jamopp.model.java.containers.Package) packContainer.getContents().get(0);
				}
			} catch (RuntimeException e) {
			}
		} else if (!packContainer.getContents().isEmpty()) {
			return (tools.mdsd.jamopp.model.java.containers.Package) packContainer.getContents().get(0);
		}
		return convertBindingToPackage(binding, uri);
	}
	
	private tools.mdsd.jamopp.model.java.containers.Package convertBindingToPackage(IPackageBinding binding, URI uri) {
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.setBaseURI(uri);
		tools.mdsd.jamopp.model.java.containers.Package result = JDTBindingConverterUtility.convertToPackage(binding);
		// The logical URI is used to create the corresponding resource.
		Resource packContainer = this.getParentResolver().getResourceSet().createResource(uri);
		packContainer.getContents().add(result);
		// For the registration, the physical URI is used.
		uri = JavaClasspath.get(this.getParentResolver().getResourceSet()).getURIMap().get(uri);
		JavaClasspath.get(this.getParentResolver().getResourceSet()).registerJavaRoot(result, uri);
		return result;
	}
}
