/*******************************************************************************
 * Copyright (c) 2019-2020, Martin Armbruster
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

package org.emftext.language.java.extensions.statements;

import org.eclipse.emf.common.util.EList;
import org.emftext.language.java.statements.CatchBlock;
import org.emftext.language.java.statements.Statement;

public class CatchBlockExtension {
	/**
	 * Returns a list with all statements within the block of a catch statement.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param block the catch statement.
	 * @return the list.
	 * @deprecated Use getBlock().getStatements().
	 */
	@Deprecated
	public static EList<Statement> getStatements(CatchBlock block) {
		return block.getBlock().getStatements();
	}
}
