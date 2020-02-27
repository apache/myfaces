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
package org.apache.myfaces.renderkits;

import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;

import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.ResponseStateManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author martin.haimberger
 */
public class OwnRenderKitImpl
        extends RenderKit {

    RenderKit renderKit = new HtmlRenderKitImpl();

    public Renderer getRenderer(String componentFamily, String rendererType) {
        OwnRenderkitTest.SetIsOwnRenderKit();
        return renderKit.getRenderer(componentFamily, rendererType);
    }

    public void addRenderer(String componentFamily, String rendererType, Renderer renderer) {
        renderKit.addRenderer(componentFamily, rendererType, renderer);
    }

    public ResponseStateManager getResponseStateManager() {
        return renderKit.getResponseStateManager();
    }

    public ResponseWriter createResponseWriter(Writer writer,
                                               String contentTypeListString,
                                               String characterEncoding) {


        return renderKit.createResponseWriter(writer, contentTypeListString, characterEncoding);
    }

    public ResponseStream createResponseStream(OutputStream outputStream) {
        final OutputStream output = outputStream;

        return new ResponseStream() {
            public void write(int b) throws IOException {
                output.write(b);
            }


            public void write(byte b[]) throws IOException {
                output.write(b);
            }


            public void write(byte b[], int off, int len) throws IOException {
                output.write(b, off, len);
            }


            public void flush() throws IOException {
                output.flush();
            }


            public void close() throws IOException {
                output.close();
            }
        };
    }


}
