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

import java.io.StringWriter;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

public class DefineIncludeTestCase extends FaceletTestCase {

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
    }

    @Test
    public void testDefineInclude() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "defineInclude.xml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        String response = sw.toString();
        //Two Hello World, the first one by a ui:include on defineInclude.xml,
        //the second one injected on subdir/template.xml
        Assert.assertEquals("Hello World!Hello World!", response);
        //the content of subdir/template.xml ui:insert is ignored
        Assert.assertFalse(response.contains("Not Found"));
        
        //System.out.println("************************");
        //System.out.println(sw.toString());
        //System.out.println("************************");
    }

}
