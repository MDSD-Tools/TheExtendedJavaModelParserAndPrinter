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

package tools.mdsd.jamopp.model.java.extensions.statements;

import org.eclipse.emf.common.util.EList;

import tools.mdsd.jamopp.model.java.statements.Statement;
import tools.mdsd.jamopp.model.java.statements.SynchronizedBlock;

public class SynchronizedBlockExtension {
	/**
	 * Returns a list with all statements within the block of a synchronized statement.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param block the synchronized statement.
	 * @return the list.
	 * @deprecated Use getBlock().getStatements().
	 */
	@Deprecated
	public static EList<Statement> getStatements(SynchronizedBlock block) {
		return block.getBlock().getStatements();
	}
}
