/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.myfaces.mock.api;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;
import javax.faces.FacesException;
import java.util.Locale;
import java.io.IOException;


public class MockViewHandler extends ViewHandler
{
    public Locale calculateLocale(FacesContext context)
    {
        return Locale.getDefault();
    }

    public String calculateRenderKitId(FacesContext context)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UIViewRoot createView(FacesContext context, String viewId)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getActionURL(FacesContext context, String viewId)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getResourceURL(FacesContext context, String path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void writeState(FacesContext context) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
