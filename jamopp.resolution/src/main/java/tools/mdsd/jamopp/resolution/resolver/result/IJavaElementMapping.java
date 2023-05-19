/*******************************************************************************
 * Copyright (c) 2020-2023
 * Modelling for Continuous Software Engineering (MCSE) group,
 *     Institute of Information Security and Dependability (KASTEL),
 *     Karlsruhe Institute of Technology (KIT).
 * 
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
package tools.mdsd.jamopp.resolution.resolver.result;

import org.eclipse.emf.ecore.EObject;

/**
 * A mapping from an identifier to an EObject.
 * 
 * @param <ReferenceType> the type of the reference this mapping points to.
 */
public interface IJavaElementMapping<ReferenceType extends EObject> extends IJavaReferenceMapping<ReferenceType> {
}
