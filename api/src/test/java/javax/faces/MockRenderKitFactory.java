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

import javax.faces.render.RenderKitFactory;
import javax.faces.render.RenderKit;
import javax.faces.context.FacesContext;
import java.util.Iterator;


public class MockRenderKitFactory extends RenderKitFactory
{
    RenderKitFactory delegate;

    public MockRenderKitFactory(RenderKitFactory factory)
    {
        delegate = factory;
    }

    public MockRenderKitFactory()
    {

    }

    public void addRenderKit(String renderKitId, RenderKit renderKit)
    {
        if(delegate != null)
        {
            delegate.addRenderKit(renderKitId, renderKit);
        }
    }

    public RenderKit getRenderKit(FacesContext context, String renderKitId)
    {
        if(delegate != null)
        {
            return delegate.getRenderKit(context, renderKitId);
        }
        return new MockRenderKit();
    }

    public Iterator getRenderKitIds()
    {
        if(delegate != null)
        {
            return delegate.getRenderKitIds();
        }
        return null;    }
}
