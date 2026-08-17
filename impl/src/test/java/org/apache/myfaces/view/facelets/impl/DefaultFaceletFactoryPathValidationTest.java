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

import jakarta.faces.application.ProjectStage;
import org.apache.myfaces.context.InvalidFileException;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link DefaultFaceletFactory#resolveURL(jakarta.faces.context.FacesContext, java.net.URL, String)}
 * correctly rejects non-Facelet extensions and external URLs when running outside the
 * {@code UnitTest} ProjectStage (which normally bypasses validation).
 *
 * <p>Tests switch the stage to {@code Development} before each assertion so the
 * validation code-path inside {@code resolveURL} is active.</p>
 */
public class DefaultFaceletFactoryPathValidationTest extends FaceletTestCase
{
    private DefaultFaceletFactory getFactory()
    {
        return (DefaultFaceletFactory) vdl.getFaceletFactory();
    }

    /**
     * An external {@code http:} URL must be rejected with
     * {@link InvalidFileException.Reason#DISALLOWED_SCHEME} even before the
     * stage check, because scheme validation happens first.
     */
    @Test
    public void testExternalHttpUrlIsRejected() throws Exception
    {
        // Switch away from UnitTest so the validation block is not skipped
        setProjectStage(ProjectStage.Development);

        DefaultFaceletFactory factory = getFactory();

        InvalidFileException ex = Assertions.assertThrows(
                InvalidFileException.class,
                () -> factory.resolveURL(facesContext, null, "http://someverybadmaliciouswebsite.com/attack.xhtml"),
                "Expected InvalidFileException for external http: URL");

        Assertions.assertEquals(InvalidFileException.Reason.DISALLOWED_SCHEME, ex.getReason(),
                "Rejection reason should be DISALLOWED_SCHEME");
    }

    /**
     * A relative path whose extension is not a configured Facelet suffix must
     * be rejected with {@link InvalidFileException.Reason#INVALID_EXTENSION}.
     *
     * <p>The default Facelet suffix is {@code .xhtml}; {@code .html} is not allowed.
     * A non-null {@code source} URL is supplied so the code resolves via
     * {@code new URL(source, path)} — pure string arithmetic, no I/O — and reaches
     * the {@code mappingAllowed} check.</p>
     */
    @Test
    public void testNonFaceletExtensionIsRejected() throws Exception
    {
        setProjectStage(ProjectStage.Development);

        DefaultFaceletFactory factory = getFactory();
        // Use the webapp context root as source so the resolved URL stays within
        // the application base (bypassing PATH_TRAVERSAL) and the extension check runs.
        java.net.URL base = getContext().toURL();
        java.net.URL source = new java.net.URL(base, "views/index.xhtml");

        InvalidFileException ex = Assertions.assertThrows(
                InvalidFileException.class,
                () -> factory.resolveURL(facesContext, source, "template.html"),
                "Expected InvalidFileException for a .html path");

        Assertions.assertEquals(InvalidFileException.Reason.INVALID_EXTENSION, ex.getReason(),
                "Rejection reason should be INVALID_EXTENSION");
    }
}
