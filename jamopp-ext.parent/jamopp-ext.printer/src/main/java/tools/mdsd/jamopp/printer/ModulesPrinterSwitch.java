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
import tools.mdsd.jamopp.model.java.commons.NamespaceAwareElement;
import tools.mdsd.jamopp.model.java.modifiers.Static;
import tools.mdsd.jamopp.model.java.modules.AccessProvidingModuleDirective;
import tools.mdsd.jamopp.model.java.modules.ExportsModuleDirective;
import tools.mdsd.jamopp.model.java.modules.ModuleReference;
import tools.mdsd.jamopp.model.java.modules.OpensModuleDirective;
import tools.mdsd.jamopp.model.java.modules.ProvidesModuleDirective;
import tools.mdsd.jamopp.model.java.modules.RequiresModuleDirective;
import tools.mdsd.jamopp.model.java.modules.UsesModuleDirective;
import tools.mdsd.jamopp.model.java.modules.util.ModulesSwitch;
import tools.mdsd.jamopp.model.java.types.TypeReference;

class ModulesPrinterSwitch extends ModulesSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	ModulesPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseUsesModuleDirective(UsesModuleDirective element) {
		try {
			writer.append("uses ");
			parent.doSwitch(element.getTypeReference());
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseProvidesModuleDirective(ProvidesModuleDirective element) {
		try {
			writer.append("provides ");
			parent.doSwitch(element.getTypeReference());
			writer.append(" with ");
			for (int index = 0; index < element.getServiceProviders().size(); index++) {
				TypeReference ref = element.getServiceProviders().get(index);
				parent.doSwitch(ref);
				if (index < element.getServiceProviders().size() - 1) {
					writer.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
				}
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseRequiresModuleDirective(RequiresModuleDirective element) {
		try {
			writer.append("requires ");
			if (element.getModifier() != null) {
				if (element.getModifier() instanceof Static) {
					writer.append("static ");
				} else {
					writer.append("transitive ");
				}
			}
			if (element.getRequiredModule().getTarget() != null) {
				printNamespaceAwareElementWithoutTrailingDot(element.getRequiredModule().getTarget());
			} else {
				printNamespaceAwareElementWithoutTrailingDot(element.getRequiredModule());
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseOpensModuleDirective(OpensModuleDirective element) {
		try {
			writer.append("opens ");
			printRemainingAccessProvidingModuleDirective(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExportsModuleDirective(ExportsModuleDirective element) {
		try {
			writer.append("exports ");
			printRemainingAccessProvidingModuleDirective(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	private void printRemainingAccessProvidingModuleDirective(AccessProvidingModuleDirective element) {
		try {
			if (element.getAccessablePackage() != null) {
				printNamespaceAwareElementWithoutTrailingDot(element.getAccessablePackage());
			} else {
				printNamespaceAwareElementWithoutTrailingDot(element);
			}
			if (element.getModules().size() > 0) {
				writer.append(" to ");
				ModuleReference m = element.getModules().get(0);
				if (m.getTarget() != null) {
					printNamespaceAwareElementWithoutTrailingDot(m.getTarget());
				} else {
					printNamespaceAwareElementWithoutTrailingDot(m);
				}
				for (int index = 1; index < element.getModules().size(); index++) {
					writer.append(", ");
					m = element.getModules().get(index);
					if (m.getTarget() != null) {
						printNamespaceAwareElementWithoutTrailingDot(m.getTarget());
					} else {
						printNamespaceAwareElementWithoutTrailingDot(m);
					}
				}
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
	}
	
	private void printNamespaceAwareElementWithoutTrailingDot(NamespaceAwareElement nae) throws IOException {
		String n = LogicalJavaURIGenerator.packageName(nae);
		n = n.substring(0, n.length() - 1);
		writer.append(n);
	}
}
