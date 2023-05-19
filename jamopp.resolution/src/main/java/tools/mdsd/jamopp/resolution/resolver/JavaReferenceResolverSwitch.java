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
package tools.mdsd.jamopp.resolution.resolver;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import tools.mdsd.jamopp.model.java.annotations.AnnotationAttributeSetting;
import tools.mdsd.jamopp.model.java.annotations.AnnotationInstance;
import tools.mdsd.jamopp.model.java.annotations.AnnotationsPackage;
import tools.mdsd.jamopp.model.java.classifiers.Classifier;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.imports.Import;
import tools.mdsd.jamopp.model.java.imports.ImportsPackage;
import tools.mdsd.jamopp.model.java.imports.StaticMemberImport;
import tools.mdsd.jamopp.model.java.members.InterfaceMethod;
import tools.mdsd.jamopp.model.java.references.ElementReference;
import tools.mdsd.jamopp.model.java.references.ReferenceableElement;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;
import tools.mdsd.jamopp.model.java.types.ClassifierReference;
import tools.mdsd.jamopp.model.java.types.TypesPackage;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaReferenceResolveResult;
import tools.mdsd.jamopp.resolution.resolver.result.JavaDelegatingResolveResult;

/**
 * A IJavaReferenceResolverSwitch holds references to multiple other reference
 * resolvers and delegates requests to the appropriate resolver.
 */
public class JavaReferenceResolverSwitch implements IJavaReferenceResolver<EObject, EObject> {
	private ClassifierImportClassifierReferenceResolver classifierImportClassifierReferenceResolver
		= new ClassifierImportClassifierReferenceResolver();
	private StaticMemberImportStaticMembersReferenceResolver staticMemberImportStaticMembersReferenceResolver
		= new StaticMemberImportStaticMembersReferenceResolver();
	private AnnotationInstanceAnnotationReferenceResolver annotationInstanceAnnotationReferenceResolver
		= new AnnotationInstanceAnnotationReferenceResolver();
	private AnnotationAttributeSettingAttributeReferenceResolver annotationAttributeSettingAttributeReferenceResolver
		= new AnnotationAttributeSettingAttributeReferenceResolver();
	private ClassifierReferenceTargetReferenceResolver classifierReferenceTargetReferenceResolver
		= new ClassifierReferenceTargetReferenceResolver();
	private ElementReferenceTargetReferenceResolver elementReferenceTargetReferenceResolver
		= new ElementReferenceTargetReferenceResolver();
	
	public IJavaReferenceResolver<Import, ConcreteClassifier> getClassifierImportClassifierReferenceResolver() {
		return this.classifierImportClassifierReferenceResolver;
	}
	
	public IJavaReferenceResolver<StaticMemberImport, ReferenceableElement>
		getStaticMemberImportStaticMembersReferenceResolver() {
		return this.staticMemberImportStaticMembersReferenceResolver;
	}
	
	public IJavaReferenceResolver<AnnotationInstance, Classifier> getAnnotationInstanceAnnotationReferenceResolver() {
		return this.annotationInstanceAnnotationReferenceResolver;
	}
	
	public IJavaReferenceResolver<AnnotationAttributeSetting, InterfaceMethod>
		getAnnotationAttributeSettingAttributeReferenceResolver() {
		return this.annotationAttributeSettingAttributeReferenceResolver;
	}
	
	public IJavaReferenceResolver<ClassifierReference, Classifier> getClassifierReferenceTargetReferenceResolver() {
		return this.classifierReferenceTargetReferenceResolver;
	}
	
	public IJavaReferenceResolver<ElementReference, ReferenceableElement> getElementReferenceTargetReferenceResolver() {
		return this.elementReferenceTargetReferenceResolver;
	}
	
