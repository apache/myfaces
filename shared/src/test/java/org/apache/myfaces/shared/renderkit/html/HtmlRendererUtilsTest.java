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
package org.apache.myfaces.shared.renderkit.html;

import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorBase;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import junit.framework.Assert;
import org.apache.myfaces.shared.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

import jakarta.faces.component.UIComponent;

import jakarta.faces.component.html.HtmlInputText;

import java.util.*;

public class HtmlRendererUtilsTest extends AbstractJsfTestCase
{

    public HtmlRendererUtilsTest(String name)
    {
        super(name);
    }

    public void testBuildBehaviorChain()
    {
        Map<String, List<ClientBehavior>> behaviors = new HashMap<String, List<ClientBehavior>>();

        //Map<String, String> params = new HashMap<String, String>();
        Collection<ClientBehaviorContext.Parameter> params = new ArrayList<ClientBehaviorContext.Parameter>();
        
        UIComponent component = new HtmlInputText();
        Assert.assertEquals("", HtmlRendererUtils.buildBehaviorChain(facesContext, component, 
                component.getClientId(facesContext),
                ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, null,
                null));

        Assert.assertEquals("return jsf.util.chain(document.getElementById('j_id__v_0'), event,'huhn', 'suppe');",
                HtmlRendererUtils.buildBehaviorChain(facesContext,
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

        Assert.assertEquals("jsf.util.chain(document.getElementById('j_id__v_0'), event,'huhn', 'script()', 'suppe'); return false;",
                HtmlRendererUtils.buildBehaviorChain(facesContext,
                        component, component.getClientId(facesContext),
                        ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

    }
    
    public void testBuildBehaviorChain2()
    {
        Map<String, List<ClientBehavior>> behaviors = new HashMap<String, List<ClientBehavior>>();

        //Map<String, String> params = new HashMap<String, String>();
        Collection<ClientBehaviorContext.Parameter> params = new ArrayList<ClientBehaviorContext.Parameter>();
        
        UIComponent component = new HtmlInputText();
        Assert.assertEquals("", HtmlRendererUtils.buildBehaviorChain(facesContext, component, 
                ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, null,
                null));

        Assert.assertEquals("return jsf.util.chain(this, event,'huhn', 'suppe');",
                HtmlRendererUtils.buildBehaviorChain(facesContext,
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

        Assert.assertEquals("jsf.util.chain(this, event,'huhn', 'script()', 'suppe'); return false;",
                HtmlRendererUtils.buildBehaviorChain(facesContext,
                        component, 
                        ClientBehaviorEvents.CLICK, params, ClientBehaviorEvents.ACTION, params, behaviors, "huhn",
                        "suppe"));

    }    
    
    public void testEscapeJavaScriptForChain()
    {
        
        Assert.assertEquals("var foo = &quot; \\\\&quot; test &quot;; alert(foo);", HtmlRendererUtils.escapeJavaScriptForChain("var foo = &quot; \\&quot; test &quot;; alert(foo);"));
        
        Assert.assertEquals("var foo = \\'bar \\'", HtmlRendererUtils.escapeJavaScriptForChain("var foo = 'bar '"));
        
        Assert.assertEquals("var foo = \\'bar \\\\\\' \\'", HtmlRendererUtils.escapeJavaScriptForChain("var foo = 'bar \\' '"));
    }
}