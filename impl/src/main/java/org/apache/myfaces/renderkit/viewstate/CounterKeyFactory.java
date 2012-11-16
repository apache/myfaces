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
package org.apache.myfaces.renderkit.viewstate;

import org.apache.myfaces.shared.renderkit.RendererUtils;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * View Key factory which incrementally counts upwards
 * in each session.
 */
public class CounterKeyFactory extends KeyFactory<Integer, String>
{
    /**
     * Take the counter from session scope and increment
     *
     * @param facesContext
     * @return
     */
    @Override
    public Integer generateKey(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        Object sessionObj = externalContext.getSession(true);
        Integer sequence = null;
        synchronized(sessionObj) // synchronized to increase sequence if multiple requests
                                // are handled at the same time for the session
        {
            Map<String, Object> map = externalContext.getSessionMap();
            sequence = (Integer) map.get( RendererUtils.SEQUENCE_PARAM);
            if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
            {
                sequence = Integer.valueOf(1);
            }
            else
            {
                sequence = Integer.valueOf(sequence.intValue() + 1);
            }
            map.put(RendererUtils.SEQUENCE_PARAM, sequence);
        }
        return sequence;
    }

    public String encode(Integer sequence)
    {
        return Integer.toString(sequence, Character.MAX_RADIX);
    }

    public Integer decode(String serverStateId)
    {
         return Integer.valueOf((String) serverStateId, Character.MAX_RADIX);
    }
}
