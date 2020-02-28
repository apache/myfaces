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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.render.ResponseStateManager;
import static junit.framework.TestCase.assertEquals;
import org.apache.myfaces.test.FacesTestCase;
import org.junit.Test;
import org.mockito.Mockito;

public class HtmlResponseStateManagerTest extends FacesTestCase
{
    @Test
    public void testIsPostback()
    {
        Mockito.when(_facesContext.getExternalContext()).thenReturn(_externalContext);
        Mockito.when(_externalContext.getApplicationMap()).thenReturn(new ConcurrentHashMap<>());
        
        HtmlResponseStateManager hrsm = Mockito.spy(HtmlResponseStateManager.class);

        Map<String, String> map = new HashMap<>();
        map.put(ResponseStateManager.VIEW_STATE_PARAM, "seomthing");
        Mockito.when(_externalContext.getRequestParameterMap()).thenReturn(map);

        assertEquals(true, hrsm.isPostback(_facesContext));
    }
    
    @Test
    public void testIsNoPostback()
    {
        Mockito.when(_facesContext.getExternalContext()).thenReturn(_externalContext);
        Mockito.when(_externalContext.getApplicationMap()).thenReturn(new ConcurrentHashMap<>());
        Mockito.when(_externalContext.getRequestParameterMap()).thenReturn(new HashMap<>());
        
        HtmlResponseStateManager hrsm = Mockito.spy(HtmlResponseStateManager.class);

        assertEquals(false, hrsm.isPostback(_facesContext));
    }
}
