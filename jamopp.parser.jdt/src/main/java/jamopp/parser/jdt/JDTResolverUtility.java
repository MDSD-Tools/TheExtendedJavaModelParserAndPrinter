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

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.emftext.language.java.JavaClasspath;

public class JDTResolverUtility {
	private static ResourceSet resourceSet;
	private static HashMap<String, org.emftext.language.java.containers.Module> modBindToMod = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.containers.Package> nameToPackage = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.classifiers.Annotation> typeBindToAnnot = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.classifiers.Enumeration> typeBindToEnum = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.classifiers.Interface> typeBindToInterface = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.classifiers.Class> typeBindToClass = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.generics.TypeParameter> typeBindToTP = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.InterfaceMethod> methBindToInter = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.ClassMethod> methBindToCM = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.Constructor> methBindToConstr = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.Field> nameToField = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.AdditionalField> nameToAddField = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.variables.LocalVariable> nameToLocVar = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.variables.AdditionalLocalVariable> nameToAddLocVar = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.members.EnumConstant> nameToEnumConst = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.parameters.VariableLengthParameter> nameToVarLenParam = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.parameters.OrdinaryParameter> nameToOrdParam = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.parameters.CatchParameter> nameToCatchParam = new HashMap<>();
	private static HashMap<String, org.emftext.language.java.classifiers.AnonymousClass> nameToAnonymousClass = new HashMap<>();
	private static HashSet<IModuleBinding> moduleBindings = new HashSet<>();
	private static HashSet<IPackageBinding> packageBindings = new HashSet<>();
	private static HashSet<ITypeBinding> typeBindings = new HashSet<>();
	private static HashSet<IMethodBinding> methodBindings = new HashSet<>();
	private static HashSet<IVariableBinding> variableBindings = new HashSet<>();
	private static int uid = 0;
	private static HashMap<IVariableBinding, Integer> varBindToUid = new HashMap<>();
	private static HashSet<EObject> objVisited = new HashSet<>();
	private static HashMap<IBinding, String> nameCache = new HashMap<>();
	private final static String SYNTH_CLASS = "SyntheticContainerClass";
	private final static boolean extractAdditionalInformationFromTypeBindings = true;
	
	static void setResourceSet(ResourceSet set) {
		resourceSet = set;
	}
	
	static org.emftext.language.java.containers.Module getModule(IModuleBinding binding) {
		moduleBindings.add(binding);
		return getModule(binding.getName());
	}
	
