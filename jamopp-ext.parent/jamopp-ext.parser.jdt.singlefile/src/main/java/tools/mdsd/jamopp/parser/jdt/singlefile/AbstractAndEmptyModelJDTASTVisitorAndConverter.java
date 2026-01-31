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

package tools.mdsd.jamopp.parser.jdt.singlefile;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;

import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class AbstractAndEmptyModelJDTASTVisitorAndConverter extends ASTVisitor {
	private tools.mdsd.jamopp.model.java.containers.JavaRoot convertedRootElement;
	private String originalSource;
	
	void setSource(String src) {
		this.originalSource = src;
	}
	
	String getSource() {
		return this.originalSource;
	}
	
	void setConvertedElement(tools.mdsd.jamopp.model.java.containers.JavaRoot root) {
		this.convertedRootElement = root;
	}
	
	tools.mdsd.jamopp.model.java.containers.JavaRoot getConvertedElement() {
		return this.convertedRootElement;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(CompilationUnit node) {
		if (this.convertedRootElement == null) {
			this.convertedRootElement =
					tools.mdsd.jamopp.model.java.containers.ContainersFactory.eINSTANCE.createEmptyModel();
			this.convertedRootElement.setName("");
		}
		node.imports().forEach(obj -> this.convertedRootElement.getImports().add(this.convertToImport((ImportDeclaration) obj)));
		return false;
	}
	
	private tools.mdsd.jamopp.model.java.imports.Import convertToImport(ImportDeclaration importDecl) {
		if (!importDecl.isOnDemand() && !importDecl.isStatic()) {
			tools.mdsd.jamopp.model.java.imports.ClassifierImport convertedImport =
				tools.mdsd.jamopp.model.java.imports.ImportsFactory.eINSTANCE.createClassifierImport();
			tools.mdsd.jamopp.model.java.classifiers.Class proxy =
					tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			convertedImport.setClassifier(proxy);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxy);
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(
					convertedImport, tools.mdsd.jamopp.model.java.imports.ImportsPackage.Literals.IMPORT__CLASSIFIER, proxy.getName(), proxy, -1,
					importDecl.resolveBinding());
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else if (!importDecl.isOnDemand() && importDecl.isStatic()) {
			tools.mdsd.jamopp.model.java.imports.StaticMemberImport convertedImport =
				tools.mdsd.jamopp.model.java.imports.ImportsFactory.eINSTANCE.createStaticMemberImport();
			convertedImport.setStatic(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
			tools.mdsd.jamopp.model.java.members.Field proxyMember =
					tools.mdsd.jamopp.model.java.members.MembersFactory.eINSTANCE.createField();
			QualifiedName qualifiedName = (QualifiedName) importDecl.getName();
			proxyMember.setName(qualifiedName.getName().getIdentifier());
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(
				convertedImport, tools.mdsd.jamopp.model.java.imports.ImportsPackage.Literals.STATIC_MEMBER_IMPORT__STATIC_MEMBERS, proxyMember.getName(), proxyMember, -1,
				importDecl.resolveBinding());
			convertedImport.getStaticMembers().add(proxyMember);
			qualifiedName = (QualifiedName) qualifiedName.getQualifier();
			tools.mdsd.jamopp.model.java.classifiers.Class proxyClass =
					tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE
				.registerContextDependentURIFragment(convertedImport,
					tools.mdsd.jamopp.model.java.imports.ImportsPackage.Literals.IMPORT__CLASSIFIER,
					qualifiedName.getName().getIdentifier(), proxyClass, -1, null);
			convertedImport.setClassifier(proxyClass);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(
					qualifiedName, convertedImport, proxyClass);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else if (importDecl.isOnDemand() && !importDecl.isStatic()) {
			tools.mdsd.jamopp.model.java.imports.PackageImport convertedImport = tools.mdsd.jamopp.model.java.imports.ImportsFactory.eINSTANCE.createPackageImport();
			BaseConverterUtility.convertToNamespacesAndSet(importDecl.getName(), convertedImport);
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		} else { // importDecl.isOnDemand() && importDecl.isStatic()
			tools.mdsd.jamopp.model.java.imports.StaticClassifierImport convertedImport = tools.mdsd.jamopp.model.java.imports.ImportsFactory.eINSTANCE.createStaticClassifierImport();
			convertedImport.setStatic(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
			tools.mdsd.jamopp.model.java.classifiers.Class proxyClass =
					tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			convertedImport.setClassifier(proxyClass);
			BaseConverterUtility.convertToNamespacesAndSimpleNameAndSet(importDecl.getName(), convertedImport, proxyClass);
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(
				convertedImport, tools.mdsd.jamopp.model.java.imports.ImportsPackage.Literals.IMPORT__CLASSIFIER, proxyClass.getName(), proxyClass, -1,
				importDecl.resolveBinding());
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedImport, importDecl);
			return convertedImport;
		}
	}
}
