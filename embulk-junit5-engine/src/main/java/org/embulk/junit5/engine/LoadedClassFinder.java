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

class LoadedClassFinder extends ClassLoader {
    private LoadedClassFinder(final ClassLoader inner) {
        this.inner = inner;
    }

    static Class<?> findFrom(final ClassLoader classLoader, final String name) {
        return (new LoadedClassFinder(classLoader)).findLoadedClass(name);
    }

    private final ClassLoader inner;
}
