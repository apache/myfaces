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
 * MYFACES-4585: Facelets taglib attributes must be exposed on {@link jakarta.el.VariableMapper}
 * (as in {@code LegacyUserTagHandler}), not only on {@code TemplateContext}, so EL that is
 * evaluated outside TemplateContext-only paths resolves correctly.
 */
public class UserTagHandlerVariableMapperTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, "/user4585.taglib.xml");
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testTaglibAttributeAvailableViaVariableMapper4585() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("user4585Bean", new User4585Bean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "user4585_main.xhtml");

        String out = render(root);
        Assertions.assertTrue(out.contains("OK:z"), "Expected taglib 'converter' to resolve for method call: " + out);
    }

    /**
     * Mimics a JSF Converter (or delegate) passed into a taglib as in MYFACES-4585 / PrimeFaces AutoComplete.
     */
    public static class User4585Bean
    {
        public ConverterHelper getConverter()
        {
            return new ConverterHelper();
        }
    }

    public static class ConverterHelper
    {
        public String getDisplayValue(String s)
        {
            return "OK:" + s;
        }
    }
}
