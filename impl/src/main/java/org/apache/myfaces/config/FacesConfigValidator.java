/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationCase;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.shared_impl.util.ClassUtils;

public class FacesConfigValidator
{

    public static final String VALIDATE_CONTEXT_PARAM = "org.apache.myfaces.VALIDATE";
    
    private FacesConfigValidator(){
        // hidden 
    }

    public static List validate(ExternalContext ctx, String ctxPath){
        
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(ctx);
        
        Map managedBeansMap = runtimeConfig.getManagedBeans();
        
        Iterator managedBeans = managedBeansMap == null ? null : 
                                managedBeansMap.values() == null ? null :
                                    managedBeansMap.values().iterator();
        
        Iterator navRules = runtimeConfig.getNavigationRules() == null ? 
                            null : runtimeConfig.getNavigationRules().iterator();
        
        return validate(managedBeans, navRules, ctxPath);
        
    }
    
    public static List validate(Iterator managedBeans, Iterator navRules, String ctxPath){
        
        List list = new ArrayList();
        
        if(navRules != null)
            validateNavRules(navRules, list, ctxPath);
        
        if(managedBeans != null)
            validateManagedBeans(managedBeans, list);
        
        return list;
    }

    private static void validateNavRules(Iterator navRules, List list, String ctxPath){
        
        while(navRules.hasNext()){
            
            NavigationRule navRule = (NavigationRule) navRules.next();
            
            validateNavRule(navRule, list, ctxPath);
            
        }
        
    }
    
    private static void validateNavRule(NavigationRule navRule, List list, String ctxPath){
        
        String fromId = navRule.getFromViewId();
        String filePath = ctxPath + fromId;
        
        if(fromId != null && ! "*".equals(fromId) && ! new File(filePath).exists())
            list.add("File for navigation 'from id' does not exist " + filePath);
        
        Collection cases = navRule.getNavigationCases();
        
        Iterator iterator = cases.iterator();
        
        while(iterator.hasNext()){
            
            NavigationCase caze = (NavigationCase) iterator.next();
            
            String toViewPath = ctxPath + caze.getToViewId();
            
            if(! new File(toViewPath).exists())
                list.add("File for navigation 'to id' does not exist " + toViewPath);
            
        }
        
    }
    
    private static void validateManagedBeans(Iterator managedBeans, List list){
        
        while(managedBeans.hasNext()){
            
            ManagedBean managedBean = (ManagedBean) managedBeans.next();
            
            validateManagedBean(managedBean, list);
            
        }
        
    }

    private static void validateManagedBean(ManagedBean managedBean, List list){
        
        String className = managedBean.getManagedBeanClassName();
        
        try
        {
            Class clazz = ClassUtils.classForName(className);
        }
        catch (ClassNotFoundException e)
        { 
            
            String msg = "Could not locate class " 
                + className + " for managed bean '" + managedBean.getManagedBeanName() + "'";
            
            list.add(msg);
            
        }

    }

}