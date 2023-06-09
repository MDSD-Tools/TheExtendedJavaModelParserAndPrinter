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
 ******************************************************************************/
package tools.mdsd.jamopp.model.java.impl;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.mdsd.jamopp.model.java.JavaFactory;
import tools.mdsd.jamopp.model.java.JavaPackage;
import tools.mdsd.jamopp.model.java.annotations.AnnotationsPackage;
import tools.mdsd.jamopp.model.java.annotations.impl.AnnotationsPackageImpl;
import tools.mdsd.jamopp.model.java.arrays.ArraysPackage;
import tools.mdsd.jamopp.model.java.arrays.impl.ArraysPackageImpl;
import tools.mdsd.jamopp.model.java.classifiers.ClassifiersPackage;
import tools.mdsd.jamopp.model.java.classifiers.impl.ClassifiersPackageImpl;
import tools.mdsd.jamopp.model.java.commons.CommonsPackage;
import tools.mdsd.jamopp.model.java.commons.impl.CommonsPackageImpl;
import tools.mdsd.jamopp.model.java.containers.ContainersPackage;
import tools.mdsd.jamopp.model.java.containers.impl.ContainersPackageImpl;
import tools.mdsd.jamopp.model.java.expressions.ExpressionsPackage;
import tools.mdsd.jamopp.model.java.expressions.impl.ExpressionsPackageImpl;
import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.generics.impl.GenericsPackageImpl;
import tools.mdsd.jamopp.model.java.imports.ImportsPackage;
import tools.mdsd.jamopp.model.java.imports.impl.ImportsPackageImpl;
import tools.mdsd.jamopp.model.java.instantiations.InstantiationsPackage;
import tools.mdsd.jamopp.model.java.instantiations.impl.InstantiationsPackageImpl;
import tools.mdsd.jamopp.model.java.literals.LiteralsPackage;
import tools.mdsd.jamopp.model.java.literals.impl.LiteralsPackageImpl;
import tools.mdsd.jamopp.model.java.members.MembersPackage;
import tools.mdsd.jamopp.model.java.members.impl.MembersPackageImpl;
import tools.mdsd.jamopp.model.java.modifiers.ModifiersPackage;
import tools.mdsd.jamopp.model.java.modifiers.impl.ModifiersPackageImpl;
import tools.mdsd.jamopp.model.java.modules.ModulesPackage;
import tools.mdsd.jamopp.model.java.modules.impl.ModulesPackageImpl;
import tools.mdsd.jamopp.model.java.operators.OperatorsPackage;
import tools.mdsd.jamopp.model.java.operators.impl.OperatorsPackageImpl;
import tools.mdsd.jamopp.model.java.parameters.ParametersPackage;
import tools.mdsd.jamopp.model.java.parameters.impl.ParametersPackageImpl;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;
import tools.mdsd.jamopp.model.java.references.impl.ReferencesPackageImpl;
import tools.mdsd.jamopp.model.java.statements.StatementsPackage;
import tools.mdsd.jamopp.model.java.statements.impl.StatementsPackageImpl;
import tools.mdsd.jamopp.model.java.types.TypesPackage;
import tools.mdsd.jamopp.model.java.types.impl.TypesPackageImpl;
import tools.mdsd.jamopp.model.java.variables.VariablesPackage;
import tools.mdsd.jamopp.model.java.variables.impl.VariablesPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class JavaPackageImpl extends EPackageImpl implements JavaPackage {

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory method
	 * {@link #init init()}, which also performs initialization of the package, or
	 * returns the registered package, if one already exists. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see tools.mdsd.jamopp.model.java.JavaPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private JavaPackageImpl() {
		super(eNS_URI, JavaFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and
	 * for any others upon which it depends. Simple dependencies are satisfied by
	 * calling this method on all dependent packages before doing anything else.
	 * This method drives initialization for interdependent packages directly, in
	 * parallel with this package, itself.
	 * <p>
	 * Of this package and its interdependencies, all packages which have not yet
	 * been registered by their URI values are first created and registered. The
	 * packages are then initialized in two steps: meta-model objects for all of the
	 * packages are created before any are initialized, since one package's
	 * meta-model objects may refer to those of another.
	 * <p>
	 * Invocation of this method will not affect any packages that have already been
	 * initialized. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static JavaPackage init() {
		if (isInited)
			return (JavaPackage) EPackage.Registry.INSTANCE.getEPackage(JavaPackage.eNS_URI);

		// Obtain or create and register package
		JavaPackageImpl theJavaPackage = (JavaPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(eNS_URI) instanceof JavaPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI)
						: new JavaPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		AnnotationsPackageImpl theAnnotationsPackage = (AnnotationsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(AnnotationsPackage.eNS_URI) instanceof AnnotationsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(AnnotationsPackage.eNS_URI)
						: AnnotationsPackage.eINSTANCE);
		ArraysPackageImpl theArraysPackage = (ArraysPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ArraysPackage.eNS_URI) instanceof ArraysPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ArraysPackage.eNS_URI)
						: ArraysPackage.eINSTANCE);
		ClassifiersPackageImpl theClassifiersPackage = (ClassifiersPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ClassifiersPackage.eNS_URI) instanceof ClassifiersPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ClassifiersPackage.eNS_URI)
						: ClassifiersPackage.eINSTANCE);
		CommonsPackageImpl theCommonsPackage = (CommonsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(CommonsPackage.eNS_URI) instanceof CommonsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(CommonsPackage.eNS_URI)
						: CommonsPackage.eINSTANCE);
		ContainersPackageImpl theContainersPackage = (ContainersPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ContainersPackage.eNS_URI) instanceof ContainersPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ContainersPackage.eNS_URI)
						: ContainersPackage.eINSTANCE);
		ExpressionsPackageImpl theExpressionsPackage = (ExpressionsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ExpressionsPackage.eNS_URI) instanceof ExpressionsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ExpressionsPackage.eNS_URI)
						: ExpressionsPackage.eINSTANCE);
		GenericsPackageImpl theGenericsPackage = (GenericsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(GenericsPackage.eNS_URI) instanceof GenericsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(GenericsPackage.eNS_URI)
						: GenericsPackage.eINSTANCE);
		ImportsPackageImpl theImportsPackage = (ImportsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ImportsPackage.eNS_URI) instanceof ImportsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ImportsPackage.eNS_URI)
						: ImportsPackage.eINSTANCE);
		InstantiationsPackageImpl theInstantiationsPackage = (InstantiationsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(InstantiationsPackage.eNS_URI) instanceof InstantiationsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(InstantiationsPackage.eNS_URI)
						: InstantiationsPackage.eINSTANCE);
		LiteralsPackageImpl theLiteralsPackage = (LiteralsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(LiteralsPackage.eNS_URI) instanceof LiteralsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(LiteralsPackage.eNS_URI)
						: LiteralsPackage.eINSTANCE);
		MembersPackageImpl theMembersPackage = (MembersPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(MembersPackage.eNS_URI) instanceof MembersPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(MembersPackage.eNS_URI)
						: MembersPackage.eINSTANCE);
		ModifiersPackageImpl theModifiersPackage = (ModifiersPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ModifiersPackage.eNS_URI) instanceof ModifiersPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ModifiersPackage.eNS_URI)
						: ModifiersPackage.eINSTANCE);
		ModulesPackageImpl theModulesPackage = (ModulesPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ModulesPackage.eNS_URI) instanceof ModulesPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ModulesPackage.eNS_URI)
						: ModulesPackage.eINSTANCE);
		OperatorsPackageImpl theOperatorsPackage = (OperatorsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(OperatorsPackage.eNS_URI) instanceof OperatorsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(OperatorsPackage.eNS_URI)
						: OperatorsPackage.eINSTANCE);
		ParametersPackageImpl theParametersPackage = (ParametersPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ParametersPackage.eNS_URI) instanceof ParametersPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ParametersPackage.eNS_URI)
						: ParametersPackage.eINSTANCE);
		ReferencesPackageImpl theReferencesPackage = (ReferencesPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(ReferencesPackage.eNS_URI) instanceof ReferencesPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(ReferencesPackage.eNS_URI)
						: ReferencesPackage.eINSTANCE);
		StatementsPackageImpl theStatementsPackage = (StatementsPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(StatementsPackage.eNS_URI) instanceof StatementsPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(StatementsPackage.eNS_URI)
						: StatementsPackage.eINSTANCE);
		TypesPackageImpl theTypesPackage = (TypesPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(TypesPackage.eNS_URI) instanceof TypesPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI)
						: TypesPackage.eINSTANCE);
		VariablesPackageImpl theVariablesPackage = (VariablesPackageImpl) (EPackage.Registry.INSTANCE
				.getEPackage(VariablesPackage.eNS_URI) instanceof VariablesPackageImpl
						? EPackage.Registry.INSTANCE.getEPackage(VariablesPackage.eNS_URI)
						: VariablesPackage.eINSTANCE);

		// Create package meta-data objects
		theJavaPackage.createPackageContents();
		theAnnotationsPackage.createPackageContents();
		theArraysPackage.createPackageContents();
		theClassifiersPackage.createPackageContents();
		theCommonsPackage.createPackageContents();
		theContainersPackage.createPackageContents();
		theExpressionsPackage.createPackageContents();
		theGenericsPackage.createPackageContents();
		theImportsPackage.createPackageContents();
		theInstantiationsPackage.createPackageContents();
		theLiteralsPackage.createPackageContents();
		theMembersPackage.createPackageContents();
		theModifiersPackage.createPackageContents();
		theModulesPackage.createPackageContents();
		theOperatorsPackage.createPackageContents();
		theParametersPackage.createPackageContents();
		theReferencesPackage.createPackageContents();
		theStatementsPackage.createPackageContents();
		theTypesPackage.createPackageContents();
		theVariablesPackage.createPackageContents();

		// Initialize created meta-data
		theJavaPackage.initializePackageContents();
		theAnnotationsPackage.initializePackageContents();
		theArraysPackage.initializePackageContents();
		theClassifiersPackage.initializePackageContents();
		theCommonsPackage.initializePackageContents();
		theContainersPackage.initializePackageContents();
		theExpressionsPackage.initializePackageContents();
		theGenericsPackage.initializePackageContents();
		theImportsPackage.initializePackageContents();
		theInstantiationsPackage.initializePackageContents();
		theLiteralsPackage.initializePackageContents();
		theMembersPackage.initializePackageContents();
		theModifiersPackage.initializePackageContents();
		theModulesPackage.initializePackageContents();
		theOperatorsPackage.initializePackageContents();
		theParametersPackage.initializePackageContents();
		theReferencesPackage.initializePackageContents();
		theStatementsPackage.initializePackageContents();
		theTypesPackage.initializePackageContents();
		theVariablesPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theJavaPackage.freeze();

		return theJavaPackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public JavaFactory getJavaFactory() {
		return (JavaFactory) getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package. This method is guarded to
	 * have no affect on any invocation but its first. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated)
			return;
		isCreated = true;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This method is
	 * guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized)
			return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		AnnotationsPackage theAnnotationsPackage = (AnnotationsPackage) EPackage.Registry.INSTANCE
				.getEPackage(AnnotationsPackage.eNS_URI);
		ArraysPackage theArraysPackage = (ArraysPackage) EPackage.Registry.INSTANCE.getEPackage(ArraysPackage.eNS_URI);
		ClassifiersPackage theClassifiersPackage = (ClassifiersPackage) EPackage.Registry.INSTANCE
				.getEPackage(ClassifiersPackage.eNS_URI);
		CommonsPackage theCommonsPackage = (CommonsPackage) EPackage.Registry.INSTANCE
				.getEPackage(CommonsPackage.eNS_URI);
		ContainersPackage theContainersPackage = (ContainersPackage) EPackage.Registry.INSTANCE
				.getEPackage(ContainersPackage.eNS_URI);
		ExpressionsPackage theExpressionsPackage = (ExpressionsPackage) EPackage.Registry.INSTANCE
				.getEPackage(ExpressionsPackage.eNS_URI);
		GenericsPackage theGenericsPackage = (GenericsPackage) EPackage.Registry.INSTANCE
				.getEPackage(GenericsPackage.eNS_URI);
		ImportsPackage theImportsPackage = (ImportsPackage) EPackage.Registry.INSTANCE
				.getEPackage(ImportsPackage.eNS_URI);
		InstantiationsPackage theInstantiationsPackage = (InstantiationsPackage) EPackage.Registry.INSTANCE
				.getEPackage(InstantiationsPackage.eNS_URI);
		LiteralsPackage theLiteralsPackage = (LiteralsPackage) EPackage.Registry.INSTANCE
				.getEPackage(LiteralsPackage.eNS_URI);
		MembersPackage theMembersPackage = (MembersPackage) EPackage.Registry.INSTANCE
				.getEPackage(MembersPackage.eNS_URI);
		ModifiersPackage theModifiersPackage = (ModifiersPackage) EPackage.Registry.INSTANCE
				.getEPackage(ModifiersPackage.eNS_URI);
		ModulesPackage theModulesPackage = (ModulesPackage) EPackage.Registry.INSTANCE
				.getEPackage(ModulesPackage.eNS_URI);
		OperatorsPackage theOperatorsPackage = (OperatorsPackage) EPackage.Registry.INSTANCE
				.getEPackage(OperatorsPackage.eNS_URI);
		ParametersPackage theParametersPackage = (ParametersPackage) EPackage.Registry.INSTANCE
				.getEPackage(ParametersPackage.eNS_URI);
		ReferencesPackage theReferencesPackage = (ReferencesPackage) EPackage.Registry.INSTANCE
				.getEPackage(ReferencesPackage.eNS_URI);
		StatementsPackage theStatementsPackage = (StatementsPackage) EPackage.Registry.INSTANCE
				.getEPackage(StatementsPackage.eNS_URI);
		TypesPackage theTypesPackage = (TypesPackage) EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI);
		VariablesPackage theVariablesPackage = (VariablesPackage) EPackage.Registry.INSTANCE
				.getEPackage(VariablesPackage.eNS_URI);

		// Add subpackages
		getESubpackages().add(theAnnotationsPackage);
		getESubpackages().add(theArraysPackage);
		getESubpackages().add(theClassifiersPackage);
		getESubpackages().add(theCommonsPackage);
		getESubpackages().add(theContainersPackage);
		getESubpackages().add(theExpressionsPackage);
		getESubpackages().add(theGenericsPackage);
		getESubpackages().add(theImportsPackage);
		getESubpackages().add(theInstantiationsPackage);
		getESubpackages().add(theLiteralsPackage);
		getESubpackages().add(theMembersPackage);
		getESubpackages().add(theModifiersPackage);
		getESubpackages().add(theModulesPackage);
		getESubpackages().add(theOperatorsPackage);
		getESubpackages().add(theParametersPackage);
		getESubpackages().add(theReferencesPackage);
		getESubpackages().add(theStatementsPackage);
		getESubpackages().add(theTypesPackage);
		getESubpackages().add(theVariablesPackage);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Create resource
		createResource(eNS_URI);
	}

} // JavaPackageImpl
