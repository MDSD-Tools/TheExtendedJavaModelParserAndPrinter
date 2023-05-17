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

import org.emftext.language.java.annotations.AnnotationsPackage;
import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.members.AdditionalField;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.EmptyMember;
import org.emftext.language.java.members.EnumConstant;
import org.emftext.language.java.members.ExceptionThrower;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.InterfaceMethod;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.MemberContainer;
import org.emftext.language.java.members.util.MembersSwitch;
import org.emftext.language.java.modifiers.Modifier;
import org.emftext.language.java.modifiers.ModifiersPackage;
import org.emftext.language.java.modifiers.Public;
import org.emftext.language.java.modifiers.Static;
import org.emftext.language.java.parameters.ParametersPackage;
import org.emftext.language.java.references.ReferencesPackage;
import org.emftext.language.java.types.Type;

class MembersPrinterSwitch extends MembersSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	MembersPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseEnumConstant(EnumConstant element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append(element.getName() + " ");
			if (element.getArguments().size() > 0) {
				parent.doSwitch(ReferencesPackage.Literals.ARGUMENTABLE, element);
			}
			if (element.getAnonymousClass() != null) {
				parent.doSwitch(element.getAnonymousClass());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseMemberContainer(MemberContainer element) {
		for (Member mem : element.getMembers()) {
			parent.doSwitch(mem);
		}
		return true;
	}

	@Override
	public Boolean caseField(Field element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			writer.append(" " + element.getName());
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			if (element.getInitialValue() != null) {
				writer.append(" = ");
				parent.doSwitch(element.getInitialValue());
			}
			for (AdditionalField f : element.getAdditionalFields()) {
				writer.append(", ");
				caseAdditionalField(f);
			}
			writer.append(";\n\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAdditionalField(AdditionalField element) {
		try {
			writer.append(element.getName());
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			if (element.getInitialValue() != null) {
				writer.append(" = ");
				parent.doSwitch(element.getInitialValue());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseConstructor(Constructor element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(GenericsPackage.Literals.TYPE_PARAMETRIZABLE, element);
			writer.append(" " + element.getName());
			parent.doSwitch(ParametersPackage.Literals.PARAMETRIZABLE, element);
			caseExceptionThrower(element);
			parent.doSwitch(element.getBlock());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseClassMethod(ClassMethod element) {
		try {
			if (element.eContainer() instanceof org.emftext.language.java.classifiers.Enumeration) {
				boolean isStatic = false;
				boolean isPublic = false;
				for (Modifier m : element.getModifiers()) {
					if (m instanceof Static) {
						isStatic = true;
					} else if (m instanceof Public) {
						isPublic = true;
					}
				}
				if (isStatic && isPublic) {
					if (element.getName().equals("valueOf") && element.getParameters().size() == 1) {
						Type t = element.getParameters().get(0).getTypeReference().getTarget();
						if (t instanceof org.emftext.language.java.classifiers.Class) {
							org.emftext.language.java.classifiers.Class cla =
									(org.emftext.language.java.classifiers.Class) t;
							if (cla.getQualifiedName().equals("java.lang.String")) {
								return true;
							}
						}
					} else if (element.getName().equals("values") && element.getParameters().size() == 0) {
						return true;
					}
				}
			}
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(GenericsPackage.Literals.TYPE_PARAMETRIZABLE, element);
			writer.append(" ");
			parent.doSwitch(element.getTypeReference());
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			writer.append(" " + element.getName());
			parent.doSwitch(ParametersPackage.Literals.PARAMETRIZABLE, element);
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			caseExceptionThrower(element);
			writer.append(" ");
			parent.doSwitch(element.getStatement());
			writer.append("\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseInterfaceMethod(InterfaceMethod element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(GenericsPackage.Literals.TYPE_PARAMETRIZABLE, element);
			writer.append(" ");
			parent.doSwitch(element.getTypeReference());
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			writer.append(" " + element.getName());
			parent.doSwitch(ParametersPackage.Literals.PARAMETRIZABLE, element);
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			caseExceptionThrower(element);
			writer.append(" ");
			if (element.getDefaultValue() != null) {
				writer.append("default ");
				parent.doSwitch(element.getDefaultValue());
			}
			parent.doSwitch(element.getStatement());
			writer.append("\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExceptionThrower(ExceptionThrower element) {
		try {
			if (element.getExceptions().size() > 0) {
				writer.append("throws ");
				parent.doSwitch(element.getExceptions().get(0));
				for (int index = 1; index < element.getExceptions().size(); index++) {
					writer.append(", ");
					parent.doSwitch(element.getExceptions().get(index));
				}
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseEmptyMember(EmptyMember element) {
		try {
			writer.append(";\n\n");
		} catch (IOException e) {
		}
		return true;
	}
}
