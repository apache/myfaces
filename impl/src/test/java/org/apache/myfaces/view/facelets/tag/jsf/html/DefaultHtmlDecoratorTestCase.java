/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.view.facelets.tag.jsf.html;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlBody;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlHead;
import javax.faces.component.html.HtmlInputFile;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutcomeTargetButton;
import javax.faces.component.html.HtmlOutcomeTargetLink;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.view.Location;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagDecorator;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.TagAttributeImpl;
import org.apache.myfaces.view.facelets.tag.TagAttributesImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Leonardo Uribe
 */
public class DefaultHtmlDecoratorTestCase extends FaceletTestCase
{
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        //servletContext.addInitParameter(FaceletViewDeclarationLanguage.PARAM_DECORATORS,
        //    DefaultTagDecorator.class.getName());
    }

    @Test
    public void testHtmlPassthrough1() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testHtmlPassthrough1.xhtml");

        checkTags();
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        //sw.flush();
    }
    
    @Test
    public void testHtmlPassthrough2() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testHtmlPassthrough2.xhtml");
        
        checkTags();

        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        //sw.flush();
    }

    private void checkTags() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();

        HtmlHead head = (HtmlHead) root.findComponent("idHead");
        int linksFound = 0;
        int scriptsFound = 0;
        for (UIComponent child : head.getChildren())
        {
            if (child instanceof UIOutput)
            {
                if ("javax.faces.resource.Script".equals(child.getRendererType()))
                {
                    Assert.assertEquals("osc", child.getId());
                    scriptsFound++;
                }
                if ("javax.faces.resource.Stylesheet".equals(child.getRendererType()))
                {
                    Assert.assertEquals("osh", child.getId());
                    linksFound++;
                }
            }
        }
        for (UIComponent child : root.getComponentResources(facesContext, "head"))
        {
            if ("javax.faces.resource.Stylesheet".equals(child.getRendererType()))
            {
                Assert.assertEquals("osh", child.getId());
                linksFound++;
            }
        }
        
        Assert.assertEquals(1, linksFound);
        Assert.assertEquals(1, scriptsFound);
        
        HtmlBody body = (HtmlBody) root.findComponent("idBody");
        Assert.assertNotNull(body);

        UIForm form = (UIForm) root.findComponent("myForm");
        Assert.assertNotNull(form);

        HtmlCommandLink link1 = (HtmlCommandLink) form.findComponent("link1");
        Assert.assertNotNull(link1);
        Assert.assertEquals("#{test.testAction}", link1.getActionExpression().getExpressionString());
        
        HtmlCommandLink link2 = (HtmlCommandLink) form.findComponent("link2");
        Assert.assertNotNull(link2);
        Assert.assertEquals(1, link2.getActionListeners().length);
        
        HtmlOutputLink link3 = (HtmlOutputLink) form.findComponent("link3");
        Assert.assertNotNull(link3);
        Assert.assertEquals("/my/new/location.txt", link3.getValue());
        
        HtmlOutcomeTargetLink link4 = (HtmlOutcomeTargetLink) form.findComponent("link4");
        Assert.assertNotNull(link4);
        Assert.assertEquals("rollback", link4.getOutcome());
        
        HtmlCommandButton button = (HtmlCommandButton) form.findComponent("button1");
        Assert.assertNotNull(button);
        Assert.assertEquals("#{test.testAction}", button.getActionExpression().getExpressionString());
        
        HtmlOutcomeTargetButton button2 = (HtmlOutcomeTargetButton) form.findComponent("button2");
        Assert.assertNotNull(button2);
        Assert.assertEquals("rollback", button2.getOutcome());
        
        HtmlGraphicImage img1 = (HtmlGraphicImage) form.findComponent("img1");
        Assert.assertNotNull(img1);
        Assert.assertEquals("/my/image.png", img1.getUrl());
        
        HtmlCommandButton input1 = (HtmlCommandButton) form.findComponent("input1");
        Assert.assertNotNull(input1);
        
        HtmlSelectBooleanCheckbox input2 = (HtmlSelectBooleanCheckbox) form.findComponent("input2");
        Assert.assertNotNull(input2);
        
        HtmlInputText input3 = (HtmlInputText) form.findComponent("input3");
        Assert.assertNotNull(input3);

        HtmlInputText input4 = (HtmlInputText) form.findComponent("input4");
        Assert.assertNotNull(input4);

        HtmlInputText input5 = (HtmlInputText) form.findComponent("input5");
        Assert.assertNotNull(input5);

        HtmlInputText input6 = (HtmlInputText) form.findComponent("input6");
        Assert.assertNotNull(input6);

        HtmlInputText input7 = (HtmlInputText) form.findComponent("input7");
        Assert.assertNotNull(input7);

        HtmlInputText input8 = (HtmlInputText) form.findComponent("input8");
        Assert.assertNotNull(input8);

        HtmlInputText input9 = (HtmlInputText) form.findComponent("input9");
        Assert.assertNotNull(input9);

        HtmlInputText input10 = (HtmlInputText) form.findComponent("input10");
        Assert.assertNotNull(input10);

        HtmlInputText input11 = (HtmlInputText) form.findComponent("input11");
        Assert.assertNotNull(input11);

        HtmlInputText input12 = (HtmlInputText) form.findComponent("input12");
        Assert.assertNotNull(input12);

        HtmlInputText input13 = (HtmlInputText) form.findComponent("input13");
        Assert.assertNotNull(input13);

        HtmlInputText input14 = (HtmlInputText) form.findComponent("input14");
        Assert.assertNotNull(input14);
        
        HtmlInputFile input15 = (HtmlInputFile) form.findComponent("input15");
        Assert.assertNotNull(input15);
        
        HtmlInputHidden input16 = (HtmlInputHidden) form.findComponent("input16");
        Assert.assertNotNull(input16);
        
        HtmlInputSecret input17 = (HtmlInputSecret) form.findComponent("input17");
        Assert.assertNotNull(input17);
        
        HtmlCommandButton input18 = (HtmlCommandButton) form.findComponent("input18");
        Assert.assertNotNull(input18);
        
        HtmlCommandButton input19 = (HtmlCommandButton) form.findComponent("input19");
        Assert.assertNotNull(input19);

        HtmlInputText input20 = (HtmlInputText) form.findComponent("input20");
        Assert.assertNotNull(input20);
        
        HtmlOutputLabel label1 = (HtmlOutputLabel) form.findComponent("label1");
        Assert.assertNotNull(label1);
        
        HtmlSelectOneListbox select1 = (HtmlSelectOneListbox) form.findComponent("select1");
        Assert.assertNotNull(select1);
        
        HtmlSelectManyListbox select2 = (HtmlSelectManyListbox) form.findComponent("select2");
        Assert.assertNotNull(select2);
        
    }

    @Test
    public void testDefaultTagDecorator1() throws Exception
    {
        Location location = new Location("/test.xhtml", 20, 5);
        Tag tag = new Tag(location, DefaultTagDecorator.XHTML_NAMESPACE, "body", "body", 
            new TagAttributesImpl(new TagAttribute[]
                {
                    new TagAttributeImpl(location, DefaultTagDecorator.JSF_NAMESPACE, "id", "jsf:id", "idBody")
                }
            ));
        
        TagDecorator tagDecorator = new DefaultTagDecorator();
        Tag decoratedTag = tagDecorator.decorate(tag);
        
        Assert.assertNotNull(decoratedTag);
    }
    
    @Test
    public void testDefaultTagDecorator2() throws Exception
    {
        Location location = new Location("/test.xhtml", 20, 5);
        Tag tag = new Tag(location, DefaultTagDecorator.XHTML_NAMESPACE, "a", "a", 
            new TagAttributesImpl(new TagAttribute[]
                {
                    new TagAttributeImpl(location, DefaultTagDecorator.JSF_NAMESPACE, "action", "jsf:action", "#{test.testAction}")
                }
            ));
        
        TagDecorator tagDecorator = new DefaultTagDecorator();
        Tag decoratedTag = tagDecorator.decorate(tag);
        
        Assert.assertNotNull(decoratedTag);
    }
    
    @Test
    public void testDefaultTagDecorator3() throws Exception
    {
        Location location = new Location("/test.xhtml", 20, 5);
        Tag tag = new Tag(location, DefaultTagDecorator.XHTML_NAMESPACE, "body", "body", 
            new TagAttributesImpl(new TagAttribute[]
                {
                    new TagAttributeImpl(location, DefaultTagDecorator.JSF_ALIAS_NAMESPACE, "id", "jsf:id", "idBody")
                }
            ));
        
        TagDecorator tagDecorator = new DefaultTagDecorator();
        Tag decoratedTag = tagDecorator.decorate(tag);
        
        Assert.assertNotNull(decoratedTag);
    }
    
    @Test
    public void testDefaultTagDecorator4() throws Exception
    {
        Location location = new Location("/test.xhtml", 20, 5);
        Tag tag = new Tag(location, DefaultTagDecorator.XHTML_NAMESPACE, "a", "a", 
            new TagAttributesImpl(new TagAttribute[]
                {
                    new TagAttributeImpl(location, DefaultTagDecorator.JSF_ALIAS_NAMESPACE, "action", "jsf:action", "#{test.testAction}")
                }
            ));
        
        TagDecorator tagDecorator = new DefaultTagDecorator();
        Tag decoratedTag = tagDecorator.decorate(tag);
        
        Assert.assertNotNull(decoratedTag);
    }
    
}
