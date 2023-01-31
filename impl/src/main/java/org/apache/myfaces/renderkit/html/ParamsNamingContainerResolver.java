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
package org.apache.myfaces.renderkit.html;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;


import java.util.Map;

/**
 * A centralized wrapper class which deals with naming container resolution
 * on params level.
 * The problem is, the naming container ajax requests introduce per spec
 * that the parameters need to be prefixed. This works for normal components
 * but not standardized request parameters, we have to prefix them for a params
 * lookup.
 *
 * The best bet is to collect this functionality on a central place (aka here)
 * to keep it maintainable.
 * Also, we cannot rely on UIViewRoot here, because this class is accessed
 * at places which do not have UIViewRoot enabled.
 */
public class ParamsNamingContainerResolver
{
    Map<String, String> delegate;
    FacesContext facesContext;

    public ParamsNamingContainerResolver(FacesContext facesContext)
    {
        this.facesContext = facesContext;
        this.delegate = facesContext.getExternalContext().getRequestParameterMap();
    }

    public String get(String key)
    {
        if(this.delegate.containsKey(key))
        {
            return this.delegate.get(key);
        }
        return this.delegate.get(resolveNamingContainerPrefix(facesContext) + key);
    }

    public String get(StringBuilder key)
    {
        return this.get(key.toString());
    }

    public boolean containsKey(String key)
    {
        return this.delegate.containsKey(key) ||
                this.delegate.containsKey(resolveNamingContainerPrefix(facesContext) + key);
    }

    public static boolean isPostBack(FacesContext context)
    {
        Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
        boolean ret = requestParameterMap.
                containsKey(ResponseStateManager.VIEW_STATE_PARAM) ||
                //we might have a prefixed VIEWSTATE
                requestParameterMap.keySet().stream()
                        .filter(key -> key.contains(ResponseStateManager.VIEW_STATE_PARAM))
                        .findFirst().isPresent();
        return ret;
    }

    public static String resolveNamingContainerPrefix(FacesContext facesContext)
    {
        UIViewRoot viewRoot = facesContext.getViewRoot();

        //not yet present, we are in a postback phase, without a viewroot yet present
        if(viewRoot == null)
        {
            Map<String, String> reqParamMap = facesContext.getExternalContext().getRequestParameterMap();

            //no prefix, we have a blank ViewState in the request!
            if(reqParamMap.containsKey(ResponseStateManager.VIEW_STATE_PARAM))
            {
                return "";
            }
            // TODO please optimize this code, we probably can store the data below on request level
            // it is static per request!

            //we have a prefixed viewstate
            String firstViewStateKey = reqParamMap.keySet().stream()
                    .filter(item -> item.contains(ResponseStateManager.VIEW_STATE_PARAM))
                    .findFirst().orElse("");
            if(firstViewStateKey.length() > 0)
            {
                char sep = facesContext.getNamingContainerSeparatorChar();
                return firstViewStateKey.split(String.valueOf(sep))[0] + sep;
            }
            return firstViewStateKey;
        }
        if(viewRoot instanceof NamingContainer)
        {
            return viewRoot.getContainerClientId(facesContext) +
                    UINamingContainer.getSeparatorChar(facesContext);
        }
        else
        {
            return "";
        }
    }
}
