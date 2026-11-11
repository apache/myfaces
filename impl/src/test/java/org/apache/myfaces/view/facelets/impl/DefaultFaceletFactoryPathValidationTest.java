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

import javax.faces.application.ProjectStage;
import org.apache.myfaces.context.InvalidFileException;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

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

        try
        {
            factory.resolveURL(facesContext, null, "http://someverybadmaliciouswebsite.com/attack.xhtml");
            Assert.fail("Expected InvalidFileException for external http: URL");
        }
        catch (InvalidFileException ex)
        {
            Assert.assertEquals("Rejection reason should be DISALLOWED_SCHEME",
                    InvalidFileException.Reason.DISALLOWED_SCHEME, ex.getReason());
        }
    }

    /**
     * A relative path whose extension is not a configured Facelet suffix must
     * be rejected with {@link InvalidFileException.Reason#INVALID_EXTENSION}.
     *
     * <p>The default Facelet suffixes are {@code .xhtml}, {@code .html}, {@code .jsf} and
     * {@code .faces}; {@code .properties} is not allowed.
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

        try
        {
            factory.resolveURL(facesContext, source, "template.properties");
            Assert.fail("Expected InvalidFileException for a .properties path");
        }
        catch (InvalidFileException ex)
        {
            Assert.assertEquals("Rejection reason should be INVALID_EXTENSION",
                    InvalidFileException.Reason.INVALID_EXTENSION, ex.getReason());
        }
    }

    /**
     * With no source URL, a relative traversal path must still be rejected (the guard must not
     * fail open when {@code source == null}).
     */
    @Test
    public void testRelativeTraversalWithNullSourceIsRejected() throws Exception
    {
        setProjectStage(ProjectStage.Development);

        try
        {
            getFactory().resolveURL(facesContext, null, "../../etc/passwd.xhtml");
            Assert.fail("Expected IOException for null-source traversal");
        }
        catch (java.io.IOException ex)
        {
            System.out.println("null-source rejection: " + ex);
        }
    }


    /**
     * Reproducer: a facelet packaged in {@code WEB-INF/lib/templates.jar!/META-INF/resources/} that
     * includes a sibling with a relative path (<code>&lt;ui:include src="breadcrumbs.xhtml"/&gt;</code>)
     * must not be rejected as {@code PATH_TRAVERSAL}.
     */
    @Test
    public void testRelativeIncludeBetweenSiblingsInJarIsAllowed() throws Exception
    {
        setProjectStage(ProjectStage.Development);

        java.net.URL source = createTemplatesJar("includeOnly.xhtml");

        java.net.URL resolved = getFactory().resolveURL(facesContext, source, "breadcrumbs.xhtml");

        Assert.assertTrue("Unexpected URL: " + resolved,
                resolved.toExternalForm().endsWith("!/META-INF/resources/breadcrumbs.xhtml"));
    }

    /**
     * A relative path that climbs out of the jar's {@code META-INF/resources} root must still be rejected.
     */
    @Test
    public void testRelativePathEscapingJarResourceRootIsRejected() throws Exception
    {
        setProjectStage(ProjectStage.Development);

        java.net.URL source = createTemplatesJar("includeOnly.xhtml");

        try
        {
            getFactory().resolveURL(facesContext, source, "../../escaped.xhtml");
            Assert.fail("Expected InvalidFileException");
        }
        catch (InvalidFileException ex)
        {
            Assert.assertEquals(InvalidFileException.Reason.PATH_TRAVERSAL, ex.getReason());
        }
    }

    /** Creates templates.jar with META-INF/resources/{includeOnly,breadcrumbs}.xhtml; returns URL of 'entry'. */
    private java.net.URL createTemplatesJar(String entry) throws Exception
    {
        java.io.File jar = java.io.File.createTempFile("templates", ".jar");
        jar.deleteOnExit();
        try (java.util.jar.JarOutputStream out = new java.util.jar.JarOutputStream(
                new java.io.FileOutputStream(jar)))
        {
            for (String name : new String[] {"includeOnly.xhtml", "breadcrumbs.xhtml"})
            {
                out.putNextEntry(new java.util.jar.JarEntry("META-INF/resources/" + name));
                out.write("<ui:composition xmlns:ui=\"http://xmlns.jcp.org/jsf/facelets\"/>"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return new java.net.URL("jar:" + jar.toURI().toURL() + "!/META-INF/resources/" + entry);
    }
}
