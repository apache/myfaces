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
package org.apache.myfaces.view.jsp;

import java.io.IOException;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.Assert;

/**
 * Test class for JspViewDeclarationLanguage.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class JspViewDeclarationLanguageTest extends AbstractJsfTestCase
{

    private TrackingJspViewDeclarationLanguage jspVdl;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        jspVdl = new TrackingJspViewDeclarationLanguage();
    }

    @Override
    public void tearDown() throws Exception
    {
        jspVdl = null;
        
        super.tearDown();
    }

    /**
     * Tests if renderView() implicitly calls buildView() if there was no call
     * to buildView() for the given UIViewRoot yet. This is needed in order to
     * support legacy ViewHandlers which return null on getViewDeclarationLanguage()
     * and thus vdl.buildView() was not called yet when renderView() is invoked.
     */
    public void testBuildViewCalledBeforeViewRendered() 
    {
        try
        {
            jspVdl.renderView(facesContext, facesContext.getViewRoot());
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        
        // assert that buildView() was implicitly called once (by renderView())
        Assert.assertEquals(1, jspVdl._buildViewCalled);
    }
    
    /**
     * Tests if buildView() is not called twice if it has already been called
     * before renderView() is invoked.
     * This test is related to testBuildViewCalledBeforeViewRendered.
     */
    public void testBuildViewNotCalledTwiceInRenderView()
    {
        try
        {
            jspVdl.buildView(facesContext, facesContext.getViewRoot());
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        try
        {
            jspVdl.renderView(facesContext, facesContext.getViewRoot());
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        
        // assert that buildView() was only called once
        Assert.assertEquals(1, jspVdl._buildViewCalled);
    }
    
    /**
     * Tests if the direct/implicit calls to builView() work correctly
     * for different views.
     * This test is related to testBuildViewCalledBeforeViewRendered
     * and testBuildViewNotCalledTwiceInRenderView.
     */
    public void testBuildViewRenderViewContractForDifferentViews()
    {
        UIViewRoot firstView = facesContext.getViewRoot();
        UIViewRoot secondView = new UIViewRoot();
        
        try
        {
            jspVdl.buildView(facesContext, firstView);
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        try
        {
            jspVdl.renderView(facesContext, firstView);
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        try
        {
            jspVdl.renderView(facesContext, secondView);
        }
        catch (Exception e)
        {
            // we're not testing the real behavior here, so Exceptions may occur
        }
        
        // assert that buildView() was called twice:
        // the first time directly by jspVdl.buildView() for firstView
        // the second time implicitly by jspVdl.renderView() for secondView
        Assert.assertEquals(2, jspVdl._buildViewCalled);
    }
    
    
    /**
     * Extends JspViewDeclarationLanguage to count the calls to buildView().
     * 
     * @author Jakob Korherr
     */
    private class TrackingJspViewDeclarationLanguage extends JspViewDeclarationLanguage
    {

        private int _buildViewCalled = 0;
        
        @Override
        public void buildView(FacesContext context, UIViewRoot view)
                throws IOException
        {
            _buildViewCalled++;
            
            super.buildView(context, view);
        }
        
    }
    
}
