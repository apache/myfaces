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
package org.apache.myfaces.view.facelets.impl;

import java.net.URL;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.context.InvalidFileException;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link DefaultFaceletFactory#resolveURL} rejects external URLs
 * and non-Facelet extensions when running outside the {@code UnitTest} ProjectStage.
 *
 * <p>The suite runs under {@code Development} stage (overriding the default
 * {@code UnitTest} set by the base class) so the validation code-path is always active.</p>
 */
public class DefaultFaceletFactoryPathValidationTest extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        // Replace "UnitTest" so the validation branch in resolveURL is active
        servletContext.addInitParameter("jakarta.faces.PROJECT_STAGE", "Development");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private DefaultFaceletFactory getFactory()
    {
        ViewDeclarationLanguage vdl = facesContext.getApplication()
                .getViewHandler()
                .getViewDeclarationLanguage(facesContext, "/test.xhtml");
        return (DefaultFaceletFactory) ((FaceletViewDeclarationLanguage) vdl).getFaceletFactory();
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * An external {@code http:} URL must be rejected with
     * {@link InvalidFileException.Reason#DISALLOWED_SCHEME}.
     */
    @Test
    public void testExternalHttpUrlIsRejected() throws Exception
    {
        // Only a live facesContext is needed — no view rendering required
        startViewRequest(null);

        DefaultFaceletFactory factory = getFactory();

        InvalidFileException ex = Assertions.assertThrows(
                InvalidFileException.class,
                () -> factory.resolveURL(facesContext, null,
                        "http://someverybadmaliciouswebsite.com/attack.xhtml"),
                "Expected InvalidFileException for external http: URL");

        Assertions.assertEquals(InvalidFileException.Reason.DISALLOWED_SCHEME, ex.getReason());

        endRequest();
    }

    /**
     * A relative path whose extension is not a configured Facelet suffix must
     * be rejected with {@link InvalidFileException.Reason#INVALID_EXTENSION}.
     */
    @Test
    public void testNonFaceletExtensionIsRejected() throws Exception
    {
        servletContext.addInitParameter(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, ".xhtml");
        startViewRequest(null);

        DefaultFaceletFactory factory = getFactory();

        URL webappRoot = getWebappContextURI().toURL();
        URL source = new URL(webappRoot, "views/index.xhtml");

        InvalidFileException ex = Assertions.assertThrows(
                InvalidFileException.class,
                () -> factory.resolveURL(facesContext, source, "template.html"),
                "Expected InvalidFileException for a .html path");

        Assertions.assertEquals(InvalidFileException.Reason.INVALID_EXTENSION, ex.getReason());

        endRequest();
    }
}
