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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import javax.el.ExpressionFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.util.FastWriter;
import org.junit.Assert;
import org.junit.Test;

public class CsetTestCase extends FaceletTestCase {

    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, "/user.taglib.xml");
    }
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        // For this test we need the a real one, because the Mock does not
        // handle VariableMapper stuff properly and ui:param logic will not work
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    /**
     * c:set should not pass to pages referenced by ui:decorate
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope1.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * c:set should not pass to pages referenced by ui:composition
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope2() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope2.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }

    /**
     * c:set should not pass to pages referenced by ui:include
     * 
     * - user tags
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope3() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope3.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }

    /**
     * c:set should not pass to pages referenced by user tags
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope4() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope4.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * c:set should not pass to pages referenced by composite components
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope5.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }

    /**
     * c:set tags called outside ui:decorate should apply to definitions inside it,
     * because c:set apply values to page scope.
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope6() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope6.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * c:set tags defined before ui:decorate applies for expressions after that tag 
     * 
     * @throws Exception
     */
    @Test
    public void testCsetPageScope7() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "csetpagescope7.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
}
