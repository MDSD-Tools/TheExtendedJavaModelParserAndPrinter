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

/**
 * A basic implementation of the IJavaReferenceMapping interface.
 * 
 * @param <ReferenceType> the type of the reference that can be mapped to.
 */
public abstract class AbstractBaseJavaReferenceMapping<ReferenceType> implements IJavaReferenceMapping<ReferenceType> {
	private final ReferenceType target;
	private String identifier;
	private String warning;
	
	public AbstractBaseJavaReferenceMapping(String identifier, ReferenceType target, String warning) {
		this.target = target;
		this.identifier = identifier;
		this.warning = warning;
	}
	
	@Override
	public ReferenceType getTarget() {
		return target;
	}
	
	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public String getWarning() {
		return warning;
	}
}
