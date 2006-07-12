/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.el;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class PropertyResolverImpl extends PropertyResolver
{
    private static final Log log =
        LogFactory.getLog(PropertyResolverImpl.class);

    //~ Static fields/initializers ---------------------------------------------

    private static final Object[] NO_ARGS = {};

    //~ Public PropertyResolver Methods ----------------------------------------

    public Object getValue(Object base, Object property)
            throws EvaluationException, PropertyNotFoundException
    {
        try
        {
           if(base == null)
           {
               if(log.isDebugEnabled())
                 log.debug("property : "+property +" could not be retrieved as base was null.");

               return null;
           }

           //fix for myfaces-315 - empty string as key to a map-value is allowed
           //thanks to duffy gillman
           if (property == null ||
               (property instanceof String && ((String)property).length() == 0 &&
                !(base instanceof Map)))
           {
               if(log.isDebugEnabled())
                 log.debug("property for base with class: "+ base.getClass().getName()  +" could not be retrieved as property was null.");

               return null;
           }
           if (base instanceof Map)
           {
               return ((Map) base).get(property);
           }

            // If none of the special bean types, then process as normal Bean
            return getProperty(base, property.toString());
        }
        catch (PropertyNotFoundException e) {

            if(log.isDebugEnabled())
              log.debug("Exception while retrieving property; base with class : "+base.getClass().getName()+", property : "+property,e);

            throw e;
        }
        catch (RuntimeException e)
        {
            if(log.isDebugEnabled())
              log.debug("Exception while retrieving property; base : "+base.getClass().getName()+", property : "+property,e);

            throw new EvaluationException("Exception getting value of property " + property
                + " of base of type : "
                + base.getClass().getName(), e);
        }
    }

    public Object getValue(Object base, int index)
            throws EvaluationException, PropertyNotFoundException
    {
        try
        {
            if (base == null)
            {
                log.debug("index : "+index+" not retrievable cause base is null.");
                return null;
            }

            try
            {
                if (base.getClass().isArray())
                {
                    return Array.get(base, index);
                }
                if (base instanceof List)
                {
                    return ((List) base).get(index);
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                if(log.isDebugEnabled())
                    log.debug("IndexOutOfBoundException while getting property; base with class: "+base.getClass().getName()+", index : "+index,e);

                // Note: ArrayIndexOutOfBoundsException also here
                return null;
            }

            throw new ReferenceSyntaxException("Must be array or List. Bean with class: "
                + base.getClass().getName() + ", index " + index);
        }
        catch (RuntimeException e)
        {
            if(log.isDebugEnabled())
                log.debug("Exception while getting property; base with class: "+base.getClass().getName()+", index : "+index,e);

            throw new EvaluationException("Exception getting value for index " + index
                + " of bean "
                + base != null ? base.getClass().getName() : "NULL", e);
        }
    }

    public void setValue(Object base, Object property, Object newValue)
            throws EvaluationException, PropertyNotFoundException
    {
        try
        {
            if (base == null)
            {
                throw new PropertyNotFoundException(
                    "Null bean, property: " + property);
            }
            if (property == null ||
                property instanceof String && ((String)property).length() == 0)
            {
                throw new PropertyNotFoundException("Bean with class : "
                    + base.getClass().getName()
                    + ", null or empty property name");
            }

            if (base instanceof Map)
            {
                ((Map) base).put(property, newValue);

                return;
            }

            // If none of the special bean types, then process as normal Bean
            setProperty(base, property.toString(), newValue);
        }
        catch (PropertyNotFoundException e) {
            if(log.isDebugEnabled())
                log.debug("Exception while setting property; base with class : "+base.getClass().getName()+", property : "+property,e);
            throw e;
        }
        catch (RuntimeException e)
        {
            if(log.isDebugEnabled())
                log.debug("Exception while setting property; base : "+base.getClass().getName()+", property : "+property,e);

            throw new EvaluationException("Exception setting property " + property
                + " of base with class "
                + base.getClass().getName(), e);
        }
    }

    public void setValue(Object base, int index, Object newValue)
            throws EvaluationException, PropertyNotFoundException
    {
        try
        {
            if (base == null)
            {
                throw new PropertyNotFoundException(
                    "Null bean, index: " + index);
            }

            try
            {
                if (base.getClass().isArray())
                {
                    Array.set(base, index, newValue);

                    return;
                }
                if (base instanceof List)
                {
                    // REVISIT: should we try to grow the list, if growable type
                    //          (e.g., ArrayList, etc.), and if not large
                    //          enough?
                    ((List) base).set(index, newValue);

                    return;
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new PropertyNotFoundException("Base with class : "
                    + base.getClass().getName() + ", index " + index, e);
            }

            throw new EvaluationException(
                "Bean must be array or List. Base with class: "
                + base.getClass().getName() + ", index " + index);
        }
        catch (PropertyNotFoundException e) {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw new EvaluationException("Exception setting value of index " + index + " of bean "
                + base.getClass().getName(), e);
        }
    }

    public boolean isReadOnly(Object base, Object property)
    {
        try
        {
            if (base == null || property == null ||
                property instanceof String && ((String)property).length() == 0)
            {
                // Cannot determine read-only, return false (is this what the spec requires?)
                return false;
            }

            // Is there any way to determine whether Map.put() will fail?
            if (base instanceof Map)
            {
                return false;
            }

            // If none of the special bean types, then process as normal Bean
            PropertyDescriptor propertyDescriptor =
                getPropertyDescriptor(base, property.toString());

            return propertyDescriptor.getWriteMethod() == null;
        }
        catch (Exception e)
        {
            // Cannot determine read-only, return false (is this what the spec requires?)
            return false;
        }
    }

    public boolean isReadOnly(Object base, int index)
    {
        try
        {
            /*todo: actually implement something here*/
            if (base == null)
            {
                // Cannot determine read-only, return false (is this what the spec requires?)
                return false;
            }
            if (base instanceof List || base.getClass().isArray())
            {
                // Is there any way to determine whether List.set() will fail?
                return false;
            }

            // Cannot determine read-only, return false (is this what the spec requires?)
            return false;
        }
        catch (Exception e)
        {
            // Cannot determine read-only, return false (is this what the spec requires?)
            return false;
        }
    }

    public Class getType(Object base, Object property)
    {
        try
        {
            if (base == null)
            {
                throw new PropertyNotFoundException("Base bean is null.");
            }
            else if (property == null)
            {
                throw new PropertyNotFoundException("Property name is null.");
            }
            else if (property instanceof String && ((String)property).length() == 0)
            {
                throw new PropertyNotFoundException("Property name is an empty String.");
            }

            if (base instanceof Map)
            {
                Object value = ((Map) base).get(property);

                // REVISIT: when generics are imlemented in JVM 1.5
                return (value == null) ? Object.class : value.getClass();
            }

            // If none of the special bean types, then process as normal Bean
            PropertyDescriptor propertyDescriptor =
                getPropertyDescriptor(base, property.toString());

            return propertyDescriptor.getPropertyType();
        }
        catch (PropertyNotFoundException e) {
            throw e;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Class getType(Object base, int index)
    {
        if (base == null)
        {
            throw new PropertyNotFoundException("Bean is null");
        }

        try
        {
            if (base.getClass().isArray())
            {
                if (base instanceof Object[] && ((Object[])base)[index] != null) {
                    Object[] array = (Object[]) base;
                    return array[index].getClass().getComponentType();
                } else {
                    return base.getClass().getComponentType();
                }
            }

            if (base instanceof List)
            {
                // REVISIT: does it make sense to do this or simply return
                //          Object.class? What if the new value is not of
                //          the old value's class?
                Object value = ((List) base).get(index);

                // REVISIT: when generics are implemented in JVM 1.5
                return (value != null) ? value.getClass() : Object.class;
            }

            // Cannot determine type, return null per JSF spec
            return null;
        }
        catch (IndexOutOfBoundsException e) {
            throw new PropertyNotFoundException("Bean: "
                + base.getClass().getName() + ", index " + index, e);
        }
        catch (Exception e)
        {
            throw new EvaluationException("Exception getting type of index " + index + " of bean with class : "
                + base.getClass().getName(), e);
        }
    }


    //~ Internal Helper Methods ------------------------------------------------

    public static void setProperty(Object base, String name, Object newValue)
    {
        PropertyDescriptor propertyDescriptor =
            getPropertyDescriptor(base, name);

        Method m = propertyDescriptor.getWriteMethod();
        if (m == null)
        {
            throw new PropertyNotFoundException(
                getMessage(base, name));
        }

        // Check if the concrete class of this method is accessible and if not
        // search for a public interface that declares this method
        m = MethodUtils.getAccessibleMethod(m);
        if (m == null)
        {
            throw new PropertyNotFoundException(
                getMessage(base, name) + " (not accessible!)");
        }

        try
        {
            m.invoke(base, new Object[] {newValue});
        }
        catch (Throwable t)
        {
            throw new EvaluationException(getMessage(base, name, newValue, m), t);
        }
    }

    private static String getMessage(Object base, String name, Object newValue, Method m)
    {
        return "Bean: "
            + base.getClass().getName() + ", property: " + name +", newValue: "+(newValue==null?" null ":newValue)+
                ",newValue class: "+(newValue==null?" null ":newValue.getClass().getName())+" method parameter class: "
                +((m.getParameterTypes()!=null&&m.getParameterTypes().length>0)
                    ?m.getParameterTypes()[0].getName():"null");

    }

    private static String getMessage(Object base, String name)
    {
        return "Bean: "
            + base.getClass().getName() + ", property: " + name;
    }

    public static Object getProperty(Object base, String name)
    {
        PropertyDescriptor propertyDescriptor =
            getPropertyDescriptor(base, name);

        Method m = propertyDescriptor.getReadMethod();
        if (m == null)
        {
            throw new PropertyNotFoundException(
                getMessage(base, name));
        }

        // Check if the concrete class of this method is accessible and if not
        // search for a public interface that declares this method
        m = MethodUtils.getAccessibleMethod(m);
        if (m == null)
        {
            throw new PropertyNotFoundException(
                getMessage(base, name) + " (not accessible!)");
        }

        try
        {
            return m.invoke(base, NO_ARGS);
        }
        catch (Throwable t)
        {
            throw new EvaluationException(getMessage(base, name), t);
        }
    }

    public static PropertyDescriptor getPropertyDescriptor(
        Object base, String name)
    {
        PropertyDescriptor propertyDescriptor;

        try
        {
            propertyDescriptor =
                getPropertyDescriptor(
                    Introspector.getBeanInfo(base.getClass()), name);
        }
        catch (IntrospectionException e)
        {
            throw new PropertyNotFoundException(getMessage(base, name), e);
        }

        return propertyDescriptor;
    }

    public static PropertyDescriptor getPropertyDescriptor(
        BeanInfo beanInfo, String propertyName)
    {
        PropertyDescriptor[] propDescriptors =
            beanInfo.getPropertyDescriptors();

        if (propDescriptors != null)
        {
            // TODO: cache this in classLoader safe way
            for (int i = 0, len = propDescriptors.length; i < len; i++)
            {
                if (propDescriptors[i].getName().equals(propertyName))
                    return propDescriptors[i];
            }
        }

        throw new PropertyNotFoundException("Bean: "
            + beanInfo.getBeanDescriptor().getBeanClass().getName()
            + ", property: " + propertyName);
    }

}
