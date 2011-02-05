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

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.el.unified.FacesELContext;

/**
 * Provides a base implementation of the FacesContext for the use
 * in FacesContextImpl and StartupFacesContextImpl.
 * 
 * @author Jakob Korherr (latest modification by $Author: jakobk $)
 * @version $Revision: 963629 $ $Date: 2010-07-13 04:29:07 -0500 (Mar, 13 Jul 2010) $
 */
public abstract class FacesContextImplBase extends FacesContext
{

    private Application _application;
    private ExternalContext _externalContext;
    private ReleaseableExternalContext _defaultExternalContext;
    private UIViewRoot _viewRoot;
    private RenderKitFactory _renderKitFactory;
    private ELContext _elContext;
    
    // Variables used to cache values
    private RenderKit _cachedRenderKit = null;
    private String _cachedRenderKitId = null;
    
    protected boolean _released = false;
    
    /**
     * Base constructor.
     * Calls FacesContext.setCurrentInstance(this);
     */
    public FacesContextImplBase(final ExternalContext externalContext,
            final ReleaseableExternalContext defaultExternalContext)
    {
        _externalContext = externalContext;
        _defaultExternalContext = defaultExternalContext;
        
        // this FacesContext impl is now the current instance
        // note that because this method is protected, it has to be called from here
        FacesContext.setCurrentInstance(this);
    }
    
    /**
     * Releases the instance fields on FacesContextImplBase.
     * Must be called by sub-classes, when overriding it!
     */
    @Override
    public void release()
    {
        if (_defaultExternalContext != null)
        {
            _defaultExternalContext.release();
            _defaultExternalContext = null;
        }
        
        _application = null;
        _externalContext = null;
        _viewRoot = null;
        _renderKitFactory = null;
        _elContext = null;
        _cachedRenderKit = null;
        _cachedRenderKitId = null;
        
        _released = true;
        FacesContext.setCurrentInstance(null);
    }
    
    @Override
    public final ExternalContext getExternalContext()
    {
        assertNotReleased();

        return _externalContext;
    }
    
    @Override
    public final Application getApplication()
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

    @Override
    public UIViewRoot getViewRoot()
    {
        assertNotReleased();

        return _viewRoot;
    }
    
    @Override
    public final void setViewRoot(final UIViewRoot viewRoot)
    {
        assertNotReleased();

        _viewRoot = viewRoot;
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
     * has to be thrown in many of the methods if the method is called after the instance has been released!
     */
    protected final void assertNotReleased()
    {
        if (_released)
        {
            throw new IllegalStateException("Error the FacesContext is already released!");
        }
    }
    
}
