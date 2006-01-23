/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.myfaces;

import org.apache.myfaces.mock.MockExternalContext;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.render.RenderKit;
import javax.faces.component.UIViewRoot;
import java.util.Iterator;

/**
 * Simple test helper class to allow unit tests to configure
 * mock FacesContext objects as the "current instance".
 * <p>
 * The method FacesContext.setCurrentInstance is protected, and
 * hence cannot be accessed by unit tests wanting to configure
 * a mock object as the value seen by code calling method
 * FacesContext.getCurrentInstance().
 */

public class FacesContextHelper extends FacesContext
{
    private Application application;
    private ExternalContext externalContext;
    private UIViewRoot viewRoot;

    public Application getApplication()
    {
        return application;
    }

    public void setApplication(Application application)
    {
        this.application = application;
    }

    public ExternalContext getExternalContext()
    {
        if(externalContext==null)
        {
            externalContext = new MockExternalContext();
        }

        return externalContext;
    }

    public void setExternalContext(ExternalContext externalContext)
    {
        this.externalContext = externalContext;
    }

    public UIViewRoot getViewRoot()
    {
        return viewRoot;
    }

    public void setViewRoot(UIViewRoot viewRoot)
    {
        this.viewRoot = viewRoot;
    }


    public Iterator getClientIdsWithMessages()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public FacesMessage.Severity getMaximumSeverity()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator getMessages()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator getMessages(String clientId)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RenderKit getRenderKit()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getRenderResponse()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getResponseComplete()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResponseStream getResponseStream()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setResponseStream(ResponseStream responseStream)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResponseWriter getResponseWriter()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addMessage(String clientId, FacesMessage message)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void release()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void renderResponse()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void responseComplete()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void setCurrentInstance(FacesContext other)
    {
        FacesContext.setCurrentInstance(other);
    }
}

