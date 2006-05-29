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

import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;

import javax.faces.context.ExternalContext;
import java.util.*;

/**
 * Holds all configuration information (from the faces-config xml files) that
 * is needed later during runtime.
 * The config information in this class is only available to the MyFaces core
 * implementation classes (i.e. the myfaces source tree). See MyfacesConfig
 * for config parameters that can be used for shared or component classes.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RuntimeConfig
{
    private static final String APPLICATION_MAP_PARAM_NAME = RuntimeConfig.class.getName();

    private Collection _navigationRules = new ArrayList();
    private Map _managedBeans = new HashMap();
    private boolean _navigationRulesChanged=false;


    public static RuntimeConfig getCurrentInstance(ExternalContext externalContext)
    {
        RuntimeConfig runtimeConfig
                = (RuntimeConfig)externalContext.getApplicationMap().get(APPLICATION_MAP_PARAM_NAME);
        if (runtimeConfig == null)
        {
            runtimeConfig = new RuntimeConfig();
            externalContext.getApplicationMap().put(APPLICATION_MAP_PARAM_NAME, runtimeConfig);
        }
        return runtimeConfig;
    }

    /**
     * Return the navigation rules that can be used by the NavigationHandler implementation.
     * @return a Collection of {@link org.apache.myfaces.config.element.NavigationRule NavigationRule}s
     */
    public Collection getNavigationRules()
    {
        return Collections.unmodifiableCollection(_navigationRules);
    }

    public void addNavigationRule(NavigationRule navigationRule)
    {
        _navigationRules.add(navigationRule);

        _navigationRulesChanged = true;
    }

    public boolean isNavigationRulesChanged()
    {
        return _navigationRulesChanged;
    }

    public void setNavigationRulesChanged(boolean navigationRulesChanged)
    {
        _navigationRulesChanged = navigationRulesChanged;
    }

    /**
     * Return the managed bean info that can be used by the VariableResolver implementation.
     * @return a {@link org.apache.myfaces.config.element.ManagedBean ManagedBean}
     */
    public ManagedBean getManagedBean(String name)
    {
        return (ManagedBean)_managedBeans.get(name);
    }
    
    public Map<String, ManagedBean> getManagedBeans() {
        return Collections.unmodifiableMap(_managedBeans);
    }

    public void addManagedBean(String name, ManagedBean managedBean)
    {
        _managedBeans.put(name, managedBean);
    }
}
