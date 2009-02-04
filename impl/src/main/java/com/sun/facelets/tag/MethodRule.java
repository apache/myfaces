/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.facelets.tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.el.MethodExpression;
import javax.faces.el.MethodBinding;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.el.LegacyMethodBinding;

/**
 * Optional Rule for binding Method[Binding|Expression] properties
 * 
 * @author Mike Kienenberger
 * @author Jacob Hookom
 */
public final class MethodRule extends MetaRule
{

    private final String methodName;

    private final Class returnTypeClass;

    private final Class[] params;

    public MethodRule(String methodName, Class returnTypeClass, Class[] params)
    {
        this.methodName = methodName;
        this.returnTypeClass = returnTypeClass;
        this.params = params;
    }

    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (false == name.equals(this.methodName))
            return null;

        if (MethodBinding.class.equals(meta.getPropertyType(name)))
        {
            Method method = meta.getWriteMethod(name);
            if (method != null)
            {
                return new MethodBindingMetadata(method, attribute, this.returnTypeClass, this.params);
            }
        }
        else if (MethodExpression.class.equals(meta.getPropertyType(name)))
        {
            Method method = meta.getWriteMethod(name);
            if (method != null)
            {
                return new MethodExpressionMetadata(method, attribute, this.returnTypeClass, this.params);
            }
        }

        return null;
    }

    private class MethodBindingMetadata extends Metadata
    {
        private final Method _method;

        private final TagAttribute _attribute;

        private Class[] _paramList;

        private Class _returnType;

        public MethodBindingMetadata(Method method, TagAttribute attribute, Class returnType, Class[] paramList)
        {
            _method = method;
            _attribute = attribute;
            _paramList = paramList;
            _returnType = returnType;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            MethodExpression expr = _attribute.getMethodExpression(ctx, _returnType, _paramList);

            try
            {
                _method.invoke(instance, new Object[] { new LegacyMethodBinding(expr) });
            }
            catch (InvocationTargetException e)
            {
                throw new TagAttributeException(_attribute, e.getCause());
            }
            catch (Exception e)
            {
                throw new TagAttributeException(_attribute, e);
            }
        }
    }

    private class MethodExpressionMetadata extends Metadata
    {
        private final Method _method;

        private final TagAttribute _attribute;

        private Class[] _paramList;

        private Class _returnType;

        public MethodExpressionMetadata(Method method, TagAttribute attribute, Class returnType, Class[] paramList)
        {
            _method = method;
            _attribute = attribute;
            _paramList = paramList;
            _returnType = returnType;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            MethodExpression expr = _attribute.getMethodExpression(ctx, _returnType, _paramList);

            try
            {
                _method.invoke(instance, new Object[] { expr });
            }
            catch (InvocationTargetException e)
            {
                throw new TagAttributeException(_attribute, e.getCause());
            }
            catch (Exception e)
            {
                throw new TagAttributeException(_attribute, e);
            }
        }
    }
}
