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
package org.apache.myfaces.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.lifecycle.ClientWindowFactory;
import javax.faces.lifecycle.Lifecycle;

import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.util.lang.Assert;
import org.apache.myfaces.util.DebugUtils;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Nikolay Petrov
 * @version $Revision$ $Date$
 */
public class LifecycleImpl extends Lifecycle
{
    private static final Logger log = Logger.getLogger(LifecycleImpl.class.getName());
    
    /**
     * Boolean.TRUE is stored under this key in the application map if
     * the first request has been processed.
     */
    public static final String FIRST_REQUEST_PROCESSED_PARAM = "org.apache.myfaces.lifecycle.first.request.processed";
    
    private final PhaseExecutor[] lifecycleExecutors;
    private final PhaseExecutor renderExecutor;
    private final ClientWindowFactory clientWindowFactory;
    private final List<PhaseListener> phaseListeners;

    /**
     * This variable should be marked as volatile to ensure all threads can see it
     * after the first request is processed. Note that LifecycleImpl instance could be
     * shared by multiple requests at the same time, so this is relevant to prevent
     * multiple updates to FIRST_REQUEST_PROCESSED_PARAM. Really since the value
     * only changes from false to true, have a racy single check here does not harm, but
     * note in this case the semantic of the variable must be preserved.
     */
    private volatile boolean _firstRequestProcessed = false;
    
    public LifecycleImpl()
    {
        // hide from public access
        lifecycleExecutors = new PhaseExecutor[] { new RestoreViewExecutor(), new ApplyRequestValuesExecutor(),
                new ProcessValidationsExecutor(), new UpdateModelValuesExecutor(), new InvokeApplicationExecutor() };

        renderExecutor = new RenderResponseExecutor();
        clientWindowFactory = (ClientWindowFactory) FactoryFinder.getFactory(FactoryFinder.CLIENT_WINDOW_FACTORY);
        phaseListeners = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public void attachWindow(FacesContext facesContext)
    {
        ClientWindow clientWindow = facesContext.getExternalContext().getClientWindow();
        if (clientWindow == null)
        {
            clientWindow = getClientWindowFactory().getClientWindow(facesContext);
        }
        if (clientWindow != null)
        {
            try
            {
                facesContext.getExternalContext().setClientWindow(clientWindow);
                clientWindow.decode(facesContext);
            }
            catch (RuntimeException e)
            {
                facesContext.getExternalContext().setClientWindow(null);
                throw e;
            }
        }
    }
    
    protected ClientWindowFactory getClientWindowFactory()
    {
        return clientWindowFactory;
    }

    @Override
    public void execute(FacesContext facesContext) throws FacesException
    {
        if (!facesContext.isProjectStage(ProjectStage.Production))
        {
            new FacesConfigurator(facesContext.getExternalContext()).update();
        }

        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this, facesContext, getPhaseListeners());
        for (PhaseExecutor executor : lifecycleExecutors)
        {
            if (executePhase(facesContext, executor, phaseListenerMgr))
            {
                return;
            }
        }
    }

    private boolean executePhase(FacesContext context, PhaseExecutor executor, PhaseListenerManager phaseListenerMgr)
        throws FacesException
    {
        boolean skipFurtherProcessing = false;

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("entering " + executor.getPhase() + " in " + LifecycleImpl.class.getName());
        }

        PhaseId currentPhaseId = executor.getPhase();
        Flash flash = context.getExternalContext().getFlash();

        try
        {
            /* 
             * Specification, section 2.2
             * The default request lifecycle processing implementation must ensure that the currentPhaseId property 
             * of the FacesContext instance for this request is set with the proper PhaseId constant for the current 
             * phase as the first instruction at the beginning of each phase
             */
            context.setCurrentPhaseId(currentPhaseId);
            
            flash.doPrePhaseActions(context);
            
            // let the PhaseExecutor do some pre-phase actions
            executor.doPrePhaseActions(context);

            phaseListenerMgr.informPhaseListenersBefore(currentPhaseId);

            if (isResponseComplete(context, currentPhaseId, true))
            {
                // have to return right away
                return true;
            }
            if (shouldRenderResponse(context, currentPhaseId, true))
            {
                skipFurtherProcessing = true;
            }

            if (executor.execute(context))
            {
                return true;
            }
        }
        
        catch (Throwable e)
        {
            // JSF 2.0: publish the executor's exception (if any).
            
            publishException (e, currentPhaseId, context);
        }
        
        finally
        {
            phaseListenerMgr.informPhaseListenersAfter(currentPhaseId);
            
            flash.doPostPhaseActions(context);
            
        }
        
