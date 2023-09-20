import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

import styles from './index.module.css';

function FeatureDisplay() {
    return (
        <div className={clsx('container text--center', styles.features)}>
            <h2>Features</h2>
            <div className="row">
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Model</h3>
                    <p>The extended JaMoPP provides an <a href="https://eclipse.dev/modeling/emf/" target="_blank">Ecore</a>-based meta-model for the <a href="https://www.oracle.com/java/" target="_blank">Java programming language</a> covering features of all versions up to <a href="https://docs.oracle.com/javase/specs/" target="_blank">Java 15</a>.</p>
                </div>
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Parser</h3>
                    <p>A parser employs the <a href="https://projects.eclipse.org/projects/eclipse.jdt" target="_blank">Eclipse Java Development Tools</a> to actually parse Java source code into ASTs and transforms the ASTs into models conforming to the extended JaMoPP's meta-model.</p>
                </div>
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Printer</h3>
                    <p>Java models can be again printed as Java code.</p>
                </div>
            </div>
            <div className="row">
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Class Files</h3>
                    <p>With the help of the <a href="https://commons.apache.org/proper/commons-bcel/" target="_blank">Apache Commons Byte Code Engineering Library</a>, class files can also be parsed and converted to Java models.</p>
                </div>
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Resolution</h3>
                    <p>References between Java models (e.g., imports, method calls to methods) are resolved by one of three reference resolution variants.</p>
                </div>
                <div className={clsx('col', styles.singleFeature)}>
                    <h3>Recovery</h3>
                    <p>Missing dependencies and unresolvable references can be recovered with a trivial recovery strategy.</p>
                </div>
            </div>
            <div className="row">
                <div className={clsx('col col--4 col--offset-4', styles.singleFeature)}>
                    <h3>Open Source</h3>
                    <p>The extended JaMoPP is licensed under the open source <a href="https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/blob/develop/LICENSE.txt" target="_blank">Eclipse Public License 1.0</a>. It is developed at the <a href="https://mcse.kastel.kit.edu/" target="_blank">Modelling for Continuous Software Engineering (MCSE) group</a> at KIT (independently from the original authors).</p>
                </div>
            </div>
        </div>
    );
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className="hero hero--primary">
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/getting-started">
                        Get Started
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    return (
        <Layout>
            <HomepageHeader />
            <main>
                <FeatureDisplay />
            </main>
        </Layout>
    );
}
