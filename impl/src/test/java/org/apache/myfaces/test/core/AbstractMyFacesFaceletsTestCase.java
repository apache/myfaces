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
package org.apache.myfaces.test.core;

import org.apache.myfaces.test.core.mock.MockMyFacesFaceletViewDeclarationLanguage;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * <p>Abstract JUnit test case base class, which provide a var called vdl, that
 * can be used to build facelet views calling for example:</p>
 * <p>vdl.buildView(facesContext, facesContext.getViewRoot(), "/hello.xhtml");</p>
 * <p>It already initalize a request, and keep in mind there is no any lifecycle
 * execution. This test case is used to check the view structure.</p>
 * 
 * @author Leonardo Uribe
 *
 */
public abstract class AbstractMyFacesFaceletsTestCase extends AbstractMyFacesRequestTestCase
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        setUpVDL();

        startFaceletRequest();
    }
    
    protected void setUpVDL()
    {
        ViewDeclarationLanguageFactory vdlFactory = (ViewDeclarationLanguageFactory) 
            FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
        vdl = (MockMyFacesFaceletViewDeclarationLanguage) 
            vdlFactory.getViewDeclarationLanguage("/a.xhtml");
    }
    
    /**
     * Initialize a request providing an empty UIViewRoot without enter into the
     * lifecycle. 
     */
    public void startFaceletRequest()
    {
        startRequest();
        // Create a new UIViewRoot to work with it later
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/test");
        root.setRenderKitId("HTML_BASIC");
        facesContext.setViewRoot(root);
        // Set the current phase to render response.
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        endRequest();
        
        super.tearDown();
    }
    
    protected MockMyFacesFaceletViewDeclarationLanguage vdl;
}
