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

package tools.mdsd.jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;

import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.model.java.annotations.AnnotationsPackage;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.containers.util.ContainersSwitch;
import tools.mdsd.jamopp.model.java.imports.ImportsPackage;
import tools.mdsd.jamopp.model.java.modules.ModuleDirective;

class ContainersPrinterSwitch extends ContainersSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ContainersPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseJavaRoot(JavaRoot root) {
		try {
			if (root instanceof tools.mdsd.jamopp.model.java.containers.Module) {
				parent.doSwitch(ImportsPackage.Literals.IMPORTING_ELEMENT, root);
				caseModule((tools.mdsd.jamopp.model.java.containers.Module) root);
			} else {
				if (root.getNamespaces().size() > 0) {
					parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, root);
					String p = root.getNamespacesAsString();
					p = p.substring(0, p.length() - 1);
					writer.append("package " + p + ";\n\n");
				}
				parent.doSwitch(ImportsPackage.Literals.IMPORTING_ELEMENT, root);
				if (root instanceof CompilationUnit) {
					caseCompilationUnit((CompilationUnit) root);
				}
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseModule(tools.mdsd.jamopp.model.java.containers.Module element) {
		try {
			writer.append("module ");
			if (element.getOpen() != null) {
				writer.append("open ");
			}
			String n = LogicalJavaURIGenerator.packageName(element);
			n = n.substring(0, n.length() - 1);
			writer.append(n);
			writer.append(" {\n");
			for (ModuleDirective dir : element.getTarget()) {
				parent.doSwitch(dir);
			}
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseCompilationUnit(CompilationUnit element) {
		for (ConcreteClassifier classifier : element.getClassifiers()) {
			parent.doSwitch(classifier);
		}
		return true;
	}
}
