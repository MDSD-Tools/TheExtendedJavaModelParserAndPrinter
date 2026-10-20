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

import tools.mdsd.jamopp.model.java.operators.Addition;
import tools.mdsd.jamopp.model.java.operators.Assignment;
import tools.mdsd.jamopp.model.java.operators.AssignmentAnd;
import tools.mdsd.jamopp.model.java.operators.AssignmentDivision;
import tools.mdsd.jamopp.model.java.operators.AssignmentExclusiveOr;
import tools.mdsd.jamopp.model.java.operators.AssignmentLeftShift;
import tools.mdsd.jamopp.model.java.operators.AssignmentMinus;
import tools.mdsd.jamopp.model.java.operators.AssignmentModulo;
import tools.mdsd.jamopp.model.java.operators.AssignmentMultiplication;
import tools.mdsd.jamopp.model.java.operators.AssignmentOr;
import tools.mdsd.jamopp.model.java.operators.AssignmentPlus;
import tools.mdsd.jamopp.model.java.operators.AssignmentRightShift;
import tools.mdsd.jamopp.model.java.operators.AssignmentUnsignedRightShift;
import tools.mdsd.jamopp.model.java.operators.Complement;
import tools.mdsd.jamopp.model.java.operators.Division;
import tools.mdsd.jamopp.model.java.operators.Equal;
import tools.mdsd.jamopp.model.java.operators.GreaterThan;
import tools.mdsd.jamopp.model.java.operators.GreaterThanOrEqual;
import tools.mdsd.jamopp.model.java.operators.LeftShift;
import tools.mdsd.jamopp.model.java.operators.LessThan;
import tools.mdsd.jamopp.model.java.operators.LessThanOrEqual;
import tools.mdsd.jamopp.model.java.operators.MinusMinus;
import tools.mdsd.jamopp.model.java.operators.Multiplication;
import tools.mdsd.jamopp.model.java.operators.Negate;
import tools.mdsd.jamopp.model.java.operators.NotEqual;
import tools.mdsd.jamopp.model.java.operators.PlusPlus;
import tools.mdsd.jamopp.model.java.operators.Remainder;
import tools.mdsd.jamopp.model.java.operators.RightShift;
import tools.mdsd.jamopp.model.java.operators.Subtraction;
import tools.mdsd.jamopp.model.java.operators.UnsignedRightShift;
import tools.mdsd.jamopp.model.java.operators.util.OperatorsSwitch;

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
