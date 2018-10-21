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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextFactory;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.Lifecycle;

/**
 * <p>Mock implementation of <code>FacesContext</code> that includes the semantics
 * added by JavaServer Faces 2.0.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public class MockFacesContext20 extends MockFacesContext12
{

    // ------------------------------------------------------------ Constructors

    public MockFacesContext20()
    {
        super();
        setCurrentInstance(this);
    }

    public MockFacesContext20(ExternalContext externalContext)
    {
        super(externalContext);
    }

    public MockFacesContext20(ExternalContext externalContext,
            Lifecycle lifecycle)
    {
        super(externalContext, lifecycle);
    }

    // ------------------------------------------------------ Instance Variables

    private boolean _processingEvents = true;
    private ExceptionHandler _exceptionHandler = null;
    private PhaseId _currentPhaseId = PhaseId.RESTORE_VIEW;
    private boolean _postback;
    private PartialViewContext _partialViewContext = null;
    private Map<Object, Object> attributes;
    private boolean _validationFailed = false;

    // ----------------------------------------------------- Mock Object Methods   

    public boolean isPostback()
    {
        return _postback;
    }

    public void setPostback(boolean value)
    {
        _postback = value;
    }

    public PhaseId getCurrentPhaseId()
    {
        return _currentPhaseId;
    }

    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        this._currentPhaseId = currentPhaseId;
    }

    public Map<Object, Object> getAttributes()
    {
        if (attributes == null)
        {
            attributes = new HashMap<Object, Object>();
        }
        return attributes;
    }

    public PartialViewContext getPartialViewContext()
    {
        if (_partialViewContext == null)
        {
            //Get through factory finder
            PartialViewContextFactory factory = (PartialViewContextFactory) FactoryFinder
                    .getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
            _partialViewContext = factory.getPartialViewContext(this);
        }
        return _partialViewContext;
    }

    public boolean isProcessingEvents()
    {
        return _processingEvents;
    }

    public void setProcessingEvents(boolean processingEvents)
    {
        _processingEvents = processingEvents;
    }

    public ExceptionHandler getExceptionHandler()
    {
        return _exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        _exceptionHandler = exceptionHandler;
    }

    @SuppressWarnings("unchecked")
    public List<FacesMessage> getMessageList()
    {
        if (messages == null)
        {
            return Collections.unmodifiableList(Collections
                    .<FacesMessage> emptyList());
        }

        List<FacesMessage> lst = new ArrayList<FacesMessage>();
        for (List<FacesMessage> curLst : ((Map<String, List<FacesMessage>>) messages)
                .values())
        {
            lst.addAll(curLst);
        }

        return Collections.unmodifiableList(lst);
    }

    @SuppressWarnings("unchecked")
    public List<FacesMessage> getMessageList(String clientId)
    {
        if (messages == null || !messages.containsKey(clientId))
        {
            return Collections.unmodifiableList(Collections
                    .<FacesMessage> emptyList());
        }

        return ((Map<String, List<FacesMessage>>) messages).get(clientId);
    }

    public boolean isValidationFailed()
    {
        return _validationFailed;
    }

    public void validationFailed()
    {
        _validationFailed = true;
    }

    // ------------------------------------------------- ExternalContext Methods

}
