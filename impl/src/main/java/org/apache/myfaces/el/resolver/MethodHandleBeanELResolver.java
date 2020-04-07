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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

public class MethodHandleBeanELResolver extends BeanELResolver
{
    private static Method privateLookupIn;

    static
    {
        try
        {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class,
                    MethodHandles.Lookup.class);
        }
        catch (Exception e)
        {
        }
    }

    public static boolean isSupported()
    {
        return privateLookupIn != null;
    }

    private final ConcurrentHashMap<String, Map<String, PropertyInfo>> cache;

    public MethodHandleBeanELResolver()
    {
        cache = new ConcurrentHashMap<>(1000);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return null;
        }

        context.setPropertyResolved(base, property);

        return getPropertyInfo(base, property).type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return null;
        }

        context.setPropertyResolved(base, property);

        try
        {
            return getPropertyInfo(base, property).getter.apply(base);
        }
        catch (Exception e)
        {
            throw new ELException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return;
        }

        context.setPropertyResolved(base, property);

        PropertyInfo propertyInfo = getPropertyInfo(base, property);
        if (propertyInfo.setter == null)
        {
            throw new PropertyNotWritableException("Property \"" + (String) property
                    + "\" in \"" + base.getClass().getName() + "\" is not writable!");
        }

        try
        {
            propertyInfo.setter.accept(base, value);
        }
        catch (Exception e)
        {
            throw new ELException(e);
        }
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params)
    {
        // just use the original BeanELResolver method
        // it's higher effort to implement this
        return super.invoke(context, base, method, paramTypes, params);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return false;
        }

        context.setPropertyResolved(base, property);

        return getPropertyInfo(base, property).setter == null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (base != null)
        {
            return Object.class;
        }

        return null;
    }

    protected class PropertyInfo
    {
        Class<?> type;
        Function getter;
        BiConsumer setter;
    }

    protected PropertyInfo getPropertyInfo(Object base, Object property)
    {
        Map<String, PropertyInfo> beanCache = cache.computeIfAbsent(base.getClass().getName(),
                k -> new ConcurrentHashMap<>());
        return beanCache.computeIfAbsent((String) property, k -> initPropertyInfo(base.getClass(), k));
    }

    protected PropertyInfo initPropertyInfo(Class<?> target, String fieldName)
    {

        PropertyInfo info = new PropertyInfo();

        try
        {
            PropertyDescriptor pd = null;
            for (PropertyDescriptor cpd : Introspector.getBeanInfo(target, Object.class).getPropertyDescriptors())
            {
                if (fieldName.equals(cpd.getName()))
                {
                    pd = cpd;
                }
            }

            if (pd == null)
            {
                throw new PropertyNotFoundException("Property \"" + fieldName + "\" not found on \""
                        + target.getName() + "\"");
            }

            info.type = pd.getPropertyType();

            MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, target,
                    MethodHandles.lookup());

            Method getter = pd.getReadMethod();
            if (getter != null)
            {
                MethodHandle getterHandle = lookup.unreflect(getter);
                CallSite getterCallSite = LambdaMetafactory.metafactory(lookup,
                        "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        getterHandle,
                        getterHandle.type());
                info.getter = (Function) getterCallSite.getTarget().invokeExact();
            }

            Method setter = pd.getWriteMethod();
            if (setter != null)
            {
                MethodHandle setterHandle = lookup.unreflect(setter);
                info.setter = createSetter(lookup, info, setterHandle);
            }
        }
        catch (Throwable e)
        {
            throw new ELException(e);
        }

        return info;
    }

    @SuppressWarnings("unchecked")
    protected BiConsumer createSetter(MethodHandles.Lookup lookup, PropertyInfo propertyInfo,
            MethodHandle setterHandle)
            throws LambdaConversionException, Throwable
    {
        // special handling for primitives required, see https://dzone.com/articles/setters-method-handles-and-java-11
        if (propertyInfo.type.isPrimitive())
        {
            if (propertyInfo.type == double.class)
            {
                ObjDoubleConsumer consumer = (ObjDoubleConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjDoubleConsumer.class, double.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (double) b);
            }
            else if (propertyInfo.type == int.class)
            {
                ObjIntConsumer consumer = (ObjIntConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjIntConsumer.class, int.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (int) b);
            }
            else if (propertyInfo.type == long.class)
            {
                ObjLongConsumer consumer = (ObjLongConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjLongConsumer.class, long.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (long) b);
            }
            else if (propertyInfo.type == float.class)
            {
                ObjFloatConsumer consumer = (ObjFloatConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjFloatConsumer.class, float.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (float) b);
            }
            else if (propertyInfo.type == byte.class)
            {
                ObjByteConsumer consumer = (ObjByteConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjByteConsumer.class, byte.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (byte) b);
            }
            else if (propertyInfo.type == char.class)
            {
                ObjCharConsumer consumer = (ObjCharConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjCharConsumer.class, char.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (char) b);
            }
            else if (propertyInfo.type == short.class)
            {
                ObjShortConsumer consumer = (ObjShortConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjShortConsumer.class, short.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (short) b);
            }
            else if (propertyInfo.type == boolean.class)
            {
                ObjBooleanConsumer consumer = (ObjBooleanConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjBooleanConsumer.class, boolean.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (boolean) b);
            }
            else
            {
                throw new RuntimeException("Type is not supported yet: " + propertyInfo.type.getName());
            }
        }
        else
        {
            return (BiConsumer) createSetterCallSite(lookup, setterHandle, BiConsumer.class, Object.class).getTarget()
                    .invokeExact();
        }
    }

    protected CallSite createSetterCallSite(MethodHandles.Lookup lookup, MethodHandle setter, Class<?> interfaceType,
            Class<?> valueType)
            throws LambdaConversionException
    {
        return LambdaMetafactory.metafactory(lookup,
                "accept",
                MethodType.methodType(interfaceType),
                MethodType.methodType(void.class, Object.class, valueType),
                setter,
                setter.type());
    }

    @FunctionalInterface
    public interface ObjFloatConsumer<T extends Object>
    {
        public void accept(T t, float i);
    }

    @FunctionalInterface
    public interface ObjByteConsumer<T extends Object>
    {
        public void accept(T t, byte i);
    }

    @FunctionalInterface
    public interface ObjCharConsumer<T extends Object>
    {
        public void accept(T t, char i);
    }

    @FunctionalInterface
    public interface ObjShortConsumer<T extends Object>
    {
        public void accept(T t, short i);
    }

    @FunctionalInterface
    public interface ObjBooleanConsumer<T extends Object>
    {
        public void accept(T t, boolean i);
    }
}
