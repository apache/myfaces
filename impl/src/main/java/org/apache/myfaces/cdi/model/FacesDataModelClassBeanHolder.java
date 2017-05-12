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

package org.apache.myfaces.cdi.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

/**
 *
 */
@ApplicationScoped
public class FacesDataModelClassBeanHolder extends DataModelBuilder
{
    private volatile Map<Class<?>,Class<? extends DataModel>> 
            classInstanceToDataModelWrapperClassMap = new ConcurrentHashMap<Class<?>, Class<? extends DataModel>>();
    private Map<Class<?>,Class<? extends DataModel>> umclassInstanceToDataModelWrapperClassMap = null;
    
    /**
     * @return the classInstanceToDataModelWrapperClassMap
     */
    public Map<Class<?>,Class<? extends DataModel>> getClassInstanceToDataModelWrapperClassMap()
    {
        if (umclassInstanceToDataModelWrapperClassMap == null)
        {
            umclassInstanceToDataModelWrapperClassMap = 
                    Collections.unmodifiableMap(classInstanceToDataModelWrapperClassMap);
        }
        return umclassInstanceToDataModelWrapperClassMap;
    }
    
    public void addFacesDataModel(Class<?> forClass, Class<? extends DataModel> dataModelClass)
    {
        classInstanceToDataModelWrapperClassMap.put(forClass, dataModelClass);
    }

    @Override
    public DataModel createDataModel(FacesContext facesContext, Class<?> forClass, Object value)
    {
        Class<? extends DataModel> dataModelClass = getClassInstanceToDataModelWrapperClassMap().get(forClass);
        if (dataModelClass != null)
        {
            return createDataModel(forClass, value, dataModelClass);
        }
        else
        {
            // Iterate over map and try to find a valid match if any.
            Class<?> entryForClass = null;
            Class<? extends DataModel> valueForClass = null;
            for (Map.Entry<Class<?>,Class<? extends DataModel>> entry : 
                    getClassInstanceToDataModelWrapperClassMap().entrySet())
            {
                if (entry.getKey().isAssignableFrom(forClass))
                {
                    if (entryForClass != null)
                    {
                        // Both models work, but we need to prefer the one
                        // that is closest to in the hierarchy.
                        if (entryForClass.isAssignableFrom(entry.getKey()))
                        {
                            entryForClass = entry.getKey();
                            valueForClass = entry.getValue();
                        }
                    }
                    else
                    {
                        entryForClass = entry.getKey();
                        valueForClass = entry.getValue();
                    }
                }
            }
            if(entryForClass != null)
            {
                return createDataModel(forClass, value, valueForClass);
            }
        }
        return null;
    }
    
    private DataModel createDataModel(Class<?> forClass, Object value,
            Class<? extends DataModel> dataModelClass)
    {
        try
        {
            Constructor selectedConstructor = null;
            boolean equalsFound = false;
            for (Constructor constructor : dataModelClass.getConstructors())
            {
                if (constructor.getParameterCount() == 1)
                {
                    if (constructor.getParameterTypes()[0].equals(forClass))
                    {
                        selectedConstructor = constructor;
                        equalsFound = true;
                    }
                    else if (constructor.getParameterTypes()[0].isAssignableFrom(forClass))
                    {
                        if (!equalsFound)
                        {
                            selectedConstructor = constructor;
                        }
                    }
                }
            }
            
            Constructor constructor = null;
            if (selectedConstructor != null)
            {
                constructor = selectedConstructor;
                return (DataModel) constructor.newInstance(value);
            }
            else
            {
                constructor = dataModelClass.getConstructor();
                DataModel dm = (DataModel) constructor.newInstance();
                dm.setWrappedData(value);
                return dm;
            }
        } 
        catch (NoSuchMethodException ex)
        {
            throw new FacesException(
                    "Cannot find constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        }
        catch (SecurityException ex)
        {
            throw new FacesException(
                    "Cannot access constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        } 
        catch (InstantiationException ex)
        {
            throw new FacesException(
                    "Cannot access constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new FacesException(
                    "Cannot access constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        } 
        catch (IllegalArgumentException ex)
        {
            throw new FacesException(
                    "Cannot find constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        } 
        catch (InvocationTargetException ex)
        {
            throw new FacesException(
                    "Cannot find constructor of DataModel with "+forClass.getName()+" as parameter", ex);
        }
    }
}
