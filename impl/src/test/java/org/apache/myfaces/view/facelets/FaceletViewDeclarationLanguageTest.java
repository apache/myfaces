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
package org.apache.myfaces.view.facelets;

import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.ViewMetadata;
import org.apache.myfaces.view.facelets.bean.ViewBean;

/**
 * Test cases for FaceletViewDeclarationLanguage.
 *
 * @author Jakob Korherr
 */
public class FaceletViewDeclarationLanguageTest extends FaceletTestCase
{

    /**
     * Test case for MYFACES-3002.
     */
    @Test
    public void testBuildViewUIViewParameters() throws Exception
    {
        // build a UIViewRoot with a UIViewParameter
        ViewMetadata viewMetadata = this.vdl.getViewMetadata(facesContext, "viewparameter1.xhtml");
        UIViewRoot root = viewMetadata.createMetadataView(facesContext);
        facesContext.setViewRoot(root);

        // UIViewParameter must be there
        checkUIViewParameter(root);

        // build and render view (must not remove UIViewParameter)
        vdl.buildView(facesContext, root, "viewparameter1.xhtml");
        vdl.renderView(facesContext, root);

        // UIViewParameter must still be there, because its value is saved in the state!
        checkUIViewParameter(root);
    }

    private void checkUIViewParameter(UIViewRoot root)
    {
        // the metadata facet MUST be there
        UIComponent metadataFacet = root.getFacet(UIViewRoot.METADATA_FACET_NAME);
        Assertions.assertNotNull(metadataFacet);
        Assertions.assertTrue(metadataFacet instanceof UIPanel); // the metadata-facet must be a UIPanel

        // get the UIViewParameter
        UIComponent viewParameter = metadataFacet.getChildren().get(0);
        Assertions.assertTrue(viewParameter instanceof UIViewParameter);
    }

    @Test
    public void testBuildViewUIViewParametersLocale() throws Exception
    {
        request.setAttribute("viewBean", new ViewBean());
        
        // build a UIViewRoot with a UIViewParameter
        ViewMetadata viewMetadata = this.vdl.getViewMetadata(facesContext, "viewparameter2.xhtml");
        UIViewRoot root = viewMetadata.createMetadataView(facesContext);
        facesContext.setViewRoot(root);

        // UIViewParameter must be there
        checkUIViewParameter(root);
        
        Assertions.assertEquals(Locale.FRANCE, root.getLocale());

        // build and render view (must not remove UIViewParameter)
        vdl.buildView(facesContext, root, "viewparameter2.xhtml");
        vdl.renderView(facesContext, root);

        // UIViewParameter must still be there, because its value is saved in the state!
        checkUIViewParameter(root);
    }
}
