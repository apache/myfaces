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
package org.apache.myfaces.view.facelets.tag.ui;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.util.FastWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases related to the Faceletes templating mechanism.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class TemplateTestCase extends FaceletTestCase
{

    private FastWriter _writer;

    public void setUp() throws Exception
    {
        super.setUp();

        // install the FastWriter
        _writer = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(_writer);
        facesContext.setResponseWriter(rw);
    }

    @Override
    public void tearDown() throws Exception
    {
        _writer = null;

        super.tearDown();
    }

    /**
     * Tests the case when <ui:insert> is used without a name attribute.
     * In this case the body of the surrounding <ui:composition> must be inserted.
     * @throws Exception
     */
    @Test
    public void testInsertWithoutNameComposition() throws Exception
    {
        _testInsertWithoutName("s_page_composition.xhtml");
    }

    /**
     * Tests the case when <ui:insert> is used without a name attribute.
     * In this case the body of the surrounding <ui:decorate> must be inserted.
     * @throws Exception
     */
    @Test
    public void testInsertWithoutNameDecorate() throws Exception
    {
        _testInsertWithoutName("s_page_decorate.xhtml");
    }

    private void _testInsertWithoutName(String page) throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, page);

        // render view
        root.encodeAll(facesContext);
        final String output = _writer.toString();

        // assertions
        Assert.assertTrue(output.contains("<span id=\"body\">BODY</span>"));
        Assert.assertTrue(output.contains("<span id=\"popupSpan\"><p>[POPUPCONTENT]</p></span>"));
    }

    /**
     * Tests the case when there's a <ui:decorate> with a template that inserts
     * a property defined by the <ui:decorate>.
     * @throws Exception
     */
    @Test
    public void testInsertWithDefinedNameDecorate() throws Exception
    {
        _testInsertWithDefinedName("decorate_with_template_and_define.xhtml");
    }

    /**
     * Tests the case when there's a <ui:composition> with a template that inserts
     * a property defined by the <ui:composition>.
     * @throws Exception
     */
    @Test
    public void testInsertWithDefinedNameComposition() throws Exception
    {
        _testInsertWithDefinedName("composition_with_template_and_define.xhtml");
    }

    private void _testInsertWithDefinedName(String page) throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, page);

        // render view
        root.encodeAll(facesContext);
        final String output = _writer.toString();

        // assertions
        Assert.assertTrue(output.contains("<span>defined content</span>"));
    }

    /**
     * Tests the case when there's a <ui:decorate> with a template that wants to
     * insert a property defined by the <ui:decorate>, but the property was not
     * defined. In this case the default value (= body of <ui:insert>) must be rendered.
     * @throws Exception
     */
    @Test
    public void testInsertWithoutDefinedNameDecorate() throws Exception
    {
        _testInsertWithoutDefinedName("decorate_with_template.xhtml");
    }

    /**
     * Tests the case when there's a <ui:composition> with a template that wants to
     * insert a property defined by the <ui:composition>, but the property was not
     * defined. In this case the default value (= body of <ui:insert>) must be rendered.
     * @throws Exception
     */
    @Test
    public void testInsertWithoutDefinedNameComposition() throws Exception
    {
        _testInsertWithoutDefinedName("composition_with_template.xhtml");
    }

    private void _testInsertWithoutDefinedName(String page) throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, page);

        // render view
        root.encodeAll(facesContext);
        final String output = _writer.toString();

        // assertions
        Assert.assertTrue(output
                .contains("<span>default ui:insert content</span>"));
    }

    /**
     * Tests multilevel templating. In this case multilevel_template_OuterClient_with_define.xhtml
     * is a <ui:decorate> which has a template and a defined content of a <ui:include> of
     * multilevel_template_InnerClient_with_define.xhtml, which itself has a template and a defined
     * content of "inner content".
     * @throws Exception
     */
    @Test
    public void _testMultilevelTemplating() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,
                "multilevel_template_OuterClient_with_define.xhtml");

        // render view
        root.encodeAll(facesContext);
        String output = _writer.toString();
        output = output.replaceAll("(?s)<!--.*?-->", ""); // remove license headers for clarity

        // assertions
        Assert.assertTrue(output.contains("<body>inner content</body>"));
    }

    /**
     * Tests multilevel templating. In this case multilevel_template_OuterClient_with_define.xhtml
     * is a <ui:decorate> which has a template and a defined content of a <ui:include> of
     * multilevel_template_InnerClient_with_define.xhtml, which itself has a template, but not a
     * defined content. However it inserts content and thus it must use the fallback content of
     * the innerTemplate.
     * @throws Exception
     */
    @Test
    public void _testMultilevelTemplatingInnerFallback() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,
                "multilevel_template_OuterClient.xhtml");

        // render view
        root.encodeAll(facesContext);
        String output = _writer.toString();
        output = output.replaceAll("(?s)<!--.*?-->", ""); // remove license headers for clarity

        // assertions
        Assert.assertTrue(output
                .contains("<body>inner fallback content</body>"));
    }

}
