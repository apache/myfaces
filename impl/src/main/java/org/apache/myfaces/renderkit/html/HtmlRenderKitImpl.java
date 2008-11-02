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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderKit;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlResponseWriterImpl;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderKit(renderKitId = "HTML_BASIC")
public class HtmlRenderKitImpl extends RenderKit
{
    private static final Log log = LogFactory.getLog(HtmlRenderKitImpl.class);

    // ~ Instance fields ----------------------------------------------------------------------------

    private Map<String, Renderer> _renderers;
    private ResponseStateManager _responseStateManager;
    private Map<String,Set<String>> _families;

    // ~ Constructors -------------------------------------------------------------------------------

    public HtmlRenderKitImpl()
    {
        _renderers = new HashMap<String, Renderer>();
        _responseStateManager = new HtmlResponseStateManager();
        _families = new HashMap<String, Set<String> >();
    }

    // ~ Methods ------------------------------------------------------------------------------------

    private String key(String componentFamily, String rendererType)
    {
        return componentFamily + "." + rendererType;
    }

    @Override
    public Renderer getRenderer(String componentFamily, String rendererType)
    {
        if (componentFamily == null)
        {
            throw new NullPointerException("component family must not be null.");
        }
        if (rendererType == null)
        {
            throw new NullPointerException("renderer type must not be null.");
        }
        Renderer renderer = _renderers.get(key(componentFamily, rendererType));
        if (renderer == null)
        {
            log.warn("Unsupported component-family/renderer-type: " + componentFamily + "/" + rendererType);
        }
        return renderer;
    }

    @Override
    public void addRenderer(String componentFamily, String rendererType, Renderer renderer)
    {
        if (componentFamily == null)
        {
            log.error("addRenderer: componentFamily = null is not allowed");
            throw new NullPointerException("component family must not be null.");
        }
        if (rendererType == null)
        {
            log.error("addRenderer: rendererType = null is not allowed");
            throw new NullPointerException("renderer type must not be null.");
        }
        if (renderer == null)
        {
            log.error("addRenderer: renderer = null is not allowed");
            throw new NullPointerException("renderer must not be null.");
        }

        String rendererKey = key(componentFamily, rendererType);
        if (_renderers.get(rendererKey) != null)
        {
            // this is not necessarily an error, but users do need to be
            // very careful about jar processing order when overriding
            // some component's renderer with an alternate renderer.
            log.debug("Overwriting renderer with family = " + componentFamily + " rendererType = " + rendererType
                    + " renderer class = " + renderer.getClass().getName());
        }

        if (_families.get(componentFamily) == null)
        {
            Set<String> rendererTypes = new HashSet<String>();
            rendererTypes.add(rendererType);
            _families.put(componentFamily, rendererTypes);
        }
        else
        {
            _families.get(componentFamily).add(rendererType);
        }
        
        _renderers.put(rendererKey, renderer);

        if (log.isTraceEnabled())
            log.trace("add Renderer family = " + componentFamily + " rendererType = " + rendererType
                    + " renderer class = " + renderer.getClass().getName());
    }

    @Override
    public ResponseStateManager getResponseStateManager()
    {
        return _responseStateManager;
    }
    
    /**
     * @since JSF 2.0
     */
    @Override
    public Iterator<String> getComponentFamilies()
    {
        return _families.keySet().iterator();
    }
    
    /**
     * @since JSF 2.0
     */
    @Override
    public Iterator<String> getRendererTypes(String componentFamily)
    {
        return _families.get(componentFamily).iterator();
    }

    @Override
    public ResponseWriter createResponseWriter(Writer writer, String contentTypeListString, String characterEncoding)
    {
        String selectedContentType = HtmlRendererUtils.selectContentType(contentTypeListString);

        if (characterEncoding == null)
        {
            characterEncoding = HtmlRendererUtils.DEFAULT_CHAR_ENCODING;
        }

        return new HtmlResponseWriterImpl(writer, selectedContentType, characterEncoding);
    }

    @Override
    public ResponseStream createResponseStream(OutputStream outputStream)
    {
        return new MyFacesResponseStream(outputStream);
    }

    private static class MyFacesResponseStream extends ResponseStream
    {
        private OutputStream output;

        public MyFacesResponseStream(OutputStream output)
        {
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException
        {
            output.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException
        {
            output.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException
        {
            output.write(b, off, len);
        }

        @Override
        public void flush() throws IOException
        {
            output.flush();
        }

        @Override
        public void close() throws IOException
        {
            output.close();
        }
    }
}
