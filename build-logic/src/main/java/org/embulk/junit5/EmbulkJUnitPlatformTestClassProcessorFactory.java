/*
 * This file is based on a copy from Gradle 8.10 with modifications.
 *
 * It is licensed under the Apache License, Version 2.0.
 */

/*
 * Copyright 2022 the original author or authors.
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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformSpec;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.time.Clock;

/**
 * Implementation of {@link WorkerTestClassProcessorFactory} which instantiates a {@code JUnitPlatformTestClassProcessor}.
 * This class is loaded on test workers themselves and acts as the entry-point to running JUnit Platform tests on a test worker.
 */
public class EmbulkJUnitPlatformTestClassProcessorFactory implements WorkerTestClassProcessorFactory, Serializable {
    public EmbulkJUnitPlatformTestClassProcessorFactory(final JUnitPlatformSpec spec) {
        this.spec = spec;
    }

    @Override
    public TestClassProcessor create(
            final IdGenerator<?> idGenerator,
            final ActorFactory actorFactory,
            final Clock clock) {
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass("org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestClassProcessor");
            Constructor<?> constructor = clazz.getConstructor(JUnitPlatformSpec.class, IdGenerator.class, ActorFactory.class, Clock.class);
            return (TestClassProcessor) constructor.newInstance(spec, idGenerator, actorFactory, clock);
        } catch (final Exception e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    private final JUnitPlatformSpec spec;
}
