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

module SimpleNormalModule {
	exports pkg2;
	uses pkg2.SimpleInterfaceWithDefaultMethods;
	exports simplepackage;
	requires transitive java.desktop;
	provides pkg2.SimpleInterfaceWithDefaultMethods with pkg2.SimpleInterfaceWithDefaultMethods.Implementation;
	opens pkg2 to java.base;
}
