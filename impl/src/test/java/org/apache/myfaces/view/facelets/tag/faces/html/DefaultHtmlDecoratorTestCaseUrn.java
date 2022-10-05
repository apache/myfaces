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
package org.apache.myfaces.view.facelets.tag.faces.html;

import java.io.StringWriter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.component.html.HtmlHead;
import jakarta.faces.component.html.HtmlInputFile;
import jakarta.faces.component.html.HtmlInputHidden;
import jakarta.faces.component.html.HtmlInputSecret;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlInputTextarea;
import jakarta.faces.component.html.HtmlOutcomeTargetButton;
import jakarta.faces.component.html.HtmlOutcomeTargetLink;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.component.html.HtmlOutputLink;
import jakarta.faces.component.html.HtmlSelectBooleanCheckbox;
import jakarta.faces.component.html.HtmlSelectManyListbox;
import jakarta.faces.component.html.HtmlSelectOneListbox;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagDecorator;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.TagAttributeImpl;
import org.apache.myfaces.view.facelets.tag.TagAttributesImpl;
import org.apache.myfaces.view.facelets.tag.faces.JsfLibrary;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Leonardo Uribe
 */
public class DefaultHtmlDecoratorTestCaseUrn extends FaceletTestCase
{
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        //servletContext.addInitParameter(FaceletViewDeclarationLanguage.PARAM_DECORATORS,
        //    DefaultTagDecorator.class.getName());

