/*
 * This file is based on a copy from Gradle 8.10 with modifications.
 *
 * It is licensed under the Apache License, Version 2.0.
 */

/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.junit5;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
// import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.internal.tasks.testing.TestFrameworkDistributionModule;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformSpec;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.testing.TestFilter;
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions;
import org.gradle.internal.jvm.UnsupportedJavaRuntimeException;
import org.gradle.internal.scan.UsedByScanPlugin;
import org.gradle.process.internal.worker.WorkerProcessBuilder;

public class EmbulkJUnitPlatformTestFramework implements TestFramework {
    public EmbulkJUnitPlatformTestFramework(
            final DefaultTestFilter filter,
            final boolean useImplementationDependencies,
            final Provider<Boolean> dryRun) {
        this(filter, useImplementationDependencies, new JUnitPlatformOptions(), dryRun);
    }

    private EmbulkJUnitPlatformTestFramework(
            final DefaultTestFilter filter,
            final boolean useImplementationDependencies,
            final JUnitPlatformOptions options,
            final Provider<Boolean> dryRun) {
        this.filter = filter;
        this.useImplementationDependencies = useImplementationDependencies;
        this.options = options;
        this.dryRun = dryRun;
    }

    /**
     * Returns a copy of the test framework but with the specified test filters.
     *
     * @param newTestFilters new test filters
     * @return test framework with new test filters
     */
    @UsedByScanPlugin("test-retry")
    @Override
    public EmbulkJUnitPlatformTestFramework copyWithFilters(final TestFilter newTestFilters) {
        final JUnitPlatformOptions copiedOptions = new JUnitPlatformOptions();
        copiedOptions.copyFrom(this.options);

        return new EmbulkJUnitPlatformTestFramework(
            (DefaultTestFilter) newTestFilters,
            useImplementationDependencies,
            copiedOptions,
            dryRun
        );
    }

    /**
     * Returns a factory which is used to create a {@link org.gradle.api.internal.tasks.testing.TestClassProcessor} in
     * each worker process. This factory is serialized across to the worker process, and then its {@link
     * org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory#create(org.gradle.internal.service.ServiceRegistry)}
     * method is called to create the test processor.
     */
    @Internal
    @Override
    public WorkerTestClassProcessorFactory getProcessorFactory() {
        // Assume it runs only on Java 8+.
        if (!JavaVersion.current().isJava8Compatible()) {
            throw new UnsupportedJavaRuntimeException("Running JUnit Platform requires Java 8+, please configure your test java executable with Java 8 or higher.");
        }

        validateOptions(this.options);
        return new EmbulkJUnitPlatformTestClassProcessorFactory(new JUnitPlatformSpec(
            this.filter.toSpec(),
            this.options.getIncludeEngines(),
            this.options.getExcludeEngines(),
            this.options.getIncludeTags(),
            this.options.getExcludeTags(),
            this.dryRun.get()
        ));
    }

    /**
     * Returns an action which is used to perform some framework specific worker process configuration. This action is
     * executed before starting each worker process.
     */
    @Internal
    @Override
    public Action<WorkerProcessBuilder> getWorkerConfigurationAction() {
        return workerProcessBuilder -> workerProcessBuilder.sharedPackages("org.junit");
    }

    /**
     * Returns a list of distribution modules that the test worker requires on the application modulepath if it runs as a module.
     * These dependencies are loaded from the Gradle distribution.
     *
     * Application classes specified by {@link WorkerProcessBuilder#sharedPackages} are
     * also included in the implementation classpath.
     *
     * @see #getUseDistributionDependencies()
     */
    @Internal
    @Override
    public List<TestFrameworkDistributionModule> getWorkerApplicationModulepathModules() {
        return DISTRIBUTION_MODULES;
    }

    /**
     * Whether the legacy behavior of loading test framework dependencies from the Gradle distribution
     * is enabled. If true, jars specified by this framework are loaded from the Gradle distribution
     * and placed on the test worker implementation/application classpath/modulepath.
     * <p>
     * This functionality is legacy and will eventually be deprecated and removed. Test framework dependencies
     * should be managed externally from the Gradle distribution, as is done by test suites.
     *
     * @return Whether test framework dependencies should be loaded from the Gradle distribution.
     */
    @Internal
    @Override
    public boolean getUseDistributionDependencies() {
        return this.useImplementationDependencies;
    }

    @Nested
    @Override
    public JUnitPlatformOptions getOptions() {
        return this.options;
    }

    /**
     * Returns a detector which is used to determine which of the candidate class files correspond to test classes to be
     * executed.
     */
    @Internal
    @Override
    public TestFrameworkDetector getDetector() {
        return null;
    }

    @Override
    public void close() throws IOException {
        // this test framework doesn't hold any state
    }

    private static void validateOptions(final JUnitPlatformOptions options) {
        final Set<String> intersection = new HashSet<>();
        intersection.addAll(options.getIncludeTags());
        intersection.retainAll(options.getExcludeTags());
        if (!intersection.isEmpty()) {
            if (intersection.size() == 1) {
                logger.warn("The tag '" + intersection.iterator().next() + "' is both included and excluded.  " +
                    "This will result in the tag being excluded, which may not be what was intended.  " +
                    "Please either include or exclude the tag but not both.");
            } else {
                final String allTags = intersection.stream().sorted().map(s -> "'" + s + "'").collect(Collectors.joining(", "));
                logger.warn("The tags " + allTags + " are both included and excluded.  " +
                    "This will result in the tags being excluded, which may not be what was intended.  " +
                    "Please either include or exclude the tags but not both.");
            }
        }
    }

    private static final Logger logger = Logging.getLogger(EmbulkJUnitPlatformTestFramework.class);

    private static final List<TestFrameworkDistributionModule> DISTRIBUTION_MODULES =
        Collections.unmodifiableList(Arrays.asList(
            new TestFrameworkDistributionModule(
                "junit-platform-engine",
                Pattern.compile("junit-platform-engine-1.*\\.jar"),
                "org.junit.platform.engine.DiscoverySelector"
            ),
            new TestFrameworkDistributionModule(
                "junit-platform-launcher",
                Pattern.compile("junit-platform-launcher-1.*\\.jar"),
                "org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder"
            ),
            new TestFrameworkDistributionModule(
                "junit-platform-commons",
                Pattern.compile("junit-platform-commons-1.*\\.jar"),
                "org.junit.platform.commons.util.ReflectionUtils"
            )
        ));

    private final JUnitPlatformOptions options;
    private final DefaultTestFilter filter;
    private final boolean useImplementationDependencies;
    private final Provider<Boolean> dryRun;
}
