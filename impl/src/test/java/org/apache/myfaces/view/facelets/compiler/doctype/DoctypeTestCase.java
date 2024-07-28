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
package org.apache.myfaces.view.facelets.compiler.doctype;
import org.apache.myfaces.view.facelets.tag.ui.*;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.facelets.Facelet;

import org.apache.myfaces.view.facelets.AbstractFacelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DoctypeTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
        application.addComponent(ComponentRef.COMPONENT_TYPE,
                ComponentRef.class.getName());
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
    public void testCompilerHtml5() throws Exception {
        FaceletFactory ff = vdl.getFaceletFactory();
        Facelet f = ff.getFacelet(this.getClass().getResource("/org/apache/myfaces/view/facelets/compiler/doctype/html5.xhtml"));
        
        Assertions.assertNotNull(((AbstractFacelet) f).getDoctype());
        Assertions.assertEquals("html", ((AbstractFacelet) f).getDoctype().getRootElement());
        Assertions.assertEquals(null, ((AbstractFacelet) f).getDoctype().getPublic());
        Assertions.assertEquals(null, ((AbstractFacelet) f).getDoctype().getSystem());
    }
    
    @Test
    public void testCompilerXhtml() throws Exception {
        FaceletFactory ff = vdl.getFaceletFactory();
        Facelet f = ff.getFacelet(this.getClass().getResource("/org/apache/myfaces/view/facelets/compiler/doctype/xhtml.xhtml"));
        
        Assertions.assertNotNull(((AbstractFacelet) f).getDoctype());
        Assertions.assertEquals("html", ((AbstractFacelet) f).getDoctype().getRootElement());
        Assertions.assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", ((AbstractFacelet) f).getDoctype().getPublic());
        Assertions.assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", ((AbstractFacelet) f).getDoctype().getSystem());
    }
    
    @Test
    public void testHtml5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "html5.xhtml");

        Assertions.assertNotNull(root.getDoctype());
        Assertions.assertEquals("html", root.getDoctype().getRootElement());
        Assertions.assertEquals(null, root.getDoctype().getPublic());
        Assertions.assertEquals(null, root.getDoctype().getSystem());
    }
    
    @Test
    public void testXhtml() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "xhtml.xhtml");

        Assertions.assertNotNull(root.getDoctype());
        Assertions.assertEquals("html", root.getDoctype().getRootElement());
        Assertions.assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", root.getDoctype().getPublic());
        Assertions.assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", root.getDoctype().getSystem());
    }
}
