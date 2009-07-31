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
package org.apache.myfaces.context;

import java.lang.reflect.Field;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.context.servlet.FacesContextImpl;

/**
 * DOCUMENT ME!
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesContextFactoryImpl extends FacesContextFactory
    implements ReleaseableFacesContextFactory
{
    private static final Log log = LogFactory.getLog(FacesContextFactoryImpl.class);
    
    /**
     * Reference to factory to prevent unnecessary lookups
     */
    private final ExternalContextFactory _externalContextFactory;
    
    /**
     * Reference to factory to prevent unnecessary lookups
     */
    private final ExceptionHandlerFactory _exceptionHandlerFactory;
        
    /**
     * This var is assigned as the same as javax.faces.context.ExternalContext._firstInstance,
     * and since it is a static reference and does not change, we can cache it here safely.
     * 
     * We need
     */
    private final ThreadLocal<ExternalContext> _firstExternalContextInstance;
    
    @SuppressWarnings("unchecked")
    public FacesContextFactoryImpl()
    {
        super();
        ThreadLocal<ExternalContext> firstExternalContextInstance = null;
        try
        {
            Field externalContextFirstInstance = ExternalContext.class.getDeclaredField("_firstInstance");
            externalContextFirstInstance.setAccessible(true);
            
            if (externalContextFirstInstance != null)
            {
                if (firstExternalContextInstance == null)
                {
                    firstExternalContextInstance = 
                        (ThreadLocal<ExternalContext>) externalContextFirstInstance.get(null);
                }
            }
        }
        catch (SecurityException e)
        {
            // It could happen, but we can ignore it.
            if (log.isDebugEnabled())
                log.debug("Cannot access private field _firstInstance from ExternalContext ",e);
        }
        catch (Exception e)
        {
            //It should not happen if we have only myfaces on classpath
            if (log.isErrorEnabled())
                log.error("Cannot found private field _firstInstance from ExternalContext ",e);
        }
        
        _firstExternalContextInstance = firstExternalContextInstance;
        
        _externalContextFactory = (ExternalContextFactory)
            FactoryFinder.getFactory(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);

        _exceptionHandlerFactory = (ExceptionHandlerFactory)
            FactoryFinder.getFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
        
    }

    @Override
    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
        throws FacesException
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        if (request == null)
        {
            throw new NullPointerException("request");
        }
        if (response == null)
        {
            throw new NullPointerException("response");
        }
        if (lifecycle == null)
        {
            throw new NullPointerException("lifecycle");
        }
        
        ExternalContext externalContext = _externalContextFactory.getExternalContext(context, request, response);

        ExternalContext defaultExternalContext = null;
        
        if (_firstExternalContextInstance != null)
        {
            if (_firstExternalContextInstance.get() == null)
            {
                defaultExternalContext = (ExternalContext)
                    externalContext.getRequestMap().remove(
                            ExternalContextFactoryImpl.EXTERNAL_CONTEXT_KEY);
                
                if (defaultExternalContext != null)
                {
                    // Initialize the firstExternalContext that old jsf 1.2 or lower
                    // implementations of ExternalContext should fall when call jsf 2.0
                    // methods.
                    _firstExternalContextInstance.set(defaultExternalContext);
                }
            }
        }
        
        if (context instanceof ServletContext)
        {
            FacesContext facesContext = new FacesContextImpl(externalContext, null, this);
            
            facesContext.setExceptionHandler(_exceptionHandlerFactory.getExceptionHandler());
            
            return facesContext;
            //return new FacesContextImpl((ServletContext)context, (ServletRequest)request, (ServletResponse)response);
        }

        throw new FacesException("Unsupported context type " + context.getClass().getName());
    }

    @Override
    public void release()
    {
        if (_firstExternalContextInstance != null)
        {
            _firstExternalContextInstance.remove();
        }
    }
}
