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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.generics.TypeParametrizable;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.MemberContainer;

import jamopp.options.ParserOptions;
import jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class ITypeBindingResolver extends AbstractBindingResolver<ITypeBinding> {
	private Pattern parentNamePattern;
	private Pattern innerNamesPattern;
	
	protected ITypeBindingResolver(CentralBindingBasedResolver parentResolver) {
		super(parentResolver);
		parentNamePattern = Pattern.compile("([a-zA-Z0-9\\.]++)((\\$[0-9]*+([a-zA-z][a-zA-z0-9]*+)?+)++)");
		innerNamesPattern = Pattern.compile("\\$((([0-9]++)([a-zA-Z][a-zA-Z0-9]*+))|([0-9]++)|([a-zA-Z][a-zA-Z0-9]*+))");
	}

	@Override
	protected EObject resolve(ITypeBinding binding) {
		binding = binding.getTypeDeclaration();
		if (binding.isAnonymous() || binding.isLocal()) {
			return findLocalOrAnonymousClass(binding.getBinaryName());
		} else if (binding.isArray()) {
			return this.getParentResolver().resolve(binding.getElementType());
		} else if (binding.isTypeVariable()) {
			TypeParametrizable param = null;
			if (binding.getDeclaringClass() != null) {
				param = (TypeParametrizable) this.getParentResolver().resolve(binding.getDeclaringClass());
			}
			if (param == null || param.getTypeParameters() == null) {
				return null;
			}
			for (TypeParameter p : param.getTypeParameters()) {
				if (p.getName().equals(binding.getName())) {
					return p;
				}
			}
		} else if (binding.isMember()) {
			EObject parent = this.getParentResolver().resolve(binding.getDeclaringClass());
			if (parent != null && !parent.eIsProxy() && parent instanceof MemberContainer) {
				MemberContainer con = (MemberContainer) parent;
				for (Member member : con.getMembers()) {
					if (member instanceof ConcreteClassifier && ((ConcreteClassifier) member)
							.getName().equals(binding.getName())) {
						return member;
					}
				}
			}
		} else if (binding.isTopLevel()) {
			URI baseURI = LogicalJavaURIGenerator.getClassifierURI(binding.getQualifiedName()).trimFragment();
			Resource potRes = this.getParentResolver().getResourceSet().getResource(baseURI, false);
			if (potRes == null && ParserOptions.PREFER_BINDING_CONVERSION.isTrue()) {
				return convertBinding(binding, baseURI);
			}
			ConcreteClassifier classifier =	JavaClasspath.get(this.getParentResolver().getResourceSet())
					.getConcreteClassifier(binding.getQualifiedName());
			classifier = (ConcreteClassifier) EcoreUtil.resolve(classifier,
					this.getParentResolver().getResourceSet());
			if (classifier == null || classifier.eIsProxy()) {
				classifier = convertBinding(binding, baseURI);
			}
			return classifier;
		}
		return null;
	}
	
	private ConcreteClassifier convertBinding(ITypeBinding binding, URI baseURI) {
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.setBaseURI(baseURI);
		CompilationUnit cu = JDTBindingConverterUtility.convertToCompilationUnit(binding);
		// The logical URI is used to create the corresponding resource.
		Resource potRes = this.getParentResolver().getResourceSet().createResource(baseURI);
		potRes.getContents().add(cu);
		// For the registration, the physical URI is used.
		baseURI = JavaClasspath.get(this.getParentResolver().getResourceSet()).getURIMap().get(baseURI);
		JavaClasspath.get(this.getParentResolver().getResourceSet()).registerJavaRoot(cu, baseURI);
		return cu.getClassifiers().get(0);
	}
	
	private EObject findLocalOrAnonymousClass(String binaryName) {
		if (binaryName == null) {
			return null;
		}
		Matcher m1 = parentNamePattern.matcher(binaryName);
		if (m1.matches()) {
			String parentName = m1.group(1);
			ConcreteClassifier parentClassifier = JavaClasspath.get(this.getParentResolver().getResourceSet()
					).getConcreteClassifier(parentName);
			if (parentClassifier != null) {
				parentClassifier = (ConcreteClassifier) EcoreUtil.resolve(parentClassifier,
						this.getParentResolver().getResourceSet());
			}
			if (parentClassifier != null && !parentClassifier.eIsProxy()) {
				Matcher m2 = innerNamesPattern.matcher(m1.group(2));
				MemberContainer currentContainer = parentClassifier;
				while (m2.find()) {
					String index = m2.group(3);
					index = index == null ? m2.group(5) : index;
					String innerClass = m2.group(4);
					innerClass = innerClass == null ? m2.group(6) : innerClass;
					if (index == null && innerClass != null) {
						for (Member mem : currentContainer.getMembers()) {
							if (mem instanceof ConcreteClassifier
									&& ((ConcreteClassifier) mem).getName()
									.equals(innerClass)) {
								currentContainer = (ConcreteClassifier) mem;
								break;
							}
						}
					} else if (index != null && innerClass != null) {
						int i = Integer.parseInt(index);
						int currentIndex = 1;
						for (ConcreteClassifier conClass : currentContainer
								.getChildrenByType(ConcreteClassifier.class)) {
							if (!(conClass.eContainer() instanceof MemberContainer)) {
								EObject conContainer = ((Commentable) conClass.eContainer())
										.getContainingAnonymousClass();
								if (conContainer != null
										&& conContainer.equals(currentContainer)) {
									if (currentIndex == i) {
										currentContainer = conClass;
										break;
									} else {
										currentIndex++;
										continue;
									}
								}
								conContainer = ((Commentable) conClass.eContainer())
										.getContainingConcreteClassifier();
								if (currentContainer.equals(conContainer)) {
									if (currentIndex == i) {
										currentContainer = conClass;
										break;
									} else {
										currentIndex++;
									}
								}
							}
						}
					} else if (index != null && innerClass == null) {
						int i = Integer.parseInt(index);
						int currentIndex = 1;
						for (AnonymousClass ano
								: currentContainer.getChildrenByType(AnonymousClass.class)) {
							EObject anoContainer = ((Commentable) ano.eContainer())
									.getContainingAnonymousClass();
							if (anoContainer != null && anoContainer.equals(currentContainer)) {
								if (currentIndex == i) {
									currentContainer = ano;
									break;
								} else {
									currentIndex++;
									continue;
								}
							}
							anoContainer = ((Commentable) ano.eContainer())
									.getContainingConcreteClassifier();
							if (currentContainer.equals(anoContainer)) {
								if (currentIndex == i) {
									currentContainer = ano;
									break;
								} else {
									currentIndex++;
								}
							}
						}
					}
				}
				return currentContainer;
			}
		}
		return null;
	}
}
