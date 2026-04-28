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
package jakarta.faces.validator;

import jakarta.enterprise.util.AnnotationLiteral;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.inject.Qualifier;
import java.util.Objects;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">The</span> presence of this annotation on a class automatically registers the
 * class with the runtime as a {@link Validator}. The value of the {@link #value} attribute is taken to be the
 * <em>validator-id</em> and the fully qualified class name of the class to which this annotation is attached is taken
 * to be the <em>validator-class</em>.
 *
 * The implementation must guarantee that for each class annotated with <code>FacesValidator</code>,
 * <span class="changed_modified_5_0">discovered during CDI bean discovery</span>,
 * {@link jakarta.faces.application.Application#addValidator(java.lang.String,java.lang.String)} is called, passing the
 * derived <em>validator-id</em> as the first argument and the derived <em>validator-class</em> as the second argument.
 * The implementation must guarantee that all such calls to <code>addValidator()</code> happen during application
 * startup time and before any requests are serviced.
 * </p>
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Qualifier
public @interface FacesValidator
{
    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> value of this annotation attribute is taken to be the
     * <em>validator-id</em> with which instances of this class of component can be instantiated by calling
     * {@link jakarta.faces.application.Application#createValidator(java.lang.String)}
     * <span class="changed_added_2_2">If
     * no value is specified, or the value is <code>null</code>, the value is taken to be the return of calling
     * <code>getSimpleName</code> on the class to which this annotation is attached and lowercasing the first character
     * If more than one validator with this derived name is found, the results are undefined.</span>
     * </p>
     *
     * @return the validator-id
     */
    public String value() default "";

    /**
     * <p class="changed_added_2_0">
     * If <code>true</code>, the validator id for this annotation is added to the list of default validators by a cal
     * to {@link jakarta.faces.application.Application#addDefaultValidatorId}.
     * </p>
     *
     * @return whether or not this is a default validator
     */
    boolean isDefault() default false;

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be an indicator that flags whether or not the given validato
     * is a CDI managed validator.
     * </p>
     *
     * <p class="changed_modified_5_0">
     * Since Faces 5.0, all validators are CDI managed. This attribute is ignored.
     * </p>
     *
     * @return true if CDI managed, false otherwise.
     * @deprecated Since 5.0. All validators are now CDI managed. This attribute is ignored.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public boolean managed() default false;

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesValidator} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesValidator> implements FacesValidator
    {
        private static final long serialVersionUID = 1L;

        public static final Literal INSTANCE = of("", false, false);

        private final String value;
        private final boolean isDefault;
        private final boolean managed;

        public static Literal of(String value, boolean isDefault, boolean managed)
        {
            return new Literal(value, isDefault, managed);
        }

        private Literal(String value, boolean isDefault, boolean managed)
        {
            this.value = value;
            this.isDefault = isDefault;
            this.managed = managed;
        }

        @Override
        public String value()
        {
            return value;
        }

        @Override
        public boolean isDefault()
        {
            return isDefault;
        }

        @Override
        public boolean managed()
        {
            return managed;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.value);
            hash = 59 * hash + (this.isDefault ? 1 : 0);
            hash = 59 * hash + (this.managed ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Literal other = (Literal) obj;
            if (this.isDefault != other.isDefault)
            {
                return false;
            }
            if (this.managed != other.managed)
            {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }
        
        
    }
}
