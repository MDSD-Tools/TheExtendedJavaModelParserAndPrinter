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

package tools.mdsd.jamopp.parser.jdt;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

class OrdinaryCompilationUnitJDTASTVisitorAndConverter extends ModuleJDTASTVisitorAndConverter {
	@Override
	public boolean visit(CompilationUnit node) {
		this.setConvertedElement(null);
		if (node.types().size() > 0) {
			this.setConvertedElement(this.convertToCompilationUnit(node));
		}
		super.visit(node);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private tools.mdsd.jamopp.model.java.containers.CompilationUnit convertToCompilationUnit(CompilationUnit cu) {
		tools.mdsd.jamopp.model.java.containers.CompilationUnit result = tools.mdsd.jamopp.model.java.containers.ContainersFactory.eINSTANCE.createCompilationUnit();
		result.setName("");
		LayoutInformationConverter.convertJavaRootLayoutInformation(result, cu, getSource());
		cu.types().forEach(obj -> result.getClassifiers().add(ClassifierConverterUtility.convertToConcreteClassifier((AbstractTypeDeclaration) obj)));
		return result;
	}
}
