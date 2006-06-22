/*
 * Copyright 2004 The Apache Software Foundation.
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
package javax.faces.render;

import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseStream;
import java.io.Writer;
import java.io.OutputStream;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class RenderKit
{
    public abstract void addRenderer(String family,
                                     String rendererType,
                                     Renderer renderer);

    public abstract Renderer getRenderer(String family,
                                         String rendererType);

    public abstract ResponseStateManager getResponseStateManager();

    public abstract ResponseWriter createResponseWriter(Writer writer,
                                                        String contentTypeList,
                                                        String characterEncoding);

    public abstract ResponseStream createResponseStream(OutputStream out);

}
