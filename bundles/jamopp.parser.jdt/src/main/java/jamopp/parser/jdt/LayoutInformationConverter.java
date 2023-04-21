/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
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

package jamopp.parser.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.emftext.commons.layout.LayoutFactory;
import org.emftext.commons.layout.MinimalLayoutInformation;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.containers.JavaRoot;

class LayoutInformationConverter
{
	private static MinimalLayoutInformation currentRootLayout;
	
	static void convertJavaRootLayoutInformation(JavaRoot root, ASTNode rootSource, String sourceCode) {
		currentRootLayout = null;
		currentRootLayout = LayoutFactory.eINSTANCE.createMinimalLayoutInformation();
		currentRootLayout.setVisibleTokenText(sourceCode == null ? "" : sourceCode);
		currentRootLayout.setStartOffset(rootSource.getStartPosition());
		currentRootLayout.setLength(rootSource.getLength());
		currentRootLayout.setObject(root);
		currentRootLayout.setRootLayout(currentRootLayout);
		root.getLayoutInformations().add(currentRootLayout);
	}
	
	static void convertToMinimalLayoutInformation(Commentable target, ASTNode source) {
		if (currentRootLayout != null) {
			MinimalLayoutInformation li = LayoutFactory.eINSTANCE.createMinimalLayoutInformation();
			li.setStartOffset(source.getStartPosition());
			li.setLength(source.getLength());
			li.setObject(target);
			li.setRootLayout(currentRootLayout);
			target.getLayoutInformations().add(li);
		}
	}
}