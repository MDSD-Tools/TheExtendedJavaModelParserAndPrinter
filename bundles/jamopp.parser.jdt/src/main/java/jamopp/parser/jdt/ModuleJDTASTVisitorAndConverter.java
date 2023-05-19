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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.ModuleDirective;
import org.eclipse.jdt.core.dom.ModuleModifier;
import org.eclipse.jdt.core.dom.ModulePackageAccess;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.UsesDirective;

class ModuleJDTASTVisitorAndConverter extends PackageJDTASTVisitorAndConverter {
	@Override
	public boolean visit(CompilationUnit node) {
		if (node.getModule() != null) {
			org.emftext.language.java.containers.Module module = this.convertToModule(node.getModule());
			this.setConvertedElement(module);
		}
		super.visit(node);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private org.emftext.language.java.containers.Module convertToModule(ModuleDeclaration node) {
		org.emftext.language.java.containers.Module module = JDTResolverUtility.getModule(node.resolveBinding());
		if (node.isOpen()) {
			module.setOpen(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createOpen());
		}
		LayoutInformationConverter.convertJavaRootLayoutInformation(module, node, this.getSource());
		BaseConverterUtility.convertToNamespacesAndSet(node.getName(), module);
		module.setName("");
		node.annotations().forEach(obj -> module.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
			.convertToAnnotationInstance((Annotation) obj)));
		node.moduleStatements().forEach(obj -> module.getTarget().add(this.convertToDirective((ModuleDirective) obj)));
		return module;
	}
	
	@SuppressWarnings("unchecked")
	private org.emftext.language.java.modules.ModuleDirective convertToDirective(ModuleDirective directive) {
		if (directive.getNodeType() == ASTNode.REQUIRES_DIRECTIVE) {
			RequiresDirective reqDir = (RequiresDirective) directive;
			org.emftext.language.java.modules.RequiresModuleDirective result = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createRequiresModuleDirective();
			reqDir.modifiers().forEach(obj -> {
				ModuleModifier modifier = (ModuleModifier) obj;
				if (modifier.isStatic()) {
					result.setModifier(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
				} else if (modifier.isTransitive()) {
					result.setModifier(org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createTransitive());
				}
			});
			result.setRequiredModule(this.convertToModuleReference(reqDir.getName()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, directive);
			return result;
		} else if (directive.getNodeType() == ASTNode.EXPORTS_DIRECTIVE || directive.getNodeType() == ASTNode.OPENS_DIRECTIVE) {
			ModulePackageAccess accessDir = (ModulePackageAccess) directive;
			org.emftext.language.java.modules.AccessProvidingModuleDirective convertedDir;
			if (directive.getNodeType() == ASTNode.OPENS_DIRECTIVE) {
				convertedDir = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createOpensModuleDirective();
			} else { // directive.getNodeType() == ASTNode.EXPORTS_DIRECTIVE
				convertedDir = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createExportsModuleDirective();
			}
			IPackageBinding binding = (IPackageBinding) accessDir.getName().resolveBinding();
			convertedDir.setAccessablePackage(JDTResolverUtility.getPackage(binding));
			accessDir.modules().forEach(obj -> convertedDir.getModules().add(this.convertToModuleReference((Name) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedDir, directive);
			return convertedDir;
		} else if (directive.getNodeType() == ASTNode.PROVIDES_DIRECTIVE) {
			ProvidesDirective provDir = (ProvidesDirective) directive;
			org.emftext.language.java.modules.ProvidesModuleDirective result = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createProvidesModuleDirective();
			result.setTypeReference(BaseConverterUtility.convertToClassifierOrNamespaceClassifierReference(provDir.getName()));
			provDir.implementations().forEach(obj -> result.getServiceProviders().add(BaseConverterUtility.convertToClassifierOrNamespaceClassifierReference((Name) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, directive);
			return result;
		} else { // directive.getNodeType() == ASTNode.USES_DIRECTIVE
			UsesDirective usDir = (UsesDirective) directive;
			org.emftext.language.java.modules.UsesModuleDirective result = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createUsesModuleDirective();
			result.setTypeReference(BaseConverterUtility.convertToClassifierOrNamespaceClassifierReference(usDir.getName()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, directive);
			return result;
		}
	}
	
	private org.emftext.language.java.modules.ModuleReference convertToModuleReference(Name name) {
		org.emftext.language.java.modules.ModuleReference ref = org.emftext.language.java.modules.ModulesFactory.eINSTANCE.createModuleReference();
		org.emftext.language.java.containers.Module modProxy = JDTResolverUtility.getModule((IModuleBinding) name.resolveBinding());
		modProxy.setName("");
		ref.setTarget(modProxy);
		BaseConverterUtility.convertToNamespacesAndSet(name, modProxy);
		return ref;
	}
}
