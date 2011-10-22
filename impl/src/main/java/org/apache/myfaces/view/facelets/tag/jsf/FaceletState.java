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
package org.apache.myfaces.view.facelets.tag.jsf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

public class FaceletState implements StateHolder, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -7823771271935942737L;
    
    public Map<String, Object> stateMap;
    
    public Object getState(String key)
    {
        if(stateMap == null)
        {
            return null;
        }
        return stateMap.get(key);
    }
    
    public Object putState(String key, Object value)
    {
        if (stateMap == null)
        {
            stateMap = new HashMap<String, Object>();
        }
        return stateMap.put(key, value);
    }
    

    public Object saveState(FacesContext context)
    {
        if (stateMap != null)
        {
            return UIComponentBase.saveAttachedState(context, stateMap);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            stateMap = null;
        }
        else
        {
            stateMap = (Map<String,Object>) UIComponentBase.restoreAttachedState(context, state);
        }
    }

    public boolean isTransient()
    {
        return false;
    }

    public void setTransient(boolean newTransientValue)
    {
    }

}
