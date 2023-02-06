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

package org.apache.myfaces.test.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.ResponseStateManager;

/**
 * <p>Mock implementation of <code>RenderKit</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockRenderKit extends RenderKit
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Return a default instance.</p>
     */
    public MockRenderKit()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    public void setResponseStateManager(ResponseStateManager rsm)
    {
        this.rsm = rsm;
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The set of renderers registered here.</p>
     */
    private Map renderers = new HashMap();
    private ResponseStateManager rsm = new MockResponseStateManager();

    // ------------------------------------------------------- RenderKit Methods

    @Override
    public void addRenderer(String family, String rendererType,
            Renderer renderer)
    {

        if ((family == null) || (rendererType == null) || (renderer == null))
        {
            throw new NullPointerException();
        }
        renderers.put(family + '|' + rendererType, renderer);

    }

    @Override
    public Renderer getRenderer(String family, String rendererType)
    {

        if ((family == null) || (rendererType == null))
        {
            throw new NullPointerException();
        }
        return (Renderer) renderers.get(family + '|' + rendererType);

    }

    @Override
    public ResponseWriter createResponseWriter(Writer writer,
            String contentTypeList, String characterEncoding)
    {

        return new MockResponseWriter(writer, contentTypeList,
                characterEncoding);

    }

    @Override
    public ResponseStream createResponseStream(OutputStream out)
    {

        final OutputStream stream = out;
        return new ResponseStream()
        {

            public void close() throws IOException
            {
                stream.close();
            }

            public void flush() throws IOException
            {
                stream.flush();
            }

            public void write(byte[] b) throws IOException
            {
                stream.write(b);
            }

            public void write(byte[] b, int off, int len) throws IOException
            {
                stream.write(b, off, len);
            }

            public void write(int b) throws IOException
            {
                stream.write(b);
            }
        };
    }

    @Override
    public ResponseStateManager getResponseStateManager()
    {
        return rsm;
    }
}
