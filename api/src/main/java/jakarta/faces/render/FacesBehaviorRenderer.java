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
 * The presence of this annotation on a class automatically registers the class with the runtime as a
 * {@link ClientBehaviorRenderer}.
 *
 * The value of the {@link #renderKitId} attribute is taken to be the <em>render-kit-id</em> to which an instance o
 * this <code>Renderer</code> is to be added.
 *
 * There must be a public zero-argument constructor on any class where this annotation appears. The implementation mus
 * indicate a fatal error if such a constructor does not exist and the application must not be placed in service.
 *
 * Within that {@link RenderKit}, the value of the {@link #rendererType} attribute is taken to be th
 * <em>renderer-type</em>
 *
 * The implementation must guarantee that for each class annotated with <code>FacesBehaviorRenderer</code>,
 * <span class="changed_modified_5_0">discovered during CDI bean discovery</span>,
 * the following actions are taken.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <ul>
 *   <li>
 *     <p>
 *        Obtain a reference to the {@link RenderKitFactory} for this application.
 *     </p>
 *   </li>
 *
 *   <li>
 *     <p>
 *       See if a <code>RenderKit</code> exists for <em>render-kit-id</em>. If so, let that instance be
 *       <em>renderKit</em> for
 *       discussion. If not, the implementation must indicate a fatal error if such a <code>RenderKit</code> does not
 *       exist and the application must not be placed in service.
 *     </p>
 *   </li>
 *
 *   <li>
 *     <p>
 *       Create an instance of this class using the public zero-argument constructor.
 *     </p>
 *   </li>
 *
 *   <li>
 *     <p>
 *       Call {@link RenderKit#addClientBehaviorRenderer} on <em>renderKit</em>, passing <em>type</em> as the first
 *       argument,
 *       and a {@link ClientBehaviorRenderer} instance as the second argument.
 *     </p>
 *   </li>
 * </ul>
 *
 *
 * </div>
 *
 * @since 2.0
 *
 */
@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Stereotype
public @interface FacesBehaviorRenderer
{
    /**
     * The value of this annotation attribute is taken to be the <i>renderer-type</i> which, in combination with
     * componentFamily can be used to obtain a reference to an instance of this {@link Renderer} by calling
     * {@link RenderKit#getRenderer(java.lang.String, java.lang.String)}.
     */
    public String rendererType();

    /**
     * The value of this annotation attribute is taken to be the <i>render-kit-id</i> in which an instance of this class
     * of {@link Renderer} must be installed.
     */
    public String renderKitId() default RenderKitFactory.HTML_BASIC_RENDER_KIT;
}
