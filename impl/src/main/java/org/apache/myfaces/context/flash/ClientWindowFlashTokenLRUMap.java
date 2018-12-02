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

package org.apache.myfaces.context.flash;

import java.io.Serializable;
import java.util.Map;
import javax.faces.FacesWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.apache.myfaces.util.LRULinkedHashMap;

/**
 *
 */
class ClientWindowFlashTokenLRUMap extends LRULinkedHashMap<String, String> implements Serializable
{
    public ClientWindowFlashTokenLRUMap(int capacity)
    {
        super(capacity);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, String> eldest)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        if (flash != null)
        {
            ReleasableFlash rf = null;
            while (flash != null)
            {
                if (flash instanceof ReleasableFlash)
                {
                    rf = (ReleasableFlash) flash;
                    break;
                }
                if (flash instanceof FacesWrapper)
                {
                    flash = ((FacesWrapper<? extends Flash>) flash).getWrapped();
                }
                else
                {
                    flash = null;
                }
            }
            if (rf != null)
            {
                rf.clearFlashMap(facesContext, (String) eldest.getKey(), (String) eldest.getValue());
            }
        }

        return super.removeEldestEntry(eldest);
    }
}
