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
package jakarta.faces.component;

import jakarta.enterprise.inject.Stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">The presence of this annotation on a
 * class automatically registers the class with the runtime as a {@link
 * jakarta.faces.component.UIComponent}.  The value of the {@link #value} attribute is taken to
 * be the <em>component-type</em> and the fully qualified class name of
 * the class to which this annotation is attached is taken to be the
 * <em>component-class</em>.  The implementation must guarantee that for
 * each class annotated with <code>FacesComponent</code>, <span class="changed_modified_5_0">discovered during CDI bean discovery</span>,
 * {@link jakarta.faces.application.Application#addComponent(java.lang.String,java.lang.String)}
 * is called, passing the derived <em>component-type</em> as the first
 * argument and the derived <em>component-class</em> as the second
 * argument.  The implementation must guarantee that all such calls to
 * <code>addComponent()</code> happen during application startup time
 * and before any requests are serviced.</p>

 * <p>
 * <span class="changed_modified_2_2">The</span> presence of this annotation on a class that extends {@link UIComponent}
 * must cause the runtime to register this class as a component suitable for inclusion in a view.
 * <span class="changed_added_2_2">If the <code>createTag</code> attribute is <code>true</code>, the runtime must create
 * a corresponding Facelet tag handler according to the rules specified in the attributes of this annotation.</span>
 * </p>
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Stereotype
public @interface FacesComponent
{
    /**
     * <p class="changed_added_2_2">
     * Components that declare a <code>createTag = true</code> attribute will be placed into this tag namespace if the
     * namespace attribute is omitted.
     * </p>
     */
    public static final String NAMESPACE = "jakarta.faces.component";

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> value of this annotation attribute is taken to be the
     * <em>component-type</em> with which instances of this class of component can be instantiated by calling
     * {@link jakarta.faces.application.Application#createComponent(java.lang.String)}. <span class="changed_added_2_2">If
     * no value is specified, or the value is <code>null</code>, the value is taken to be the return of calling
     * <code>getSimpleName</code> on the class to which this annotation is attached and lowercasing the first character. If
     * more than one component with this derived name is found, the results are undefined.</span>
     * </p>
     *
     * @return the component type.
     */
    public String value() default "";

    /**
     * <p class="changed_added_2_2">
     * If the value of this attribute is <code>true</code>, the runtime must create a Facelet tag handler, that extends from
     * {@link jakarta.faces.view.facelets.ComponentHandler}, suitable for use in pages under the tag library with namespace
     * given by the value of the {@link #namespace} attribute.
     * </p>
     *
     * @return <code>true</code> to create the Facelet tag handler, <code>false</code> otherwise.
     */
    public boolean createTag() default false;

    /**
     * <p class="changed_added_2_2">
     * If the value of the {@link #createTag} attribute is <code>true</code>, the runtime must use this value as the tag
     * name for including an instance of the component annotated with this annotation in a view. If this attribute is not
     * specified on a usage of this annotation, the simple name of the class on which this annotation is declared, with the
     * first character lowercased, is taken to be the value.
     * </p>
     *
     * @return the tag name.
     */
    public String tagName() default "";

    /**
     * <p class="changed_added_2_2">
     * If the value of the {@link #createTag} attribute is <code>true</code>, the value of this attribute is taken to be the
     * tag library namespace into which this component is placed.
     * </p>
     *
     * @return the namespace.
     */
    public String namespace() default NAMESPACE;
}
