/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces;

import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseStream;
import java.io.Writer;
import java.io.OutputStream;

public class MockRenderKit extends RenderKit
{
    public void addRenderer(String family, String rendererType, Renderer renderer)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Renderer getRenderer(String family, String rendererType)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResponseStateManager getResponseStateManager()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResponseWriter createResponseWriter(Writer writer, String contentTypeList, String characterEncoding)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResponseStream createResponseStream(OutputStream out)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
