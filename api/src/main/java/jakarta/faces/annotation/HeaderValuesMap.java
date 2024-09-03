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

import jakarta.enterprise.util.AnnotationLiteral;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.util.Map;

import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Qualifier;

/**
 *
 */
@Qualifier
@Retention(value=RUNTIME)
@Target(value={ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface HeaderValuesMap
{
    public static final TypeLiteral<Map<String, String[]>> TYPE = new TypeLiteral<>()
    {
        private static final long serialVersionUID = 1L;
    };

    public static final class Literal extends AnnotationLiteral<HeaderValuesMap> implements HeaderValuesMap
    {
        private static final long serialVersionUID = 1L;

        public static final Literal INSTANCE = new Literal();
    }   
}
