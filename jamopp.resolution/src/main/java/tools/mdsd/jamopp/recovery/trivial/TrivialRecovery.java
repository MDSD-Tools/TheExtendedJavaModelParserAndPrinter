/*******************************************************************************
 * Copyright (c) 2022, Martin Armbruster
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

package tools.mdsd.jamopp.recovery.trivial;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import tools.mdsd.jamopp.model.java.classifiers.Annotation;
import tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.ContainersFactory;
import tools.mdsd.jamopp.model.java.members.ClassMethod;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.InterfaceMethod;
import tools.mdsd.jamopp.model.java.members.MembersFactory;
import tools.mdsd.jamopp.model.java.statements.StatementsFactory;
import tools.mdsd.jamopp.model.java.types.TypesFactory;

/**
 * Implements a trivial recovery strategy: for every proxy object, a new object with the same type and name is created and set.
 * 
 * @author Martin Armbruster
 */
public class TrivialRecovery {
	private ResourceSet set;
	private Resource artificialResource;
	private CompilationUnit artificialCU;
	private tools.mdsd.jamopp.model.java.classifiers.Class artificialClass;
	private tools.mdsd.jamopp.model.java.classifiers.Class objectClass;
	private HashMap<String, tools.mdsd.jamopp.model.java.classifiers.Class> artClasses = new HashMap<>();
	private HashMap<String, Annotation> artAnnotations = new HashMap<>();
	private HashMap<String, Field> artFields = new HashMap<>();
	private HashMap<String, ClassMethod> artClassMethods = new HashMap<>();
	private HashMap<String, InterfaceMethod> artInterfaceMethods = new HashMap<>();
	private HashMap<String, tools.mdsd.jamopp.model.java.containers.Package> artPackages = new HashMap<>();
	private HashMap<String, tools.mdsd.jamopp.model.java.containers.Module> artModules = new HashMap<>();
	
	public TrivialRecovery(ResourceSet set) {
		this.set = set;
	}
	
	public void recover() {
		var proxies = EcoreUtil.ProxyCrossReferencer.find(this.set);
		for (var entry : proxies.entrySet()) {
			var proxy = entry.getKey();
			var actualElement = this.recoverActualElement(proxy);
			for (var setting : entry.getValue()) {
				if (setting.getEStructuralFeature().isMany()) {
					EList<EObject> list = (EList<EObject>) setting.getEObject()
							.eGet(setting.getEStructuralFeature());
					var idx = list.indexOf(proxy);
					list.set(idx, actualElement);
				} else {
					setting.getEObject().eSet(setting.getEStructuralFeature(), actualElement);
				}
			}
		}
		this.set.getResources().forEach(resource -> resource.getErrors().clear());
	}
	
	private EObject recoverActualElement(EObject obj) {
		this.initArtificialResource();
		var name = ((tools.mdsd.jamopp.model.java.commons.NamedElement) obj).getName();
		if (obj instanceof tools.mdsd.jamopp.model.java.classifiers.Class) {
			if (this.artClasses.containsKey(name)) {
				return this.artClasses.get(name);
			}
			tools.mdsd.jamopp.model.java.classifiers.Class result = ClassifiersFactory.eINSTANCE.createClass();
			result.setName(name);
			this.artificialCU.getClassifiers().add(result);
			this.artClasses.put(name, result);
			return result;
		} else if (obj instanceof Annotation) {
			if (this.artAnnotations.containsKey(name)) {
				return this.artAnnotations.get(name);
			}
			Annotation result = ClassifiersFactory.eINSTANCE.createAnnotation();
			result.setName(name);
			this.artificialCU.getClassifiers().add(result);
			this.artAnnotations.put(name, result);
			return result;
		} else if (obj instanceof Field) {
			if (this.artFields.containsKey(name)) {
				return this.artFields.get(name);
			}
			var result = MembersFactory.eINSTANCE.createField();
			var reference = TypesFactory.eINSTANCE.createClassifierReference();
			reference.setTarget(this.objectClass);
			result.setTypeReference(reference);
			result.setName(name);
			this.artificialClass.getMembers().add(result);
			this.artFields.put(name, result);
			return result;
		} else if (obj instanceof ClassMethod) {
			if (this.artClassMethods.containsKey(name)) {
				return this.artClassMethods.get(name);
			}
			var result = MembersFactory.eINSTANCE.createClassMethod();
			result.setName(name);
			var statement = StatementsFactory.eINSTANCE.createBlock();
			statement.setName("");
			result.setStatement(statement);
			result.setTypeReference(TypesFactory.eINSTANCE.createVoid());
			this.artificialClass.getMembers().add(result);
			this.artClassMethods.put(name, result);
			return result;
		} else if (obj instanceof InterfaceMethod) {
			if (this.artInterfaceMethods.containsKey(name)) {
				return this.artInterfaceMethods.get(name);
			}
			var result = MembersFactory.eINSTANCE.createInterfaceMethod();
			result.setName(name);
			result.setStatement(StatementsFactory.eINSTANCE.createEmptyStatement());
			result.setTypeReference(TypesFactory.eINSTANCE.createVoid());
			this.artificialClass.getMembers().add(result);
			this.artInterfaceMethods.put(name, result);
			return result;
		} else if (obj instanceof tools.mdsd.jamopp.model.java.containers.Package) {
			var p = (tools.mdsd.jamopp.model.java.containers.Package) obj;
			name = p.getNamespacesAsString();
			if (this.artPackages.containsKey(name)) {
				return this.artPackages.get(name);
			}
			var result = ContainersFactory.eINSTANCE.createPackage();
			p.getNamespaces().forEach(ns -> result.getNamespaces().add(ns));
			this.artificialResource.getContents().add(result);
			this.artPackages.put(name, result);
			return result;
		} else if (obj instanceof tools.mdsd.jamopp.model.java.containers.Module) {
			if (this.artModules.containsKey(name)) {
				return this.artModules.get(name);
			}
			var result = ContainersFactory.eINSTANCE.createModule();
			result.setName(name);
			this.artificialResource.getContents().add(result);
			this.artModules.put(name, result);
			return result;
		}
		return null;
	}
	
	private void initArtificialResource() {
		if (this.artificialResource == null) {
			this.artificialResource = this.set.createResource(
					URI.createURI("pathmap:/javaclass/ArtificialResource.java"));
			
			this.artificialCU = ContainersFactory.eINSTANCE.createCompilationUnit();
			this.artificialResource.getContents().add(this.artificialCU);
			
			this.artificialClass = ClassifiersFactory.eINSTANCE.createClass();
			this.artificialClass.setName("SyntheticClass");
			this.artificialCU.getClassifiers().add(this.artificialClass);
			
			this.objectClass = findObjectClass();
			this.artClasses.put("Object", objectClass);
		}
	}
	
	private tools.mdsd.jamopp.model.java.classifiers.Class findObjectClass() {
		return this.set.getResources().stream().filter(resource -> !resource.getContents().isEmpty()
				&& resource.getContents().get(0) instanceof CompilationUnit)
			.map(resource -> (CompilationUnit) resource.getContents().get(0))
			.filter(cu -> cu.getNamespaces().size() == 2 && cu.getNamespaces().get(0).equals("java")
					&& cu.getNamespaces().get(1).equals("lang") && cu.getName().equals("Object"))
			.map(cu -> (tools.mdsd.jamopp.model.java.classifiers.Class) cu.getClassifiers().get(0)).findFirst().get();
	}
}
