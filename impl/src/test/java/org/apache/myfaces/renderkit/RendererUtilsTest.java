/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.renderkit;

import org.apache.myfaces.renderkit.RendererUtils;
import java.io.IOException;
import java.io.StringWriter;

import javax.el.ELContext;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.junit.Assert;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.JSFAttr;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

public class RendererUtilsTest extends AbstractJsfTestCase {

	private MockResponseWriter writer;

	/**
	 * ResourceHandler nice easy mock
	 */
	ResourceHandler resourceHandlerMock;

	/**
	 * {@link Application} nice easy mock
	 */
	Application applicationMock;

	/**
	 * A {@link Resource} nice easy mock
	 */
	private Resource resourceMock;

	String libraryName = "images";

	String resourceName = "picture.gif";

	String requestPath = "/somePrefix/faces/jakarta.faces.resource/picture.gif?ln=\"images\"";

	// a Component instance:
	HtmlGraphicImage graphicImage = new HtmlGraphicImage();

    private UIPanel parent;

	public void setUp() throws Exception {
		super.setUp();

		writer = new MockResponseWriter(new StringWriter(), null, null);
		facesContext.setResponseWriter(writer);

		applicationMock = EasyMock.createNiceMock(Application.class);
		facesContext.setApplication(applicationMock);

		resourceHandlerMock = EasyMock.createNiceMock(ResourceHandler.class);
		applicationMock.getResourceHandler();
		EasyMock.expectLastCall().andReturn(resourceHandlerMock);
		
		applicationMock.getProjectStage();
		EasyMock.expectLastCall().andReturn(ProjectStage.Development);

		resourceMock = EasyMock.createNiceMock(Resource.class);

		EasyMock.replay(applicationMock);

		graphicImage.getAttributes().put(JSFAttr.LIBRARY_ATTR, libraryName);
		graphicImage.getAttributes().put(JSFAttr.NAME_ATTR, resourceName);
		graphicImage.setId("graphicImageId");
		
		parent = new UIPanel();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testGetIconSrc() {

		// Training a mock:
		resourceHandlerMock.createResource(resourceName, libraryName);
		EasyMock.expectLastCall().andReturn(resourceMock);
		resourceMock.getRequestPath();
		EasyMock.expectLastCall().andReturn(requestPath);
		EasyMock.replay(resourceHandlerMock);
		EasyMock.replay(resourceMock);

		// Tested method :
		String iconSrc = RendererUtils.getIconSrc(facesContext, graphicImage,
				HTML.IMG_ELEM);

		Assert.assertEquals(
				"If name or name/library present, source must be obtained from ResourceHandler",
				requestPath, iconSrc);

	}

	public void testGetIconSrcResourceNotFound() throws Exception {
		// Training a mock:
		EasyMock.reset(resourceHandlerMock);
		resourceHandlerMock.createResource(resourceName, libraryName);
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.replay(resourceHandlerMock);

		// Tested method :
		String iconSrc = RendererUtils.getIconSrc(facesContext, graphicImage,
				                    HTML.IMG_ELEM);

		Assert.assertEquals("RES_NOT_FOUND", iconSrc);
		Assert.assertTrue("If resource is not found, a Message must be added",
				facesContext.getMessages(graphicImage.getClientId(facesContext)).hasNext());

	}

    public void testGetStringValue()
    {
        // Test for situation where submittedValue IS NOT String: 
        UIInput uiInput = new UIInput();
        Object submittedValue = new Object();
        uiInput.setSubmittedValue(submittedValue);
        
        String stringValue = RendererUtils.getStringValue(facesContext, uiInput);
        Assert.assertNotNull(stringValue);
        Assert.assertEquals("If submittedvalue is not String, toString() must be used", submittedValue.toString(), stringValue);
    }

    public void testGetConvertedUIOutputValue()
    {
        UIInput uiInput = new UIInput();
        StringBuilder submittedValue = new StringBuilder("submittedValue");
        uiInput.setSubmittedValue(submittedValue);
        
        
        Object convertedUIOutputValue = RendererUtils.getConvertedUIOutputValue(facesContext, uiInput, submittedValue);
        Assert.assertEquals("If submittedvalue is not String, toString() must be used", submittedValue.toString(), convertedUIOutputValue);
    }
    
    @Test
    public void testIsRendered()
    {
        UIComponent uiComponent = new UIOutput();
        boolean rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assert.assertTrue(rendered);
        
        uiComponent.setRendered(false);
        rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assert.assertFalse(rendered);
        
        uiComponent = _setUpComponentStack();
        rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assert.assertFalse(rendered);
        Assert.assertEquals("isRendered must not change current component", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    /**
     * Verifies the current component on stack
     */
    private class MockRenderedValueExpression extends org.apache.myfaces.test.el.MockValueExpression {

        private final UIComponent toVerify;

        public MockRenderedValueExpression(String expression, Class<?> expectedType, UIComponent toVerify) {
            super(expression, expectedType);
            this.toVerify = toVerify;
        }
        
        @Override
        public Object getValue(ELContext elContext) {
          UIComponent currentComponent = UIComponent.getCurrentComponent(facesContext);
          Assert.assertEquals("If this VE is called, component on stack must be actual" , currentComponent , toVerify);
          return false;
        }
    }
    
    /** Verifies no call to encode* methods */
    private class MockComponent extends UIOutput
    {
        @Override
        public boolean isRendered() {
            return false;
        }
        @Override
        public void encodeBegin(FacesContext context) throws IOException {
            Assert.fail();
        }
        @Override
        public void encodeChildren(FacesContext context) throws IOException {
            Assert.fail();
        }
        @Override
        public void encodeEnd(FacesContext context) throws IOException {
            Assert.fail();
        }
    }
    
    private UIInput _setUpComponentStack() {
        UIInput uiInput = new UIInput();
        parent.getChildren().add(uiInput);
        uiInput.setId("testId");
        
        
        MockRenderedValueExpression ve = new MockRenderedValueExpression("#{component.id eq 'testId'}", Boolean.class, uiInput);
        uiInput.setValueExpression("rendered", ve);
        
       // simlulate that parent panel encodes children and is on the stack:
       parent.pushComponentToEL(facesContext, null);
        return uiInput;
    }

}
