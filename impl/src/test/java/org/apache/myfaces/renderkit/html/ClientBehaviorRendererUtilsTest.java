/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.renderkit.html;

import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorBase;
import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;

import java.util.*;

import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientBehaviorRendererUtilsTest extends AbstractFacesTestCase
{

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        servletContext.addInitParameter(MyfacesConfig.RENDER_CLIENTBEHAVIOR_SCRIPTS_AS_STRING, "true");
    }

    @Test
    public void testBuildBehaviorChain()
    {
        Map<String, List<ClientBehavior>> behaviors = new HashMap<String, List<ClientBehavior>>();

        //Map<String, String> params = new HashMap<String, String>();
        Collection<ClientBehaviorContext.Parameter> params = new ArrayList<ClientBehaviorContext.Parameter>();
        
        UIComponent component = new HtmlInputText();
        Assertions.assertEquals("", ClientBehaviorRendererUtils.buildBehaviorChain(facesContext, component, 
                component.getClientId(facesContext),
                ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, null,
                null));

        Assertions.assertEquals("return faces.util.chain(document.getElementById('j_id__v_0'), event,'huhn', 'suppe');",
                ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                        component, component.getClientId(facesContext), ClientBehaviorEvents.CLICK, 
                        params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

        ClientBehavior submittingBehavior = new ClientBehaviorBase()
        {
            @Override
            public String getScript(ClientBehaviorContext behaviorContext)
            {
                return "script()";
            }

            @Override
            public Set<ClientBehaviorHint> getHints()
            {
                return EnumSet.allOf(ClientBehaviorHint.class);
            }
        };

        behaviors.put(ClientBehaviorEvents.CLICK, Arrays.asList(submittingBehavior));

        Assertions.assertEquals("faces.util.chain(document.getElementById('j_id__v_0'), event,'huhn', 'script()', 'suppe'); return false;",
                ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                        component, component.getClientId(facesContext),
                        ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

    }
    
    @Test
    public void testBuildBehaviorChain2()
    {
        Map<String, List<ClientBehavior>> behaviors = new HashMap<String, List<ClientBehavior>>();

        //Map<String, String> params = new HashMap<String, String>();
        Collection<ClientBehaviorContext.Parameter> params = new ArrayList<ClientBehaviorContext.Parameter>();
        
        UIComponent component = new HtmlInputText();
        Assertions.assertEquals("", ClientBehaviorRendererUtils.buildBehaviorChain(facesContext, component, 
                ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, null,
                null));

        Assertions.assertEquals("return faces.util.chain(this, event,'huhn', 'suppe');",
                ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                        component, ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

        ClientBehavior submittingBehavior = new ClientBehaviorBase()
        {
            @Override
            public String getScript(ClientBehaviorContext behaviorContext)
            {
                return "script()";
            }

            @Override
            public Set<ClientBehaviorHint> getHints()
            {
                return EnumSet.allOf(ClientBehaviorHint.class);
            }
        };

        behaviors.put(ClientBehaviorEvents.CLICK, Arrays.asList(submittingBehavior));

        Assertions.assertEquals("faces.util.chain(this, event,'huhn', 'script()', 'suppe'); return false;",
                ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                        component, 
                        ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

    }    
    
    @Test
    public void testEscapeJavaScriptForChain()
    {
        MockFacesContext facesContext = new MockFacesContext();
        
        Assertions.assertEquals("var foo = &quot; \\\\&quot; test &quot;; alert(foo);",
                ClientBehaviorRendererUtils.escapeJavaScriptForChain(facesContext, "var foo = &quot; \\&quot; test &quot;; alert(foo);"));
        
        Assertions.assertEquals("var foo = \\'bar \\'",
                ClientBehaviorRendererUtils.escapeJavaScriptForChain(facesContext, "var foo = 'bar '"));
        
        Assertions.assertEquals("var foo = \\'bar \\\\\\' \\'",
                ClientBehaviorRendererUtils.escapeJavaScriptForChain(facesContext, "var foo = 'bar \\' '"));
    }
}