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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlResponseWriterImpl;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;
import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;
import java.util.*;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlRenderKitImpl
    extends RenderKit
{
    private static final Log log = LogFactory.getLog(HtmlRenderKitImpl.class);

    //~ Instance fields ----------------------------------------------------------------------------

    private Map _renderers;
    private ResponseStateManager _responseStateManager;

    //~ Constructors -------------------------------------------------------------------------------

    public HtmlRenderKitImpl()
    {
        _renderers = new HashMap();
        _responseStateManager = new HtmlResponseStateManager();
    }

    //~ Methods ------------------------------------------------------------------------------------

    private String key(String componentFamily, String rendererType)
    {
        return componentFamily + "." + rendererType;
    }

    public Renderer getRenderer(String componentFamily, String rendererType)
    {
        if(componentFamily == null)
        {
            throw new NullPointerException("component family must not be null.");
        }
        if(rendererType == null)
        {
            throw new NullPointerException("renderer type must not be null.");
        }
        Renderer renderer = (Renderer) _renderers.get(key(componentFamily, rendererType));
        if (renderer == null)
        {
            log.warn("Unsupported component-family/renderer-type: " + componentFamily + "/" + rendererType);
        }
        return renderer;
    }

    public void addRenderer(String componentFamily, String rendererType, Renderer renderer)
    {
        if(componentFamily == null)
        {
            log.error("addRenderer: componentFamily = null is not allowed");
            throw new NullPointerException("component family must not be null.");
        }
        if(rendererType == null)
        {
            log.error("addRenderer: rendererType = null is not allowed");
            throw new NullPointerException("renderer type must not be null.");
        }
        if(renderer == null)
        {
            log.error("addRenderer: renderer = null is not allowed");
            throw new NullPointerException("renderer must not be null.");
        }

        String rendererKey = key(componentFamily, rendererType);
        if (_renderers.get(rendererKey) != null) {
            // this is not necessarily an error, but users do need to be
            // very careful about jar processing order when overriding
            // some component's renderer with an alternate renderer.
            log.info("Overwriting renderer with family = " + componentFamily +
               " rendererType = " + rendererType +
               " renderer class = " + renderer.getClass().getName());
        }

        _renderers.put(rendererKey, renderer);

        if (log.isTraceEnabled()) 
            log.trace("add Renderer family = " + componentFamily +
                " rendererType = " + rendererType +
                " renderer class = " + renderer.getClass().getName());
    }

    public ResponseStateManager getResponseStateManager()
    {
        return _responseStateManager;
    }

    public ResponseWriter createResponseWriter(Writer writer,
                                               String contentTypeListString,
                                               String characterEncoding)
    {
        String selectedContentType = HtmlRendererUtils.selectContentType(contentTypeListString);

        if(characterEncoding==null)
        {
            characterEncoding = HtmlRendererUtils.DEFAULT_CHAR_ENCODING;
        }

        return new HtmlResponseWriterImpl(writer, selectedContentType, characterEncoding);
    }

    public ResponseStream createResponseStream(OutputStream outputStream)
    {
        final OutputStream output = outputStream;

        return new ResponseStream()
        {
            public void write(int b) throws IOException
            {
                output.write(b);
            }


            public void write(byte b[]) throws IOException
            {
                output.write(b);
            }


            public void write(byte b[], int off, int len) throws IOException
            {
                output.write(b, off, len);
            }


            public void flush() throws IOException
            {
                output.flush();
            }


            public void close() throws IOException
            {
                output.close();
            }
        };
    }
}
