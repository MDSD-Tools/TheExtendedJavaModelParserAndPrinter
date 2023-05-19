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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.MemberContainer;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.parameters.ReceiverParameter;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.emftext.language.java.types.TypeReference;

class IMethodBindingResolver extends AbstractBindingResolver<IMethodBinding> {
	protected IMethodBindingResolver(CentralBindingBasedResolver parentResolver) {
		super(parentResolver);
	}

	@Override
	protected EObject resolve(IMethodBinding binding) {
		binding = binding.getMethodDeclaration();
		EObject container = this.getParentResolver().resolve(binding.getDeclaringClass());
		if (container instanceof MemberContainer) {
			for (Member mem : ((MemberContainer) container).getMembers()) {
				if (binding.isConstructor() && mem instanceof Constructor
						&& checkConstructor(binding, (Constructor) mem)) {
					return mem;
				} else if (mem instanceof Method && checkMethod((Method) mem, binding)) {
					return mem;
				}
			}
		}
		return null;
	}
	
	private boolean checkMethod(Method meth, IMethodBinding binding) {
		if (meth.getName().equals(binding.getName())) {
			if (meth.getName().equals("clone")) {
				return true;
			}
			int receiveOffset = 0;
			if (binding.getDeclaredReceiverType() != null) {
				receiveOffset = 1;
			}
			if (binding.getParameterTypes().length + receiveOffset == meth.getParameters().size()) {
				if (receiveOffset == 1
					&& !(meth.getParameters().get(0) instanceof ReceiverParameter
					&& convertToTypeName(binding.getDeclaredReceiverType()).equals(
						convertToTypeName(meth.getParameters().get(0).getTypeReference())))) {
					return false;
				}
				if (!convertToTypeName(binding.getReturnType()).equals(
						convertToTypeName(meth.getTypeReference()))) {
					return false;
				}
				for (int i = 0; i < binding.getParameterTypes().length; i++) {
					ITypeBinding currentParamType = binding.getParameterTypes()[i];
					Parameter currentParam = meth.getParameters().get(i + receiveOffset);
					if (!convertToTypeName(currentParamType).equals(
							convertToTypeName(currentParam.getTypeReference()))
							|| currentParamType.getDimensions()
							!= currentParam.getTypeReference().getArrayDimension()) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	private static String convertToTypeName(ITypeBinding binding) {
		if (binding == null) {
			return "";
		}
		if (binding.isTypeVariable()) {
			return binding.getName();
		}
		String qualifiedName;
		if (binding.isMember()) {
			qualifiedName = convertToTypeName(binding.getDeclaringClass()) + "." + binding.getName();
		} else if (binding.isLocal()) {
			return binding.getName();
		} else if (binding.isArray()) {
			return convertToTypeName(binding.getElementType());
		} else {
			qualifiedName = binding.getQualifiedName();
		}
		if (qualifiedName.contains("<")) {
			qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("<"));
		}
		return qualifiedName;
	}
	
	private String convertToTypeName(TypeReference ref) {
		if (ref instanceof ClassifierReference) {
			ClassifierReference convRef = (ClassifierReference) ref;
			Classifier target = convRef.getTarget();
			if (target.eIsProxy()) {
				target = (Classifier) EcoreUtil.resolve(target,
						this.getParentResolver().getResourceSet());
			}
			if (target instanceof ConcreteClassifier) {
				return ((ConcreteClassifier) target).getQualifiedName();
			} else if (target instanceof InferableType) {
				return "var";
			} else {
				return ((TypeParameter) target).getName();
			}
		} else if (ref instanceof NamespaceClassifierReference) {
			NamespaceClassifierReference nRef = (NamespaceClassifierReference) ref;
			if (nRef.getClassifierReferences().size() > 0) {
				return convertToTypeName(nRef.getClassifierReferences().get(
						nRef.getClassifierReferences().size() - 1));
			}
			return LogicalJavaURIGenerator.packageName(nRef);
		} else if (ref instanceof org.emftext.language.java.types.Boolean) {
			return "boolean";
		} else if (ref instanceof org.emftext.language.java.types.Byte) {
			return "byte";
		} else if (ref instanceof org.emftext.language.java.types.Char) {
			return "char";
		} else if (ref instanceof org.emftext.language.java.types.Double) {
			return "double";
		} else if (ref instanceof org.emftext.language.java.types.Float) {
			return "float";
		} else if (ref instanceof org.emftext.language.java.types.Int) {
			return "int";
		} else if (ref instanceof org.emftext.language.java.types.Long) {
			return "long";
		} else if (ref instanceof org.emftext.language.java.types.Short) {
			return "short";
		} else {
			return "void";
		}
	}
	
	private boolean checkConstructor(IMethodBinding binding, Constructor c) {
		if (c.getName().equals(binding.getName())) {
			int receiveOffset = 0;
			if (binding.getDeclaredReceiverType() != null) {
				receiveOffset = 1;
			}
			if (c.getParameters().size() == binding.getParameterTypes().length + receiveOffset) {
				if (receiveOffset == 1
						&& !(c.getParameters().get(0) instanceof ReceiverParameter
						&& convertToTypeName(binding.getDeclaredReceiverType()).equals(
							convertToTypeName(c.getParameters().get(0).getTypeReference())))) {
					return false;
				}
				for (int i = 0; i < binding.getParameterTypes().length; i++) {
					ITypeBinding currentType = binding.getParameterTypes()[i];
					Parameter currentParam = c.getParameters().get(i + receiveOffset);
					if (!convertToTypeName(currentType).equals(
							convertToTypeName(currentParam.getTypeReference()))
							|| currentType.getDimensions()
							!= currentParam.getTypeReference().getArrayDimension()) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
}
