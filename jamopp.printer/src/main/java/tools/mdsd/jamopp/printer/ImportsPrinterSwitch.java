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

import org.eclipse.emf.ecore.util.Switch;

import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.model.java.imports.ClassifierImport;
import tools.mdsd.jamopp.model.java.imports.Import;
import tools.mdsd.jamopp.model.java.imports.ImportingElement;
import tools.mdsd.jamopp.model.java.imports.PackageImport;
import tools.mdsd.jamopp.model.java.imports.StaticClassifierImport;
import tools.mdsd.jamopp.model.java.imports.StaticMemberImport;
import tools.mdsd.jamopp.model.java.imports.util.ImportsSwitch;

class ImportsPrinterSwitch extends ImportsSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	ImportsPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseImportingElement(ImportingElement element) {
		for (Import ele : element.getImports()) {
			caseImport(ele);
		}
		return true;
	}
	
	@Override
	public Boolean caseImport(Import element) {
		try {
			writer.append("import ");
			this.doSwitch(element);
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseClassifierImport(ClassifierImport element) {
		try {
			writer.append(LogicalJavaURIGenerator.packageName(element) + element.getClassifier().getName());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePackageImport(PackageImport element) {
		try {
			String n = LogicalJavaURIGenerator.packageName(element);
			n = n.substring(0, n.length() - 1);
			writer.append(n);
			if (element.getClassifier() != null) {
				writer.append("." + element.getClassifier().getName());
			}
			writer.append(".*");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseStaticClassifierImport(StaticClassifierImport element) {
		try {
			writer.append("static " + LogicalJavaURIGenerator.packageName(element)
				+ element.getClassifier().getName() + ".*");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseStaticMemberImport(StaticMemberImport element) {
		try {
			writer.append("static " + LogicalJavaURIGenerator.packageName(element) + element.getClassifier().getName()
				+ LogicalJavaURIGenerator.PACKAGE_SEPARATOR + element.getStaticMembers().get(0).getName());
		} catch (IOException e) {
		}
		return true;
	}
}
