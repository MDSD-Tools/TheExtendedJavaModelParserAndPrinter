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
import org.emftext.language.java.operators.Addition;
import org.emftext.language.java.operators.Assignment;
import org.emftext.language.java.operators.AssignmentAnd;
import org.emftext.language.java.operators.AssignmentDivision;
import org.emftext.language.java.operators.AssignmentExclusiveOr;
import org.emftext.language.java.operators.AssignmentLeftShift;
import org.emftext.language.java.operators.AssignmentMinus;
import org.emftext.language.java.operators.AssignmentModulo;
import org.emftext.language.java.operators.AssignmentMultiplication;
import org.emftext.language.java.operators.AssignmentOr;
import org.emftext.language.java.operators.AssignmentPlus;
import org.emftext.language.java.operators.AssignmentRightShift;
import org.emftext.language.java.operators.AssignmentUnsignedRightShift;
import org.emftext.language.java.operators.Complement;
import org.emftext.language.java.operators.Division;
import org.emftext.language.java.operators.Equal;
import org.emftext.language.java.operators.GreaterThan;
import org.emftext.language.java.operators.GreaterThanOrEqual;
import org.emftext.language.java.operators.LeftShift;
import org.emftext.language.java.operators.LessThan;
import org.emftext.language.java.operators.LessThanOrEqual;
import org.emftext.language.java.operators.MinusMinus;
import org.emftext.language.java.operators.Multiplication;
import org.emftext.language.java.operators.Negate;
import org.emftext.language.java.operators.NotEqual;
import org.emftext.language.java.operators.PlusPlus;
import org.emftext.language.java.operators.Remainder;
import org.emftext.language.java.operators.RightShift;
import org.emftext.language.java.operators.Subtraction;
import org.emftext.language.java.operators.UnsignedRightShift;
import org.emftext.language.java.operators.util.OperatorsSwitch;

class OperatorsPrinterSwitch extends OperatorsSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	OperatorsPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseAssignment(Assignment element) {
		try {
			writer.append(" = ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentAnd(AssignmentAnd element) {
		try {
			writer.append(" &= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentDivision(AssignmentDivision element) {
		try {
			writer.append(" /= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentExclusiveOr(AssignmentExclusiveOr element) {
		try {
			writer.append(" ^= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentMinus(AssignmentMinus element) {
		try {
			writer.append(" -= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentModulo(AssignmentModulo element) {
		try {
			writer.append(" %= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentMultiplication(AssignmentMultiplication element) {
		try {
			writer.append(" *= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentLeftShift(AssignmentLeftShift element) {
		try {
			writer.append(" <<= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentOr(AssignmentOr element) {
		try {
			writer.append(" |= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentPlus(AssignmentPlus element) {
		try {
			writer.append(" += ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentRightShift(AssignmentRightShift element) {
		try {
			writer.append(" >>= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentUnsignedRightShift(AssignmentUnsignedRightShift element) {
		try {
			writer.append(" >>>= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseGreaterThan(GreaterThan element) {
		try {
			writer.append(" > ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseGreaterThanOrEqual(GreaterThanOrEqual element) {
		try {
			writer.append(" >= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseLessThan(LessThan element) {
		try {
			writer.append(" < ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseLessThanOrEqual(LessThanOrEqual element) {
		try {
			writer.append(" <= ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseLeftShift(LeftShift element) {
		try {
			writer.append(" << ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseRightShift(RightShift element) {
		try {
			writer.append(" >> ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseUnsignedRightShift(UnsignedRightShift element) {
		try {
			writer.append(" >>> ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseEqual(Equal element) {
		try {
			writer.append(" == ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNotEqual(NotEqual element) {
		try {
			writer.append(" != ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAddition(Addition element) {
		try {
			writer.append(" + ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSubtraction(Subtraction element) {
		try {
			writer.append(" - ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseMultiplication(Multiplication element) {
		try {
			writer.append(" * ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDivision(Division element) {
		try {
			writer.append(" / ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseRemainder(Remainder element) {
		try {
			writer.append(" % ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNegate(Negate element) {
		try {
			writer.append("!");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseComplement(Complement element) {
		try {
			writer.append("~");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePlusPlus(PlusPlus element) {
		try {
			writer.append("++");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseMinusMinus(MinusMinus element) {
		try {
			writer.append("--");
		} catch (IOException e) {
		}
		return true;
	}
}
