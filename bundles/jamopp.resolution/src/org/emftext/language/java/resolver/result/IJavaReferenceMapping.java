/*******************************************************************************
 * Copyright (c) 2006-2014
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 *   Martin Armbruster
 *      - Extension for Java 7-13
 ******************************************************************************/
package org.emftext.language.java.resolver.result;

/**
 * A mapping from an identifier to something else. The &quot;something else&quot;
 * is defined by subclasses of this interface. Implementors of such subclasses are
 * used during the process of resolving references where identifiers need to be
 * mapped to other objects.
 * This interface must not be implemented by clients.
 * 
 * @param <ReferenceType> the type of the reference this mapping points to.
 */
public interface IJavaReferenceMapping<ReferenceType> {
	/**
	 * Returns the identifier that is mapped.
	 * 
	 * @return the identifier.
	 */
	public String getIdentifier();
	
	/**
	 * Returns the mapped target.
	 * 
	 * @return the target.
	 */
	public ReferenceType getTarget();
	
	/**
	 * A mapping can have a warning attached that contains additional information
	 * (e.g., when the mapping might be wrong under specific conditions). The warning
	 * is meant to be presented to the user together with the mapping result.
	 * 
	 * @return the warning.
	 */
	public String getWarning();
}
