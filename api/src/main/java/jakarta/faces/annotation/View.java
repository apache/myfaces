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
package jakarta.faces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_4_0">
 * The presence of this annotation on a target (type, method, parameter or field) within anapplication
 * is used to indicate that this target is somehow handling a Faces View Id or Ids.
 * </p>
 *
 * <p>
 * The exact way in which such view is handled depends on the annotated element in question.
 *
 * @since 4.0
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Documented
public @interface View
{
    /**
     * <p>
     * Set the Faces View Id pattern.
     * </p>
     *
     * The Faces View Id pattern can represent a single view, such as "/index.xhtml",
     * or a pattern like "/foo/bar/*". Though the exact interpretation of the Faces View Id
     * for a single view is ultimately defined by the annotated element, in general it should
     * align with the return value from an action expression
     * (see {@link jakarta.faces.component.ActionSource#setActionExpression(jakarta.el.MethodExpression)}
     *
     * @return the Faces View Id pattern
     */
    String value() default "";

    /**
     * Supports inline instantiation of the {@link View} annotation.
     *
     */
    public final static class Literal extends AnnotationLiteral<View> implements View
    {
        public static final Literal INSTANCE = of("");

        private static final long serialVersionUID = 1L;

        private final String value;

        public static Literal of(String value)
        {
            return new Literal(value);
        }

        private Literal(String value)
        {
            this.value = value;
        }

        @Override
        public String value()
        {
            return value;
        }
    }
}
