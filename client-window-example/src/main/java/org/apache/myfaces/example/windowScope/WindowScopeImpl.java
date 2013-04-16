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
package org.apache.myfaces.example.windowScope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author lu4242
 */
public class WindowScopeImpl extends WindowScope
{

    private static final String WINDOW_PREFIX = "oam.Window";

    /**
     * Key for the cached render FlashMap instance on the request map.
     */
    static final String WINDOW_MAP = WINDOW_PREFIX + ".WINDOWMAP";

    /**
     * Key on application map to keep current instance
     */
    static final String WINDOW_INSTANCE = WINDOW_PREFIX + ".INSTANCE";

    /**
     * Session map prefix to flash maps
     */
    static final String WINDOW_SESSION_MAP_SUBKEY_PREFIX = WINDOW_PREFIX + ".SCOPE";

    /**
     * Token separator.
     */
    static final char SEPARATOR_CHAR = '.';

    public WindowScopeImpl(ExternalContext externalContext)
    {

    }

    public int size()
    {
        return getActiveWindowScopeMap().size();
    }

    public boolean isEmpty()
    {
        return getActiveWindowScopeMap().isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return getActiveWindowScopeMap().containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return getActiveWindowScopeMap().containsValue(value);
    }

    public Object get(Object key)
    {
        return getActiveWindowScopeMap().get(key);
    }

    public Object put(String key, Object value)
    {
        return getActiveWindowScopeMap().put(key, value);
    }

    public Object remove(Object key)
    {
        return getActiveWindowScopeMap().remove(key);
    }

    public void putAll(Map<? extends String, ? extends Object> m)
    {
        getActiveWindowScopeMap().putAll(m);
    }

    public void clear()
    {
        getActiveWindowScopeMap().clear();
    }

    public Set<String> keySet()
    {
        return getActiveWindowScopeMap().keySet();
    }

    public Collection<Object> values()
    {
        return getActiveWindowScopeMap().values();
    }

    public Set<Entry<String, Object>> entrySet()
    {
        return getActiveWindowScopeMap().entrySet();
    }

    private Map<String, Object> getActiveWindowScopeMap()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> requestMap = context.getAttributes();
        Map<String, Object> map = (Map<String, Object>) requestMap.get(WINDOW_MAP);
        if (map == null)
        {
            String token = (String) context.getExternalContext().getClientWindow().getId();
            String fullToken = WINDOW_SESSION_MAP_SUBKEY_PREFIX + SEPARATOR_CHAR + token;
            map =  _createSubKeyMap(context, fullToken);
            requestMap.put(WINDOW_MAP, map);
        }
        return map;
    }

    /**
     * Create a new subkey-wrapper of the session map with the given prefix.
     * This wrapper is used to implement the maps for the flash scope.
     * For more information see the SubKeyMap doc.
     */
    private Map<String, Object> _createSubKeyMap(FacesContext context, String prefix)
    {
        ExternalContext external = context.getExternalContext();
        Map<String, Object> sessionMap = external.getSessionMap();

        return new SubKeyMap<Object>(sessionMap, prefix);
    }

    public static WindowScope getCurrentInstance(ExternalContext context)
    {
        Map<String, Object> applicationMap = context.getApplicationMap();

        WindowScope flash = (WindowScope) applicationMap.get(WINDOW_INSTANCE);
        if (flash == null)
        {
            // synchronize the ApplicationMap to ensure that only
            // once instance of FlashImpl is created and stored in it.
            synchronized (applicationMap)
            {
                // check again, because first try was un-synchronized
                flash = (WindowScope) applicationMap.get(WINDOW_INSTANCE);
                if (flash == null)
                {
                    flash = new WindowScopeImpl(context);
                    applicationMap.put(WINDOW_INSTANCE, flash);
                }
            }
        }

        return flash;
    }
}
