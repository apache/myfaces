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
package javax.faces.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.FacesException;

/**
 * Utility class that isolates UEL calls, to prevent ClassNotFoundException
 * when bean validation is used without it. It is a similar hack to the one
 * used in portlet case.
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 18:31:37 -0400 (mer., 17 sept. 2008) $
 */
final class _BeanValidatorUELUtils
{

    /**
     * Get the ValueReference from the ValueExpression.
     *
     * @param component The component.
     * @param context The FacesContext.
     * @return A ValueReferenceWrapper with the necessary information about the ValueReference.
     */
    public static ValueReferenceWrapper getUELValueReferenceWrapper(final ValueExpression valueExpression, final ELContext elCtx)
    {
        final ValueReference valueReference = getUELValueReference(valueExpression, elCtx);
        if (valueReference == null)
        {
            return null;
        }
        return new ValueReferenceWrapper(valueReference.getBase(), valueReference.getProperty());
    }

    private static ValueReference getUELValueReference(final ValueExpression valueExpression, final ELContext elCtx)
    {
        final String methodName = "getValueReference";
        final String methodSignature = valueExpression.getClass().getName() +
                "." + methodName +
                "(" + ELContext.class + ")";
        try
        {
            final Method method = valueExpression.getClass().getMethod(methodName, ELContext.class);
            if (!ValueReference.class.equals(method.getReturnType())
             && !ValueReference.class.isAssignableFrom(method.getReturnType()))
            {
                throw new NoSuchMethodException(
                        methodSignature +
                        "doesn't return " + ValueReference.class +
                        ", but " + method.getReturnType());
            }
            return (ValueReference) method.invoke(valueExpression, elCtx);
        }
        catch (NoSuchMethodException e)
        {
            throw new FacesException(
                    "MyFaces indicates Unified EL is available, but method: " +
                    methodSignature +
                    " is not available", e);
        }
        catch (InvocationTargetException e)
        {
            throw new FacesException("Exception invoking " + methodSignature, e);
        }
        catch (IllegalAccessException e)
        {
            throw new FacesException("Exception invoking " + methodSignature, e);
        }
    }   
}
