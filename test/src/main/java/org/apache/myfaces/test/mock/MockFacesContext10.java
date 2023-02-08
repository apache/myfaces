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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

/**
 * <p>Mock implementation of <code>FacesContext</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */
public abstract class MockFacesContext10 extends FacesContext
{

    // ------------------------------------------------------------ Constructors

    public MockFacesContext10()
    {
        super();
        setCurrentInstance(this);
    }

    public MockFacesContext10(ExternalContext externalContext)
    {
        setExternalContext(externalContext);
        setCurrentInstance(this);
    }

    public MockFacesContext10(ExternalContext externalContext, Lifecycle lifecycle)
    {
        this(externalContext);
        this.lifecycle = lifecycle;
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Set the <code>Application</code> instance for this instance.</p>
     *
     * @param application The new Application
     */
    public void setApplication(Application application)
    {

        this.application = application;

    }

    /**
     * <p>Set the <code>ExternalContext</code> instance for this instance.</p>
     *
     * @param externalContext The new ExternalContext
     */
    public void setExternalContext(ExternalContext externalContext)
    {

        this.externalContext = externalContext;

    }

    /**
     * <p>Set the <code>FacesContext</code> instance for this instance.</p>
     *
     * @param facesContext The new FacesContext
     */
    public static void setCurrentInstance(FacesContext facesContext)
    {

        FacesContext.setCurrentInstance(facesContext);

    }

    // ------------------------------------------------------ Instance Variables

    private Application application = null;
    private ExternalContext externalContext = null;
    private Lifecycle lifecycle = null;
    protected Map messages = new HashMap(); // needs to be accessed in subclass MockFacesContext20
    private boolean renderResponse = false;
    private boolean responseComplete = false;
    private ResponseStream responseStream = null;
    private ResponseWriter responseWriter = null;
    private UIViewRoot viewRoot = null;

    // ---------------------------------------------------- FacesContext Methods

    @Override
    public Application getApplication()
    {

        return this.application;

    }

    @Override
    public Iterator getClientIdsWithMessages()
    {

        return messages.keySet().iterator();

    }

    @Override
    public ExternalContext getExternalContext()
    {

        return this.externalContext;

    }

    @Override
    public Severity getMaximumSeverity()
    {

        Severity severity = null;
        Iterator messages = getMessages();
        while (messages.hasNext())
        {
            FacesMessage message = (FacesMessage) messages.next();
            if (severity == null)
            {
                severity = message.getSeverity();
            }
            else if (message.getSeverity().getOrdinal() > severity.getOrdinal())
            {
                severity = message.getSeverity();
            }
        }
        return severity;

    }

    @Override
    public Iterator getMessages()
    {

        ArrayList results = new ArrayList();
        Iterator clientIds = messages.keySet().iterator();
        while (clientIds.hasNext())
        {
            String clientId = (String) clientIds.next();
            results.addAll((List) messages.get(clientId));
        }
        return results.iterator();

    }

    @Override
    public Iterator getMessages(String clientId)
    {

        List list = (List) messages.get(clientId);
        if (list == null)
        {
            list = new ArrayList();
        }
        return list.iterator();

    }

    @Override
    public RenderKit getRenderKit()
    {

        UIViewRoot vr = getViewRoot();
        if (vr == null)
        {
            return null;
        }
        String renderKitId = vr.getRenderKitId();
        if (renderKitId == null)
        {
            return null;
        }
        RenderKitFactory rkFactory = (RenderKitFactory) FactoryFinder
                .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        return rkFactory.getRenderKit(this, renderKitId);

    }

    @Override
    public boolean getRenderResponse()
    {

        return this.renderResponse;

    }

    @Override
    public boolean getResponseComplete()
    {

        return this.responseComplete;

    }

    @Override
    public ResponseStream getResponseStream()
    {

        return this.responseStream;

    }

    @Override
    public void setResponseStream(ResponseStream responseStream)
    {

        this.responseStream = responseStream;

    }

    @Override
    public ResponseWriter getResponseWriter()
    {

        return this.responseWriter;

    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter)
    {

        this.responseWriter = responseWriter;

    }

    @Override
    public UIViewRoot getViewRoot()
    {

        return this.viewRoot;

    }

    @Override
    public void setViewRoot(UIViewRoot viewRoot)
    {

        this.viewRoot = viewRoot;

    }

    @Override
    public void addMessage(String clientId, FacesMessage message)
    {

        if (message == null)
        {
            throw new NullPointerException();
        }
        List list = (List) messages.get(clientId);
        if (list == null)
        {
            list = new ArrayList();
            messages.put(clientId, list);
        }
        list.add(message);

    }

    @Override
    public void release()
    {

        application = null;
        externalContext = null;
        messages.clear();
        renderResponse = false;
        responseComplete = false;
        responseStream = null;
        responseWriter = null;
        viewRoot = null;
        setCurrentInstance(null);

    }

    @Override
    public void renderResponse()
    {

        this.renderResponse = true;

    }

    @Override
    public void responseComplete()
    {

        this.responseComplete = true;

    }

}
