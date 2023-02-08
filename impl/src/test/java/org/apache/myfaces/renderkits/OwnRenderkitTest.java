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
package org.apache.myfaces.renderkits;

import java.io.StringWriter;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockFacesContext12;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author martin.haimberger
 */
public class OwnRenderkitTest extends AbstractJsfTestCase {
    private MockResponseWriter writer;
    private HtmlInputText inputText;

    private static boolean isOwnRenderKit = false;

    public static void SetIsOwnRenderKit() {
        isOwnRenderKit = true;
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        addRenderKit();
        inputText = new HtmlInputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        MockFacesContext12.getCurrentInstance();

        facesContext.getViewRoot().setRenderKitId("OWN_BASIC");
        facesContext.getRenderKit().addRenderer(
                inputText.getFamily(),
                inputText.getRendererType(),
                new HtmlTextRenderer());

    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        inputText = null;
        writer = null;
        isOwnRenderKit = false;
    }

    @Test
    public void testOwnRenderKit() throws Exception {

        inputText.encodeEnd(facesContext);
        facesContext.renderResponse();

        Assertions.assertTrue(isOwnRenderKit);
    }


    private void addRenderKit() {
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        String renderKitId = "OWN_BASIC";
        String renderKitClass = "org.apache.myfaces.renderkits.OwnRenderKitImpl";

        RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);

        renderKitFactory.addRenderKit(renderKitId, renderKit);

    }

}
