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
package org.apache.myfaces.config;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import javax.naming.NamingException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.annotation.AnnotationProcessorFactory;
import org.apache.myfaces.config.element.ListEntries;
import org.apache.myfaces.config.element.ListEntry;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.ManagedProperty;
import org.apache.myfaces.config.element.MapEntries;
import org.apache.myfaces.config.element.MapEntry;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.AnnotationProcessor;


/**
 * Create and initialize managed beans
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a> (latest modification by $Author$)
 * @author Anton Koinov
 */
public class ManagedBeanBuilder
{
    private static Log log = LogFactory.getLog(ManagedBeanBuilder.class);
    private RuntimeConfig _runtimeConfig;
    public final static String REQUEST = "request";
    public final static String APPLICATION = "application";
    public final static String SESSION = "session";
    public final static String NONE = "none";

    public Object buildManagedBean(FacesContext facesContext, ManagedBean beanConfiguration) throws FacesException
    {

        /*final AnnotatedManagedBeanHandler handler = new AnnotatedManagedBeanHandler(bean,
                  beanConfiguration.getManagedBeanScope(), beanConfiguration.getManagedBeanName());

          final boolean threwUnchecked = handler.invokePostConstruct();

          if(threwUnchecked)
              return null;*/
        try
        {
            AnnotationProcessor annotationProcessor =
                    AnnotationProcessorFactory.getAnnotatonProcessorFactory().getAnnotatonProcessor(facesContext.getExternalContext());
            final Object bean = annotationProcessor.newInstance(beanConfiguration.getManagedBeanClassName());

            annotationProcessor.processAnnotations(bean);

            if (!beanConfiguration.getManagedBeanScope().equals(ManagedBeanBuilder.NONE))
            {
                annotationProcessor.postConstruct(bean);
            }

            switch (beanConfiguration.getInitMode())
            {
                case ManagedBean.INIT_MODE_PROPERTIES:
                    try
                    {
                        initializeProperties(facesContext, beanConfiguration.getManagedProperties(),
                                beanConfiguration.getManagedBeanScope(), bean);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new IllegalArgumentException(
                                e.getMessage()
                                        + " for bean '"
                                        + beanConfiguration.getManagedBeanName()
                                        + "' check the configuration to make sure all properties correspond with get/set methods", e);
                    }
                    break;

                case ManagedBean.INIT_MODE_MAP:
                    if (!(bean instanceof Map))
                    {
                        throw new IllegalArgumentException("Class " + bean.getClass().getName()
                                + " of managed bean "
                                + beanConfiguration.getManagedBeanName()
                                + " is not a Map.");
                    }
                    initializeMap(facesContext, beanConfiguration.getMapEntries(), (Map) bean);
                    break;

                case ManagedBean.INIT_MODE_LIST:
                    if (!(bean instanceof List))
                    {
                        throw new IllegalArgumentException("Class " + bean.getClass().getName()
                                + " of managed bean "
                                + beanConfiguration.getManagedBeanName()
                                + " is not a List.");
                    }
                    initializeList(facesContext, beanConfiguration.getListEntries(), (List) bean);
                    break;

                case ManagedBean.INIT_MODE_NO_INIT:
                    // no init values
                    break;

                default:
                    throw new IllegalStateException("Unknown managed bean type "
                            + bean.getClass().getName() + " for managed bean "
                            + beanConfiguration.getManagedBeanName() + '.');
            }
            return bean;
        }
        catch (IllegalAccessException e)
        {
            throw new FacesException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new FacesException(e);
        }
        catch (NamingException e)
        {
            throw new FacesException(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new FacesException(e);
        }
        catch (InstantiationException e)
        {
            throw new FacesException(e);
        }

    }


    private void initializeProperties(FacesContext facesContext, Iterator managedProperties, String targetScope, Object bean)
    {
        ELResolver elResolver = facesContext.getApplication().getELResolver();
        ELContext elContext = facesContext.getELContext();

        while (managedProperties.hasNext())
        {
            ManagedProperty property = (ManagedProperty) managedProperties.next();
            Object value = null;

            switch (property.getType())
            {
                case ManagedProperty.TYPE_LIST:

                    // JSF 1.1, 5.3.1.3
                    // Call the property getter, if it exists.
                    // If the getter returns null or doesn't exist, create a java.util.ArrayList,
                    // otherwise use the returned Object ...
                    if (PropertyUtils.isReadable(bean, property.getPropertyName()))
                        value = elResolver.getValue(elContext, bean, property.getPropertyName());
                    value = value == null ? new ArrayList() : value;

                    if (value instanceof List)
                    {
                        initializeList(facesContext, property.getListEntries(), (List) value);

                    }
                    else if (value != null && value.getClass().isArray())
                    {
                        int length = Array.getLength(value);
                        ArrayList temp = new ArrayList(length);
                        for (int i = 0; i < length; i++)
                        {
                            temp.add(Array.get(value, i));
                        }
                        initializeList(facesContext, property.getListEntries(), temp);
                        value = Array.newInstance(value.getClass().getComponentType(), temp.size());
                        length = temp.size();

                        for (int i = 0; i < length; i++)
                        {
                            Array.set(value, i, temp.get(i));
                        }
                    }
                    else
                    {
                        value = new ArrayList();
                        initializeList(facesContext, property.getListEntries(), (List) value);
                    }

                    break;
                case ManagedProperty.TYPE_MAP:

                    // JSF 1.1, 5.3.1.3
                    // Call the property getter, if it exists.
                    // If the getter returns null or doesn't exist, create a java.util.HashMap,
                    // otherwise use the returned java.util.Map .
                    if (PropertyUtils.isReadable(bean, property.getPropertyName()))
                        value = elResolver.getValue(elContext, bean, property.getPropertyName());
                    value = value == null ? new HashMap() : value;

                    if (!(value instanceof Map))
                    {
                        value = new HashMap();
                    }

                    initializeMap(facesContext, property.getMapEntries(), (Map) value);
                    break;
                case ManagedProperty.TYPE_NULL:
                    value = null;
                    break;
                case ManagedProperty.TYPE_VALUE:
                    // check for correct scope of a referenced bean
                    if (!isInValidScope(facesContext, property, targetScope))
                    {
                        throw new FacesException("Property " + property.getPropertyName() +
                                " references object in a scope with shorter lifetime than the target scope " + targetScope);
                    }
                    value = property.getRuntimeValue(facesContext);
                    break;
            }
            Class propertyClass = null;

            if (property.getPropertyClass() == null)
            {
                propertyClass = elResolver
                        .getType(elContext, bean, property.getPropertyName());
            }
            else
            {
                propertyClass = ClassUtils
                        .simpleJavaTypeToClass(property.getPropertyClass());
            }
            if (null == propertyClass)
            {
                throw new IllegalArgumentException("unable to find the type of property " + property.getPropertyName());
            }
            Object coercedValue = coerceToType(facesContext, value, propertyClass);
            elResolver.setValue(
                    elContext, bean, property.getPropertyName(), coercedValue);
        }
    }

    // We no longer use the convertToType from shared impl because we switched
    // to unified EL in JSF 1.2
    public static Object coerceToType(FacesContext facesContext, Object value, Class desiredClass)
    {
        if (value == null) return null;

        try
        {
            ExpressionFactory expFactory = facesContext.getApplication().getExpressionFactory();
            // Use coersion implemented by JSP EL for consistency with EL
            // expressions. Additionally, it caches some of the coersions.
            return expFactory.coerceToType(value, desiredClass);
        }
        catch (ELException e)
        {
            String message = "Cannot coerce " + value.getClass().getName()
                    + " to " + desiredClass.getName();
            log.error(message, e);
            throw new FacesException(message, e);
        }
    }


    /**
     * Check if the scope of the property value is valid for a bean to be stored in targetScope.
     *
     * @param facesContext
     * @param property     the property to be checked
     * @param targetScope  name of the target scope of the bean under construction
     */
    private boolean isInValidScope(FacesContext facesContext, ManagedProperty property, String targetScope)
    {
        if (!property.isValueReference())
        {
            // no value reference but a literal value -> nothing to check
            return true;
        }
        String[] expressions = extractExpressions(property.getValueBinding(facesContext).getExpressionString());

        for (int i = 0; i < expressions.length; i++)
        {
            String expression = expressions[i];
            if (expression == null)
            {
                continue;
            }

            String valueScope = getScope(facesContext, expression);

            // if the target scope is 'none' value scope has to be 'none', too
            if (targetScope == null || targetScope.equalsIgnoreCase(NONE))
            {
                if (valueScope != null && !(valueScope.equalsIgnoreCase(NONE)))
                {
                    return false;
                }
                return true;
            }

            // 'application' scope can reference 'application' and 'none'
            if (targetScope.equalsIgnoreCase(APPLICATION))
            {
                if (valueScope != null)
                {
                    if (valueScope.equalsIgnoreCase(REQUEST) ||
                            valueScope.equalsIgnoreCase(SESSION))
                    {
                        return false;
                    }
                }
                return true;
            }

            // 'session' scope can reference 'session', 'application', and 'none' but not 'request'
            if (targetScope.equalsIgnoreCase(SESSION))
            {
                if (valueScope != null)
                {
                    if (valueScope.equalsIgnoreCase(REQUEST))
                    {
                        return false;
                    }
                }
                return true;
            }

            // 'request' scope can reference any value scope
            if (targetScope.equalsIgnoreCase(REQUEST))
            {
                return true;
            }
        }
        return false;
    }


    private String getScope(FacesContext facesContext, String expression)
    {
        String beanName = getFirstSegment(expression);
        ExternalContext externalContext = facesContext.getExternalContext();

        // check scope objects
        if (beanName.equalsIgnoreCase("requestScope"))
        {
            return REQUEST;
        }
        if (beanName.equalsIgnoreCase("sessionScope"))
        {
            return SESSION;
        }
        if (beanName.equalsIgnoreCase("applicationScope"))
        {
            return APPLICATION;
        }

        // check implicit objects
        if (beanName.equalsIgnoreCase("cookie"))
        {
            return REQUEST;
        }
        if (beanName.equalsIgnoreCase("facesContext"))
        {
            return REQUEST;
        }

        if (beanName.equalsIgnoreCase("header"))
        {
            return REQUEST;
        }
        if (beanName.equalsIgnoreCase("headerValues"))
        {
            return REQUEST;
        }

        if (beanName.equalsIgnoreCase("initParam"))
        {
            return APPLICATION;
        }
        if (beanName.equalsIgnoreCase("param"))
        {
            return REQUEST;
        }
        if (beanName.equalsIgnoreCase("paramValues"))
        {
            return REQUEST;
        }
        if (beanName.equalsIgnoreCase("view"))
        {
            return REQUEST;
        }

        // not found so far - check all scopes
        if (externalContext.getRequestMap().get(beanName) != null)
        {
            return REQUEST;
        }
        if (externalContext.getSessionMap().get(beanName) != null)
        {
            return SESSION;
        }
        if (externalContext.getApplicationMap().get(beanName) != null)
        {
            return APPLICATION;
        }

        //not found - check mangaged bean config

        ManagedBean mbc = getRuntimeConfig(facesContext).getManagedBean(beanName);

        if (mbc != null)
        {
            return mbc.getManagedBeanScope();
        }

        return null;
    }

    /**
     * Extract the first expression segment, that is the substring up to the first '.' or '['
     *
     * @param expression
     * @return first segment of the expression
     */
    private String getFirstSegment(String expression)
    {
        int indexDot = expression.indexOf('.');
        int indexBracket = expression.indexOf('[');

        if (indexBracket < 0)
        {

            return indexDot < 0 ? expression : expression.substring(0, indexDot);

        }

        if (indexDot < 0)
        {
            return expression.substring(0, indexBracket);
        }

        return expression.substring(0, Math.min(indexDot, indexBracket));

    }

    private String[] extractExpressions(String expressionString)
    {
        String[] expressions = expressionString.split("\\#\\{");
        for (int i = 0; i < expressions.length; i++)
        {
            String expression = expressions[i];
            if (expression.trim().length() == 0)
            {
                expressions[i] = null;
            }
            else
            {
                int index = expression.indexOf('}');
                expressions[i] = expression.substring(0, index);
            }
        }
        return expressions;
    }


    private void initializeMap(FacesContext facesContext, MapEntries mapEntries, Map map)
    {
        Application application = facesContext.getApplication();
        Class keyClass = (mapEntries.getKeyClass() == null)
                ? String.class : ClassUtils.simpleJavaTypeToClass(mapEntries.getKeyClass());
        Class valueClass = (mapEntries.getValueClass() == null)
                ? String.class : ClassUtils.simpleJavaTypeToClass(mapEntries.getValueClass());
        ValueExpression valueExpression;
        ExpressionFactory expFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        for (Iterator iterator = mapEntries.getMapEntries(); iterator.hasNext();)
        {
            MapEntry entry = (MapEntry) iterator.next();
            Object key = entry.getKey();

            if (UIComponentTag.isValueReference((String) key))
            {
                valueExpression = expFactory.createValueExpression(elContext, (String) key, Object.class);
                key = valueExpression.getValue(elContext);
            }

            if (entry.isNullValue())
            {
                map.put(coerceToType(facesContext, key, keyClass), null);
            }
            else
            {
                Object value = entry.getValue();
                if (UIComponentTag.isValueReference((String) value))
                {
                    valueExpression = expFactory.createValueExpression(elContext, (String) value, Object.class);
                    value = valueExpression.getValue(elContext);
                }
                map.put(coerceToType(facesContext, key, keyClass), coerceToType(facesContext, value, valueClass));
            }
        }
    }


    private void initializeList(FacesContext facesContext, ListEntries listEntries, List list)
    {
        Application application = facesContext.getApplication();
        Class valueClass = listEntries.getValueClass() == null ? String.class : ClassUtils.simpleJavaTypeToClass(listEntries.getValueClass());
        ExpressionFactory expFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        for (Iterator iterator = listEntries.getListEntries(); iterator.hasNext();)
        {
            ListEntry entry = (ListEntry) iterator.next();
            if (entry.isNullValue())
            {
                list.add(null);
            }
            else
            {
                Object value = entry.getValue();
                if (UIComponentTag.isValueReference((String) value))
                {
                    ValueExpression valueExpression = expFactory.createValueExpression(elContext, (String) value, Object.class);
                    value = valueExpression.getValue(elContext);
                }
                list.add(coerceToType(facesContext, value, valueClass));
            }
        }
    }

    private RuntimeConfig getRuntimeConfig(FacesContext facesContext)
    {
        if (_runtimeConfig == null)
        {
            _runtimeConfig = RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
        }
        return _runtimeConfig;
    }
}
