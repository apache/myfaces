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
package org.apache.myfaces.view.facelets.tag;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4589: ancestor Facelets taglib attribute short names (e.g. {@code color}) must not
 * shadow managed beans with the same identifier inside nested composite implementations.
 */
public class UserTagHandlerCompositeBeanShadowing4589TestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, "/user4589.taglib.xml");
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testBeanNotShadowedByTaglibAttributeInNestedComposite4589() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("color", new ColorBean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "user4589_main.xhtml");

        String out = render(root);
        Assertions.assertTrue(out.contains("MYFACES4589_OK"),
                "expected #{color.blue} to resolve bean, not taglib attribute: " + out);
    }

    public static class ColorBean
    {
        public String getBlue()
        {
            return "MYFACES4589_OK";
        }
    }
}
