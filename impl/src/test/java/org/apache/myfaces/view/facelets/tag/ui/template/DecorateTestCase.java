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

public class DecorateTestCase extends FaceletTestCase {

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
    }

    @Test
    public void testDecorate1() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorate1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber1"));
        Assert.assertTrue(response.contains("decorateContent"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
    }
    
    @Test
    public void testDecorate2EmptyDecorate() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorate2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
    }
    
    @Test
    public void testDecorate3EmptyDecorate() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorate3.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
    }
    
    @Test
    public void testDecorate4DefineOverInsert() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorate4.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber1"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
    }
    
    /**
     * Based on
     * 
     * https://facelets.dev.java.net/issues/show_bug.cgi?id=256
     * 
     * In few words it says how two nested ui:decorate tags should be resolved.
     * Note the difference between ui:composition and ui:decorate. Since the first one
     * trims everything above and below, the page author can only use once per view, but
     * with ui:decorate you can use more than once per view. Additionally, an ui:insert
     * inside a ui:decorate, should take into account the outer one first.
     * 
     * @throws Exception
     */
    @Test
    public void testDecorate5() throws Exception 
    {
        /*
        decorate5.xhtml

        <ui:decorate template="/decorate5_1.xhtml">
        decorate5Content
        </ui:decorate>

        decorate5_1.xhtml

        <ui:composition>
            start first decoration
            <ui:decorate template="/decorate5_2.xhtml">
              start inner text
              <ui:insert />
              end inner text
            </ui:decorate>
            end first decoration
        </ui:composition>

        decorate5_2.xhtml

        <ui:composition>      
            start second decoration
            <ui:insert/>
            end second decoration
        </ui:composition>

        Result

            start first decoration
             start second decoration
              start inner text
               decorate5Content
              end inner text
             end second decoration
            end first decoration
        */

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorate5.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();

        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
        
        response = checkStringInOrder(response, "start first decoration");
        response = checkStringInOrder(response, "start second decoration");
        response = checkStringInOrder(response, "start inner text");
        response = checkStringInOrder(response, "decorate5Content");
        response = checkStringInOrder(response, "end inner text");
        response = checkStringInOrder(response, "end second decoration");
        response = checkStringInOrder(response, "end first decoration");
    }
    
    private String checkStringInOrder(String response, String token)
    {
        int pos = response.indexOf(token);
        Assert.assertTrue(pos > -1);
        return response.substring(pos+token.length());
    }

    /**
     * An outer ui:decorate definition takes precedence over an inner
     * ui:composition definition.
     * 
     * @throws Exception
     */
    @Test
    public void testDecorateNested1() throws Exception {
        /*
        decorate4.xhtml

        <ui:decorate template="/composition4.xhtml">
          <ui:define name="fragment1">
            fragmentNumber2
          </ui:define>
        fragmentNumber3
        </ui:decorate>

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

            fragmentNumber3
            fragmentNumber2

        */

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorateNested1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        Assert.assertTrue(response.contains("fragmentNumber3"));
        Assert.assertTrue(response.contains("fragmentNumber2"));
        Assert.assertFalse(response.contains("fragmentNumber1"));
        Assert.assertFalse(response.contains("This fragment will not be inserted"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED"));
        Assert.assertTrue(response.contains("THIS SHOULD BE RENDERED TOO"));
    }
    
    
    @Test
    public void testDecorateNested2() throws Exception
    {
        /*
        decorateNested2.xhtml
        
        <ui:composition>
          <ui:decorate template="/decorateNested2_1.xhtml">
            <ui:define name="fragment1">
              decorateNested2Content
            </ui:define>
          </ui:decorate>
        </ui:composition>
      
        decorateNested2_1.xhtml
        
        <ui:composition>
            start first decoration
            <ui:decorate template="/decorateNested2_2.xhtml">
              <ui:define name="fragment1">
                start inner text
                  <ui:insert name="fragment1"/>
                end inner text
              </ui:define>
            </ui:decorate>
            end first decoration
        </ui:composition>
        
        decorateNested2_2.xhtml
    
        <ui:composition>
            start second composition
            <ui:insert name="fragment1"/>
            end second composition
        </ui:composition>
        
        Response
        
        start first decoration
        start second composition
        start inner text
        decorateNested2Content
        end inner text
        end second composition
        end first decoration
        
        */
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "decorateNested2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();

        response = checkStringInOrder(response, "start first decoration");
        response = checkStringInOrder(response, "start second composition");
        response = checkStringInOrder(response, "start inner text");
        response = checkStringInOrder(response, "decorateNested2Content");
        response = checkStringInOrder(response, "end inner text");
        response = checkStringInOrder(response, "end second composition");
        response = checkStringInOrder(response, "end first decoration");
    }
}
