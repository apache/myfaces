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
package org.apache.myfaces.core.api.shared.lang;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyDescriptorUtils
{
    private static final Logger LOG = Logger.getLogger(PropertyDescriptorUtils.class.getName());

    /**
     * Defines if Lambdas should be used over reflection whenever possible to access getter/setter.
     */
    @JSFWebConfigParam(since="4.1.1", defaultValue="false", expectedValues="true,false", tags="performance")
    public static final String USE_LAMBDAS_OVER_REFLECTION
            = "org.apache.myfaces.USE_LAMBDAS_OVER_REFLECTION";

    private static final String CACHE_KEY = PropertyDescriptorUtils.class.getName() + ".CACHE";

    private static Map<Module, List<Module>> moduleAccessCache = new ConcurrentHashMap<>();

    private static Map<String, Map<String, ? extends PropertyDescriptorWrapper>> getCache(ExternalContext ec)
    {
        Map<String, Map<String, ? extends PropertyDescriptorWrapper>> cache =
                (Map<String, Map<String, ? extends PropertyDescriptorWrapper>>) ec.getApplicationMap().get(CACHE_KEY);
        if (cache == null)
        {
            cache = new ConcurrentHashMap<>(1000);
            ec.getApplicationMap().put(CACHE_KEY, cache);
        }

        return cache;
    }

    public static Map<String, ? extends PropertyDescriptorWrapper> getCachedPropertyDescriptors(ExternalContext ec,
                                                                                                Class<?> caller,
                                                                                                Class<?> target)
    {
        Map<String, ? extends PropertyDescriptorWrapper> cache = getCache(ec).get(target.getName());
        if (cache == null)
        {
            final Class<?> realTarget = target;
            cache = getCache(ec).computeIfAbsent(target.getName(), k -> getPropertyDescriptors(ec, caller, target));
        }

        return cache;
    }

    public static boolean isUseLambdas(ExternalContext ec)
    {
        // disabled per default
        String useMethodHandles = ec.getInitParameter(USE_LAMBDAS_OVER_REFLECTION);
        return useMethodHandles != null && useMethodHandles.contains("true");
    }

    protected static Map<String, ? extends PropertyDescriptorWrapper> getPropertyDescriptors(
            ExternalContext ec,
            Class<?> caller,
            Class<?> target)
    {
        try
        {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(target).getPropertyDescriptors();

            Map<String, PropertyDescriptorWrapper> map = new ConcurrentHashMap<>(propertyDescriptors.length);

            for (int i = 0; i < propertyDescriptors.length; i++)
            {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];

                LambdaPropertyDescriptor lambdaPropertyDescriptor = null;

                if (isUseLambdas(ec))
                {
                    List<Module> cache = moduleAccessCache.computeIfAbsent(caller.getModule(),
                            (k) -> new CopyOnWriteArrayList<>());
                    if (!cache.contains(target.getModule()))
                    {
                        if (!caller.getModule().canRead(target.getModule()))
                        {
                            caller.getModule().addReads(target.getModule());
                        }
                        cache.add(target.getModule());
                    }

                    try
                    {
                        lambdaPropertyDescriptor =
                                createLambdaPropertyDescriptor(caller, target, propertyDescriptor);
                        if (lambdaPropertyDescriptor == null)
                        {
                            LOG.log(Level.SEVERE,
                                    "Could not generate LambdaPropertyDescriptor for "
                                            + target.getName() + "#" + propertyDescriptor.getName()
                                            + ". Use PropertyDescriptor...");
                        }
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                        LOG.log(Level.SEVERE,
                                "Could not generate LambdaPropertyDescriptor for "
                                        + target.getName() + "#" + propertyDescriptor.getName()
                                        + ". Use PropertyDescriptor...",
                                e);
                    }
                }

                if (lambdaPropertyDescriptor == null)
                {
                    map.put(propertyDescriptor.getName(),
                            new PropertyDescriptorWrapper(target, propertyDescriptor));
                }
                else
                {
                    map.put(propertyDescriptor.getName(), lambdaPropertyDescriptor);
                }
            }

            return map;
        }
        catch (IntrospectionException e)
        {
            throw new FacesException(e);
        }
    }

    public static LambdaPropertyDescriptor createLambdaPropertyDescriptor(
            Class<?> caller,
            Class<?> target,
            PropertyDescriptor pd) throws Throwable
    {
        if (pd.getPropertyType() == null)
        {
            return null;
        }

        LambdaPropertyDescriptor lpd = new LambdaPropertyDescriptor(target, pd);

        MethodHandles.Lookup lookup =
                MethodHandles.privateLookupIn(PropertyDescriptorUtils.class, MethodHandles.lookup());

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
