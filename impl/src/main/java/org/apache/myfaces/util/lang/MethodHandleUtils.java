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
package org.apache.myfaces.util.lang;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import javax.faces.FacesException;

public final class MethodHandleUtils
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

    public static class LambdaPropertyDescriptor
    {
        private PropertyDescriptor wrapped;
        private Function<Object, Object> readFunction;
        private BiConsumer<Object, Object> writeFunction;

        public PropertyDescriptor getWrapped()
        {
            return wrapped;
        }

        public Class<?> getPropertyType()
        {
            return wrapped.getPropertyType();
        }

        public Function<Object, Object> getReadFunction()
        {
            return readFunction;
        }

        public BiConsumer<Object, Object> getWriteFunction()
        {
            return writeFunction;
        }
    }

    public static LambdaPropertyDescriptor getLambdaPropertyDescriptor(Class<?> target, String name)
    {
        try
        {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(target).getPropertyDescriptors();
            if (propertyDescriptors == null || propertyDescriptors.length == 0)
            {
                return null;
            }
            
            for (PropertyDescriptor pd : propertyDescriptors)
            {
                if (name.equals(pd.getName()))
                {
                    MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, target,
                            MethodHandles.lookup());
                    return createLambdaPropertyDescriptor(pd, lookup);
                }
            }

            throw new FacesException("Property \"" + name + "\" not found on \"" + target.getName() + "\"");
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }
    
    public static LambdaPropertyDescriptor createLambdaPropertyDescriptor(PropertyDescriptor pd,
            MethodHandles.Lookup lookup) throws Throwable
    {
        LambdaPropertyDescriptor lpd = new LambdaPropertyDescriptor();
        lpd.wrapped = pd;

        Method readMethod = pd.getReadMethod();
        if (readMethod != null)
        {
            MethodHandle handle = lookup.unreflect(readMethod);
            CallSite callSite = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    handle,
                    handle.type());
            lpd.readFunction = (Function) callSite.getTarget().invokeExact();
        }

        Method writeMethod = pd.getWriteMethod();
        if (writeMethod != null)
        {
            MethodHandle handle = lookup.unreflect(writeMethod);
            lpd.writeFunction = createSetter(lookup, lpd, handle);
        }

        return lpd;
    }
    
    public static Map<String, LambdaPropertyDescriptor> getLambdaPropertyDescriptors(Class<?> target)
    {
        try
        {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(target).getPropertyDescriptors();
            if (propertyDescriptors == null || propertyDescriptors.length == 0)
            {
                return Collections.emptyMap();
            }

            HashMap<String, LambdaPropertyDescriptor> properties = new HashMap<>(propertyDescriptors.length);

            MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, target,
                    MethodHandles.lookup());
            
            for (PropertyDescriptor pd : Introspector.getBeanInfo(target).getPropertyDescriptors())
            {
                LambdaPropertyDescriptor lpd = createLambdaPropertyDescriptor(pd, lookup);
                properties.put(pd.getName(), lpd);
            }
            
            return properties;
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static BiConsumer createSetter(MethodHandles.Lookup lookup, LambdaPropertyDescriptor propertyInfo,
            MethodHandle setterHandle)
            throws LambdaConversionException, Throwable
    {
        Class<?> propertyType = propertyInfo.getPropertyType();
        // special handling for primitives required, see https://dzone.com/articles/setters-method-handles-and-java-11
        if (propertyType.isPrimitive())
        {
            if (propertyType == double.class)
            {
                ObjDoubleConsumer consumer = (ObjDoubleConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjDoubleConsumer.class, double.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (double) b);
            }
            else if (propertyType == int.class)
            {
                ObjIntConsumer consumer = (ObjIntConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjIntConsumer.class, int.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (int) b);
            }
            else if (propertyType == long.class)
            {
                ObjLongConsumer consumer = (ObjLongConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjLongConsumer.class, long.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (long) b);
            }
            else if (propertyType == float.class)
            {
                ObjFloatConsumer consumer = (ObjFloatConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjFloatConsumer.class, float.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (float) b);
            }
            else if (propertyType == byte.class)
            {
                ObjByteConsumer consumer = (ObjByteConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjByteConsumer.class, byte.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (byte) b);
            }
            else if (propertyType == char.class)
            {
                ObjCharConsumer consumer = (ObjCharConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjCharConsumer.class, char.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (char) b);
            }
            else if (propertyType == short.class)
            {
                ObjShortConsumer consumer = (ObjShortConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjShortConsumer.class, short.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (short) b);
            }
            else if (propertyType == boolean.class)
            {
                ObjBooleanConsumer consumer = (ObjBooleanConsumer) createSetterCallSite(
                        lookup, setterHandle, ObjBooleanConsumer.class, boolean.class).getTarget().invokeExact();
                return (a, b) -> consumer.accept(a, (boolean) b);
            }
            else
            {
                throw new RuntimeException("Type is not supported yet: " + propertyType.getName());
            }
        }
        else
        {
            return (BiConsumer) createSetterCallSite(lookup, setterHandle, BiConsumer.class, Object.class).getTarget()
                    .invokeExact();
        }
    }

    protected static CallSite createSetterCallSite(MethodHandles.Lookup lookup, MethodHandle setter,
            Class<?> interfaceType, Class<?> valueType)
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
