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
package org.emftext.language.java.resolver;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.emftext.language.java.annotations.AnnotationAttributeSetting;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.annotations.AnnotationsPackage;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.imports.ImportsPackage;
import org.emftext.language.java.imports.StaticMemberImport;
import org.emftext.language.java.members.InterfaceMethod;
import org.emftext.language.java.references.ElementReference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.references.ReferencesPackage;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;
import org.emftext.language.java.resolver.result.JavaDelegatingResolveResult;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.TypesPackage;

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
