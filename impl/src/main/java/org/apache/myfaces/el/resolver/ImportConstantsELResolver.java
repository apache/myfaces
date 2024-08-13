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
package org.apache.myfaces.el.resolver;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewMetadata;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.FINE;

/**
 *
 */
public final class ImportConstantsELResolver extends ELResolver
{
    private static final String ERROR_MISSING_CLASS = "Cannot find type '%s' in classpath.";
    private static final String ERROR_FIELD_ACCESS = "Cannot access constant field '%s' of type '%s'.";

    private static final String IMPORT_CONSTANTS = "oam.importConstants";
    
    private Map<String, Map<String, Object>> constantsTypeMap = new ConcurrentHashMap<>();

    @Override
    public Object getValue(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return null;
        }

        final FacesContext facesContext = facesContext(context);
        if (facesContext == null)
        {
            return null;
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot == null)
        {
            return null;
        }

        Map<String, String> importConstantsMap = (Map<String, String>) 
                viewRoot.getTransientStateHelper().getTransient(IMPORT_CONSTANTS);
        if (importConstantsMap == null)
        {
            Collection<UIImportConstants> constants = ViewMetadata.getImportConstants(viewRoot);
            if (constants != null && !constants.isEmpty())
            {
                importConstantsMap = new HashMap<>();
                for (UIImportConstants c : constants)
                {
                    String var = c.getVar();
                    String type = c.getType();
                    if (var == null) 
                    {
                        int innerClass = type.lastIndexOf('$');
                        int outerClass = type.lastIndexOf('.');
                        var = type.substring(Math.max(innerClass, outerClass) + 1);
                    }                    
                    importConstantsMap.put(var, type);
                }
            } 
            else
            {
                importConstantsMap = Collections.emptyMap();
            }
            if (!FaceletViewDeclarationLanguage.isBuildingViewMetadata(facesContext))
            {
                viewRoot.getTransientStateHelper().putTransient(IMPORT_CONSTANTS, importConstantsMap);
            }
        }

        if (importConstantsMap != null && !importConstantsMap.isEmpty())
        {
            String type = importConstantsMap.get((String)property);
            if (type != null)
            {
                Map<String, Object> constantsMap = constantsTypeMap.computeIfAbsent(type, k -> collectConstants(type));
                if (!constantsMap.isEmpty())
                {
                    context.setPropertyResolved(true);
                    return constantsMap;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        return null;
    }

    @Override
    public void setValue(ELContext elc, Object o, Object o1, Object o2)
            throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        //No op
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context, final Object base)
    {
        return base == null ? Object.class : null;
    }

    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

    // Helpers --------------------------------------------------------------------------------------------------------
    /**
     * Collect constants of the given type. That are, all public static final fields of the given type.
     *
     * @param type The fully qualified name of the type to collect constants for.
     * @return Constants of the given type.
     */
    private static Map<String, Object> collectConstants(final String type)
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
                    throw new IllegalArgumentException(format(ERROR_FIELD_ACCESS, type, field.getName()), e);
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
                catch (Exception ignore)
                {
                    Logger.getLogger(ImportConstantsELResolver.class.getName()).log(
                            FINE, "Ignoring thrown exception; previous exception will be rethrown instead.", ignore);
                    // Just continue to IllegalArgumentException on original ClassNotFoundException.
                }
            }

            throw new IllegalArgumentException(format(ERROR_MISSING_CLASS, type), e);
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
