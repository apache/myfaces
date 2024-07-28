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

import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlMessages;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.convert.IntegerConverter;

import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlMenuRenderer;
import org.apache.myfaces.renderkit.html.HtmlMessagesRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

public class SelectTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE,
                HtmlForm.class.getName());        
        application.addComponent(HtmlSelectOneMenu.COMPONENT_TYPE,
                HtmlSelectOneMenu.class.getName());
        application.addComponent(UISelectItem.COMPONENT_TYPE,
                UISelectItem.class.getName());        
        application.addComponent(HtmlCommandButton.COMPONENT_TYPE,
                HtmlCommandButton.class.getName());
        application.addComponent(HtmlMessages.COMPONENT_TYPE,
                HtmlMessages.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
        application.addConverter(IntegerConverter.CONVERTER_ID,
                IntegerConverter.class.getName());
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY,
                "jakarta.faces.Text", new HtmlTextRenderer());        
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "jakarta.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(HtmlSelectOneMenu.COMPONENT_FAMILY,
                "jakarta.faces.Menu", new HtmlMenuRenderer());        
        renderKit.addRenderer(HtmlCommandButton.COMPONENT_FAMILY,
                "jakarta.faces.Button", new HtmlButtonRenderer());
        renderKit.addRenderer(HtmlMessages.COMPONENT_FAMILY,
                "jakarta.faces.Messages", new HtmlMessagesRenderer());
    }

    @Test
    public void testSelectOne() throws Exception
    {
        request.getSession().setAttribute("test", new MockBean());
        request.addParameter("testForm:alignment", "10");

        UIViewRoot root = new UIViewRoot();
        vdl.buildView(facesContext, root,"selectOne.xml");
        UISelectOne one = (UISelectOne) root
                .findComponent("testForm:alignment");
        root.processDecodes(facesContext);
        root.processValidators(facesContext);
        //System.out.println(facesContext.getMessages().hasNext());
    }

}
