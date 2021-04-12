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
package org.apache.myfaces.view.facelets.compiler;

import java.util.HashMap;
import java.util.Map;
import jakarta.faces.component.UIComponent;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.faces.html.HtmlLibrary;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class DynamicComponentFaceletTestCase extends FaceletTestCase
{
    
    @Test
    public void testCompileComponentHandler() throws Exception
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("value", "hello");
        UIComponent component = vdl.createComponent(facesContext, 
            HtmlLibrary.NAMESPACE, "outputText", attributes);
    }

}