        servletContext.addInitParameter(MyfacesConfig.RENDER_CLIENTBEHAVIOR_SCRIPTS_AS_STRING, "true");
    }

    @Test
    public void testHtmlPassthrough1() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testHtmlPassthrough1urn.xhtml");

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
        vdl.buildView(facesContext, root, "testHtmlPassthrough2urn.xhtml");
        
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
                if (ResourceUtils.DEFAULT_SCRIPT_RENDERER_TYPE.equals(child.getRendererType()))
                {
                    Assert.assertEquals("osc", child.getId());
                    scriptsFound++;
                }
                if (ResourceUtils.DEFAULT_STYLESHEET_RENDERER_TYPE.equals(child.getRendererType()))
                {
                    Assert.assertEquals("osh", child.getId());
                    linksFound++;
                }
            }
        }
        for (UIComponent child : root.getComponentResources(facesContext, "head"))
        {
            if ("jakarta.faces.resource.Stylesheet".equals(child.getRendererType()))
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
        
        HtmlInputTextarea textarea1 = (HtmlInputTextarea) form.findComponent("textarea1");
        Assert.assertNotNull(textarea1);
    }

    @Test
    public void testDefaultTagDecorator1() throws Exception
    {
        Location location = new Location("/test.xhtml", 20, 5);
        Tag tag = new Tag(location, DefaultTagDecorator.XHTML_NAMESPACE, "body", "body", 
            new TagAttributesImpl(new TagAttribute[]
                {
                    new TagAttributeImpl(location, JsfLibrary.NAMESPACE, "id", "jsf:id", "idBody")
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
                    new TagAttributeImpl(location, JsfLibrary.NAMESPACE, "action", "jsf:action", "#{test.testAction}")
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
                    new TagAttributeImpl(location, JsfLibrary.SUN_NAMESPACE, "id", "jsf:id", "idBody")
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
                    new TagAttributeImpl(location, JsfLibrary.SUN_NAMESPACE, "action", "jsf:action", "#{test.testAction}")
                }
            ));
        
        TagDecorator tagDecorator = new DefaultTagDecorator();
        Tag decoratedTag = tagDecorator.decorate(tag);
        
        Assert.assertNotNull(decoratedTag);
    }
    
    @Test
    public void testNoMatchJSFElement1() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testNoMatchJSFElement1.xhtml");

        UIComponent box1 = root.findComponent("myForm:box1");
        Assert.assertNotNull(box1);
        Assert.assertEquals(box1.getRendererType(), "jakarta.faces.passthrough.Element");
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        //sw.flush();
    }    
    
    @Test
    public void testConvertTagAttributes1() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testConvertTagAttributes1.xhtml");

        //<input jsf:id="box1" type="text"
        //       jsf:value="#{test.value}" jsf:customAttr="SomeValue"
        //       onclick="alert('hello')"
        //       placeholder="Enter text" 
        //       pt:data_up="Going Up"/>
        UIInput input1 = (UIInput) root.findComponent("myForm:box1");
        Assert.assertNotNull(input1);
        
        Assert.assertEquals(input1.getPassThroughAttributes().get("placeholder"), "Enter text");
        Assert.assertNull(input1.getAttributes().get("placeholder"));
        
        
        Assert.assertEquals(input1.getAttributes().get("customAttr"), "SomeValue");
        // Attributes outside "id", "binding", "rendered" or "transient" can be 
        // copied on passthrough attribute map.
        Assert.assertNull(input1.getPassThroughAttributes().get("customAttr"));
        
        Assert.assertEquals(input1.getPassThroughAttributes().get("data_up"), "Going Up");
        Assert.assertNull(input1.getAttributes().get("data_up"));
        
        Assert.assertNotNull(input1.getValueExpression("value"));
        //Assert.assertNotNull(input1.getPassThroughAttributes().get("value"));
        Assert.assertNull(input1.getPassThroughAttributes().get("value"));
        Assert.assertEquals(input1.getValue(), "value1");
        Assert.assertEquals(input1.getAttributes().get("value"), "value1");
        
        //<input jsf:id="box2" pt:elementName="meter"
        //       jsf:value="#{test.value}" jsf:customAttr="SomeValue"
        //       onclick="alert('hello')"
        //       placeholder="Enter text" 
        //       pt:data_up="Going Up">Hello World!</input>
        UIComponent input2 = root.findComponent("myForm:box2");
        Assert.assertFalse(input2 instanceof UIInput);
        Assert.assertEquals(input2.getRendererType(), "jakarta.faces.passthrough.Element");
        
        Assert.assertEquals(input2.getPassThroughAttributes().get("placeholder"), "Enter text");
        //Assert.assertEquals(input2.getAttributes().get("placeholder"), "Enter text");
        Assert.assertNull(input2.getAttributes().get("placeholder"));
        
        Assert.assertEquals(input2.getAttributes().get("customAttr"), "SomeValue");
        //Assert.assertNull(input2.getAttributes().get("customAttr"));
        //Assert.assertEquals(input2.getPassThroughAttributes().get("customAttr"), "SomeValue");
        Assert.assertNull(input2.getPassThroughAttributes().get("customAttr"));
        
        Assert.assertEquals(input2.getPassThroughAttributes().get("data_up"), "Going Up");
        Assert.assertNull(input2.getAttributes().get("data_up"));
        
        // note there is no type attribute, so it is translated into a jsf:element, and in that
        // component, "value" is not defined, so it is set as passthrough
        Assert.assertNotNull(input2.getValueExpression("value"));
        Assert.assertNull(input2.getPassThroughAttributes().get("value"));
        Assert.assertNotNull(input2.getAttributes().get("value"));
        
        //<jsf:element id="box3" elementName="meter" 
        //       value="#{test.value}" customAttr="SomeValue"
        //       onclick="alert('hello')"
        //       placeholder="Enter text" 
        //       pt:data_up="Going Up">
        //       Hello Element!
        //</jsf:element>
        UIComponent input3 = root.findComponent("myForm:box3");
        Assert.assertFalse(input3 instanceof UIInput);
        Assert.assertEquals(input3.getRendererType(), "jakarta.faces.passthrough.Element");     

        Assert.assertEquals(input3.getAttributes().get("placeholder"), "Enter text");
        Assert.assertNull(input3.getPassThroughAttributes().get("placeholder"));
        
        Assert.assertNull(input3.getPassThroughAttributes().get("customAttr"));
        Assert.assertEquals(input3.getAttributes().get("customAttr"), "SomeValue");
        
        Assert.assertEquals(input3.getPassThroughAttributes().get("data_up"), "Going Up");
        Assert.assertNull(input3.getAttributes().get("data_up"));
        
        Assert.assertNotNull(input3.getValueExpression("value"));
        Assert.assertNull(input3.getPassThroughAttributes().get("value"));
        Assert.assertNotNull(input3.getAttributes().get("value"));
        
        //Assert.assertEquals(input2.getPassThroughAttributes().get("elementName"), "meter");
        
        //<h:panelGroup id="box4">
        //<div jsf:class="noprint">
        //    MYBOX4
        //</div>
        //</h:panelGroup>
        UIComponent box4 = root.findComponent("myForm:box4");
        Assert.assertNotNull(box4);
        UIComponent boxDiv4 = box4.getChildren().get(0);
        Assert.assertNotNull(boxDiv4);
        Assert.assertEquals(boxDiv4.getAttributes().get("styleClass"), "noprint");
        //Assert.assertEquals(boxDiv4.getPassThroughAttributes().get("class"), "noprint");
        Assert.assertNull(boxDiv4.getPassThroughAttributes().get("class"));
        
        //<h:panelGroup id="box5">
        //<div jsf:style="noprint">
        //    MYBOX5
        //</div>
        //</h:panelGroup>
        UIComponent box5 = root.findComponent("myForm:box5");
        Assert.assertNotNull(box5);
        UIComponent boxDiv5 = box5.getChildren().get(0);
        Assert.assertNotNull(boxDiv5);
        Assert.assertNotNull(boxDiv5.getAttributes().get("style"));
        //Assert.assertEquals(boxDiv5.getPassThroughAttributes().get("style"), "noprint");
        Assert.assertNull(boxDiv5.getPassThroughAttributes().get("style"));
        
        StringWriter sw = new StringWriter();
        
        ResponseWriter mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);
        
        HtmlRenderedAttr[] attrs = {
            new HtmlRenderedAttr("data_up", "Going Up"),
            new HtmlRenderedAttr("placeholder", "Enter text"),
            new HtmlRenderedAttr("onclick", "alert('hello')"),
            //new HtmlRenderedAttr("customAttr", "SomeValue"),
            new HtmlRenderedAttr("value", "value1")
        };
        
        input1.encodeAll(facesContext);
        
        sw.flush();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        
        sw = new StringWriter();
        mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);
        
        input2.encodeAll(facesContext);
        
        sw.flush();
        
        attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("data_up", "Going Up"),
            new HtmlRenderedAttr("onclick", "alert('hello')"),
            //new HtmlRenderedAttr("customAttr", "SomeValue"),
            new HtmlRenderedAttr("placeholder", "Enter text")
        };        
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("<meter "));
        Assert.assertTrue(sw.toString().contains("</meter>"));

        sw = new StringWriter();
        mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);
        
        input3.encodeAll(facesContext);
        
        sw.flush();
        
        attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("data_up", "Going Up"),
            //new HtmlRenderedAttr("placeholder", "Enter text"),
            //new HtmlRenderedAttr("customAttr", "SomeValue"),
            //new HtmlRenderedAttr("value", "value1"),
            new HtmlRenderedAttr("onclick", "alert('hello')"),
        };
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("<meter "));
        Assert.assertTrue(sw.toString().contains("</meter>"));

        // TEST 4
        sw = new StringWriter();
        mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);
        
        boxDiv4.encodeAll(facesContext);
        
        sw.flush();
        
        attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("class", "noprint"),
        };
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("MYBOX4"));
        Assert.assertTrue(sw.toString().contains("<div "));
        Assert.assertTrue(sw.toString().contains("</div>"));
        
        // TEST 5
        sw = new StringWriter();
        mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);
        
        boxDiv5.encodeAll(facesContext);
        
        sw.flush();
        
        attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("style", "noprint"),
        };
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("MYBOX5"));
        Assert.assertTrue(sw.toString().contains("<div "));
        Assert.assertTrue(sw.toString().contains("</div>"));
    }  
    
    @Test
    public void testConvertTagAttributes6() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testConvertTagAttributes6.xhtml");
        
        //<div jsf:id="box6" jsf:onclick="alert('hello')">
        //    <f:ajax event="click" render="box5"/>
        //    MYBOX6
        //</div>
        // Try second time, to avoid the script section by f:ajax effect
        UIComponent box6 = root.findComponent("myForm:box6");
        Assert.assertNotNull(box6);
        Assert.assertEquals(box6.getAttributes().get("onclick"), "alert('hello')");

        StringWriter sw = new StringWriter();
        ResponseWriter mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);

        box6.encodeAll(facesContext);
        
        sw.flush();        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("onclick", 
                    "faces.util.chain(this, event,'alert(\\'hello\\')', "
                    + "'myfaces.ab(this,event,\\'click\\',\\'\\',\\'myForm:box5\\')');"),
        };
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("MYBOX6"));
        Assert.assertTrue(sw.toString().contains("<div "));
        Assert.assertTrue(sw.toString().contains("</div>"));
    }  
    
    @Test
    public void testConvertTagAttributes7() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testConvertTagAttributes7.xhtml");
        
        //<h:panelGroup id="box7">
        //    <img jsf:name="external.png" alt="Some Logo"/>
        //</h:panelGroup>
        
        UIComponent box7 = root.findComponent("myForm:box7");
        Assert.assertNotNull(box7);
        UIComponent boxDiv7 = box7.getChildren().get(0);
        Assert.assertNotNull(boxDiv7);
        
        StringWriter sw = new StringWriter();
        
        ResponseWriter mrw = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(mrw);

        boxDiv7.encodeAll(facesContext);
        
        sw.flush();        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
            new HtmlRenderedAttr("alt", "Some Logo"),
        };
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs))
        {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, sw.toString()));
        }
        Assert.assertTrue(sw.toString().contains("<img "));
        Assert.assertTrue(sw.toString().contains("jakarta.faces.resource/external.png"));
    }      
}
