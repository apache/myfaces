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

import javax.faces.context.FacesContext;
import java.util.Iterator;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class RenderKitFactory
{
    public static final String HTML_BASIC_RENDER_KIT = "HTML_BASIC";

    public abstract void addRenderKit(String renderKitId,
                                      RenderKit renderKit);

    public abstract RenderKit getRenderKit(FacesContext context,
                                           String renderKitId);

    public abstract Iterator<String> getRenderKitIds();
}
