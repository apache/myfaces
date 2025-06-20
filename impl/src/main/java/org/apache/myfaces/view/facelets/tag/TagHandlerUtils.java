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
package org.apache.myfaces.view.facelets.tag;

import java.util.ArrayList;

import jakarta.faces.view.facelets.FaceletHandler;

/**
 * This class was created to gather some code from latest Facelets not existing in latest Faces 2.0 spec.
 * Also, since it was created on the fly while converting, it's highly possible that methods in this class
 * should be moved in a more logical location and/or removed.
 *
 * @author Simon Lessard (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 2.0
 */
public final class TagHandlerUtils
{

    /**
     * Find the first occurrence of a tag handler that is instanceof T
     *
     * @since 2.0.1
     * @param <T>
     * @param nextHandler
     * @param type
     * @return
     */
    public static <T> T findFirstNextByType(FaceletHandler nextHandler, Class<T> type)
    {
        if (type.isAssignableFrom(nextHandler.getClass()))
        {
            return (T) nextHandler;
        }
        else if (nextHandler instanceof jakarta.faces.view.facelets.CompositeFaceletHandler faceletHandler)
        {
            for (FaceletHandler handler :
                    faceletHandler.getHandlers())
            {
                if (type.isAssignableFrom(handler.getClass()))
                {
                    return (T) handler;
                }
            }
        }
        return null;
    }

    /**
     * From TagHandler:
     * protected final &lt;T&gt; Iterator&lt;T&gt; findNextByType(Class&lt;T&gt; type)
     *
     * @param nextHandler
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> findNextByType(FaceletHandler nextHandler, Class<T> type)
    {
        ArrayList<T> found = new ArrayList<>();
        if (type.isAssignableFrom(nextHandler.getClass()))
        {
            found.add((T) nextHandler);
        }
        else if (nextHandler instanceof jakarta.faces.view.facelets.CompositeFaceletHandler faceletHandler)
        {
            for (FaceletHandler handler :
                    faceletHandler.getHandlers())
            {
                if (type.isAssignableFrom(handler.getClass()))
                {
                    found.add((T) handler);
                }
            }
        }

        return found;
    }

    private TagHandlerUtils()
    {

    }

    public static ArrayList<FaceletHandler> findNextByType(FaceletHandler nextHandler, Class<?> ... type1)
    {
        ArrayList<FaceletHandler> found = new ArrayList<>();
        boolean isAssignable = false;
        for (int i = 0; i < type1.length && !isAssignable; i++)
        {
            isAssignable = type1[i].isAssignableFrom(nextHandler.getClass());
        }
        if (isAssignable)
        {
            found.add(nextHandler);
        }
        else if (nextHandler instanceof jakarta.faces.view.facelets.CompositeFaceletHandler faceletHandler)
        {
            for (FaceletHandler handler :
                    faceletHandler.getHandlers())
            {
                isAssignable = false;
                for (int i = 0; i < type1.length && !isAssignable; i++)
                {
                    isAssignable = type1[i].isAssignableFrom(handler.getClass());
                }
                if (isAssignable)
                {
                    found.add(handler);
                }
            }
        }

        return found;
    }

    public static ArrayList<FaceletHandler> findNextByType(FaceletHandler nextHandler, Class<?> type1, Class<?> type2)
    {
        ArrayList<FaceletHandler> found = new ArrayList<>();
        if (type1.isAssignableFrom(nextHandler.getClass()) || type2.isAssignableFrom(nextHandler.getClass()))
        {
            found.add(nextHandler);
        }
        else if (nextHandler instanceof jakarta.faces.view.facelets.CompositeFaceletHandler faceletHandler)
        {
            for (FaceletHandler handler :
                    faceletHandler.getHandlers())
            {
                if (type1.isAssignableFrom(handler.getClass()) || type2.isAssignableFrom(handler.getClass()))
                {
                    found.add(handler);
                }
            }
        }
        
        return found;
    }
}
