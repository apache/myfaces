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
package org.apache.myfaces.mock.api;

import org.apache.myfaces.mock.api.MockExternalContext;

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

public class MockFacesContextHelper extends FacesContext
{
    private MockFacesContextHelper()
    {

    }

    public Application getApplication()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void setApplication(Application application)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public ExternalContext getExternalContext()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void setExternalContext(ExternalContext externalContext)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public UIViewRoot getViewRoot()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void setViewRoot(UIViewRoot viewRoot)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public Iterator getClientIdsWithMessages()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }


    public FacesMessage.Severity getMaximumSeverity()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public Iterator getMessages()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public Iterator getMessages(String clientId)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public RenderKit getRenderKit()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public boolean getRenderResponse()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public boolean getResponseComplete()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public ResponseStream getResponseStream()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void setResponseStream(ResponseStream responseStream)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public ResponseWriter getResponseWriter()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void addMessage(String clientId, FacesMessage message)
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void release()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void renderResponse()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public void responseComplete()
    {
        throw new UnsupportedOperationException("this class is for setting the current faces-context only");
    }

    public static void setCurrentInstance(FacesContext other)
    {
        FacesContext.setCurrentInstance(other);
    }
}

