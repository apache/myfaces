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
package org.apache.myfaces.config;

import org.apache.myfaces.config.element.*;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.commons.beanutils.PropertyUtils;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import java.util.*;
import java.lang.reflect.Array;


/**
 * Create and initialize managed beans
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a> (latest modification by $Author$)
 * @author Anton Koinov
 */
public class ManagedBeanBuilder
{
    private RuntimeConfig _runtimeConfig;


    public Object buildManagedBean(FacesContext facesContext, ManagedBean beanConfiguration) throws FacesException
    {
        Object bean = ClassUtils.newInstance(beanConfiguration.getManagedBeanClassName());

        switch (beanConfiguration.getInitMode())
        {
            case ManagedBean.INIT_MODE_PROPERTIES:
                try {
                  initializeProperties(facesContext, beanConfiguration.getManagedProperties(),
                      beanConfiguration.getManagedBeanScope(), bean);
                } catch (IllegalArgumentException e) {
                  throw new IllegalArgumentException(
                          e.getMessage()
                              + " for bean '"
                              + beanConfiguration.getManagedBeanName()
                              + "' check the configuration to make sure all properties correspond with get/set methods");
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


    private void initializeProperties(FacesContext facesContext, Iterator managedProperties, String targetScope, Object bean)
    {
        PropertyResolver propertyResolver =
            facesContext.getApplication().getPropertyResolver();

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
                    if(PropertyUtils.isReadable(bean, property.getPropertyName()))
                        value = propertyResolver.getValue(bean, property.getPropertyName());
                    value = value == null ? new ArrayList() : value;
                    
                    if (value instanceof List) {
                        initializeList(facesContext, property.getListEntries(), (List) value);

                    } else if (value != null && value.getClass().isArray()) {
                        int length = Array.getLength(value);
                        ArrayList temp = new ArrayList(length);
                        for (int i = 0; i < length; i++) {
                            temp.add(Array.get(value, i));
                        }
                        initializeList(facesContext, property.getListEntries(), temp);
                        value = Array.newInstance(value.getClass().getComponentType(), temp.size());
                        length = temp.size();

                        for (int i = 0; i < length; i++) {
                            Array.set(value, i, temp.get(i));
                        }
                    } else {
                          value = new ArrayList();
                        initializeList(facesContext, property.getListEntries(), (List) value);
                    }

                    break;
                case ManagedProperty.TYPE_MAP:

                    // JSF 1.1, 5.3.1.3 
                    // Call the property getter, if it exists.
                    // If the getter returns null or doesn't exist, create a java.util.HashMap,
                    // otherwise use the returned java.util.Map .
                    if(PropertyUtils.isReadable(bean, property.getPropertyName()))
                        value = propertyResolver.getValue(bean, property.getPropertyName());
                    value = value == null ? new HashMap() : value;
                    
                    if (! (value instanceof Map)) {
                        value = new HashMap();
                    }

                    initializeMap(facesContext, property.getMapEntries(), (Map) value);
                    break;
                case ManagedProperty.TYPE_NULL:
                    value = null;
                    break;
                case ManagedProperty.TYPE_VALUE:
                    // check for correct scope of a referenced bean
                    if (! isInValidScope(facesContext, property, targetScope)) {
                        throw new FacesException("Property " + property.getPropertyName() +
                            " references object in a scope with shorter lifetime than the target scope " + targetScope);
                    }
                    value = property.getRuntimeValue(facesContext);
                    break;
            }
            Class propertyClass = null;

            if (property.getPropertyClass() == null)
            {
                propertyClass = propertyResolver
                    .getType(bean, property.getPropertyName());
            }
            else
            {
                propertyClass = ClassUtils
                    .simpleJavaTypeToClass(property.getPropertyClass());
            }
            if(null == propertyClass) {
              throw new IllegalArgumentException("unable to find the type of property " + property.getPropertyName());
            }
            Object coercedValue = ClassUtils.convertToType(value, propertyClass);
            propertyResolver.setValue(
                bean, property.getPropertyName(), coercedValue);
        }
    }


    /**
     * Check if the scope of the property value is valid for a bean to be stored in targetScope.
     * @param facesContext
     * @param property          the property to be checked
     * @param targetScope       name of the target scope of the bean under construction
     */
    private boolean isInValidScope(FacesContext facesContext, ManagedProperty property, String targetScope)
    {
        if (! property.isValueReference()) {
            // no value reference but a literal value -> nothing to check
            return true;
        }
        String[] expressions = extractExpressions(property.getValueBinding(facesContext).getExpressionString());

        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression == null) {
                continue;
            }

            String valueScope = getScope(facesContext, expression);

            // if the target scope is 'none' value scope has to be 'none', too
            if (targetScope == null || targetScope.equalsIgnoreCase("none")) {
                if (valueScope != null && !(valueScope.equalsIgnoreCase("none"))) {
                    return false;
                }
                return true;
            }

            // 'application' scope can reference 'application' and 'none'
            if (targetScope.equalsIgnoreCase("application")) {
                if (valueScope != null) {
                    if (valueScope.equalsIgnoreCase("request") ||
                        valueScope.equalsIgnoreCase("session")) {
                        return false;
                    }
                }
                return true;
            }

            // 'session' scope can reference 'session', 'application', and 'none' but not 'request'
            if (targetScope.equalsIgnoreCase("session")) {
                if (valueScope != null) {
                    if (valueScope.equalsIgnoreCase("request")) {
                        return false;
                    }
                }
                return true;
            }

            // 'request' scope can reference any value scope
            if (targetScope.equalsIgnoreCase("request")) {
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
        if (beanName.equalsIgnoreCase("requestScope")) {
            return "request";
        }
        if (beanName.equalsIgnoreCase("sessionScope")) {
            return "session";
        }
        if (beanName.equalsIgnoreCase("applicationScope")) {
            return "application";
        }

        // check implicit objects
        if (beanName.equalsIgnoreCase("cookie")) {
        return "request";
        }
        if (beanName.equalsIgnoreCase("facesContext")) {
            return "request";
        }

        if (beanName.equalsIgnoreCase("header")) {
            return "request";
        }
        if (beanName.equalsIgnoreCase("headerValues")) {
            return "request";
        }

        if (beanName.equalsIgnoreCase("initParam")) {
        return "application";
        }
        if (beanName.equalsIgnoreCase("param")) {
            return "request";
        }
        if (beanName.equalsIgnoreCase("paramValues")) {
            return "request";
        }
        if (beanName.equalsIgnoreCase("view")) {
            return "request";
        }


        // not found so far - check all scopes
        if (externalContext.getRequestMap().get(beanName) != null) {
            return "request";
        }
        if (externalContext.getSessionMap().get(beanName) != null) {
            return "session";
        }
        if (externalContext.getApplicationMap().get(beanName) != null) {
            return "application";
        }

        //not found - check mangaged bean config


        ManagedBean mbc = getRuntimeConfig(facesContext).getManagedBean(beanName);

        if (mbc != null) {
            return mbc.getManagedBeanScope();
        }

        return null;
    }




    /**
     * Extract the first expression segment, that is the substring up to the first '.' or '['
     * @param expression
     * @return first segment of the expression
     */
    private String getFirstSegment(String expression)
    {
        int indexDot = expression.indexOf('.');
        int indexBracket = expression.indexOf('[');

        if (indexBracket < 0) {
                if (indexDot < 0) {
                    return expression;
                } else {
                    return expression.substring(0, indexDot);
                }
        } else {
            if (indexDot < 0) {
                return expression.substring(0, indexBracket);
            } else {
                return expression.substring(0, Math.min(indexDot, indexBracket));
            }
        }
    }

    private String[] extractExpressions(String expressionString)
    {
        String[] expressions = expressionString.split("\\#\\{");
        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression.trim().length() == 0) {
                expressions[i] = null;
            } else {
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
        ValueBinding valueBinding;

        for (Iterator iterator = mapEntries.getMapEntries(); iterator.hasNext();)
        {
            MapEntry entry = (MapEntry) iterator.next();
            Object key = entry.getKey();

            if (UIComponentTag.isValueReference((String) key))
            {
                valueBinding = application.createValueBinding((String) key);
                key = valueBinding.getValue(facesContext);
            }

            if (entry.isNullValue())
            {
                map.put(ClassUtils.convertToType(key, keyClass), null);
            }
            else
            {
                Object value = entry.getValue();
                if (UIComponentTag.isValueReference((String) value))
                {
                    valueBinding = application.createValueBinding((String) value);
                    value = valueBinding.getValue(facesContext);
                }
                map.put(ClassUtils.convertToType(key, keyClass), ClassUtils.convertToType(value, valueClass));
            }
        }
    }


    private void initializeList(FacesContext facesContext, ListEntries listEntries, List list)
    {
        Application application = facesContext.getApplication();
        Class valueClass = listEntries.getValueClass() == null ? String.class : ClassUtils.simpleJavaTypeToClass(listEntries.getValueClass());
        ValueBinding valueBinding;

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
                    valueBinding = application.createValueBinding((String) value);
                    value = valueBinding.getValue(facesContext);
                }
                list.add(ClassUtils.convertToType(value, valueClass));
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
