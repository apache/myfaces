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
package org.apache.myfaces.util;

import jakarta.faces.context.FacesContext;
import org.apache.myfaces.util.lang.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConstantsCollector
{
    private static final Logger LOG = Logger.getLogger(ConstantsCollector.class.getName());

    private static final String COLLECTED_CONSTANTS = "oam.collectedConstants";

    public static Map<String, Object> collectConstants(FacesContext context, String type)
    {
        Map<String, Object> applicationMap = context.getExternalContext().getApplicationMap();
        Map<String, Map<String, Object>> constantsTypeMap =
                (Map<String, Map<String, Object>>) applicationMap.get(COLLECTED_CONSTANTS);
        if (constantsTypeMap == null)
        {
            constantsTypeMap = new HashMap<>();
            applicationMap.put(COLLECTED_CONSTANTS, constantsTypeMap);
        }
        return constantsTypeMap.computeIfAbsent(type, k -> collectConstants(type));
    }

    /**
     * Collect constants of the given type. That are, all public static final fields of the given type.
     *
     * @param type The fully qualified name of the type to collect constants for.
     * @return Constants of the given type.
     */
    private static Map<String, Object> collectConstants(String type)
    {
        Map<String, Object> constants = new HashMap<>();

        for (Field field : toClass(type).getFields())
        {
            if (isPublicStaticFinal(field))
            {
                try
                {
                    constants.put(field.getName(), field.get(null));
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(
                            String.format("Cannot access constant field '%s' of type '%s'.", type, field.getName()),
                            e);
                }
            }
        }

        return constants;
    }


    /**
     * Convert the given type, which should represent a fully qualified name, to a concrete {@link Class} instance.
     *
     * @param type The fully qualified name of the class.
     * @return The concrete {@link Class} instance.
     * @throws IllegalArgumentException When it is missing in the classpath.
     */
    static Class<?> toClass(String type)
    {
        // Package-private so that ImportFunctions can also use it.
        try
        {
            return ClassUtils.classForName(type);
        }
        catch (ClassNotFoundException e)
        {
            // Perhaps it's an inner enum which is specified as com.example.SomeClass.SomeEnum.
            // Let's be lenient on that although the proper type notation should be com.example.SomeClass$SomeEnum.
            int i = type.lastIndexOf('.');

            if (i > 0)
            {
                try
                {
                    return toClass(new StringBuilder(type).replace(i, i + 1, "$").toString());
                }
                catch (Exception ex)
                {
                    LOG.log(Level.FINE, "Ignoring thrown exception; previous exception will be rethrown instead.", ex);
                    // Just continue to IllegalArgumentException on original ClassNotFoundException.
                }
            }

            throw new IllegalArgumentException(
                    String.format("Cannot find type '%s' in classpath.", type),
                    e);
        }
    }

    /**
     * Returns whether the given field is a constant field, that is when it is public, static and final.
     *
     * @param field The field to be checked.
     * @return <code>true</code> if the given field is a constant field, otherwise <code>false</code>.
     */
    private static boolean isPublicStaticFinal(Field field)
    {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }
}
