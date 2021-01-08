package jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;

import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.modifiers.ModifiersPackage;
import org.emftext.language.java.variables.AdditionalLocalVariable;
import org.emftext.language.java.variables.LocalVariable;
import org.emftext.language.java.variables.util.VariablesSwitch;

class VariablesPrinterSwitch extends VariablesSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	VariablesPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseLocalVariable(LocalVariable element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			writer.append(" " + element.getName());
			element.getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			if (element.getInitialValue() != null) {
				writer.append(" = ");
				parent.doSwitch(element.getInitialValue());
			}
			for (AdditionalLocalVariable var : element.getAdditionalLocalVariables()) {
				writer.append(", ");
				caseAdditionalLocalVariable(var);
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAdditionalLocalVariable(AdditionalLocalVariable element) {
		try {
			writer.append(element.getName());
			element.getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			if (element.getInitialValue() != null) {
				writer.append(" = ");
				parent.doSwitch(element.getInitialValue());
			}
		} catch (IOException e) {
		}
		return true;
	}
}
