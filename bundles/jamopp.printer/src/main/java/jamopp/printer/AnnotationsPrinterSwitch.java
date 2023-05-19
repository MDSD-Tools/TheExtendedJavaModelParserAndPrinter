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

package jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;

import org.eclipse.emf.ecore.util.Switch;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.annotations.Annotable;
import org.emftext.language.java.annotations.AnnotationAttributeSetting;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.annotations.AnnotationParameterList;
import org.emftext.language.java.annotations.SingleAnnotationParameter;
import org.emftext.language.java.annotations.util.AnnotationsSwitch;

class AnnotationsPrinterSwitch extends AnnotationsSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	AnnotationsPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseAnnotable(Annotable element) {
		for (AnnotationInstance inst : element.getAnnotations()) {
			caseAnnotationInstance(inst);
		}
		return true;
	}
	
	@Override
	public Boolean caseAnnotationInstance(AnnotationInstance element) {
		try {
			writer.append("@");
			if (element.getNamespaces().size() > 0) {
				writer.append(LogicalJavaURIGenerator.packageName(element));
			}
			writer.append(element.getAnnotation().getName());
			if (element.getParameter() != null) {
				writer.append("(");
				if (element.getParameter() instanceof SingleAnnotationParameter) {
					parent.doSwitch(((SingleAnnotationParameter) element.getParameter()).getValue());
				} else {
					AnnotationParameterList list = (AnnotationParameterList) element.getParameter();
					for (int index = 0; index < list.getSettings().size(); index++) {
						AnnotationAttributeSetting setting = list.getSettings().get(index);
						writer.append(setting.getAttribute().getName() + " = ");
						parent.doSwitch(setting.getValue());
						if (index < list.getSettings().size() - 1) {
							writer.append(", ");
						}
					}
				}
				writer.append(")");
			}
			writer.append("\n");
		} catch (IOException e) {
		}
		return true;
	}
}
