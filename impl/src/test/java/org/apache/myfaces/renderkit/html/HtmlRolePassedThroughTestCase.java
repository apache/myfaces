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
package org.apache.myfaces.renderkit.html;

import java.io.StringWriter;

import javax.el.ExpressionFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;

import junit.framework.Assert;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.impl.FaceletCompositionContextImpl;
import org.junit.Test;

public class HtmlRolePassedThroughTestCase extends FaceletTestCase
{

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testRole() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "role.xhtml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        root.encodeAll(facesContext);
        sw.flush();
        //System.out.println("********************HTMLRolePassedThroughTest*****************");
        //System.out.println(sw.toString());
        //System.out.println("********************HTMLRolePassedThroughTest*****************");
        Assert.assertTrue(sw.toString().contains("value=\"user name\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("id=\"helloForm:link\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("<table role=\"new presentation\">"));
        Assert.assertTrue(sw.toString().contains("value=\"one\" role=\"new presentation\""));

        Assert.assertTrue(sw.toString().contains("id=\"helloForm:data\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("id=\"helloForm:graphic\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("type=\"file\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("name=\"helloForm:area\" role=\"new presentation\""));

        Assert.assertTrue(sw.toString().contains("id=\"helloForm:outputFormat\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("id=\"helloForm:outputLabel\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("name=\"helloForm:outputLink\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("id=\"helloForm:outputText\" role=\"new presentation\""));

        // Assert.assertTrue(sw.toString().contains("(?s).*<h:selectBooleanCheckbox\\s+role=\"new presentation\".*"));
        // Assert.assertTrue(sw.toString().contains("(?s).*<h:selectManyCheckbox\\s+role=\"new presentation\".*"));
        Assert.assertTrue(sw.toString().contains("name=\"helloForm:selectManyListbox\" multiple=\"multiple\" size=\"0\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("name=\"helloForm:selectManyMenu\" multiple=\"multiple\" size=\"1\" role=\"new presentation\""));

        Assert.assertTrue(sw.toString().contains("name=\"helloForm:selectOneListbox\" size=\"0\" role=\"new presentation\""));
        Assert.assertTrue(sw.toString().contains("name=\"helloForm:selectOneMenu\" size=\"1\" role=\"new presentation\""));
        // Assert.assertTrue(sw.toString().contains("(?s).*<h:selectOneRadio\\s+role=\"new presentation\".*"));

    }
}
