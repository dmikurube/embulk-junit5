/*
 * Copyright 2024 The Embulk project
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class LoadedClassFinder extends ClassLoader {
    static synchronized Class<?> findFrom(final ClassLoader classLoader, final String name) {
        final Method findLoadedClass;
        try {
            findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        } catch (final NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        findLoadedClass.setAccessible(true);
        try {
            final Object classObject;
            try {
                classObject = findLoadedClass.invoke(classLoader, name);
            } catch (final IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }

            if (classObject == null) {
                return null;
            }
            if (classObject instanceof Class) {
                return (Class<?>) classObject;
            } else {
                throw new RuntimeException();
            }
        } finally {
            findLoadedClass.setAccessible(false);
        }
    }
}
