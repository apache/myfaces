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
package org.apache.myfaces.view.facelets.compiler;

import java.io.File;
import java.net.URL;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.junit.Assert;
import org.junit.Test;


public class TagLibraryTestCase extends FaceletTestCase
{
    public final static String TAGLIB_SCHEMA_PATH = "/org/apache/myfaces/resource/web-facelettaglibrary_2_0.xsd";
    
    private URL _validLibUrl = null;
    private URL _invalidLibUrl = null;
    private URL _invalidOldLibUrl = null;

    public void setUp() throws Exception {
        super.setUp();
        _validLibUrl = resolveUrl("/testlib.taglib.xml");
        _invalidLibUrl = resolveUrl("/testlib_invalid.taglib.xml");
        _invalidOldLibUrl = resolveUrl("/testlib_old_invalid.taglib.xml");        

        // set document root for loading schema file as resource
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = cl.getResource(TAGLIB_SCHEMA_PATH.substring(1)).getPath();
        File documentRoot = new File(path.substring(0, path.indexOf(TAGLIB_SCHEMA_PATH)));
        servletContext.setDocumentRoot(documentRoot);
    }

    @Test
    public void testLoadValidLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.VALIDATE_XML, "true");

        FaceletTagLibrary faceletTagLib = TagLibraryConfigUnmarshallerImpl.create(
            externalContext, _validLibUrl);
        TagLibrary lib = TagLibraryConfig.create(facesContext, faceletTagLib);
        Assert.assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib"));
    }

    @Test
    public void testLoadValidLibraryWithoutValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.VALIDATE_XML, "false");

        FaceletTagLibrary faceletTagLib = TagLibraryConfigUnmarshallerImpl.create(
            externalContext, _validLibUrl);
        TagLibrary lib = TagLibraryConfig.create(facesContext, faceletTagLib);
        Assert.assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib"));
    }
    /*
    public void testLoadInvalidLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "true");

        try {
            TagLibraryConfig.create(_invalidLibUrl);
            fail("IOException expected");
        } catch (IOException ioe) {
            assertTrue(ioe.getCause() instanceof SAXException);
        }

    }

    public void testLoadInvalidLibraryWithoutValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "false");

        TagLibrary lib = TagLibraryConfig.create(_invalidLibUrl);
        assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib_invalid"));
    }

    public void testLoadInvalidOldLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "true");

        try {
            TagLibraryConfig.create(_invalidOldLibUrl);
            fail("IOException expected");
        } catch (IOException ioe) {
            assertTrue(ioe.getCause() instanceof SAXException);
        }
    }
    */
}
