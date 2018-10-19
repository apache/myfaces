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
package org.apache.myfaces.view.facelets.tag.ui.template;

import java.io.StringWriter;
import javax.faces.application.ViewHandler;

import javax.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

public class CompositionTestCase extends FaceletTestCase {

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
    }

    /**
     * Test if ui:composition is used on a page without a reference to a
     * template, it should trim everything outside the template
     * @throws Exception
     */
    @Test
    public void testCompositionTrimEffect() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "compositionTrim.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }

    /**
     * Test if ui:composition is used on a page with a reference to a
     * template, it should trim everything, but take its content and insert it
     * on a template ui:insert without name spot if available. It also test
     * if no definition for a ui:insert with name is set, just render the content
     * inside ui:insert 
     * 
     * @throws Exception
     */
    @Test
    public void testComposition1() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber1"));
        Assert.assertTrue(response.contains("compositionContent"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }

    /**
     * An empty ui:composition does not means the content of a ui:insert without
     * name to be rendered
     * 
     * @throws Exception
     */
    @Test
    public void testComposition2EmptyComposition() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }
    
    /**
     * An empty ui:composition does not means the content of a ui:insert without
     * name to be rendered
     * 
     * @throws Exception
     */
    @Test
    public void testComposition3EmptyComposition() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition3.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }
    
    /**
     * A ui:define takes precedence over a ui:insert
     * 
     * @throws Exception
     */
    @Test
    public void testComposition4DefineOverInsert() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition4.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber1"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }
    
    /**
     * A ui:insert on nested composition takes the top TemplateClient.
     * 
     * In few words, ui:composition acts like a decorator pattern, so it allows
     * override all definitions from other templates that uses ui:composition
     * 
     * @throws Exception
     */
    @Test
    public void testComposition5() throws Exception {
        /*
        composition5.xhtml

        <ui:composition template="/composition5_1.xhtml">
            composition5Content
        </ui:composition>

        composition5_1.xhtml

        <ui:composition template="/composition5_2.xhtml">
            start first composition
            <ui:insert />
            end first composition
        </ui:composition>

        composition5_2.xhtml

        <ui:composition>      
            start second composition
            <ui:insert/>
            end second composition
        </ui:composition>

        Result

            start second composition
            composition5Content
            end second composition
        */
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition5.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
        
        response = checkStringInOrder(response, "start second composition");
        response = checkStringInOrder(response, "composition5Content");
        response = checkStringInOrder(response, "end second composition");        
    }
    
    private String checkStringInOrder(String response, String token)
    {
        int pos = response.indexOf(token);
        Assert.assertTrue(pos > -1);
        return response.substring(pos+token.length());
    }
    
    
    /**
     * An ui:define on the outer most composition takes precedence over
     * the inner one.
     * 
     * @throws Exception
     */
    @Test
    public void testCompositionNested1() throws Exception {
        /*

        compositionNested1.xhtml

        <ui:composition template="/composition4.xhtml">
        <ui:define name="fragment1">
        fragmentNumber2
        </ui:define>
        </ui:composition>

        composition4.xhtml

        <ui:composition template="/template1.xhtml">
        <ui:define name="fragment1">
        fragmentNumber1
        </ui:define>
        </ui:composition>

        template1.xhtml

        <ui:composition>
        <ui:insert>
        This fragment will not be inserted
        </ui:insert>
        <ui:insert name="fragment1">
        fragmentNumber1
        </ui:insert>
        </ui:composition>

        Response

        fragmentNumber2

         */

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "compositionNested1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber2"));
        Assert.assertFalse(response.contains("fragmentNumber1"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
    }
    
    @Test
    public void testCompositionNested2() throws Exception
    {
        /*
        
        compositeNested2.xhtml
        
        <ui:composition template="/compositionNested2_1.xhtml">
          <ui:define name="fragment1">
            compositionNested2Content
          </ui:define>
        </ui:composition>
      
        compositeNested2_1.xhtml
        
        <ui:composition>
            start first decoration
            <ui:decorate template="/compositionNested2_2.xhtml">
              <ui:define name="fragment1">
                start inner text
                  <ui:insert name="fragment1"/>
                end inner text
              </ui:define>
            </ui:decorate>
            end first decoration
        </ui:composition>
            
        compositeNested2_2.xhtml
        
        <ui:composition>
          start second composition
            <ui:insert name="fragment1"/>
          end second composition
        </ui:composition>
        
        Response

            start first decoration
            start second composition
            start inner text
            compositionNested2Content
            end inner text
            end second composition
            end first decoration
        */
      
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "compositionNested2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("THIS SHOULD NOT BE RENDERED"));
        
        response = checkStringInOrder(response, "start first decoration");
        response = checkStringInOrder(response, "start second composition");
        response = checkStringInOrder(response, "start inner text");
        response = checkStringInOrder(response, "compositionNested2Content");
        response = checkStringInOrder(response, "end inner text");
        response = checkStringInOrder(response, "end second composition");
        response = checkStringInOrder(response, "end first decoration");
    }
}
