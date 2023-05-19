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
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/
package org.emftext.language.java.resolver.result;

import org.eclipse.emf.common.util.URI;

/**
 * A basic implementation of the IJavaURIMapping interface that can map
 * identifiers to URIs.
 */
public class JavaURIMapping extends AbstractBaseJavaReferenceMapping<URI> implements IJavaURIMapping {
	public JavaURIMapping(String identifier, URI newIdentifier, String warning) {
		super(identifier, newIdentifier, warning);
	}
}
