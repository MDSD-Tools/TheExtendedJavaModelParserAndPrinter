/*******************************************************************************
 * Copyright (c) 2006-2012
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
package tools.mdsd.jamopp.resolution.resolver.decider;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.references.IdentifierReference;
import tools.mdsd.jamopp.model.java.references.PackageReference;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.references.ReferencesFactory;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;

/**
 * A decider that assumes that a package is referenced if the context of the reference
 * allows for a package reference at that position. The decider creates a package
 * element as additional candidate in that case.
 */
public class PackageDecider extends AbstractDecider {

	@Override
	public boolean canFindTargetsFor(EObject referenceContainer, EReference crossReference) {

		if (referenceContainer instanceof IdentifierReference) {
			IdentifierReference idReference = (IdentifierReference) referenceContainer;
			 //a classifier must follow
			if (!(idReference.getNext() instanceof IdentifierReference)) {
				return false;
			}
			if (!referenceContainer.eContainingFeature().equals(ReferencesPackage.Literals.REFERENCE__NEXT)) {
				//maybe the root package
				return true;
			}
			if (referenceContainer.eContainingFeature().equals(ReferencesPackage.Literals.REFERENCE__NEXT)
				&& idReference.eContainer() instanceof IdentifierReference) {
				//maybe the next sub package
				return true;
			}
		}
		return false;
	}

	@Override
	public EList<? extends EObject> getAdditionalCandidates(String identifier, EObject container)  {
		if (container instanceof IdentifierReference) {
			EList<EObject> resultList = new BasicEList<EObject>();
			IdentifierReference parentPackage = (IdentifierReference) container;

			String pack = parentPackage.getTarget().getName();
			
			Reference parent = parentPackage.getPrevious();
			if (parent != null) {
				if (!(parent instanceof IdentifierReference)) {
					return resultList;
				}
				IdentifierReference parentCast = (IdentifierReference) parent;
				if (parentCast.getTarget() instanceof tools.mdsd.jamopp.model.java.containers.Package) {
					pack = ((tools.mdsd.jamopp.model.java.containers.Package) parentCast.getTarget())
						.getNamespacesAsString() + pack;
				} else if (parentCast.getTarget() instanceof PackageReference) {
					var packRef = (PackageReference) parentCast.getTarget();
					pack = packRef.getNamespacesAsString() + packRef.getName()
						+ LogicalJavaURIGenerator.PACKAGE_SEPARATOR + pack;
				} else {
					return resultList;
				}
			}
			
			
			if (JavaClasspath.get(container).isPackageRegistered(pack)) {
				tools.mdsd.jamopp.model.java.containers.Package p = JavaClasspath.get(container).getPackage(pack);
				if (p != null) {
					resultList.add(p);
				} else {
					PackageReference pr = ReferencesFactory.eINSTANCE.createPackageReference();
					for (String namespace : pack.split("\\.")) {
						pr.getNamespaces().add(namespace);
					}
					pr.getNamespaces().remove(pr.getNamespaces().size() - 1);
					pr.setName(identifier);
					resultList.add(pr);
				}
			}

			return resultList;
		}
		if (container instanceof JavaRoot && container.eResource() != null) {
			EList<EObject> resultList = new BasicEList<EObject>();

			if (JavaClasspath.get(container).isPackageRegistered(
					((JavaRoot) container).getNamespacesAsString() + identifier)) {
				PackageReference p = ReferencesFactory.eINSTANCE.createPackageReference();
				p.setName(identifier);
				resultList.add(p);
			}

			return resultList;
		}
		return null;
	}

	@Override
	public boolean containsCandidates(EObject container,
			EReference containingReference) {
		return false;
	}

	@Override
	public boolean isPossibleTarget(String id, EObject element) {
		if (element instanceof tools.mdsd.jamopp.model.java.containers.Package) {
			tools.mdsd.jamopp.model.java.containers.Package ne = (tools.mdsd.jamopp.model.java.containers.Package) element;
			return id.equals(ne.getNamespaces().get(ne.getNamespaces().size() - 1));
		} else if (element instanceof PackageReference) {
			return id.equals(((PackageReference) element).getName());
		}
		return false;
	}
}
