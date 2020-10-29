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
package org.apache.myfaces.view.facelets.tag.jsf.html;

import java.io.PrintWriter;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.Test;

public class MockNbsp extends FaceletTestCase
{

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
    public void testNbsp() throws Exception
    {
        UIViewRoot root = new UIViewRoot();
        vdl.buildView(facesContext, root,"nbsp.xml");

        PrintWriter pw = new PrintWriter(System.out);
        MockResponseWriter rw = new MockResponseWriter(pw);
        facesContext.setResponseWriter(rw);

        root.encodeAll(facesContext);

        pw.close();
    }
}