	static org.emftext.language.java.containers.Module getModule(String modName) {
		if (modBindToMod.containsKey(modName)) {
			return modBindToMod.get(modName);
		} else {
			org.emftext.language.java.containers.Module result = JavaClasspath.get().getModule(modName);
			if (result == null) {
				result = org.emftext.language.java.containers.ContainersFactory.eINSTANCE.createModule();
			}
			modBindToMod.put(modName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.containers.Package getPackage(IPackageBinding binding) {
		packageBindings.add(binding);
		return getPackage(binding.getName());
	}
	
	private static org.emftext.language.java.containers.Package getPackage(String packageName) {
		if (nameToPackage.containsKey(packageName)) {
			return nameToPackage.get(packageName);
		} else {
			org.emftext.language.java.containers.Package result = JavaClasspath.get().getPackage(packageName);
			if (result == null) {
				result = org.emftext.language.java.containers.ContainersFactory.eINSTANCE.createPackage();
			}
			nameToPackage.put(packageName, result);
			return result;
		}
	}
	
	private static String convertToTypeName(ITypeBinding binding) {
		if (binding == null) {
			return "";
		}
		if (binding.isTypeVariable()) {
			return binding.getName();
		}
		if (nameCache.containsKey(binding)) {
			return nameCache.get(binding);
		}
		String qualifiedName;
		if (binding.isMember()) {
			qualifiedName = convertToTypeName(binding.getDeclaringClass()) + "." + binding.getName();
		} else if (binding.isLocal()) {
			IBinding b = binding.getDeclaringMember();
			if (b instanceof IMethodBinding) {
				qualifiedName = convertToMethodName((IMethodBinding) b) + "." + binding.getKey();
			}  else if (b instanceof IVariableBinding) {
				qualifiedName = convertToFieldName((IVariableBinding) b) + "." + binding.getKey();
			} else {
				qualifiedName = binding.getKey();
			}
			nameCache.put(binding, qualifiedName);
			return qualifiedName;
		} else {
			qualifiedName = binding.getQualifiedName();
		}
		if (qualifiedName.contains("<")) {
			qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("<"));
		}
		nameCache.put(binding, qualifiedName);
		return qualifiedName;
	}
	
	static org.emftext.language.java.classifiers.Annotation getAnnotation(ITypeBinding binding) {
		typeBindings.add(binding);
		return getAnnotation(convertToTypeName(binding));
	}
	
	static org.emftext.language.java.classifiers.Annotation getAnnotation(String annotName) {
		if (typeBindToAnnot.containsKey(annotName)) {
			return typeBindToAnnot.get(annotName);
		} else {
			org.emftext.language.java.classifiers.Annotation result;
			org.emftext.language.java.classifiers.ConcreteClassifier potClass = JavaClasspath.get().getConcreteClassifier(annotName);
			if (potClass instanceof org.emftext.language.java.classifiers.Annotation) {
				result = (org.emftext.language.java.classifiers.Annotation) potClass;
			} else {
				result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createAnnotation();
			}
			typeBindToAnnot.put(annotName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.classifiers.Enumeration getEnumeration(ITypeBinding binding) {
		String enumName = convertToTypeName(binding);
		if (typeBindToEnum.containsKey(enumName)) {
			return typeBindToEnum.get(enumName);
		} else {
			typeBindings.add(binding);
			org.emftext.language.java.classifiers.ConcreteClassifier classifier = JavaClasspath.get().getConcreteClassifier(enumName);
			org.emftext.language.java.classifiers.Enumeration result;
			if (classifier != null && classifier instanceof org.emftext.language.java.classifiers.Enumeration) {
				result = (org.emftext.language.java.classifiers.Enumeration) classifier;
			} else {
				result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createEnumeration();
			}
			typeBindToEnum.put(enumName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.classifiers.Class getClass(ITypeBinding binding) {
		typeBindings.add(binding);
		return getClass(convertToTypeName(binding));
	}
	
	static org.emftext.language.java.classifiers.Interface getInterface(ITypeBinding binding) {
		String interName = convertToTypeName(binding);
		if (typeBindToInterface.containsKey(interName)) {
			return typeBindToInterface.get(interName);
		} else {
			typeBindings.add(binding);
			org.emftext.language.java.classifiers.Interface result;
			org.emftext.language.java.classifiers.ConcreteClassifier classifier = JavaClasspath.get().getConcreteClassifier(interName);
			if (classifier != null && classifier instanceof org.emftext.language.java.classifiers.Interface) {
				result = (org.emftext.language.java.classifiers.Interface) classifier;
			} else {
				result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createInterface();
			}
			typeBindToInterface.put(interName, result);
			return result;
		}
	}
	
	private static String convertToTypeParameterName(ITypeBinding binding) {
		if (binding == null) {
			return "";
		}
		if (nameCache.containsKey(binding)) {
			return nameCache.get(binding);
		}
		String name = "";
		if (binding.getDeclaringClass() != null) {
			name += convertToTypeName(binding.getDeclaringClass());
		} else if (binding.getDeclaringMethod() != null) {
			name += convertToMethodName(binding.getDeclaringMethod());
		}
		name += "." + binding.getName();
		nameCache.put(binding, name);
		return name;
	}
	
	static org.emftext.language.java.generics.TypeParameter getTypeParameter(ITypeBinding binding) {
		String paramName = convertToTypeParameterName(binding);
		if (typeBindToTP.containsKey(paramName)) {
			return typeBindToTP.get(paramName);
		} else {
			typeBindings.add(binding);
			org.emftext.language.java.generics.TypeParameter result = org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createTypeParameter();
			typeBindToTP.put(paramName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.classifiers.Classifier getClassifier(ITypeBinding binding) {
		String typeName = convertToTypeName(binding);
		org.emftext.language.java.classifiers.ConcreteClassifier potClass = JavaClasspath.get().getConcreteClassifier(typeName);
		if (potClass != null) {
			return potClass;
		}
		if (binding.isAnonymous() || (binding.isLocal() && binding.getDeclaringMember() == null)
				|| nameToAnonymousClass.containsKey(convertToTypeName(binding))) {
			return null;
		}
		if (binding.isAnnotation()) {
			return getAnnotation(binding);
		} else if (binding.isInterface()) {
			return getInterface(binding);
		} else if (binding.isEnum()) {
			return getEnumeration(binding);
		} else if (binding.isClass()) {
			return getClass(binding);
		} else if (binding.isTypeVariable()) {
			return getTypeParameter(binding);
		} else if (binding.isArray()) {
			return getClassifier(binding.getElementType());
		}
		return null;
	}
	
	private static String convertToMethodName(IMethodBinding binding) {
		if (binding == null) {
			return "";
		}
		if (nameCache.containsKey(binding)) {
			return nameCache.get(binding);
		}
		binding = binding.getMethodDeclaration();
		StringBuilder builder = new StringBuilder();
		builder.append(convertToTypeName(binding.getDeclaringClass()));
		builder.append("::");
		builder.append(binding.getName());
		builder.append("(");
		for (ITypeBinding p : binding.getParameterTypes()) {
			builder.append(convertToTypeName(p));
			for (int i = 0; i < p.getDimensions(); i++) {
				builder.append("[]");
			}
		}
		builder.append(")");
		if (builder.toString().equals("java.lang.Object::clone()") && binding.getReturnType().isArray()) {
			builder.append("java.lang.Object");
		} else {
			builder.append(convertToTypeName(binding.getReturnType()));
		}
		String name = builder.toString();
		nameCache.put(binding, name);
		return name;
	}
	
	static org.emftext.language.java.members.InterfaceMethod getInterfaceMethod(String methodName) {
		if (methBindToInter.containsKey(methodName)) {
			return methBindToInter.get(methodName);
		} else {
			org.emftext.language.java.members.InterfaceMethod result = createNewInterfaceMethod();
			methBindToInter.put(methodName, result);
			return result;
		}
	}
	
	private static org.emftext.language.java.members.InterfaceMethod createNewInterfaceMethod() {
		org.emftext.language.java.members.InterfaceMethod result =
			org.emftext.language.java.members.MembersFactory.eINSTANCE.createInterfaceMethod();
		result.setTypeReference(org.emftext.language.java.types.TypesFactory.eINSTANCE.createVoid());
		result.setStatement(org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
		return result;
	}
	
	static org.emftext.language.java.members.InterfaceMethod getInterfaceMethod(IMethodBinding binding) {
		binding = binding.getMethodDeclaration();
		methodBindings.add(binding);
		String methName = convertToMethodName(binding);
		if (methBindToInter.containsKey(methName)) {
			return methBindToInter.get(methName);
		} else {
			org.emftext.language.java.classifiers.ConcreteClassifier classifier =
				(org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(binding.getDeclaringClass());
			org.emftext.language.java.members.InterfaceMethod result = null;
			if (classifier != null) {
				for (org.emftext.language.java.members.Member mem : classifier.getMembers()) {
					if (mem instanceof org.emftext.language.java.members.InterfaceMethod) {
						result = checkMethod((org.emftext.language.java.members.Method) mem, binding);
						if (result != null) {
							break;
						}
					}
				}
			}
			if (result == null) {
				result = createNewInterfaceMethod();
			}
			methBindToInter.put(methName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.ClassMethod getClassMethod(String methodName) {
		if (methBindToCM.containsKey(methodName)) {
			return methBindToCM.get(methodName);
		} else {
			org.emftext.language.java.members.ClassMethod result = createNewClassMethod();
			methBindToCM.put(methodName, result);
			return result;
		}
	}
	
	private static org.emftext.language.java.members.ClassMethod createNewClassMethod() {
		org.emftext.language.java.members.ClassMethod result =
			org.emftext.language.java.members.MembersFactory.eINSTANCE.createClassMethod();
		result.setTypeReference(org.emftext.language.java.types.TypesFactory.eINSTANCE.createVoid());
		org.emftext.language.java.statements.Block block = org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBlock();
		block.setName("");
		result.setStatement(block);
		return result;
	}
	
	static org.emftext.language.java.members.ClassMethod getClassMethod(IMethodBinding binding) {
		binding = binding.getMethodDeclaration();
		methodBindings.add(binding);
		String methName = convertToMethodName(binding);
		if (methBindToCM.containsKey(methName)) {
			return methBindToCM.get(methName);
		} else {
			org.emftext.language.java.classifiers.ConcreteClassifier classifier =
				(org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(binding.getDeclaringClass());
			org.emftext.language.java.members.ClassMethod result = null;
			if (classifier != null) {
				for (org.emftext.language.java.members.Member mem : classifier.getMembers()) {
					if (mem instanceof org.emftext.language.java.members.ClassMethod) {
						result = checkMethod((org.emftext.language.java.members.Method) mem,
							binding);
						if (result != null) {
							break;
						}
					}
				}
			}
			if (result == null) {
				result = createNewClassMethod();
			}
			methBindToCM.put(methName, result);
			return result;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends org.emftext.language.java.members.Method> T checkMethod(org.emftext.language.java.members.Method mem,
			IMethodBinding binding) {
		if (mem.getName().equals(binding.getName())) {
			T meth = (T) mem;
			if (meth.getName().equals("clone")) {
				return meth;
			}
			int receiveOffset = 0;
			if (binding.getDeclaredReceiverType() != null) {
				receiveOffset = 1;
			}
			if (binding.getParameterTypes().length + receiveOffset == meth.getParameters().size()) {
				if (receiveOffset == 1 &&
					!(meth.getParameters().get(0) instanceof org.emftext.language.java.parameters.ReceiverParameter
					&& convertToTypeName(binding.getDeclaredReceiverType()).equals(
						convertToTypeName(meth.getParameters().get(0).getTypeReference())))) {
					return null;
				}
				if (!convertToTypeName(binding.getReturnType()).equals(convertToTypeName(meth.getTypeReference()))) {
					return null;
				}
				for (int i = 0; i < binding.getParameterTypes().length; i++) {
					ITypeBinding currentParamType = binding.getParameterTypes()[i];
					org.emftext.language.java.parameters.Parameter currentParam = meth.getParameters().get(i + receiveOffset);
					if (!convertToTypeName(currentParamType).equals(convertToTypeName(currentParam.getTypeReference()))
						|| currentParamType.getDimensions()
						!= currentParam.getTypeReference().getArrayDimension()) {
						return null;
					}
				}
				return meth;
			}
		}
		return null;
	}
	
	private static String convertToTypeName(org.emftext.language.java.types.TypeReference ref) {
		if (ref instanceof org.emftext.language.java.types.ClassifierReference) {
			org.emftext.language.java.types.ClassifierReference convRef = (org.emftext.language.java.types.ClassifierReference) ref;
			if (convRef.getTarget() instanceof org.emftext.language.java.classifiers.ConcreteClassifier) {
				return ((org.emftext.language.java.classifiers.ConcreteClassifier) convRef.getTarget()).getQualifiedName();
			} else if (convRef.getTarget() instanceof org.emftext.language.java.types.InferableType) {
				return "var";
			} else {
				return ((org.emftext.language.java.generics.TypeParameter) convRef.getTarget()).getName();
			}
		} else if (ref instanceof org.emftext.language.java.types.NamespaceClassifierReference) {
			org.emftext.language.java.types.NamespaceClassifierReference nRef = (org.emftext.language.java.types.NamespaceClassifierReference) ref;
			if (nRef.getClassifierReferences().size() > 0) {
				return convertToTypeName(nRef.getClassifierReferences().get(nRef.getClassifierReferences().size() - 1));
			}
			return nRef.getNamespacesAsString();
		} else if (ref instanceof org.emftext.language.java.types.Boolean) {
			return "boolean";
		} else if (ref instanceof org.emftext.language.java.types.Byte) {
			return "byte";
		} else if (ref instanceof org.emftext.language.java.types.Char) {
			return "char";
		} else if (ref instanceof org.emftext.language.java.types.Double) {
			return "double";
		} else if (ref instanceof org.emftext.language.java.types.Float) {
			return "float";
		} else if (ref instanceof org.emftext.language.java.types.Int) {
			return "int";
		} else if (ref instanceof org.emftext.language.java.types.Long) {
			return "long";
		} else if (ref instanceof org.emftext.language.java.types.Short) {
			return "short";
		} else {
			return "void";
		}
	}
	
	static org.emftext.language.java.members.Constructor getConstructor(IMethodBinding binding) {
		String methName = convertToMethodName(binding);
		if (methBindToConstr.containsKey(methName)) {
			return methBindToConstr.get(methName);
		} else {
			methodBindings.add(binding);
			org.emftext.language.java.members.Constructor result = null;
			org.emftext.language.java.classifiers.ConcreteClassifier potClass =
				(org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(binding.getDeclaringClass());
			if (potClass != null) {
				outerLoop: for (org.emftext.language.java.members.Member mem : potClass.getMembers()) {
					if (mem instanceof org.emftext.language.java.members.Constructor && mem.getName().equals(binding.getName())) {
						org.emftext.language.java.members.Constructor con = (org.emftext.language.java.members.Constructor) mem;
						int receiveOffset = 0;
						if (binding.getDeclaredReceiverType() != null) {
							receiveOffset = 1;
						}
						if (con.getParameters().size() == binding.getParameterTypes().length + receiveOffset) {
							if (receiveOffset == 1 && !(con.getParameters().get(0) instanceof org.emftext.language.java.parameters.ReceiverParameter
								&& convertToTypeName(binding.getDeclaredReceiverType()).equals(
									convertToTypeName(con.getParameters().get(0).getTypeReference())))) {
								continue outerLoop;
							}
							for (int i = 0; i < binding.getParameterTypes().length; i++) {
								ITypeBinding currentType = binding.getParameterTypes()[i];
								org.emftext.language.java.parameters.Parameter currentParam = con.getParameters().get(i + receiveOffset);
								if (!convertToTypeName(currentType).equals(convertToTypeName(currentParam.getTypeReference()))
									|| currentType.getDimensions()
									!= currentParam.getTypeReference().getArrayDimension()) {
									continue outerLoop;
								}
							}
							result = con;
							break;
						}
					}
				}
			}
			if (result == null) {
				result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createConstructor();
				org.emftext.language.java.statements.Block block =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBlock();
				block.setName("");
				result.setBlock(block);
			}
			methBindToConstr.put(methName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.Constructor getConstructor(String methName) {
		if (methBindToConstr.containsKey(methName)) {
			return methBindToConstr.get(methName);
		} else {
			org.emftext.language.java.members.Constructor result =
				org.emftext.language.java.members.MembersFactory.eINSTANCE.createConstructor();
			org.emftext.language.java.statements.Block block =
				org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBlock();
			block.setName("");
			result.setBlock(block);
			methBindToConstr.put(methName, result);
			return result;
		}
	}
	
	
	static org.emftext.language.java.members.Method getMethod(IMethodBinding binding) {
		if (binding.getDeclaringClass().isInterface()) {
			return getInterfaceMethod(binding);
		} else {
			return getClassMethod(binding);
		}
	}
	
	static org.emftext.language.java.classifiers.Class getClass(String typeName) {
		if (typeBindToClass.containsKey(typeName)) {
			return typeBindToClass.get(typeName);
		} else {
			org.emftext.language.java.classifiers.Class result;
			org.emftext.language.java.classifiers.ConcreteClassifier potClass = JavaClasspath.get().getConcreteClassifier(typeName);
			if (potClass instanceof org.emftext.language.java.classifiers.Class) {
				result = (org.emftext.language.java.classifiers.Class) potClass;
			} else {
				result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			}
			typeBindToClass.put(typeName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.classifiers.AnonymousClass getAnonymousClass(String typeName) {
		if (nameToAnonymousClass.containsKey(typeName)) {
			return nameToAnonymousClass.get(typeName);
		} else {
			org.emftext.language.java.classifiers.AnonymousClass result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createAnonymousClass();
			nameToAnonymousClass.put(typeName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.classifiers.AnonymousClass getAnonymousClass(ITypeBinding binding) {
		String typeName = convertToTypeName(binding);
		return getAnonymousClass(typeName);
	}
	
	private static String convertToFieldName(IVariableBinding binding) {
		if (binding == null || !binding.isField()) {
			return "";
		}
		if (nameCache.containsKey(binding)) {
			return nameCache.get(binding);
		}
		String name = convertToTypeName(binding.getDeclaringClass()) + "::" + binding.getName();
		nameCache.put(binding, name);
		return name;
	}
	
	static org.emftext.language.java.members.Field getField(String name) {
		if (nameToField.containsKey(name)) {
			return nameToField.get(name);
		} else {
			org.emftext.language.java.members.Field result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createField();
			nameToField.put(name, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.Field getField(IVariableBinding binding) {
		String varName = convertToFieldName(binding);
		if (nameToField.containsKey(varName)) {
			return nameToField.get(varName);
		} else {
			variableBindings.add(binding);
			org.emftext.language.java.classifiers.ConcreteClassifier potClass = null;
			if (binding.getDeclaringClass() != null) {
				potClass = (org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(binding.getDeclaringClass());
			}
			org.emftext.language.java.members.Field result = null;
			if (potClass != null) {
				for (org.emftext.language.java.members.Member mem : potClass.getMembers()) {
					if (mem instanceof org.emftext.language.java.members.Field && mem.getName().equals(binding.getName())) {
						result = (org.emftext.language.java.members.Field) mem;
						break;
					}
				}
			}
			if (result == null) {
				result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createField();
				result.setTypeReference(org.emftext.language.java.types.TypesFactory.eINSTANCE.createInt());
			}
			nameToField.put(varName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.EnumConstant getEnumConstant(IVariableBinding binding) {
		String enumCN = convertToFieldName(binding);
		if (nameToEnumConst.containsKey(enumCN)) {
			return nameToEnumConst.get(enumCN);
		} else {
			variableBindings.add(binding);
			org.emftext.language.java.classifiers.Enumeration potPar = getEnumeration(binding.getDeclaringClass());
			org.emftext.language.java.members.EnumConstant result = null;
			if (potPar != null) {
				for (org.emftext.language.java.members.EnumConstant con : potPar.getConstants()) {
					if (con.getName().equals(binding.getName())) {
						result = con;
						break;
					}
				}
			}
			if (result == null) {
				result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createEnumConstant();
			}
			nameToEnumConst.put(enumCN, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.EnumConstant getEnumConstant(String enumCN) {
		if (nameToEnumConst.containsKey(enumCN)) {
			return nameToEnumConst.get(enumCN);
		} else {
			org.emftext.language.java.members.EnumConstant result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createEnumConstant();
			nameToEnumConst.put(enumCN, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.AdditionalField getAdditionalField(String name) {
		if (nameToAddField.containsKey(name)) {
			return nameToAddField.get(name);
		} else {
			org.emftext.language.java.members.AdditionalField result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createAdditionalField();
			nameToAddField.put(name, result);
			return result;
		}
	}
	
	static org.emftext.language.java.members.AdditionalField getAdditionalField(IVariableBinding binding) {
		String varName = convertToFieldName(binding);
		if (nameToAddField.containsKey(varName)) {
			return nameToAddField.get(varName);
		} else {
			variableBindings.add(binding);
			org.emftext.language.java.members.AdditionalField result = null;
			org.emftext.language.java.classifiers.ConcreteClassifier potClass =
				(org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(binding.getDeclaringClass());
			if (potClass != null) {
				outerLoop: for (org.emftext.language.java.members.Member mem : potClass.getMembers()) {
					if (mem instanceof org.emftext.language.java.members.Field) {
						org.emftext.language.java.members.Field field = (org.emftext.language.java.members.Field) mem;
						for (org.emftext.language.java.members.AdditionalField af : field.getAdditionalFields()) {
							if (af.getName().equals(binding.getName())) {
								result = af;
								break outerLoop;
							}
						}
					}
				}
			}
			if (result == null) {
				result = org.emftext.language.java.members.MembersFactory.eINSTANCE.createAdditionalField();
			}
			nameToAddField.put(varName, result);
			return result;
		}
	}
	
	private static String convertToParameterName(IVariableBinding binding, boolean register) {
		if (binding == null) {
			return "";
		}
		if (nameCache.containsKey(binding)) {
			return nameCache.get(binding);
		}
		String prefix = "";
		if (binding.getDeclaringMethod() != null) {
			prefix = convertToMethodName(binding.getDeclaringMethod());
		} else if (varBindToUid.containsKey(binding)) {
			prefix = varBindToUid.get(binding) + "";
		} else {
			prefix = uid + "";
			if (register) {
				varBindToUid.put(binding, uid);
			}
		}
		String name = prefix + "::" + binding.getName() + "::" + binding.getVariableId() + binding.hashCode();
		nameCache.put(binding, name);
		return name;
	}
	
	static org.emftext.language.java.variables.LocalVariable getLocalVariable(IVariableBinding binding) {
		variableBindings.add(binding);
		return getLocalVariable(convertToParameterName(binding, true));
	}
	
	static org.emftext.language.java.variables.LocalVariable getLocalVariable(String varName) {
		if (nameToLocVar.containsKey(varName)) {
			return nameToLocVar.get(varName);
		} else {
			org.emftext.language.java.variables.LocalVariable result = org.emftext.language.java.variables.VariablesFactory.eINSTANCE.createLocalVariable();
			nameToLocVar.put(varName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.variables.AdditionalLocalVariable getAdditionalLocalVariable(IVariableBinding binding) {
		variableBindings.add(binding);
		return getAdditionalLocalVariable(convertToParameterName(binding, true));
	}
	
	static org.emftext.language.java.variables.AdditionalLocalVariable getAdditionalLocalVariable(String varName) {
		if (nameToAddLocVar.containsKey(varName)) {
			return nameToAddLocVar.get(varName);
		} else {
			org.emftext.language.java.variables.AdditionalLocalVariable result = org.emftext.language.java.variables.VariablesFactory.eINSTANCE.createAdditionalLocalVariable();
			nameToAddLocVar.put(varName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.parameters.OrdinaryParameter getOrdinaryParameter(IVariableBinding binding) {
		variableBindings.add(binding);
		String paramName = convertToParameterName(binding, true);
		return getOrdinaryParameter(paramName);
	}
	
	static org.emftext.language.java.parameters.OrdinaryParameter getOrdinaryParameter(String paramName) {
		if (nameToOrdParam.containsKey(paramName)) {
			return nameToOrdParam.get(paramName);
		} else {
			org.emftext.language.java.parameters.OrdinaryParameter result = org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createOrdinaryParameter();
			nameToOrdParam.put(paramName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.parameters.VariableLengthParameter getVariableLengthParameter(IVariableBinding binding) {
		String paramName = convertToParameterName(binding, true);
		if (nameToVarLenParam.containsKey(paramName)) {
			return nameToVarLenParam.get(paramName);
		} else {
			variableBindings.add(binding);
			org.emftext.language.java.parameters.VariableLengthParameter result = org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createVariableLengthParameter();
			nameToVarLenParam.put(paramName, result);
			return result;
		}
	}
	
	static org.emftext.language.java.parameters.CatchParameter getCatchParameter(IVariableBinding binding) {
		variableBindings.add(binding);
		return getCatchParameter(convertToParameterName(binding, true));
	}
	
	static org.emftext.language.java.parameters.CatchParameter getCatchParameter(String paramName) {
		if (nameToCatchParam.containsKey(paramName)) {
			return nameToCatchParam.get(paramName);
		} else {
			org.emftext.language.java.parameters.CatchParameter result = org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createCatchParameter();
			nameToCatchParam.put(paramName, result);
			return result;
		}
	}
	
	static void prepareNextUid() {
		uid++;
	}
	
	static org.emftext.language.java.references.ReferenceableElement getReferencableElement(IVariableBinding binding) {
		if (binding.isEnumConstant()) {
			return getEnumConstant(binding);
		} else if (binding.isField()) {
			String fieldName = convertToFieldName(binding);
			if (nameToField.containsKey(fieldName)) {
				return nameToField.get(fieldName);
			} else if (nameToAddField.containsKey(fieldName)) {
				return nameToAddField.get(fieldName);
			} else {
				return getField(binding);
			}
		} else if (binding.isParameter()) {
			String paramName = convertToParameterName(binding, false);
			if (nameToOrdParam.containsKey(paramName)) {
				return nameToOrdParam.get(paramName);
			} else if (nameToVarLenParam.containsKey(paramName)) {
				return nameToVarLenParam.get(paramName);
			} else {
				return getOrdinaryParameter(binding);
			}
		} else {
			String paramName = convertToParameterName(binding, false);
			if (nameToCatchParam.containsKey(paramName)) {
				return nameToCatchParam.get(paramName);
			} else if (nameToLocVar.containsKey(paramName)) {
				return nameToLocVar.get(paramName);
			} else if (nameToAddLocVar.containsKey(paramName)) {
				return nameToAddLocVar.get(paramName);
			} else if (nameToOrdParam.containsKey(paramName)) {
				return nameToOrdParam.get(paramName);
			} else {
				return getLocalVariable(binding);
			}
		}
	}
	
	static org.emftext.language.java.references.ReferenceableElement getReferenceableElementByNameMatching(String name) {
		IVariableBinding vBinding = variableBindings.stream().filter(var -> var != null &&
			var.getName().equals(name)).findFirst().orElse(null);
		if (vBinding != null) {
			return getReferencableElement(vBinding);
		}
		IMethodBinding mBinding = methodBindings.stream().filter(meth -> !meth.isConstructor() && meth.getName().equals(name))
			.findFirst().orElse(null);
		if (mBinding != null) {
			return getMethod(mBinding);
		}
		ITypeBinding tBinding = typeBindings.stream().filter(type -> type != null &&
			type.getName().equals(name)).findFirst().orElse(null);
		if (tBinding != null) {
			return getClassifier(tBinding);
		}
		org.emftext.language.java.variables.Variable par = nameToCatchParam.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (par != null) {
			return par;
		}
		par = nameToLocVar.values().stream().filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (par != null) {
			return par;
		}
		org.emftext.language.java.variables.AdditionalLocalVariable addLocVar = nameToAddLocVar.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (addLocVar != null) {
			return addLocVar;
		}
		par = nameToVarLenParam.values().stream().filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (par != null) {
			return par;
		}
		par = nameToOrdParam.values().stream().filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (par != null) {
			return par;
		}
		org.emftext.language.java.members.EnumConstant enumConst = nameToEnumConst.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (enumConst != null) {
			return enumConst;
		}
		org.emftext.language.java.members.Field field = nameToField.values().stream()
			.filter(param -> param != null && param.getName().equals(name)).findFirst().orElse(null);
		if (field != null) {
			return field;
		}
		org.emftext.language.java.members.AdditionalField addField = nameToAddField.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (addField != null) {
			return addField;
		}
		org.emftext.language.java.members.Method meth = methBindToCM.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (meth != null) {
			return meth;
		}
		meth = methBindToInter.values().stream().filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (meth != null) {
			return meth;
		}
		org.emftext.language.java.classifiers.Classifier c = typeBindToTP.values().stream()
			.filter(param -> param.getName().equals(name)).findFirst().orElse(null);
		if (c != null) {
			return c;
		}
		c = typeBindToEnum.values().stream().filter(param -> name.equals(param.getName())).findFirst().orElse(null);
		if (c != null) {
			return c;
		}
		c = typeBindToAnnot.values().stream().filter(param -> name.equals(param.getName())).findFirst().orElse(null);
		if (c != null) {
			return c;
		}
		c = typeBindToClass.values().stream().filter(param -> name.equals(param.getName())).findFirst().orElse(null);
		if (c != null) {
			return c;
		}
		c = typeBindToInterface.values().stream().filter(param -> name.equals(param.getName())).findFirst().orElse(null);
		if (c != null) {
			return c;
		}
		return JDTResolverUtility.getClass(name);
	}
	
	@SuppressWarnings("unused")
	static void completeResolution() {
		nameToEnumConst.forEach((constName, enConst) -> {
			if (enConst.eContainer() == null) {
				IVariableBinding varBind = variableBindings.stream().filter(var -> var != null &&
					constName.equals(convertToFieldName(var))).findFirst().get();
				if (!varBind.getDeclaringClass().isAnonymous()) {
					var en = getEnumeration(varBind.getDeclaringClass());
					if (!extractAdditionalInformationFromTypeBindings && !en.getConstants().contains(enConst)) {
						en.getConstants().add(enConst);
					}
				}
			}
		});
		
		nameToField.forEach((fieldName, field) -> {
			if (field.eContainer() == null) {
				IVariableBinding varBind = variableBindings.stream().filter(var -> var != null &&
					fieldName.equals(convertToFieldName(var))).findFirst().orElse(null);
				if (varBind == null || varBind.getDeclaringClass() == null) {
					addToSyntheticClass(field);
				} else {
					org.emftext.language.java.classifiers.Classifier cla = getClassifier(varBind.getDeclaringClass());
					if (cla == null) {
						String typeName = convertToTypeName(varBind.getDeclaringClass());
						if (nameToAnonymousClass.containsKey(typeName)) {
							org.emftext.language.java.classifiers.AnonymousClass anonClass = nameToAnonymousClass.get(typeName);
							if (!anonClass.getMembers().contains(field)) {
								anonClass.getMembers().add(field);
							}
						} else {
							addToSyntheticClass(field);
						}
					} else if (!extractAdditionalInformationFromTypeBindings
							&& cla instanceof org.emftext.language.java.classifiers.ConcreteClassifier) {
						var i = (org.emftext.language.java.classifiers.ConcreteClassifier) cla;
						if (!i.getMembers().contains(field)) {
							i.getMembers().add(field);
						}
					}
				}
			}
		});
		
		methBindToConstr.forEach(JDTResolverUtility::completeMethod);
		
		methBindToInter.forEach(JDTResolverUtility::completeMethod);
		
		methBindToCM.forEach(JDTResolverUtility::completeMethod);
		
		convertPureTypeBindings();

		escapeAllIdentifiers();
		
		modBindToMod.clear();
		nameToPackage.clear();
		typeBindToAnnot.clear();
		typeBindToEnum.clear();
		typeBindToInterface.clear();
		typeBindToClass.clear();
		typeBindToTP.clear();
		methBindToInter.clear();
		methBindToCM.clear();
		methBindToConstr.clear();
		nameToField.clear();
		nameToAddField.clear();
		nameToLocVar.clear();
		nameToAddLocVar.clear();
		nameToEnumConst.clear();
		nameToVarLenParam.clear();
		nameToOrdParam.clear();
		nameToCatchParam.clear();
		moduleBindings.clear();
		packageBindings.clear();
		typeBindings.clear();
		methodBindings.clear();
		variableBindings.clear();
		uid = 0;
		varBindToUid.clear();
		objVisited.clear();
		nameCache.clear();
		nameToAnonymousClass.clear();
	}
	
	@SuppressWarnings("unused")
	private static void completeMethod(String methodName, org.emftext.language.java.members.Member method) {
		if (method.eContainer() == null) {
			IMethodBinding methBind = methodBindings.stream().filter(meth -> methodName.equals(convertToMethodName(meth)))
				.findFirst().orElse(null);
			if (methBind != null) {
				org.emftext.language.java.classifiers.Classifier cla = getClassifier(methBind.getDeclaringClass());
				if (cla == null) {
					String typeName = convertToTypeName(methBind.getDeclaringClass());
					if (nameToAnonymousClass.containsKey(typeName)) {
						org.emftext.language.java.classifiers.AnonymousClass anonClass = nameToAnonymousClass.get(typeName);
						if (!anonClass.getMembers().contains(method)) {
							anonClass.getMembers().add(method);
						}
					} else {
						addToSyntheticClass(method);
					}
				} else if (!extractAdditionalInformationFromTypeBindings
						&& cla instanceof org.emftext.language.java.classifiers.ConcreteClassifier) {
					var i = (org.emftext.language.java.classifiers.ConcreteClassifier) cla;
					if(!i.getMembers().contains(method)) {
						i.getMembers().add(method);
					}
				}
			} else {
				addToSyntheticClass(method);
			}
		}
	}
	
	private static void addToSyntheticClass(org.emftext.language.java.members.Member member) {
		org.emftext.language.java.classifiers.Class container = getClass(SYNTH_CLASS);
		container.setName(SYNTH_CLASS);
		if (!container.getMembers().contains(member)) {
			container.getMembers().add(member);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void convertPureTypeBindings() {
		int oldSize;
		int newSize = typeBindToAnnot.size() + typeBindToEnum.size() + typeBindToInterface.size() + typeBindToClass.size()
			+ modBindToMod.size() + nameToPackage.size();
		do {
			oldSize = newSize;
			HashMap<String, ? extends org.emftext.language.java.classifiers.ConcreteClassifier> map =
				(HashMap<String, org.emftext.language.java.classifiers.Annotation>) typeBindToAnnot.clone();
			map.forEach(JDTResolverUtility::convertPureTypeBinding);
			map = (HashMap<String, org.emftext.language.java.classifiers.Enumeration>) typeBindToEnum.clone();
			map.forEach(JDTResolverUtility::convertPureTypeBinding);
			map = (HashMap<String, org.emftext.language.java.classifiers.Interface>) typeBindToInterface.clone();
			map.forEach(JDTResolverUtility::convertPureTypeBinding);
			map = (HashMap<String, org.emftext.language.java.classifiers.Class>) typeBindToClass.clone();
			map.forEach(JDTResolverUtility::convertPureTypeBinding);
			HashMap<String, org.emftext.language.java.containers.Package> mapP =
				(HashMap<String, org.emftext.language.java.containers.Package>) nameToPackage.clone();
			mapP.forEach(JDTResolverUtility::convertPurePackageBinding);
			HashMap<String, org.emftext.language.java.containers.Module> mapM =
				(HashMap<String, org.emftext.language.java.containers.Module>) modBindToMod.clone();
			mapM.forEach(JDTResolverUtility::convertPureModuleBinding);
			newSize = typeBindToAnnot.size() + typeBindToEnum.size() + typeBindToInterface.size() + typeBindToClass.size()
				+ modBindToMod.size() + nameToPackage.size();
		} while (oldSize < newSize);
	}
	
	private static void convertPureTypeBinding(String typeName, org.emftext.language.java.classifiers.ConcreteClassifier classifier) {
		if (objVisited.contains(classifier)) {
			return;
		}
		objVisited.add(classifier);
		org.emftext.language.java.classifiers.ConcreteClassifier potClass =
			JavaClasspath.get().getConcreteClassifier(typeName);
		if (potClass == classifier) {
			return;
		}
		ITypeBinding typeBind = typeBindings.stream().filter(type -> type != null && typeName.equals(convertToTypeName(type)))
			.findFirst().orElse(null);
		if (typeBind == null) {
			classifier.setPackage(getPackage(""));
			if (classifier.eContainer() != null) {
				return;
			}
		} else {
			if (typeBind.isTopLevel()) {
				JDTBindingConverterUtility.convertToConcreteClassifier(typeBind, extractAdditionalInformationFromTypeBindings);
			} else if (typeBind.isNested()) {
				org.emftext.language.java.classifiers.ConcreteClassifier parentClassifier =
					(org.emftext.language.java.classifiers.ConcreteClassifier) getClassifier(typeBind.getDeclaringClass());
				convertPureTypeBinding(convertToTypeName(typeBind.getDeclaringClass()), parentClassifier);
				classifier.setPackage(getPackage(typeBind.getPackage()));
			} else if (typeBind.isArray()) {
				ITypeBinding elementType = typeBind.getElementType();
				if (!elementType.isPrimitive() && !elementType.isTypeVariable()) {
					convertPureTypeBinding(typeName, (org.emftext.language.java.classifiers.ConcreteClassifier)
						getClassifier(elementType));
				}
			}
		}
		if (classifier.eContainer() == null) {
			org.emftext.language.java.containers.CompilationUnit cu = org.emftext.language.java.containers.ContainersFactory.eINSTANCE.createCompilationUnit();
			cu.setName("");
			cu.getClassifiers().add(classifier);
			String[] namespaces = typeName.substring(0, typeName.length()).split("\\.");
			classifier.setName(namespaces[namespaces.length - 1]);
			for (int index = 0; index < namespaces.length - 1; index++) {
				cu.getNamespaces().add(namespaces[index]);
			}
			Resource newResource = resourceSet.createResource(URI.createHierarchicalURI("empty",
				"JaMoPP-CompilationUnit", null, new String[] {typeName + ".java"}, null, null));
			newResource.getContents().add(cu);
		}
	}
	
	private static void convertPurePackageBinding(String packageName, org.emftext.language.java.containers.Package pack) {
		if (objVisited.contains(pack)) {
			return;
		}
		objVisited.add(pack);
		org.emftext.language.java.containers.Package potPack = JavaClasspath.get().getPackage(packageName);
		if (potPack == pack) {
			return;
		}
		IPackageBinding binding = packageBindings.stream().filter(b -> packageName.equals(b.getName())).findFirst().orElse(null);
		if (binding == null) {
			pack.setName("");
			pack.setModule(getModule(""));
		} else {
			JDTBindingConverterUtility.convertToPackage(binding);
		}
		if (pack.eResource() != null) {
			return;
		}
		Resource newResource = resourceSet.createResource(URI.createHierarchicalURI("empty", "JaMoPP-Package", null,
			new String[] {packageName, "package-info.java"}, null, null));
		newResource.getContents().add(pack);
	}
	
	private static void convertPureModuleBinding(String modName, org.emftext.language.java.containers.Module module) {
		if (objVisited.contains(module)) {
			return;
		}
		objVisited.add(module);
		org.emftext.language.java.containers.Module potMod = JavaClasspath.get().getModule(modName);
		if (potMod == module) {
			return;
		}
		if (module.eResource() != null) {
			return;
		}
		IModuleBinding binding = moduleBindings.stream().filter(b -> modName.equals(b.getName())).findFirst().orElse(null);
		if (binding == null) {
			module.getNamespaces().clear();
			String[] parts = modName.split("\\.");
			for (String part : parts) {
				module.getNamespaces().add(part);
			}
			module.setName("");
		} else {
			JDTBindingConverterUtility.convertToModule(binding);
		}
		Resource newResource = resourceSet.createResource(URI.createHierarchicalURI("empty", "JaMoPP-Module", null,
			new String[] {modName, "module-info.java"}, null, null));
		newResource.getContents().add(module);
	}
	
	private static void escapeAllIdentifiers() {
		modBindToMod.values().forEach(JDTResolverUtility::escapeIdentifiers);
		nameToPackage.values().forEach(JDTResolverUtility::escapeIdentifiers);
		typeBindToAnnot.values().forEach(JDTResolverUtility::escapeIdentifiers);
		typeBindToEnum.values().forEach(JDTResolverUtility::escapeIdentifiers);
		typeBindToClass.values().forEach(JDTResolverUtility::escapeIdentifiers);
		typeBindToInterface.values().forEach(JDTResolverUtility::escapeIdentifiers);
	}
	
	private static void escapeIdentifiers(EObject obj) {
		obj.eAllContents().forEachRemaining(JDTResolverUtility::escapeIdentifier);
	}
	
	private static void escapeIdentifier(Notifier not) {
		if (not instanceof org.emftext.language.java.commons.NamedElement) {
			org.emftext.language.java.commons.NamedElement ele = (org.emftext.language.java.commons.NamedElement) not;
			StringBuilder builder = new StringBuilder();
			String name = ele.getName();
			name.codePoints().forEach(i -> {
				if (i <= 0x20 || (Character.MIN_SURROGATE <= i && i <= Character.MAX_SURROGATE)) {
					builder.append("\\u" + String.format("%04x", i));
				} else {
					builder.appendCodePoint(i);
				}
			});
			ele.setName(builder.toString());
		}
	}
}
