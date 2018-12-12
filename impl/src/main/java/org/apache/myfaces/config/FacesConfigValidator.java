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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.element.NavigationCase;
import org.apache.myfaces.config.element.NavigationRule;

public class FacesConfigValidator
{
    private FacesConfigValidator()
    {
        // hidden 
    }

    public static List<String> validate(ExternalContext ctx)
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(ctx);
        
        Collection<? extends NavigationRule> navRules = runtimeConfig.getNavigationRules();
        
        return validate(navRules, ctx);
    }
    
    public static List<String> validate(Collection<? extends NavigationRule> navRules, ExternalContext ctx)
    {
        List<String> list = new ArrayList<String>();

        if (navRules != null)
        {
            validateNavRules(navRules, list, ctx);
        }
        
        return list;
    }

    private static void validateNavRules(Collection<? extends NavigationRule> navRules, List<String> list,
                                         ExternalContext ctx)
    {
        for (NavigationRule navRule : navRules)
        {
            validateNavRule(navRule, list, ctx);
        }
    }
    
    private static void validateNavRule(NavigationRule navRule, List<String> list, ExternalContext ctx)
    {
        String fromId = navRule.getFromViewId();
        URL filePath;
        try
        {
            filePath = ctx.getResource(fromId);

            if(fromId != null && ! "*".equals(fromId) && filePath == null)
            {
                list.add("File for navigation 'from id' does not exist " + filePath);
            }            
        }
        catch (MalformedURLException e)
        {
            list.add("File for navigation 'from id' does not exist " + fromId);
        }
        
        for (NavigationCase caze : navRule.getNavigationCases())
        {
            try
            {
                URL toViewPath = ctx.getResource(caze.getToViewId());
                
                if(toViewPath == null)
                {
                    list.add("File for navigation 'to id' does not exist " + toViewPath);
                }
            }
            catch (MalformedURLException e)
            {
                list.add("File for navigation 'from id' does not exist " + caze.getToViewId());
            }
        }
    }

}
