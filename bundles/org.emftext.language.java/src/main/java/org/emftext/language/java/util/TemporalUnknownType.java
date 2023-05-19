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

package org.emftext.language.java.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.classifiers.impl.ClassifierImpl;

/**
 * This class represents a temporarily unknown type.
 */
public class TemporalUnknownType extends ClassifierImpl {

	private EObject creator;

	public TemporalUnknownType(EObject creator) {
		this.creator = creator;
	}

	@Override
	public Resource eResource() {
		return creator.eResource();
	}

	public EObject getCreator() {
		return creator;
	}
}
