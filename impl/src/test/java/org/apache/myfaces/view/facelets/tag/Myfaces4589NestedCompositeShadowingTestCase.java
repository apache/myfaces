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
 * MYFACES-4589: outer composite passes {@code color="#{cc.attrs.color}"} (a String) into inner composite; inner
 * implementation uses {@code #{color.blue}} and must resolve the request-scoped <em>bean</em> {@code color}, not
 * shadow the composite attribute name. Unfixed implementations may throw
 * {@code Property [blue] not found on type [java.lang.String]} during {@link #render}.
 */
public class Myfaces4589NestedCompositeShadowingTestCase extends AbstractFaceletTestCase
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
    public void testInnerCompositeColorBlueMustResolveBeanNotStringAttribute4589() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("color", new ColorBean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "nested4589.xhtml");

        String out = render(root);
        Assertions.assertTrue(out.contains("MYFACES4589_OK"),
                "inner composite #{color.blue} must use Color bean getBlue(), not String.blue; output was: " + out);
    }

    /**
     * Same as {@link #testInnerCompositeColorBlueMustResolveBeanNotStringAttribute4589()} but the outer composite is
     * wrapped by a user taglib that registers short name {@code color} (external reproducer / OmniFaces-style).
     */
    @Test
    public void testUserTaglibThenInnerCompositeColorBlueMustResolveBean4589() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("color", new ColorBean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "nested_user4589.xhtml");

        String out = render(root);
        Assertions.assertTrue(out.contains("MYFACES4589_OK"),
                "MYFACES-4589: inner #{color.blue} must resolve bean after user taglib color= pass-through; output: "
                        + out);
    }

    public static class ColorBean
    {
        public String getBlue()
        {
            return "MYFACES4589_OK";
        }
    }
}
