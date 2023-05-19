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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.dom.IVariableBinding;

import tools.mdsd.jamopp.model.java.classifiers.Enumeration;
import tools.mdsd.jamopp.model.java.members.EnumConstant;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.members.MemberContainer;
import tools.mdsd.jamopp.model.java.members.Method;
import tools.mdsd.jamopp.model.java.parameters.Parametrizable;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;

class IVariableBindingResolver extends AbstractBindingResolver<IVariableBinding> {
	protected IVariableBindingResolver(CentralBindingBasedResolver parentResolver) {
		super(parentResolver);
	}

	@Override
	protected EObject resolve(IVariableBinding binding) {
		if (binding.isEnumConstant()) {
			Enumeration e = (Enumeration) this.getParentResolver().resolve(binding.getDeclaringClass());
			if (e != null) {
				for (EnumConstant enCons : e.getConstants()) {
					if (enCons.getName().equals(binding.getName())) {
						return enCons;
					}
				}
			}
		} else if (binding.isField()) {
			EObject container = this.getParentResolver().resolve(binding.getDeclaringClass());
			if (container instanceof MemberContainer) {
				for (Member mem : ((MemberContainer) container).getMembers()) {
					if (mem instanceof Field && ((Field) mem).getName().equals(binding.getName())) {
						return mem;
					}
				}
			}
		} else if (binding.isParameter()) {
			var methodBinding = binding.getDeclaringMethod();
			if (methodBinding != null) {
				EObject container = this.getParentResolver().resolve(methodBinding.getMethodDeclaration());
				if (container != null && container instanceof Parametrizable) {
					Parametrizable m = (Parametrizable) container;
					for (var param : m.getParameters()) {
						if (param.getName().equals(binding.getName())) {
							return param;
						}
					}
					for (var localVar : m.getChildrenByType(LocalVariable.class)) {
						if (localVar.getName().equals(binding.getName())) {
							return localVar;
						}
					}
				}
			}
		}
		return null;
	}
}
