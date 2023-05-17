/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
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

package jamopp.parser.jdt;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;

class AbstractAndEmptyModelJDTASTVisitorAndConverter extends ASTVisitor {
	private org.emftext.language.java.containers.JavaRoot convertedRootElement;
	private String originalSource;
	
	void setSource(String src) {
		this.originalSource = src;
	}
	
	String getSource() {
		return this.originalSource;
	}
	
	void setConvertedElement(org.emftext.language.java.containers.JavaRoot root) {
		this.convertedRootElement = root;
	}
	
	org.emftext.language.java.containers.JavaRoot getConvertedElement() {
		return this.convertedRootElement;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(CompilationUnit node) {
		if (this.convertedRootElement == null) {
			this.convertedRootElement = org.emftext.language.java.containers.ContainersFactory.eINSTANCE.createEmptyModel();
			this.convertedRootElement.setName("");
		}
		node.imports().forEach(obj -> this.convertedRootElement.getImports().add(this.convertToImport((ImportDeclaration) obj)));
		return false;
	}
	
	private org.emftext.language.java.imports.Import convertToImport(ImportDeclaration importDecl) {
		if (!importDecl.isOnDemand() && !importDecl.isStatic()) {
			org.emftext.language.java.imports.ClassifierImport convertedImport =
				org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createClassifierImport();
			org.emftext.language.java.classifiers.Classifier proxy = null;
			IBinding b = importDecl.getName().resolveBinding();
			if (b instanceof IPackageBinding) {
				proxy = JDTResolverUtility.getClass(importDecl.getName().getFullyQualifiedName());
			} else {
				ITypeBinding binding = (ITypeBinding) b;
				if (binding == null || binding.isRecovered()) {
					proxy = JDTResolverUtility.getClass(importDecl.getName().getFullyQualifiedName());
				} else {
					proxy = JDTResolverUtility.getClassifier((ITypeBinding) importDecl.getName().resolveBinding());
				}
			}
			convertedImport.setClassifier((org.emftext.language.java.classifiers.ConcreteClassifier) proxy);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxy);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else if (!importDecl.isOnDemand() && importDecl.isStatic()) {
			org.emftext.language.java.imports.StaticMemberImport convertedImport =
				org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createStaticMemberImport();
			convertedImport.setStatic(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
			QualifiedName qualifiedName = (QualifiedName) importDecl.getName();
			IBinding b = qualifiedName.resolveBinding();
			org.emftext.language.java.references.ReferenceableElement proxyMember = null;
			org.emftext.language.java.classifiers.Classifier proxyClass = null;
			if (b == null || b.isRecovered()) {
				proxyMember = JDTResolverUtility.getField(qualifiedName.getFullyQualifiedName());
			} else if (b instanceof IMethodBinding) {
				proxyMember = JDTResolverUtility.getMethod((IMethodBinding) b);
			} else if (b instanceof IVariableBinding) {
				proxyMember = JDTResolverUtility.getReferencableElement((IVariableBinding) b);
			} else if (b instanceof ITypeBinding) {
				ITypeBinding typeBinding = (ITypeBinding) b;
				if (!typeBinding.isNested()) {
					proxyClass = JDTResolverUtility.getClassifier(typeBinding);
					org.emftext.language.java.classifiers.ConcreteClassifier conCl =
						(org.emftext.language.java.classifiers.ConcreteClassifier) proxyClass;
					for (org.emftext.language.java.members.Member m : conCl.getMembers()) {
						if (!(m instanceof org.emftext.language.java.members.Constructor)
								&& m.getName().equals(qualifiedName.getName().getIdentifier())) {
							proxyMember = (org.emftext.language.java.references.ReferenceableElement) m;
							break;
						}
					}
					if (proxyMember == null) {
						proxyMember = JDTResolverUtility.getClassMethod(qualifiedName.getFullyQualifiedName());
						proxyMember.setName(qualifiedName.getName().getIdentifier());
						conCl.getMembers().add((org.emftext.language.java.members.Member) proxyMember);
					}
				} else {
					proxyMember = JDTResolverUtility.getClassifier(typeBinding);
					proxyClass = JDTResolverUtility.getClassifier(typeBinding.getDeclaringClass());
				}
			} else {
				proxyMember = JDTResolverUtility.getField(qualifiedName.getFullyQualifiedName());
			}
			proxyMember.setName(qualifiedName.getName().getIdentifier());
			convertedImport.getStaticMembers().add(proxyMember);
			if (proxyClass == null) {
				IBinding binding = qualifiedName.getQualifier().resolveBinding();
				if (binding == null || binding.isRecovered() || !(binding instanceof ITypeBinding)) {
					proxyClass = JDTResolverUtility.getClass(qualifiedName.getQualifier().getFullyQualifiedName());
				} else {
					proxyClass = JDTResolverUtility.getClassifier((ITypeBinding) binding);
				}
			}
			convertedImport.setClassifier((org.emftext.language.java.classifiers.ConcreteClassifier) proxyClass);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(qualifiedName.getQualifier(), convertedImport, proxyClass);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else if (importDecl.isOnDemand() && !importDecl.isStatic()) {
			org.emftext.language.java.imports.PackageImport convertedImport = org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createPackageImport();
			BaseConverterUtility.convertToNamespacesAndSet(importDecl.getName(), convertedImport);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else { // importDecl.isOnDemand() && importDecl.isStatic()
			org.emftext.language.java.imports.StaticClassifierImport convertedImport = org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createStaticClassifierImport();
			convertedImport.setStatic(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
			IBinding binding = importDecl.getName().resolveBinding();
			org.emftext.language.java.classifiers.Classifier proxyClass = null;
			if (binding == null || binding.isRecovered() || !(binding instanceof ITypeBinding)) {
				proxyClass = JDTResolverUtility.getClass(importDecl.getName().getFullyQualifiedName());
			} else {
				proxyClass = JDTResolverUtility.getClassifier((ITypeBinding) binding);
			}
			convertedImport.setClassifier((org.emftext.language.java.classifiers.ConcreteClassifier) proxyClass);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxyClass);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		}
	}
}
