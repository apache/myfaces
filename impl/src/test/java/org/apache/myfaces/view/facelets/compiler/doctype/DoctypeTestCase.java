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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.faces.view.facelets.Facelet;

import org.apache.myfaces.view.facelets.AbstractFacelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

class DoctypeTestCase extends AbstractFaceletTestCase
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
    void compilerHtml5() throws Exception {
        FaceletFactory ff = vdl.getFaceletFactory();
        Facelet f = ff.getFacelet(this.getClass().getResource("/org/apache/myfaces/view/facelets/compiler/doctype/html5.xhtml"));
        
        assertNotNull(((AbstractFacelet) f).getDoctype());
        assertEquals("html", ((AbstractFacelet) f).getDoctype().getRootElement());
        assertNull(((AbstractFacelet) f).getDoctype().getPublic());
        assertNull(((AbstractFacelet) f).getDoctype().getSystem());
    }

    @Test
    void compilerXhtml() throws Exception {
        FaceletFactory ff = vdl.getFaceletFactory();
        Facelet f = ff.getFacelet(this.getClass().getResource("/org/apache/myfaces/view/facelets/compiler/doctype/xhtml.xhtml"));
        
        assertNotNull(((AbstractFacelet) f).getDoctype());
        assertEquals("html", ((AbstractFacelet) f).getDoctype().getRootElement());
        // MYFACES-4681 - Simplified to <!DOCTYPE html> as HTML5 is default
        assertNull(((AbstractFacelet) f).getDoctype().getPublic());
        assertNull(((AbstractFacelet) f).getDoctype().getSystem());
    }

    @Test
    void html5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "html5.xhtml");

        assertNotNull(root.getDoctype());
        assertEquals("html", root.getDoctype().getRootElement());
        assertNull(root.getDoctype().getPublic());
        assertNull(root.getDoctype().getSystem());
    }

    @Test
    void xhtml() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "xhtml.xhtml");

        assertNotNull(root.getDoctype());
        assertEquals("html", root.getDoctype().getRootElement());
        // MYFACES-4681 - Simplified to <!DOCTYPE html> as HTML5 is default
        assertNull(root.getDoctype().getPublic()); 
        assertNull(root.getDoctype().getSystem());
    }
}
