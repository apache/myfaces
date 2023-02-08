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
package org.apache.myfaces.view.facelets.tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import jakarta.el.MethodExpression;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;

/**
 * Optional Rule for binding Method[Binding|Expression] properties
 * 
 * @author Mike Kienenberger
 * @author Jacob Hookom
 */
public final class MethodRule extends MetaRule
{

    private final String methodName;
    private final Class<?> returnTypeClass;
    private final Class<?>[] params;

    public MethodRule(String methodName, Class<?> returnTypeClass, Class<?>[] params)
    {
        this.methodName = methodName;
        this.returnTypeClass = returnTypeClass;
        this.params = params;
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (false == name.equals(this.methodName))
        {
            return null;
        }

        if (MethodExpression.class.equals(meta.getPropertyType(name)))
        {
            Method method = meta.getWriteMethod(name);
            if (method != null)
            {
                return new MethodExpressionMetadata(method, attribute, this.returnTypeClass, this.params);
            }
        }

        return null;
    }

    private class MethodExpressionMetadata extends Metadata
    {
        private final Method method;
        private final BiConsumer<Object, Object> function;
        private final TagAttribute attribute;
        private Class<?>[] paramList;
        private Class<?> returnType;

        public MethodExpressionMetadata(Method method, TagAttribute attribute, Class<?> returnType, 
                Class<?>[] paramList)
        {
            this.method = method;
            this.function = null;
            this.attribute = attribute;
            this.paramList = paramList;
            this.returnType = returnType;
        }
        
        public MethodExpressionMetadata(BiConsumer<Object, Object> function, TagAttribute attribute,
                Class<?> returnType, Class<?>[] paramList)
        {
            this.method = null;
            this.function = function;
            this.attribute = attribute;
            this.paramList = paramList;
            this.returnType = returnType;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            MethodExpression expr = attribute.getMethodExpression(ctx, returnType, paramList);

            try
            {
                if (method != null)
                {
                    method.invoke(instance, expr);
                }
                else if (function != null)
                {
                    function.accept(instance, expr);
                }
            }
            catch (InvocationTargetException e)
            {
                throw new TagAttributeException(attribute, e.getCause());
            }
            catch (Exception e)
            {
                throw new TagAttributeException(attribute, e);
            }
        }
    }
}
