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
package org.apache.myfaces.context.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.el.unified.FacesELContext;

/**
 * A FacesContext implementation which will be set as the current instance
 * during container startup and shutdown and which provides a basic set of
 * FacesContext functionality.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupFacesContextImpl extends FacesContext
{
    
    public static final String EXCEPTION_TEXT = "This method is not supported during ";

    private Application _application;
    private ExternalContext _externalContext;
    private ReleaseableExternalContext _defaultExternalContext;
    private UIViewRoot _viewRoot;
    private RenderKitFactory _renderKitFactory;
    private boolean _released = false;
    private ELContext _elContext;
    private Map<Object, Object> _attributes = null;
    private boolean _processingEvents = true;
    private ExceptionHandler _exceptionHandler = null;
    // Variables used to cache values
    private RenderKit _cachedRenderKit = null;
    private String _cachedRenderKitId = null;
    
    private boolean _startup;
    
    public StartupFacesContextImpl(
            ExternalContext externalContext, 
            ReleaseableExternalContext defaultExternalContext,
            ExceptionHandler exceptionHandler,
            boolean startup)
    {
        _startup = startup;   
        _externalContext = externalContext;
        _defaultExternalContext = defaultExternalContext;
        _exceptionHandler = exceptionHandler;
        // this FacesContext impl is now the current instance
        // note that because this method is protected, it has to be called from here
        FacesContext.setCurrentInstance(this);
    }

    // ~ Methods which are valid to be called during startup and shutdown------
    
    @Override
    public ExceptionHandler getExceptionHandler()
    {
        return _exceptionHandler;
    }
    
    /**
     * If called during application startup or shutdown, this method returns an
     * ExternalContext instance with the special behaviors indicated in the 
     * javadoc for that class. Methods document as being valid to call during 
     * application startup or shutdown must be supported.
     */
    @Override
    public final ExternalContext getExternalContext()
    {
        assertNotReleased();

        return (ExternalContext) _externalContext;
    }

    /**
     * If called during application startup or shutdown, this method returns
     * the correct current Application instance.
     */
    @Override
    public Application getApplication()
    {
        assertNotReleased();
        
        if (_application == null)
        {
            _application = ((ApplicationFactory) FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY)).getApplication();
        }
        
        return _application;
    }

    @Override
    public final RenderKit getRenderKit()
    {
        assertNotReleased();

        if (getViewRoot() == null)
        {
            return null;
        }

        String renderKitId = getViewRoot().getRenderKitId();

        if (renderKitId == null)
        {
            return null;
        }
        
        if (_cachedRenderKitId == null || !renderKitId.equals(_cachedRenderKitId))
        {
            _cachedRenderKitId = renderKitId;
            if (_renderKitFactory == null)
            {
                _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            }
            _cachedRenderKit = _renderKitFactory.getRenderKit(this, renderKitId);
        }
        
        return _cachedRenderKit;
    }
    
    /**
     * If called during application startup or shutdown, this method returns a 
     * new UIViewRoot with its locale set to Locale.getDefault().
     */
    @Override
    public final void setViewRoot(final UIViewRoot viewRoot)
    {
        assertNotReleased();

        if (viewRoot == null)
        {
            throw new NullPointerException("viewRoot");
        }
        // If the current UIViewRoot is non-null, and calling equals() on the argument root, passing the current UIViewRoot returns false
        // the clear method must be called on the Map returned from UIViewRoot.getViewMap().
        if (_viewRoot != null && !_viewRoot.equals(viewRoot))
        {
            //call getViewMap(false) to prevent unnecessary map creation
            Map<String, Object> viewMap = _viewRoot.getViewMap(false);
            if (viewMap != null)
            {
                viewMap.clear();
            }
        }
        _viewRoot = viewRoot;
    }

    @Override
    public final UIViewRoot getViewRoot()
    {
        assertNotReleased();

        return _viewRoot;
    }

    @Override
    public void release()
    {
        assertNotReleased();

        if (_defaultExternalContext != null)
        {
            _defaultExternalContext.release();
            _defaultExternalContext = null;
        }
        _externalContext = null;

        /*
         * Spec JSF 2 section getAttributes when release is called the attributes map must!!! be cleared!
         * 
         * (probably to trigger some clearance methods on possible added entries before nullifying everything)
         */
        if (_attributes != null)
        {
            _attributes.clear();
            _attributes = null;
        }

        _application = null;
        _viewRoot = null;
        _cachedRenderKit = null;
        _cachedRenderKitId = null;

        _released = true;
        FacesContext.setCurrentInstance(null);
    }
        
    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        _exceptionHandler = exceptionHandler;
    }

    @Override
    public final ELContext getELContext()
    {
        assertNotReleased();

        if (_elContext != null)
        {
            return _elContext;
        }

        _elContext = new FacesELContext(getApplication().getELResolver(), this);

        ELContextEvent event = new ELContextEvent(_elContext);
        for (ELContextListener listener : getApplication().getELContextListeners())
        {
            listener.contextCreated(event);
        }

        return _elContext;
    }

    /**
     * Returns a mutable map of attributes associated with this faces context when
     * {@link javax.faces.context.FacesContext.release} is called the map must be cleared!
     * 
     * Note this map is not associated with the request map the request map still is accessible via the
     * {@link javax.faces.context.FacesContext.getExternalContext.getRequestMap} method!
     * 
     * Also the scope is different to the request map, this map has the scope of the context, and is cleared once the
     * release method on the context is called!
     * 
     * Also the map does not cause any events according to the spec!
     * 
     * @since JSF 2.0
     * 
     * @throws IllegalStateException
     *             if the current context already is released!
     */
    @Override
    public Map<Object, Object> getAttributes()
    {
        assertNotReleased();

        if (_attributes == null)
        {
            _attributes = new HashMap<Object, Object>();
        }
        return _attributes;
    }
    
    @Override
    public boolean isProcessingEvents()
    {
        assertNotReleased();
        
        return _processingEvents;
    }
    
    @Override
    public void setProcessingEvents(boolean processingEvents)
    {
        assertNotReleased();
        
        _processingEvents = processingEvents;
    }
    
    // ~ Methods which are not valid to be called during startup and shutdown, but we implement anyway ------

    // Note that isProjectStage() also is valid to be called, but this method
    // is already implemented in FacesContext class.
    
    // ~ Methods which can be called during startup and shutdown, but are not
    //   officially supported by the spec--------------------------------------
    
    
    // ~ Methods which are unsupported during startup and shutdown-------------

    @Override
    public final FacesMessage.Severity getMaximumSeverity()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
    
    @Override
    public List<FacesMessage> getMessageList()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final Iterator<FacesMessage> getMessages()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
    
    
    @Override
    public final Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final Iterator<FacesMessage> getMessages(final String clientId)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final void addMessage(final String clientId, final FacesMessage message)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public PartialViewContext getPartialViewContext()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
    
    @Override
    public boolean isPostback()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
    
    @Override
    public void validationFailed()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isValidationFailed()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final void renderResponse()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final void responseComplete()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public PhaseId getCurrentPhaseId()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
   
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
        
    @Override
    public final boolean getRenderResponse()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final boolean getResponseComplete()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final void setResponseStream(final ResponseStream responseStream)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final ResponseStream getResponseStream()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final void setResponseWriter(final ResponseWriter responseWriter)
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public final ResponseWriter getResponseWriter()
    {
        assertNotReleased();
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }
    
    // ~ private Methods ------------------------------------------------------
    
    /**
     * Returns startup or shutdown as String according to the field _startup.
     * @return
     */
    private String _getTime()
    {
        return _startup ? "startup" : "shutdown";
    }
    
    /**
     * has to be thrown in many of the methods if the method is called after
     * the instance has been released!
     */
    private final void assertNotReleased()
    {
        if (_released)
        {
            throw new IllegalStateException(
                    "Error the FacesContext is already released!");
        }
    }
}
