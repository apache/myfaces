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

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.shared.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.shared.util.FastWriter;
import org.apache.myfaces.test.config.ConfigParser;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class AbstractClientBehaviorTestCase extends FaceletTestCase
{
    protected ResponseWriter writer;
    protected FastWriter outputWriter; 
    protected ConfigParser parser;
    
    //protected abstract UIComponent getComponentToTest();
    
    protected abstract HtmlRenderedClientEventAttr[] getClientBehaviorHtmlRenderedAttributes();
    
    protected abstract UIComponent createComponentToTest();
    
    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();
        
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                "org.apache.myfaces.view.facelets.mock.MockViewDeclarationLanguageFactory");
        FactoryFinder.setFactory(FactoryFinder.FACELET_CACHE_FACTORY,
                "org.apache.myfaces.view.facelets.impl.FaceletCacheFactoryImpl");
        FactoryFinder.setFactory(FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY,
                "org.apache.myfaces.component.search.SearchExpressionContextFactoryImpl");
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                "org.apache.myfaces.application.ApplicationFactoryImpl");
    }

    @Override
    protected void setUpJSFObjects() throws Exception
    {
        super.setUpJSFObjects();
        outputWriter = new FastWriter();
        writer = new HtmlResponseWriterImpl(outputWriter, null, null);
        facesContext.setResponseWriter(writer);
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", true);
    }
    
    @Override
    protected void setUpApplication() throws Exception
    {
        super.setUpApplication();
    }

    @Override
    protected void setUpRenderKit() throws Exception
    {
        super.setUpRenderKit();
        parser = new ConfigParser();
        parser.parse(parser.getPlatformURLs());
        //parser.parse(this.getClass().getResource("/META-INF/faces-config.xml"));    
        request.setServletPath("/test");
    }

    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
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
                Assert.assertTrue(output.indexOf(" id=\""+component.getClientId(facesContext)+"\"") > -1);
                Assert.assertTrue(output.indexOf(" name=\""+component.getClientId(facesContext)+"\"") > -1);
                outputWriter.reset();
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        }
    }
    
    @Test
    public void testClientBehaviorRendered() 
    {
        HtmlRenderedClientEventAttr[] attrs = getClientBehaviorHtmlRenderedAttributes();
        
        for (int i = 0; i < attrs.length; i++)
        {
            UIComponent component = createComponentToTest();
            
            if (!component.isInView())
            {
                UIViewRoot root = facesContext.getViewRoot();
                root.getChildren().add(component);
                facesContext.setViewRoot(root);
                root.getClientId(facesContext);
            }

            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;
            clientBehaviorHolder.addClientBehavior(attrs[i].getClientEvent(), new AjaxBehavior());
            try 
            {
                component.encodeAll(facesContext);
                String output = outputWriter.toString();
                //jsf.ajax.request('j_id0',event,{'javax.faces.behavior.event':'click'})
                //Only check if the property starts with jsf.ajax.request( is enough 
                //Assert.assertTrue("output does not match expected output jsf.ajax.request(.... for property "+attrs[i].getName(),
                //        output.matches(".+ "+attrs[i].getName()+"=\"jsf\\.ajax\\.request\\(.+"));
                int index = checkClientBehaviorRenderedOnClientEventProperty(output, 0, attrs[i]);
                outputWriter.reset();
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        }
    }
    
    public int checkClientBehaviorRenderedOnClientEventProperty(String output, int start, HtmlRenderedClientEventAttr attr)
    {
        String propStart = " "+attr.getName()+"=\"";
        int propIndex = output.indexOf(propStart, start);
        if (propIndex > -1)
        {
            int c = '"';
            int startPropIndex = propIndex + propStart.length(); 
            int endPropIndex = output.indexOf('"' , startPropIndex );
            String propertyValue = output.substring(startPropIndex, endPropIndex);
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains("jsf.ajax.request("));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains(attr.getClientEvent()));
            return endPropIndex + 1;
        }
        else
        {
            Assert.fail("Property " + attr.getName() + " not found");
            return -1;
        }
    }
    
    @Test
    public void testClientBehaviorRenderedWithHtmlAttribute() 
    {
        HtmlRenderedClientEventAttr[] attrs = getClientBehaviorHtmlRenderedAttributes();
        
        for (int i = 0; i < attrs.length; i++)
        {
            UIComponent component = createComponentToTest();
            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;
            clientBehaviorHolder.addClientBehavior(attrs[i].getClientEvent(), new AjaxBehavior());
            component.getAttributes().put(attrs[i].getName(), "htmlValue");
            try 
            {
                component.encodeAll(facesContext);
                String output = outputWriter.toString();
                //jsf.ajax.request('j_id0',event,{'javax.faces.behavior.event':'click'})
                //Only check if the property starts with jsf.ajax.request( is enough 
                //Assert.assertTrue("output does not match expected output jsf.ajax.request(.... for property "+attrs[i].getName(),
                //        output.matches(".+ "+attrs[i].getName()+"=\"jsf\\.ajax\\.request\\(.+"));
                int index = checkClientBehaviorRenderedOnClientEventPropertyAndHtmlValue(output, 0, attrs[i], "htmlValue");
                outputWriter.reset();
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        }
    }
    
    public int checkClientBehaviorRenderedOnClientEventPropertyAndHtmlValue(String output, int start, HtmlRenderedClientEventAttr attr, String value)
    {
        String propStart = " "+attr.getName()+"=\"";
        int propIndex = output.indexOf(propStart, start);
        if (propIndex > -1)
        {
            int c = '"';
            int startPropIndex = propIndex + propStart.length(); 
            int endPropIndex = output.indexOf('"' , startPropIndex );
            String propertyValue = output.substring(startPropIndex, endPropIndex);
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.startsWith("jsf.util.chain("));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains("jsf.ajax.request("));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains(attr.getClientEvent()));
            Assert.assertTrue("Property: " + attr.getName()+" Output: "+output, propertyValue.contains(value));
            return endPropIndex + 1;
        }
        else
        {
            Assert.fail("Property " + attr.getName() + " not found Output: "+output);
            return -1;
        }
    }
}
