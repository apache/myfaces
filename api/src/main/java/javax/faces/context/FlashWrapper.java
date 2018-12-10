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
package javax.faces.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.faces.FacesWrapper;

/**
 * @since 2.2
 */
public abstract class FlashWrapper extends Flash implements FacesWrapper<Flash>
{
    private Flash delegate;

    @Deprecated
    public FlashWrapper()
    {
    }

    public FlashWrapper(Flash delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isKeepMessages()
    {
        return getWrapped().isKeepMessages();
    }

    @Override
    public boolean isRedirect()
    {
        return getWrapped().isRedirect();
    }

    @Override
    public void keep(String key)
    {
        getWrapped().keep(key);
    }

    @Override
    public void putNow(String key, Object value)
    {
        getWrapped().putNow(key, value);
    }

    @Override
    public void setKeepMessages(boolean newValue)
    {
        getWrapped().setKeepMessages(newValue);
    }

    @Override
    public void setRedirect(boolean newValue)
    {
        getWrapped().setRedirect(newValue);
    }

    @Override
    public void doPrePhaseActions(FacesContext context)
    {
        getWrapped().doPrePhaseActions(context);
    }

    @Override
    public void doPostPhaseActions(FacesContext context)
    {
        getWrapped().doPostPhaseActions(context);
    }

    @Override
    public int size()
    {
        return getWrapped().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getWrapped().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return getWrapped().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return getWrapped().containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return getWrapped().get(key);
    }

    @Override
    public Object put(String key, Object value)
    {
        return getWrapped().put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return getWrapped().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        getWrapped().putAll(m);
    }

    @Override
    public void clear()
    {
        getWrapped().clear();
    }

    @Override
    public Set<String> keySet()
    {
        return getWrapped().keySet();
    }

    @Override
    public Collection<Object> values()
    {
        return getWrapped().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return getWrapped().entrySet();
    }
    
    @Override
    public Flash getWrapped()
    {
        return delegate;
    }
}
