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

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
		}
		node.imports().forEach(obj -> this.convertedRootElement.getImports().add(this.convertToImport((ImportDeclaration) obj)));
		return false;
	}
	
	private org.emftext.language.java.imports.Import convertToImport(ImportDeclaration importDecl) {
		if (!importDecl.isOnDemand() && !importDecl.isStatic()) {
			org.emftext.language.java.imports.ClassifierImport convertedImport =
				org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createClassifierImport();
			org.emftext.language.java.classifiers.Class proxy = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			((InternalEObject) proxy).eSetProxyURI(null);
			convertedImport.setClassifier(proxy);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxy);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else if (!importDecl.isOnDemand() && importDecl.isStatic()) {
			org.emftext.language.java.imports.StaticMemberImport convertedImport =
				org.emftext.language.java.imports.ImportsFactory.eINSTANCE.createStaticMemberImport();
			convertedImport.setStatic(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
			org.emftext.language.java.members.Field proxyMember = org.emftext.language.java.members.MembersFactory.eINSTANCE.createField();
			((InternalEObject) proxyMember).eSetProxyURI(null);
			QualifiedName qualifiedName = (QualifiedName) importDecl.getName();
			proxyMember.setName(qualifiedName.getName().getIdentifier());
			convertedImport.getStaticMembers().add(proxyMember);
			org.emftext.language.java.classifiers.Class proxyClass = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			((InternalEObject) proxyClass).eSetProxyURI(null);
			convertedImport.setClassifier(proxyClass);
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
			org.emftext.language.java.classifiers.Class proxyClass = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			((InternalEObject) proxyClass).eSetProxyURI(null);
			convertedImport.setClassifier(proxyClass);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxyClass);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		}
	}
}
