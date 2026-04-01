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
package jakarta.faces.convert;

import jakarta.enterprise.inject.Stereotype;
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
 * class with the runtime as a {@link Converter}. The value of the {@link #value} attribute is taken to be
 * <em>converter-id</em>, the value of the {@link #forClass} attribute is taken to be <em>converter-for-class</em> and
 * the fully qualified class name of the class to which this annotation is attached is taken to be the
 * <em>converter-class</em>. The implementation must guarantee that for each class annotated with
 * <code>FacesConverter</code>, <span class="changed_modified_5_0">discovered during CDI bean discovery</span>,
 * the proper variant of
 * <code>Application.addConverter()</code> is called. If <em>converter-id</em> is not the empty string,
 * {@link jakarta.faces.application.Application#addConverter(java.lang.String,java.lang.String)} is called, passing the
 * derived <em>converter-id</em> as the first argument and the derived <em>converter-class</em> as the second argument.
 * If <em>converter-id</em> is the empty string,
 * {@link jakarta.faces.application.Application#addConverter(java.lang.Class,java.lang.String)} is called, passing the
 * <em>converter-for-class</em> as the first argument and the derived <em>converter-class</em> as the second argument.
 * The implementation must guarantee that all such calls to <code>addConverter()</code> happen during application
 * startup time and before any requests are serviced.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * The preceding text contains an important subtlety which application users should understand. It is not possible to
 * use a single {@code @FacesConverter} annotation to register a single {@code Converter} implementation both in the
 * {@code
 * by-class} and the {@code by-converter-id} data structures. One way to achieve this result is to put the actual
 * converter logic in an abstract base class, without a {@code @FacesConverter} annotation, and derive two sub-classes,
 * each with a {@code @FacesConverter} annotation. One sub-class has a {@code value} attribute but no {@code forClass}
 * attribute, and the other sub-class has the converse.
 * </p>
 *
 * <p>
 * Please see the ViewDeclarationLanguage documentation for {@code
 * <h:selectManyListBox>} for another important subtlety regarding converters and collections.
 * </p>
 *
 * </div>

 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Qualifier
@Stereotype
public @interface FacesConverter
{
    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the <em>converter-for-class</em> with which instances
     * of this class of converter can be instantiated by calling
     * {@link jakarta.faces.application.Application#createConverter(java.lang.Class)}.
     * </p>
     *
     * @return the class
     */
    public Class forClass() default Object.class;

    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the <em>converter-id</em> with which instances of this
     * class of converter can be instantiated by calling
     * {@link jakarta.faces.application.Application#createConverter(java.lang.String)}.
     * </p>
     *
     * @return the converter-id
     */
    public String value() default "";

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be an indicator that flags whether or not the given converter
     * is a CDI managed converter.
     * </p>
     *
     * <p class="changed_modified_5_0">
     * Since Faces 5.0, all converters are CDI managed. This attribute is ignored.
     * </p>
     *
     * @return whether or not this converter is managed by CDI
     * @deprecated Since 5.0. All converters are now CDI managed. This attribute is ignored.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public boolean managed() default false;

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesConverter} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesConverter> implements FacesConverter
    {
        private static final long serialVersionUID = 1L;

        public static final Literal INSTANCE = of("", Object.class, false);

        private final String value;
        private final Class forClass;
        private final boolean managed;

        public static Literal of(String value, Class forClass, boolean managed)
        {
            return new Literal(value, forClass, managed);
        }

        private Literal(String value, Class forClass, boolean managed)
        {
            this.value = value;
            this.forClass = forClass;
            this.managed = managed;
        }

        @Override
        public String value() 
        {
            return value;
        }

        @Override
        public Class forClass()
        {
            return forClass;
        }

        @Override
        public boolean managed()
        {
            return managed;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.value);
            hash = 17 * hash + Objects.hashCode(this.forClass);
            hash = 17 * hash + (this.managed ? 1 : 0);
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
            if (this.managed != other.managed)
            {
                return false;
            }
            if (!Objects.equals(this.value, other.value))
            {
                return false;
            }
            return Objects.equals(this.forClass, other.forClass);
        }

        
    }
}