	@Override
	public void resolve(String identifier, EObject container, EReference reference, int position,
			IJavaReferenceResolveResult<EObject> result) {
		if (container == null) {
			return;
		}
		if (ImportsPackage.eINSTANCE.getImport().isInstance(container)) {
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			JavaDelegatingResolveResult<EObject, ConcreteClassifier> frr = new JavaDelegatingResolveResult<>(result);
			if (feature != null && feature instanceof EReference
					&& ImportsPackage.IMPORT__CLASSIFIER == feature.getFeatureID()) {
				classifierImportClassifierReferenceResolver.resolve(identifier, (Import) container,
						(EReference) feature, position, frr);
			}
		}
		if (ImportsPackage.eINSTANCE.getStaticMemberImport().isInstance(container)) {
			JavaDelegatingResolveResult<EObject, ReferenceableElement> frr
				= new JavaDelegatingResolveResult<>(result);
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			if (feature != null && feature instanceof EReference
					&& ImportsPackage.STATIC_MEMBER_IMPORT__STATIC_MEMBERS == feature.getFeatureID()) {
				staticMemberImportStaticMembersReferenceResolver.resolve(identifier,
						(StaticMemberImport) container, (EReference) feature, position, frr);
			}
		}
		if (AnnotationsPackage.eINSTANCE.getAnnotationInstance().isInstance(container)) {
			JavaDelegatingResolveResult<EObject, Classifier> frr = new JavaDelegatingResolveResult<>(result);
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			if (feature != null && feature instanceof EReference
					&& AnnotationsPackage.ANNOTATION_INSTANCE__ANNOTATION == feature.getFeatureID()) {
				annotationInstanceAnnotationReferenceResolver.resolve(identifier, (AnnotationInstance) container,
						(EReference) feature, position, frr);
			}
		}
		if (AnnotationsPackage.eINSTANCE.getAnnotationAttributeSetting().isInstance(container)) {
			JavaDelegatingResolveResult<EObject, InterfaceMethod> frr = new JavaDelegatingResolveResult<>(result);
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			if (feature != null && feature instanceof EReference
					&& AnnotationsPackage.ANNOTATION_ATTRIBUTE_SETTING__ATTRIBUTE == feature.getFeatureID()) {
				annotationAttributeSettingAttributeReferenceResolver.resolve(identifier,
						(AnnotationAttributeSetting) container, (EReference) feature,
						position, frr);
			}
		}
		if (TypesPackage.eINSTANCE.getClassifierReference().isInstance(container)) {
			JavaDelegatingResolveResult<EObject, Classifier> frr = new JavaDelegatingResolveResult<>(result);
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			if (feature != null && feature instanceof EReference
					&& TypesPackage.CLASSIFIER_REFERENCE__TARGET == feature.getFeatureID()) {
				classifierReferenceTargetReferenceResolver.resolve(identifier, (ClassifierReference) container,
						(EReference) feature, position, frr);
			}
		}
		if (ReferencesPackage.eINSTANCE.getElementReference().isInstance(container)) {
			JavaDelegatingResolveResult<EObject, ReferenceableElement> frr
				= new JavaDelegatingResolveResult<>(result);
			EStructuralFeature feature = container.eClass().getEStructuralFeature(reference.getName());
			if (feature != null && feature instanceof EReference
					&& ReferencesPackage.ELEMENT_REFERENCE__TARGET == feature.getFeatureID()) {
				elementReferenceTargetReferenceResolver.resolve(identifier, (ElementReference) container,
						(EReference) feature, position, frr);
			}
		}
	}
	
	public IJavaReferenceResolver<? extends EObject, ? extends EObject> getResolver(EStructuralFeature reference) {
		if (reference == ImportsPackage.eINSTANCE.getImport_Classifier()) {
			return this.getClassifierImportClassifierReferenceResolver();
		}
		if (reference == ImportsPackage.eINSTANCE.getStaticMemberImport_StaticMembers()) {
			return this.getStaticMemberImportStaticMembersReferenceResolver();
		}
		if (reference == AnnotationsPackage.eINSTANCE.getAnnotationInstance_Annotation()) {
			return this.getAnnotationInstanceAnnotationReferenceResolver();
		}
		if (reference == AnnotationsPackage.eINSTANCE.getAnnotationAttributeSetting_Attribute()) {
			return this.getAnnotationAttributeSettingAttributeReferenceResolver();
		}
		if (reference == TypesPackage.eINSTANCE.getClassifierReference_Target()) {
			return this.getAnnotationAttributeSettingAttributeReferenceResolver();
		}
		if (reference == ReferencesPackage.eINSTANCE.getElementReference_Target()) {
			return this.getElementReferenceTargetReferenceResolver();
		}
		return null;
	}
}
