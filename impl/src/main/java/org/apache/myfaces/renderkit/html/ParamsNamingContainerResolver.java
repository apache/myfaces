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
 * A centralized wrapper class, which deals with naming container resolution
 * on params level.
 * The problem is, the naming container Ajax requests introduce per spec
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
    public static final String CACHE_ATTR_NAMING_PREFIX = "myfaces.cache.namingcontainerprefix";
    public static final String EMPTY = "";
    public static final String CACHE_ATTR_POSTBACK = "myfaces.cache.postback";
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
        //cache the isPostback
        if(context.getAttributes().containsKey(CACHE_ATTR_POSTBACK))
        {
            return (boolean) context.getAttributes().get(CACHE_ATTR_POSTBACK);
        }
        boolean ret = resolvePostbackFromRequest(context);
        context.getAttributes().put(CACHE_ATTR_POSTBACK, ret);
        return ret;
    }

    public static String resolveNamingContainerPrefix(FacesContext facesContext)
    {
        UIViewRoot viewRoot = facesContext.getViewRoot();

        // not yet present, we are in a postback phase, without a ViewRoot yet present
        // the state that we have to determine a naming container before view root buildup
        // can only happen during postback and before a ViewRoot buildup, not during a page get request.
        // We always will have a ViewState being sent with the request in this case.
        // The cases, where we trigger this from outside, are always before ViewRoot buildup
        // we omit this code after we have a ViewRoot, because theoretically the naming container can change.
        // Practically it won´t. But during postback before the RestoreViewRoot we always
        // work on the postback naming container name.
        if(viewRoot == null)
        {
            if(facesContext.getAttributes().containsKey(CACHE_ATTR_NAMING_PREFIX))
            {
                return (String) facesContext.getAttributes().get(CACHE_ATTR_NAMING_PREFIX);
            }

            Map<String, String> reqParamMap = facesContext.getExternalContext().getRequestParameterMap();
            //no prefix, we have a blank ViewState in the request!
            String prefix = EMPTY;
            if(!reqParamMap.containsKey(ResponseStateManager.VIEW_STATE_PARAM))
            {
                // no viewstate param we have to determine it by other means
                prefix = resolvePrefixFromRequest(facesContext, reqParamMap);
            } // else direct viewstate param means empty prefix, which is the default
            facesContext.getAttributes().put(CACHE_ATTR_NAMING_PREFIX, prefix);
            return prefix;
        }
        // The naming container name can theoretically
        // shift after ViewRoot buildup
        // in the ajax navigation case, so once we have a ViewRoot
        // we can omit the cache and go for the normal viewRoot resolution
        if(viewRoot instanceof NamingContainer)
        {
            return viewRoot.getContainerClientId(facesContext) +
                    UINamingContainer.getSeparatorChar(facesContext);
        }
        else
        {
            return EMPTY;
        }
    }

    private static String resolvePrefixFromRequest(FacesContext facesContext, Map<String, String> reqParamMap)
    {
        String firstViewStateKey = reqParamMap.keySet().stream()
                .filter(item -> item.contains(ResponseStateManager.VIEW_STATE_PARAM))
                .findFirst().orElse(EMPTY);
        if(firstViewStateKey.length() > 0)
        {
            char sep = facesContext.getNamingContainerSeparatorChar();
            firstViewStateKey = firstViewStateKey.split(String.valueOf(sep))[0] + sep;
        }
        return firstViewStateKey;
    }

    private static boolean resolvePostbackFromRequest(FacesContext context)
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
}
