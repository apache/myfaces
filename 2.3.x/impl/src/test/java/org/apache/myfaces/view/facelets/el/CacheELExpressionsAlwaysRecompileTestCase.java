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
package org.apache.myfaces.view.facelets.el;

import java.io.StringWriter;
import javax.el.ExpressionFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.impl.FaceletCompositionContextImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class CacheELExpressionsAlwaysRecompileTestCase extends FaceletTestCase
{
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(FaceletCompositionContextImpl.INIT_PARAM_CACHE_EL_EXPRESSIONS,
            ELExpressionCacheMode.alwaysRecompile.toString());
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, "/user.taglib.xml");
    }
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Test
    public void testUIParamCaching1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamcache1.xhtml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    @Test
    public void testUserTagCaching1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "usertagtest1.xhtml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }

    @Test
    public void testUIParamIncludeCaching1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "includetagtest1.xhtml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
}
