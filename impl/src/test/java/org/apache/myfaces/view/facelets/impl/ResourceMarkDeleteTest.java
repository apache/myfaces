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
package org.apache.myfaces.view.facelets.impl;

import java.io.IOException;
import java.util.List;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceMarkDeleteTest  extends FaceletTestCase
{
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "false");
    }

    @Test
    public void test_only_ajs_is_included() throws IOException
    {
        UIViewRoot root = facesContext.getViewRoot();

        // Building the view
        // Because 'includeResource' is not set the second resource should not be included
        vdl.buildView(facesContext, root, "test_conditional_include_resources.xhtml");

        List<UIComponent> resources = root.getComponentResources(facesContext, "head");

        Assertions.assertTrue(resources.size() == 1);
        Assertions.assertTrue(resources.get(0).getAttributes().get("name").equals("a.js"));
    }

    @Test
    public void test_ajs_and_xjs_are_included() throws IOException
    {
        facesContext.getAttributes().put("includeResource", Boolean.TRUE);
        UIViewRoot root = facesContext.getViewRoot();

        // Building the view
        // Because 'includeResource' is now set the second resource should be included
        vdl.buildView(facesContext, root, "test_conditional_include_resources.xhtml");

        List<UIComponent> resources = root.getComponentResources(facesContext, "head");

        Assertions.assertTrue(resources.size() == 2);
    }

    @Test
    public void test_only_ajs_after_refresh_view_is_included() throws IOException
    {
    	UIViewRoot view = facesContext.getViewRoot();

        // Building the initial view
        // Because 'includeResource' is not set the second resource should not be included
        vdl.buildView(facesContext, view, "test_conditional_include_resources.xhtml");

        {
            List<UIComponent> resources = view.getComponentResources(facesContext, "head");

            Assertions.assertTrue(resources.size() == 1);
        }

        // reset 'isFilledView'
        facesContext.getAttributes().remove(view);

        // Building the view a second time
        // Because 'includeResource' is now set the second resource should be included
        facesContext.getAttributes().put("includeResource", Boolean.TRUE);
        vdl.buildView(facesContext, view);

        {
            List<UIComponent> resources = view.getComponentResources(facesContext, "head");

            Assertions.assertTrue(resources.size() == 2);
        }

        // reset 'isFilledView'
        facesContext.getAttributes().remove(view);
        // Building the view a third time
        // Because 'includeResource' is now removed the second resource should not be included

        facesContext.getAttributes().remove("includeResource");
        vdl.buildView(facesContext, view);

        {
            List<UIComponent> resources = view.getComponentResources(facesContext, "head");

            Assertions.assertTrue(resources.size() == 1);
        }
    }
}