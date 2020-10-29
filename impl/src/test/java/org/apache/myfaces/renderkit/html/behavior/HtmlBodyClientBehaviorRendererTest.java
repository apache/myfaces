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
package org.apache.myfaces.renderkit.html.behavior;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlBody;

import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.junit.Assert;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlBodyClientBehaviorRendererTest extends AbstractClientBehaviorTestCase
{
    private HtmlRenderedClientEventAttr[] attrs = null;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        attrs = (HtmlRenderedClientEventAttr[]) 
            org.apache.myfaces.util.lang.ArrayUtils.concat( 
                    HtmlClientEventAttributesUtil.generateClientBehaviorEventAttrs(),
                new HtmlRenderedClientEventAttr[]{
                    new HtmlRenderedClientEventAttr(HTML.ONLOAD_ATTR, ClientBehaviorEvents.LOAD),
                    new HtmlRenderedClientEventAttr(HTML.ONUNLOAD_ATTR, ClientBehaviorEvents.UNLOAD)
                });
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        attrs = null;
    }

    @Override
    protected UIComponent createComponentToTest()
    {
        return new HtmlBody();
    }

    @Override
    protected HtmlRenderedClientEventAttr[] getClientBehaviorHtmlRenderedAttributes()
    {
        return attrs;
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     * <body> does not have "name", so we just need to check "id"
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        HtmlRenderedClientEventAttr[] attrs = getClientBehaviorHtmlRenderedAttributes();
        
        for (int i = 0; i < attrs.length; i++)
        {
            UIComponent component = createComponentToTest();
            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;
            clientBehaviorHolder.addClientBehavior(attrs[i].getClientEvent(), new AjaxBehavior());
            try 
            {
                component.encodeAll(facesContext);
                String output = outputWriter.toString();
                Assert.assertTrue(output.matches(".+ id=\".+\".+"));
                outputWriter.reset();
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        }
    }

}
