/*
 * Copyright 2023 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.junit5.engine;

import java.lang.reflect.Method;
import java.util.Optional;
import org.embulk.junit5.api.EmbulkPluginTest;
import org.embulk.plugin.PluginClassLoader;
import org.embulk.plugin.PluginClassLoaderFactory;
import org.embulk.plugin.PluginClassLoaderFactoryImpl;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

public final class EmbulkPluginTestEngine extends HierarchicalTestEngine<EmbulkPluginTestEngineExecutionContext> {
    public EmbulkPluginTestEngine() {
        super();
        final Class<?> klass = this.getClass();
        this.klassLoader = klass.getClassLoader();
        logger.info(() -> "Initializing EmbulkPluginTestEngine@" + Integer.toHexString(this.hashCode()));
        logger.info(() -> "EmbulkPluginTestEngine's ClassLoader: " + this.klassLoader.toString());
        this.pluginClassLoaderFactory = PluginClassLoaderFactoryImpl.of();
        this.pluginClassLoader = null;
    }

    /**
     * Returns {@code "embulk-junit5-engine"} as the engine ID.
     */
    @Override
    public final String getId() {
        return "embulk-junit5-engine";
    }

    /**
     * Discover tests according to the supplied EngineDiscoveryRequest.
     *
     * The supplied UniqueId must be used as the unique ID of the returned root TestDescriptor.
     * In addition, the UniqueId must be used to create unique IDs for children of the root's descriptor
     * by calling UniqueId.append(java.lang.String, java.lang.String).
     */
    @Override
    public TestDescriptor discover(final EngineDiscoveryRequest discoveryRequest, final UniqueId uniqueId) {
        logger.trace(() -> "EngineDiscoveryRequest: " + discoveryRequest.toString());
        if (discoveryRequest instanceof LauncherDiscoveryRequest) {
            final LauncherDiscoveryRequest launcherDiscoveryRequest = (LauncherDiscoveryRequest) discoveryRequest;
            logger.trace(() -> "  EngineFilters: " + launcherDiscoveryRequest.getEngineFilters());
            logger.trace(() -> "  PostDiscoveryFilters: " + launcherDiscoveryRequest.getPostDiscoveryFilters());
        }
        logger.trace(() -> "  ConfigurationParameters: " + discoveryRequest.getConfigurationParameters());
        logger.trace(() -> "UniqueId: " + uniqueId.toString());

        final EmbulkPluginTestEngineDescriptor engineDescriptor = new EmbulkPluginTestEngineDescriptor(uniqueId);

        discoveryRequest.getSelectorsByType(ClassSelector.class).forEach(classSelector -> {
            // If Gradle constructs ClassSelector instances by class names, not by class objects,
            // test classes are not loaded on the top-level class loader that is the same with the TestEngine.
            //
            // v7.6.3
            // https://github.com/gradle/gradle/blob/v7.6.3/subprojects/testing-junit-platform/src/main/java/org/gradle/api/internal/tasks/testing/junitplatform/JUnitPlatformTestClassProcessor.java#L87-L94
            //
            // The situation might be better in Gradle v8?
            // https://github.com/gradle/gradle/blob/v8.5.0/platforms/jvm/testing-junit-platform/src/main/java/org/gradle/api/internal/tasks/testing/junitplatform/JUnitPlatformTestClassProcessor.java#L78-L85

            // final Class<?> testClass = classSelector.getJavaClass();  // Not to get the Java class directly!
            final String testClassName = classSelector.getClassName();
            try {
                System.out.println(Class.forName("org.embulk.input.junit5example.ExampleInputPlugin"));
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            try {
                System.out.println(Class.forName("org.embulk.input.junit5example.TestExample"));
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            try {
                System.out.println(Class.forName("org.embulk.input.junit5example.TestExample1"));
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            try {
                System.out.println(Class.forName("org.embulk.util.config.Config"));
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
            final Class<?> testClass = findOrLoadClassFrom(this.klassLoader, testClassName);

            final TestDescriptor classDescriptor =
                    new ClassTestDescriptor(uniqueId.append("class", testClass.getName()), testClass);
            System.out.println(classDescriptor);

            for (final Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(EmbulkPluginTest.class)) {
                    final MethodTestDescriptor methodDescriptor =
                            new MethodTestDescriptor(uniqueId.append("method", method.getName()), testClass, method);
                    classDescriptor.addChild(methodDescriptor);
                }
            }

            if (!classDescriptor.getChildren().isEmpty()) {
                engineDescriptor.addChild(classDescriptor);
            }
        });

        return engineDescriptor;
    }

    /**
     * Returns {@code "org.embulk"} as the artifact ID.
     */
    @Override
    public final Optional<String> getGroupId() {
        return Optional.of("org.embulk");
    }

    /**
     * Returns {@code "embulk-junit5-engine"} as the artifact ID.
     */
    @Override
    public final Optional<String> getArtifactId() {
        return Optional.of("embulk-junit5-engine");
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService​(final ExecutionRequest request) {
        return new SameThreadHierarchicalTestExecutorService();
    }

    @Override
    protected ThrowableCollector.Factory createThrowableCollectorFactory​(final ExecutionRequest request) {
        return OpenTest4JAwareThrowableCollector::new;
    }

    @Override
    protected EmbulkPluginTestEngineExecutionContext createExecutionContext​(final ExecutionRequest request) {
        return new EmbulkPluginTestEngineExecutionContext();
    }

    private static Class<?> findOrLoadClassFrom(final ClassLoader klassLoader, final String name) {
        final Class<?> foundClass = LoadedClassFinder.findFrom(klassLoader, name);
        if (foundClass != null) {
            logger.info(() -> "<" + name + "> has been already loaded in [" + klassLoader + "]: " + foundClass.toString());
            return foundClass;
        }

        logger.info(() -> "<" + name + "> has not been loaded in [" + klassLoader + "].");
        try {
            return klassLoader.loadClass(name);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(EmbulkPluginTestEngine.class);

    private final ClassLoader klassLoader;

    private final PluginClassLoaderFactory pluginClassLoaderFactory;

    private final PluginClassLoader pluginClassLoader;
}
