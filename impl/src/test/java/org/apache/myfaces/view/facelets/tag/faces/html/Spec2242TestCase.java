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
package org.apache.myfaces.view.facelets.tag.faces.html;

import java.io.StringWriter;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.application.ResourceHandlerImpl;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.resource.ResourceHandlerCache;
import org.apache.myfaces.resource.ResourceHandlerCache.ResourceValue;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Faces 5.0, spec issue 2242: resource names may carry a query string, which is not part
 * of the resource identifier but appended to {@link Resource#getRequestPath()}.
 */
public class Spec2242TestCase extends AbstractFaceletTestCase
{
    @Test
    public void testCreateResource()
    {
        ResourceHandler handler = application.getResourceHandler();

        Resource plain = handler.createResource("spec2242.png", "spec2242");
        Resource withQuery = handler.createResource("spec2242.png?v=1", "spec2242");

        Assertions.assertNotNull(plain);
        Assertions.assertNotNull(withQuery);
        Assertions.assertEquals("spec2242.png", withQuery.getResourceName());
        Assertions.assertEquals("spec2242", withQuery.getLibraryName());
        Assertions.assertEquals(plain.getContentType(), withQuery.getContentType());
        Assertions.assertEquals(plain.getRequestPath() + "&v=1", withQuery.getRequestPath());
        Assertions.assertTrue(withQuery.getRequestPath().endsWith("/jakarta.faces.resource/spec2242.png?ln=spec2242&v=1"),
                withQuery.getRequestPath());

        // without library, the query string becomes the whole metadata
        Resource noLibrary = handler.createResource("testResource.xhtml?v=1&w=2");
        Assertions.assertNotNull(noLibrary);
        Assertions.assertEquals("testResource.xhtml", noLibrary.getResourceName());
        Assertions.assertTrue(noLibrary.getRequestPath().endsWith("/jakarta.faces.resource/testResource.xhtml?v=1&w=2"),
                noLibrary.getRequestPath());

        // empty query string is ignored
        Assertions.assertEquals(plain.getRequestPath(),
                handler.createResource("spec2242.png?", "spec2242").getRequestPath());

        // only a query string is not a valid name
        Assertions.assertNull(handler.createResource("?v=1", "spec2242"));

        // createResourceFromId
        Resource fromId = handler.createResourceFromId("spec2242/spec2242.png?v=1");
        Assertions.assertNotNull(fromId);
        Assertions.assertEquals("spec2242.png", fromId.getResourceName());
        Assertions.assertEquals(plain.getRequestPath() + "&v=1", fromId.getRequestPath());
    }

    @Test
    public void testResourceHandlerCache()
    {
        // MyFaces only enables the resource cache in ProjectStage.Production
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.name());
        externalContext.getApplicationMap().remove(MyfacesConfig.class.getName());

        CacheExposingResourceHandler handler = new CacheExposingResourceHandler();
        ResourceHandlerCache cache = handler.getResourceHandlerCache();
        Assertions.assertTrue(cache.isResourceHandlerCacheEnabled());

        Resource v1 = handler.createResource("spec2242.png?v=1", "spec2242");
        String contentType = v1.getContentType();

        // the cache only knows the resource name without query string
        Assertions.assertTrue(cache.containsResource("spec2242.png", "spec2242", contentType, null));
        Assertions.assertFalse(cache.containsResource("spec2242.png?v=1", "spec2242", contentType, null));

        // the cached request path must not contain the query string
        ResourceValue cached = cache.getResource("spec2242.png", "spec2242", contentType, null);
        Assertions.assertNotNull(cached.getCachedInfo());
        String plainPath = cached.getCachedInfo().getRequestPath();
        Assertions.assertFalse(plainPath.contains("v=1"), plainPath);

        // all query string variants and the plain resource share the cache entry
        Resource v2 = handler.createResource("spec2242.png?v=2", "spec2242");
        Resource plain = handler.createResource("spec2242.png", "spec2242");
        Assertions.assertSame(cached, cache.getResource("spec2242.png", "spec2242", contentType, null));
        Assertions.assertEquals(plainPath, plain.getRequestPath());
        Assertions.assertEquals(plainPath + "&v=1", v1.getRequestPath());
        Assertions.assertEquals(plainPath + "&v=2", v2.getRequestPath());
    }

    private static class CacheExposingResourceHandler extends ResourceHandlerImpl
    {
        @Override
        public ResourceHandlerCache getResourceHandlerCache()
        {
            return super.getResourceHandlerCache();
        }
    }

    @Test
    public void testRendererTypeForResourceName()
    {
        ResourceHandler handler = application.getResourceHandler();
        Assertions.assertEquals("jakarta.faces.resource.Script",
                handler.getRendererTypeForResourceName("foo.js?v=1"));
        Assertions.assertEquals("jakarta.faces.resource.Stylesheet",
                handler.getRendererTypeForResourceName("foo.css?v=1"));
    }

    @Test
    public void testRenderedMarker()
    {
        assertRenderedMarker(application.getResourceHandler());
    }

    @Test
    public void testRenderedMarkerDefaultImplementation()
    {
        assertRenderedMarker(Mockito.mock(ResourceHandler.class, Mockito.CALLS_REAL_METHODS));
    }

    private void assertRenderedMarker(ResourceHandler handler)
    {
        handler.markResourceRendered(facesContext, "jquery.js?v=3", "lib");
        Assertions.assertTrue(handler.isResourceRendered(facesContext, "jquery.js", "lib"));
        Assertions.assertTrue(handler.isResourceRendered(facesContext, "jquery.js?v=4", "lib"));
        Assertions.assertFalse(handler.isResourceRendered(facesContext, "jquery-ui.js", "lib"));
        Assertions.assertFalse(handler.isResourceRendered(facesContext, "jquery.js", "otherlib"));

        handler.markResourceRendered(facesContext, "app.js", null);
        Assertions.assertTrue(handler.isResourceRendered(facesContext, "app.js?v=1", null));

        handler.markResourceRendered(facesContext, null, null);
        Assertions.assertTrue(handler.isResourceRendered(facesContext, null, null));
    }

    @Test
    public void testRendering() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSpec2242.xhtml");

        String plainScriptPath = application.getResourceHandler()
                .createResource("spec2242.js", "spec2242").getRequestPath();
        String plainImagePath = application.getResourceHandler()
                .createResource("spec2242.png", "spec2242").getRequestPath();

        StringWriter sw = new StringWriter();
        ResponseWriter writer = new HtmlResponseWriterImpl(sw, "text/html", "UTF-8");
        facesContext.setResponseWriter(writer);
        root.encodeAll(facesContext);
        sw.flush();
        String output = sw.toString();

        // h:outputScript, only rendered once regardless of the query string
        String expectedScriptSrc = "src=\"" + plainScriptPath.replace("&", "&amp;") + "&amp;v=1\"";
        Assertions.assertTrue(output.contains(expectedScriptSrc), output);
        Assertions.assertFalse(output.contains("v=2"), output);
        Assertions.assertEquals(1, output.split("spec2242\\.js", -1).length - 1, output);

        // h:outputStylesheet, query string with multiple params
        Assertions.assertTrue(output.contains("/jakarta.faces.resource/spec2242.css?ln=spec2242&amp;v=1&amp;w=2\""), output);

        // h:graphicImage
        String expectedImageSrc = "src=\"" + plainImagePath.replace("&", "&amp;") + "&amp;v=1\"";
        Assertions.assertTrue(output.contains(expectedImageSrc), output);

        // #{resource[...]}, the library colon is searched before the query string only
        UIComponent el1 = root.findComponent("el1");
        UIComponent el2 = root.findComponent("el2");
        Assertions.assertEquals(plainImagePath + "&v=1", ((ValueHolder) el1).getValue());
        Assertions.assertEquals(plainImagePath + "&v=1:2", ((ValueHolder) el2).getValue());
    }
}
