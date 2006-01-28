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

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;

// TODO: have real mock implementations with expected/actual support
public class MockFacesContext extends FacesContext
{
    private UIViewRoot _view;
    private Application _application;
    private ResponseWriter _writer;
    private ResponseStream _stream;
    private boolean _responseComplete;
    private boolean _renderResponse;
    private ExternalContext _externalContext;
    
    public MockFacesContext()
    {
        FacesContext.setCurrentInstance(this);
    }

    public void setApplication(Application application)
    {
        _application = application;
    }

    public Application getApplication()
    {
        return _application;
    }

    public java.util.Iterator getClientIdsWithMessages()
    {
        return null;
    }
    
    public void setExternalContext(ExternalContext externalContext)
    {
        _externalContext = externalContext;
    }

    public javax.faces.context.ExternalContext getExternalContext()
    {
        if(_externalContext == null)
        {
            _externalContext = new MockExternalContext();
        }
        return _externalContext;
    }

    public javax.faces.application.FacesMessage.Severity getMaximumSeverity()
    {
        return null;
    }

    public java.util.Iterator getMessages()
    {
        return null;
    }

    public java.util.Iterator getMessages(String clientId)
    {
        return null;
    }

    public javax.faces.render.RenderKit getRenderKit()
    {
        return null;
    }

    public boolean getRenderResponse()
    {
        return _renderResponse;
    }

    public boolean getResponseComplete()
    {
        return _responseComplete;
    }

    public javax.faces.context.ResponseStream getResponseStream()
    {
        return _stream;
    }

    public void setResponseStream(javax.faces.context.ResponseStream stream)
    {
        _stream = stream;
    }

    public javax.faces.context.ResponseWriter getResponseWriter()
    {
        return _writer;
    }

    public void setResponseWriter(javax.faces.context.ResponseWriter writer)
    {
        _writer = writer;
    }

    public javax.faces.component.UIViewRoot getViewRoot()
    {
        return _view;
    }

    public void setViewRoot(javax.faces.component.UIViewRoot view)
    {
        _view = view;
    }

    public void addMessage(String clientId,
            javax.faces.application.FacesMessage message)
    {
    }

    public void release()
    {
    }

    public void renderResponse()
    {
        _renderResponse = true;
    }

    public void responseComplete()
    {
        _responseComplete = true;
    }
}
