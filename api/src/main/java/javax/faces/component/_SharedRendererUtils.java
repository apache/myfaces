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
package javax.faces.component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

/**
 * The util methods in this class are shared between the javax.faces.component package and the
 * org.apache.myfaces.renderkit package. Please note: Any changes here must also apply to the class in the other
 * package!
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _SharedRendererUtils
{
    
    static final String COLLECTION_TYPE_KEY = "collectionType";
    
    static Converter findUIOutputConverter(FacesContext facesContext, UIOutput component)
    {
        // Attention!
        // This code is duplicated in jsfapi component package.
        // If you change something here please do the same in the other class!

        Converter converter = component.getConverter();
        if (converter != null)
            return converter;

        // Try to find out by value expression
        ValueExpression expression = component.getValueExpression("value");
        if (expression == null)
            return null;

        Class<?> valueType = expression.getType(facesContext.getELContext());
        if (valueType == null)
            return null;

        if (Object.class.equals(valueType))
            return null; // There is no converter for Object class

        try
        {
            return facesContext.getApplication().createConverter(valueType);
        }
        catch (FacesException e)
        {
            log(facesContext, "No Converter for type " + valueType.getName() + " found", e);
            return null;
        }
    }

    static Object getConvertedUISelectManyValue(FacesContext facesContext, UISelectMany component,
                                                String[] submittedValue) throws ConverterException
    {
        // Attention!
        // This code is duplicated in jsfapi component package.
        // If you change something here please do the same in the other class!

        if (submittedValue == null)
        {
            throw new NullPointerException("submittedValue");
        }

        ValueExpression expression = component.getValueExpression("value");
        Object targetForConvertedValues = null;
        // if the component has an attached converter, use it
        Converter converter = component.getConverter();
        if (expression != null)
        {
            Class<?> modelType = expression
                    .getType(facesContext.getELContext());
            if (modelType.isArray())
            {
                // the target should be an array
                Class<?> componentType = modelType.getComponentType();
                // check for optimization if the target is
                // a string array --> no conversion needed
                if (String.class.equals(componentType))
                {
                    return submittedValue;
                }
                if (converter == null)
                {
                    // the compononent does not have an attached converter
                    // --> try to get a registered-by-class converter
                    converter = facesContext.getApplication().createConverter(
                            componentType);

                    if (converter == null)
                    {
                        // could not obtain a Converter
                        // --> check if we maybe do not really have to convert
                        if (!Object.class.equals(componentType))
                        {
                            // target is not an Object array
                            // and not a String array (checked some lines above)
                            // and we do not have a Converter
                            throw new ConverterException(
                                    "Could not obtain a Converter for "
                                            + componentType.getName());
                        }
                    }
                }
                // instantiate the array
                targetForConvertedValues = Array.newInstance(componentType,
                        submittedValue.length);
            }
            else if (Collection.class.isAssignableFrom(modelType) || Object.class.equals(modelType))
            {
                if (converter == null)
                {
                    // try to get the by-type-converter from the type of the SelectItems
                    _SelectItemsIterator iterator = new _SelectItemsIterator(component, facesContext);
                    converter = getSelectItemsValueConverter(iterator, facesContext);
                }
                
                if (Collection.class.isAssignableFrom(modelType))
                {
                    // the target should be a Collection
                    Object collectionTypeAttr = component.getAttributes().get(
                            COLLECTION_TYPE_KEY);
                    if (collectionTypeAttr != null)
                    {
                        Class<?> collectionType = null;
                        // if there is a value, it must be a ...
                        // ... a ValueExpression that evaluates to a String or a Class
                        if (collectionTypeAttr instanceof ValueExpression)
                        {
                            // get the value of the ValueExpression
                            collectionTypeAttr = ((ValueExpression) collectionTypeAttr)
                                    .getValue(facesContext.getELContext());
                        }
                        // ... String that is a fully qualified Java class name
                        if (collectionTypeAttr instanceof String)
                        {
                            try
                            {
                                collectionType = Class
                                        .forName((String) collectionTypeAttr);
                            }
                            catch (ClassNotFoundException cnfe)
                            {
                                throw new FacesException(
                                        "Unable to find class "
                                                + collectionTypeAttr
                                                + " on the classpath.", cnfe);
                            }
    
                        }
                        // ... a Class object
                        else if (collectionTypeAttr instanceof Class)
                        {
                            collectionType = (Class<?>) collectionTypeAttr;
                        }
                        else
                        {
                            throw new FacesException(
                                    "The attribute "
                                            + COLLECTION_TYPE_KEY
                                            + " of component "
                                            + component.getClientId()
                                            + " does not evaluate to a "
                                            + "String, a Class object or a ValueExpression pointing "
                                            + "to a String or a Class object.");
                        }
                        // now we have a collectionType --> but is it really some kind of Collection
                        if (!Collection.class.isAssignableFrom(collectionType))
                        {
                            throw new FacesException("The attribute "
                                    + COLLECTION_TYPE_KEY + " of component "
                                    + component.getClientId()
                                    + " does not point to a valid type of Collection.");
                        }
                        // now we have a real collectionType --> try to instantiate it
                        try
                        {
                            targetForConvertedValues = collectionType.newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new FacesException("The Collection "
                                    + collectionType.getName()
                                    + "can not be instantiated.", e);
                        }
                    }
                    else
                    {
                        // component.getValue() will implement Collection at this point
                        Collection<?> componentValue = (Collection<?>) component
                                .getValue();
                        // can we clone the Collection
                        if (componentValue instanceof Cloneable)
                        {
                            // clone method of Object is protected --> use reflection
                            try
                            {
                                Method cloneMethod = componentValue.getClass()
                                        .getMethod("clone");
                                Collection<?> clone = (Collection<?>) cloneMethod
                                        .invoke(componentValue);
                                clone.clear();
                                targetForConvertedValues = clone;
                            }
                            catch (Exception e)
                            {
                                log(facesContext, "Could not clone "
                                        + componentValue.getClass().getName(), e);
                            }
                        }
    
                        // if clone did not work
                        if (targetForConvertedValues == null)
                        {
                            // try to create the (concrete) collection from modelType 
                            // or with the class object of componentValue (if any)
                            try
                            {
                                targetForConvertedValues = (componentValue != null ? componentValue
                                        .getClass()
                                        : modelType).newInstance();
                            }
                            catch (Exception e)
                            {
                                // this did not work either
                                // use the standard concrete type
                                if (SortedSet.class.isAssignableFrom(modelType))
                                {
                                    targetForConvertedValues = new TreeSet();
                                }
                                else if (Queue.class.isAssignableFrom(modelType))
                                {
                                    targetForConvertedValues = new LinkedList();
                                }
                                else if (Set.class.isAssignableFrom(modelType))
                                {
                                    targetForConvertedValues = new HashSet(
                                            submittedValue.length);
                                }
                                else
                                {
                                    targetForConvertedValues = new ArrayList(
                                            submittedValue.length);
                                }
                            }
                        }
                    }
                }
                else /* if (Object.class.equals(modelType)) */
                {
                    // a modelType of Object is also permitted, in order to support
                    // managed bean properties of type Object
                    
                    // optimization: if we don't have a converter, we can return the submittedValue
                    if (converter == null)
                    {
                        return submittedValue;
                    }
                    
                    targetForConvertedValues = new Object[submittedValue.length];
                }
            }
            else
            {
                // the expression does neither point to an array nor to a collection
                throw new ConverterException(
                        "ValueExpression for UISelectMany must be of type Collection or Array.");
            }
        }
        else
        {
            targetForConvertedValues = new Object[submittedValue.length];
        }

        // convert the values with the selected converter (if any)
        // and store them in targetForConvertedValues
        boolean isArray = (targetForConvertedValues.getClass().isArray());
        for (int i = 0; i < submittedValue.length; i++)
        {
            // get the value
            Object value;
            if (converter != null)
            {
                value = converter.getAsObject(facesContext, component,
                        submittedValue[i]);
            }
            else
            {
                value = submittedValue[i];
            }
            // store it in targetForConvertedValues
            if (isArray)
            {
                Array.set(targetForConvertedValues, i, value);
            }
            else
            {
                ((Collection) targetForConvertedValues).add(value);
            }
        }

        return targetForConvertedValues;
    }
    
    /**
     * Iterates through the SelectItems with the given Iterator and tries to obtain
     * a by-class-converter based on the Class of SelectItem.getValue().
     * @param iterator
     * @param facesContext
     * @return The first suitable Converter for the given SelectItems or null.
     */
    static Converter getSelectItemsValueConverter(Iterator<SelectItem> iterator, FacesContext facesContext)
    {
        // Attention!
        // This code is duplicated in jsfapi component package.
        // If you change something here please do the same in the other class!
        
        Converter converter = null;
        while (converter == null && iterator.hasNext())
        {
            SelectItem item = iterator.next();
            if (item instanceof SelectItemGroup)
            {
                Iterator<SelectItem> groupIterator = Arrays.asList(((SelectItemGroup) item).getSelectItems()).iterator();
                converter = getSelectItemsValueConverter(groupIterator, facesContext);
            }
            else
            {
                Class<?> selectItemsType = item.getValue().getClass();

                // optimization: no conversion for String values
                if (String.class.equals(selectItemsType))
                {
                    return null;
                }
                
                try
                {
                    converter = facesContext.getApplication().createConverter(selectItemsType);
                }
                catch (FacesException e)
                {
                    // nothing - try again
                }
            }
        }
        return converter;
    }

    /**
     * This method is different in the two versions of _SharedRendererUtils.
     */
    private static void log(FacesContext context, String msg, Exception e)
    {
        context.getExternalContext().log(msg, e);
    }
}
