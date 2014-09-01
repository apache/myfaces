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
package org.apache.myfaces.application.viewstate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

/**
 *
 */
class SerializedViewCollection implements Serializable
{
    private static final Logger log = Logger.getLogger(SerializedViewCollection.class.getName());

    private static final Object[] EMPTY_STATES = new Object[]{null, null};

    private static final long serialVersionUID = -3734849062185115847L;
    private final List<SerializedViewKey> _keys = 
        new ArrayList<SerializedViewKey>(
            ServerSideStateCacheImpl.DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
    private final Map<SerializedViewKey, Object> _serializedViews = 
        new HashMap<SerializedViewKey, Object>();

    private final Map<SerializedViewKey, SerializedViewKey> _precedence =
        new HashMap<SerializedViewKey, SerializedViewKey>();

    // old views will be hold as soft references which will be removed by
    // the garbage collector if free memory is low
    private transient Map<Object, Object> _oldSerializedViews = null;

    public synchronized void add(FacesContext context, Object state, 
        SerializedViewKey key, SerializedViewKey previousRestoredKey)
    {
        if (state == null)
        {
            state = EMPTY_STATES;
        }
        else if (state instanceof Object[] &&
            ((Object[])state).length == 2 &&
            ((Object[])state)[0] == null &&
            ((Object[])state)[1] == null)
        {
            // The generated state can be considered zero, set it as null
            // into the map.
            state = null;
        }

        Integer maxCount = getNumberOfSequentialViewsInSession(context);
        if (maxCount != null)
        {
            if (previousRestoredKey != null)
            {
                if (!_serializedViews.isEmpty())
                {
                    _precedence.put((SerializedViewKey) key, previousRestoredKey);
                }
                else
                {
                    // Note when the session is invalidated, _serializedViews map is empty,
                    // but we could have a not null previousRestoredKey (the last one before
                    // invalidate the session), so we need to check that condition before
                    // set the precence. In that way, we ensure the precedence map will always
                    // have valid keys.
                    previousRestoredKey = null;
                }
            }
        }
        _serializedViews.put(key, state);

        while (_keys.remove(key))
        {
            // do nothing
        }
        _keys.add(key);

        if (previousRestoredKey != null && maxCount != null && maxCount > 0)
        {
            int count = 0;
            SerializedViewKey previousKey = (SerializedViewKey) key;
            do
            {
                previousKey = _precedence.get(previousKey);
                count++;
            }
            while (previousKey != null && count < maxCount);

            if (previousKey != null)
            {
                SerializedViewKey keyToRemove = (SerializedViewKey) previousKey;
                // In theory it should be only one key but just to be sure
                // do it in a loop, but in this case if cache old views is on,
                // put on that map.
                do
                {
                    while (_keys.remove(keyToRemove))
                    {
                        // do nothing
                    }

                    if (_serializedViews.containsKey(keyToRemove) &&
                        !ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF.
                                equals( getCacheOldViewsInSessionMode(context)) )
                    {
                        Object removedValue = _serializedViews.remove(keyToRemove);
                        if (removedValue == null)
                        {
                            removedValue = EMPTY_STATES;
                        }
                        getOldSerializedViewsMap().put(keyToRemove, removedValue);
                    }
                    else
                    {
                        _serializedViews.remove(keyToRemove);
                    }

                    keyToRemove = _precedence.remove(keyToRemove);
                }
                while (keyToRemove != null);
            }
        }
        int views = getNumberOfViewsInSession(context);
        while (_keys.size() > views)
        {
            key = _keys.remove(0);
            if (maxCount != null && maxCount > 0)
            {
                SerializedViewKey keyToRemove = (SerializedViewKey) key;
                // Note in this case the key to delete is the oldest one,
                // so it could be at least one precedence, but to be safe
                // do it with a loop.
                do
                {
                    keyToRemove = _precedence.remove(keyToRemove);
                }
                while (keyToRemove != null);
            }
            if (_serializedViews.containsKey(key) &&
                !ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF.
                        equals( getCacheOldViewsInSessionMode( context )))
            {
                Object removedValue = _serializedViews.remove(key);
                if (removedValue == null)
                {
                    removedValue = EMPTY_STATES;
                }
                getOldSerializedViewsMap().put(key, removedValue);
            }
            else
            {
                _serializedViews.remove(key);
            }
        }
    }

    protected Integer getNumberOfSequentialViewsInSession(FacesContext context)
    {
        return WebConfigParamUtils.getIntegerInitParameter( context.getExternalContext(),
                ServerSideStateCacheImpl.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM);
    }

    /**
     * Reads the amount (default = 20) of views to be stored in session.
     * @see ServerSideStateCacheImpl#NUMBER_OF_VIEWS_IN_SESSION_PARAM
     * @param context FacesContext for the current request, we are processing
     * @return Number vf views stored in the session
     */
    protected int getNumberOfViewsInSession(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM);
        int views = ServerSideStateCacheImpl.DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
        if (value != null)
        {
            try
            {
                views = Integer.parseInt(value);
                if (views <= 0)
                {
                    log.severe("Configured value for " + ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM
                              + " is not valid, must be an value > 0, using default value ("
                              + ServerSideStateCacheImpl.DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
                    views = ServerSideStateCacheImpl.DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
                }
            }
            catch (Throwable e)
            {
                log.log( Level.SEVERE, "Error determining the value for "
                       + ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM
                       + ", expected an integer value > 0, using default value ("
                       + ServerSideStateCacheImpl.DEFAULT_NUMBER_OF_VIEWS_IN_SESSION + "): " + e.getMessage(), e);
            }
        }
        return views;
    }

    /**
     * @return old serialized views map
     */
    @SuppressWarnings("unchecked")
    protected Map<Object, Object> getOldSerializedViewsMap()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        if (_oldSerializedViews == null && context != null)
        {
            String cacheMode = getCacheOldViewsInSessionMode(context);
            if ( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK.equals(cacheMode))
            {
                _oldSerializedViews = new ReferenceMap( AbstractReferenceMap.WEAK, AbstractReferenceMap.WEAK, true);
            }
            else if ( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK.equals(cacheMode))
            {
                _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.WEAK, true);
            }
            else if ( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT.equals(cacheMode))
            {
                _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true);
            }
            else if ( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT.equals(cacheMode))
            {
                _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.SOFT);
            }
        }

        return _oldSerializedViews;
    }

    /**
     * Reads the value of the <code>org.apache.myfaces.CACHE_OLD_VIEWS_IN_SESSION_MODE</code> context parameter.
     *
     * @since 1.2.5
     * @param context
     * @return constant indicating caching mode
     * @see ServerSideStateCacheImpl#CACHE_OLD_VIEWS_IN_SESSION_MODE
     */
    protected String getCacheOldViewsInSessionMode(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE);
        if (value == null)
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF;
        }
        else if (value.equalsIgnoreCase( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT))
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT;
        }
        else if (value.equalsIgnoreCase( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK))
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK;
        }
        else if (value.equalsIgnoreCase( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK))
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK;
        }
        else if (value.equalsIgnoreCase( ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT))
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT;
        }
        else
        {
            return ServerSideStateCacheImpl.CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF;
        }
    }

    public Object get(SerializedViewKey key)
    {
        Object value = _serializedViews.get(key);
        if (value == null)
        {
            if (_serializedViews.containsKey(key))
            {
                return EMPTY_STATES;
            }
            Map<Object,Object> oldSerializedViewMap = getOldSerializedViewsMap();
            if (oldSerializedViewMap != null)
            {
                value = oldSerializedViewMap.get(key);
                if (value == null && oldSerializedViewMap.containsKey(key) )
                {
                    return EMPTY_STATES;
                }
            }
        }
        else if (value instanceof Object[] &&
            ((Object[])value).length == 2 &&
            ((Object[])value)[0] == null &&
            ((Object[])value)[1] == null)
        {
            // Remember inside the state map null is stored as an empty array.
            return null;
        }
        return value;
    }
}