        context.getExceptionHandler().handle();
        
        if (isResponseComplete(context, currentPhaseId, false) || shouldRenderResponse(context, currentPhaseId, false))
        {
            // since this phase is completed we don't need to return right away even if the response is completed
            skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isLoggable(Level.FINEST))
        {
            log.finest("exiting " + executor.getPhase() + " in " + LifecycleImpl.class.getName());
        }

        return skipFurtherProcessing;
    }

    @Override
    public void render(FacesContext facesContext) throws FacesException
    {
        // if the response is complete we should not be invoking the phase listeners
        if (isResponseComplete(facesContext, renderExecutor.getPhase(), true))
        {
            return;
        }
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("entering " + renderExecutor.getPhase() + " in " + LifecycleImpl.class.getName());
        }

        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this, facesContext, getPhaseListeners());
        Flash flash = facesContext.getExternalContext().getFlash();

        try
        {
            facesContext.setCurrentPhaseId(renderExecutor.getPhase());

            flash.doPrePhaseActions(facesContext);

            // let the PhaseExecutor do some pre-phase actions
            renderExecutor.doPrePhaseActions(facesContext);

            phaseListenerMgr.informPhaseListenersBefore(renderExecutor.getPhase());
            // also possible that one of the listeners completed the response
            if (isResponseComplete(facesContext, renderExecutor.getPhase(), true))
            {
                return;
            }

            renderExecutor.execute(facesContext);
        }

        catch (Throwable e)
        {
            // JSF 2.0: publish the executor's exception (if any).

            publishException (e, renderExecutor.getPhase(), facesContext);
        }

        finally
        {
            phaseListenerMgr.informPhaseListenersAfter(renderExecutor.getPhase());
            flash.doPostPhaseActions(facesContext);

            // publish a field in the application map to indicate
            // that the first request has been processed
            requestProcessed(facesContext);
        }

        facesContext.getExceptionHandler().handle();

        if (log.isLoggable(Level.FINEST))
        {
            // Note: DebugUtils Logger must also be in trace level
            DebugUtils.traceView("View after rendering");
        }

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("exiting " + renderExecutor.getPhase() + " in " + LifecycleImpl.class.getName());
        }
    }

    private boolean isResponseComplete(FacesContext facesContext, PhaseId phase, boolean before)
    {
        boolean flag = false;
        if (facesContext.getResponseComplete())
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("exiting from lifecycle.execute in " + phase
                        + " because getResponseComplete is true from one of the " + (before ? "before" : "after")
                        + " listeners");
            }
            flag = true;
        }
        return flag;
    }

    private boolean shouldRenderResponse(FacesContext facesContext, PhaseId phase, boolean before)
    {
        boolean flag = false;
        if (facesContext.getRenderResponse())
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("exiting from lifecycle.execute in " + phase
                        + " because getRenderResponse is true from one of the " + (before ? "before" : "after")
                        + " listeners");
            }
            flag = true;
        }
        return flag;
    }

    @Override
    public void addPhaseListener(PhaseListener phaseListener)
    {
        Assert.notNull(phaseListener, "phaseListener");

        phaseListeners.add(phaseListener);
    }

    @Override
    public void removePhaseListener(PhaseListener phaseListener)
    {
        Assert.notNull(phaseListener, "phaseListener");

        phaseListeners.remove(phaseListener);
    }

    @Override
    public PhaseListener[] getPhaseListeners()
    {
        return phaseListeners.toArray(new PhaseListener[phaseListeners.size()]);
    }
    
    private void publishException(Throwable e, PhaseId phaseId, FacesContext facesContext)
    {
        ExceptionQueuedEventContext context = new ExceptionQueuedEventContext (facesContext, e, null, phaseId);
        
        facesContext.getApplication().publishEvent (facesContext, ExceptionQueuedEvent.class, context);
    }
    
    /**
     * This method places an attribute on the application map to 
     * indicate that the first request has been processed. This
     * attribute is used by several methods in ApplicationImpl 
     * to determine whether or not to throw an IllegalStateException
     * @param facesContext
     */
    private void requestProcessed(FacesContext facesContext)
    {
        if (!_firstRequestProcessed)
        {
            // The order here is important. First it is necessary to put
            // the value on application map before change the value here.
            // If multiple threads reach this point concurrently, the
            // variable will be written on the application map at the same
            // time but always with the same value.
            facesContext.getExternalContext().getApplicationMap()
                .put(FIRST_REQUEST_PROCESSED_PARAM, Boolean.TRUE);
            
            _firstRequestProcessed = true;
        }        
    }
}
