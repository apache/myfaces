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
import java.util.Collection;
import java.util.List;

import javax.faces.view.facelets.FaceletHandler;

/**
 * This class was created to gather some code from latest Facelets not existing in latest JSF 2.0 spec.
 * Also, since it was created on the fly while converting, it's highly possible that methods in this class
 * should be moved in a more logical location and/or removed.
 * 
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-21 12:31:27 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public final class TagHandlerUtils
{

    /**
     * From TagHandler: 
     * protected final <T> Iterator<T> findNextByType(Class<T> type)
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> findNextByType(FaceletHandler nextHandler, Class<T> type)
    {
        List<T> found = new ArrayList<T>();
        if (type.isAssignableFrom(nextHandler.getClass()))
        {
            found.add((T)nextHandler);
        }
        else if (nextHandler instanceof CompositeFaceletHandler)
        {
            for (FaceletHandler handler : ((CompositeFaceletHandler)nextHandler).getHandlers())
            {
                if (type.isAssignableFrom(handler.getClass()))
                {
                    found.add((T)handler);
                }
            }
        }
        
        return found;
    }

    private TagHandlerUtils()
    {
        
    }
}
