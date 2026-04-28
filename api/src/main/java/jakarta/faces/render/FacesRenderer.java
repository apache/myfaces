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
package jakarta.faces.render;

import jakarta.enterprise.inject.Stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * The presence of this annotation on a class automatically registers the class with the runtime as a {@link Renderer}.
 * The value of the {@link #renderKitId} attribute is taken to be the <em>render-kit-id</em> to which an instance of
 * this <code>Renderer</code> is to be added. There must be a public zero-argument constructor on any class where this
 * annotation appears. The implementation must indicate a fatal error if such a constructor does not exist and the
 * application must not be placed in service. Within that {@link RenderKit}, The value of the {@link #rendererType}
 * attribute is taken to be the <em>renderer-type</em>, and the value of the {@link #componentFamily} attribute is to be
 * taken as the <em>component-family</em>. The implementation must guarantee that for each class annotated with
 * <code>FacesRenderer</code>, <span class="changed_modified_5_0">discovered during CDI bean discovery</span>,
 * the following actions are taken.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <ul>
 *
 * <li>
 * <p>
 * Obtain a reference to the {@link RenderKitFactory} for this application.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * See if a <code>RenderKit</code> exists for <em>render-kit-id</em>. If so, let that instance be <em>renderKit</em> for
 * discussion. If not, the implementation must indicate a fatal error if such a <code>RenderKit</code> does not exist
 * and the application must not be placed in service.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Create an instance of this class using the public zero-argument constructor.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Call {@link RenderKit#addRenderer} on <em>renderKit</em>, passing <em>component-family</em> as the first argument,
 * <em>renderer-type</em> as the second, and the newly instantiated <code>RenderKit</code> instance as the third
 * argument.
 * </p>
 * </li>
 *
 * </ul>
 *
 *
 * </div>
 *
 */
@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Stereotype
public @interface FacesRenderer
{
    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the <em>component-family</em> which, in combination with
     * {@link #rendererType} can be used to obtain a reference to an instance of this {@link Renderer} by calling
     * {@link jakarta.faces.render.RenderKit#getRenderer(java.lang.String, java.lang.String)}.
     * </p>
     *
     * @return the <em>component-family</em>
     */
    public String componentFamily();

    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the <em>renderer-type</em> which, in combination with
     * {@link #componentFamily} can be used to obtain a reference to an instance of this {@link Renderer} by calling
     * {@link jakarta.faces.render.RenderKit#getRenderer(java.lang.String, java.lang.String)}.
     * </p>
     *
     * @return the <em>renderer-type</em>
     */
    public String rendererType();

    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the <em>render-kit-id</em> in which an instance of thi
     * class of <code>Renderer</code> must be installed.
     * </p>
     *
     * @return the <em>render-kit-id</em>
     */
    public String renderKitId() default RenderKitFactory.HTML_BASIC_RENDER_KIT;
}
