/*
 * Copyright (C) 2014 Benedict Lau
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
package com.groundupworks.wings;

/**
 * An interface for injecting classes into Wings. This interface must be implemented by any
 * {@link android.app.Application} using Wings.
 *
 * @author Benedict Lau
 */
public interface IWingsInjector {

    /**
     * Injects dependencies into the static fields and methods of the class.
     */
    public void injectStatics();

    /**
     * Injects dependencies into the fields and methods of {@code instance}.
     *
     * @param <T> the type of {@code instance}.
     */
    public <T> void inject(T instance);
}
