/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jakarta.faces.event;

import jakarta.enterprise.inject.Stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * The presence of this annotation on a class automatically registers the class with the runtime as a
 * {@link ComponentSystemEvent} for use with the <code>&lt;f:event /&gt;</code> tag in a page. The value of the
 * {@link #shortName} attribute is taken to be the short name for the {@link jakarta.faces.event.ComponentSystemEvent}.
 * If the <em>shortName</em> has already been registered, the current class must be added to a List of of duplicate
 * events for that name. If the event name is then referenced by an application, a <code>FacesException</code> must be
 * thrown listing the <em>shortName</em> and the offending classes.
 * </p>
 *
 * @since 2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Stereotype
public @interface NamedEvent
{
    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the short name for the
     * {@link jakarta.faces.event.ComponentSystemEvent}. If the value of this attribute is ommitted, the followin
     * algorithm must be used by the code that processes this annotation to determine its value.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <ol>
     *
     * <li>
     * <p>
     * Get the unqualified class name (e.g., <code>UserLoginEvent</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Strip off the trailing "Event", if present (e.g., <code>UserLogin</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Convert the first character to lower-case (e.g., <code>userLogin</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Prepend the package name to the lower-cased name.
     * </p>
     * </li>
     *
     * </ol>
     *
     *
     * </div>
     *
     * @return the short name
     */
    public String shortName() default "";
}
