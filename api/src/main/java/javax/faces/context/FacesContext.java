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
package javax.faces.context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class FacesContext
{
    public static final String NO_PARTIAL_PHASE_CLIENT_IDS = "none";
    public static final String PARTIAL_EXECUTE_PARAM_NAME = "javax.faces.partial.execute";
    public static final String PARTIAL_RENDER_PARAM_NAME = "javax.faces.partial.render";

    /**
     * Return the context within which all EL-expressions are evaluated.
     * <p>
     * A JSF implementation is expected to provide a full implementation of this
     * class. However JSF also explicitly allows user code to apply the "decorator"
     * pattern to this type, by overriding the FacesContextFactory class. In that
     * pattern, the decorating class has a reference to an "underlying" implementation
     * and forward calls to it, possibly after taking other related actions.
     * <p>
     * The decorator pattern does have difficulties with backwards-compatibility when
     * new methods are added to the class being decorated, as with this method which
     * was added in JSF1.2. Decorator classes that were written for JSF1.1 will subclass
     * this class, but will not override this method to pass the call on to the 
     * "underlying" instance. This base implementation therefore must do that for it.
     * <p>
     * Unfortunately the JSF designers stuffed up the design; this base class has no way
     * of knowing what the "underlying" instance is! The current implementation here
     * is therefore to delegate directly to the very <i>first</i> FacesContext instance
     * registered within this request (via setCurrentInstance). This instance should
     * be the "full" implementation provided by the JSF framework. The drawback is that
     * when any decorator class is present which defaults to this base implementation,
     * then any following decorator instances that do override this method do not get
     * it invoked.
     * <p>
     * It is believed that the Sun JSF implementation (Mojarra) does something similar.
     * 
     * @since 1.2
     */
    public ELContext getELContext()
    {
        // Do NOT use getCurrentInstance here. For FacesContext decorators that
        // register themselves as "the current instance" that will cause an
        // infinite loop. For FacesContext decorators that do not register
        // themselves as "the current instance", if they are themselves wrapped
        // by a decorator that *does* register itself, then an infinite loop
        // also occurs.
        //
        // As noted above, we really want to do something like
        //   ctx = getWrappedInstance();
        // where the subclass can return the object it is delegating to. 
        // As there is no such method, however, the best we can do is pass the
        // method call on to the first-registered FacesContext instance. That
        // instance will never be "this", as the real original FacesContext
        // object will provide a proper implementation of this method.
        FacesContext ctx = _firstInstance.get();
        
        
        if (ctx == null)
        {
            throw new NullPointerException(FacesContext.class.getName());
        }

        ELContext elctx = ctx.getELContext();

        if (elctx == null)
        {
            throw new UnsupportedOperationException();
        }

        return elctx;
    }
    
    public void enableResponseWriting(boolean enable)
    {
        //do nothing per default we switch in the derived classes!
    }
    
    public abstract Application getApplication();

    public Map<Object,Object> getAttributes()
    {
        // TODO: JSF 2.0 #32
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return null;
    }

    public abstract Iterator<String> getClientIdsWithMessages();
    
    public PhaseId getCurrentPhaseId()
    {
        throw new UnsupportedOperationException();
    }
    
    public List<String> getExecutePhaseClientIds()
    {
        // TODO: JSF 2.0 #49
        return null;
    }

    public abstract ExternalContext getExternalContext();

    public abstract FacesMessage.Severity getMaximumSeverity();

    public abstract Iterator<FacesMessage> getMessages();

    public abstract Iterator<FacesMessage> getMessages(String clientId);
    
    public ResponseWriter getPartialResponseWriter()
    {
        // TODO: JSF 2.0 #58
        return null;
    }

    public abstract RenderKit getRenderKit();

    public abstract boolean getRenderResponse();
    
    public List<String> getRenderPhaseClientIds()
    {
        //according to the spec the getRenderPhaseClientIds
        //always at least must return an empty list
        //according to the specs isRenderAll must
        //check for an empty list as result of
        //the call on this method
        //the null value is not stated!
        
        // TODO: JSF 2.0 #59
        return (List<String>) Collections.EMPTY_LIST;
    }

    public abstract boolean getResponseComplete();

    public abstract ResponseStream getResponseStream();
    
    public void setExecutePhaseClientIds(List<String> executePhaseClientIds)
    {
        // TODO: JSF 2.0 #50
    }
    
    public void setRenderAll(boolean renderAll)
    {
        // TODO: JSF 2.0 #55
    }
    
    public void setRenderPhaseClientIds(List<String> renderPhaseClientIds)
    {
        // TODO: JSF 2.0 #56
    }

    public abstract void setResponseStream(ResponseStream responseStream);

    public abstract ResponseWriter getResponseWriter();

    public abstract void setResponseWriter(ResponseWriter responseWriter);

    public abstract UIViewRoot getViewRoot();
    
    public boolean isAjaxRequest()
    {
        // TODO: JSF 2.0 #51
        return false;
    }
    
    public boolean isExecuteNone()
    {
        // TODO: JSF 2.0 #52
        return false;
    }
    
    public boolean isPostback()
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean isRenderAll()
    {
        // TODO: JSF 2.0 #53
        return false;
    }
    
    public boolean isRenderNone()
    {
        // TODO: JSF 2.0 #54
        return false;
    }
    
    public ExceptionHandler getExceptionHandler()
    {
        throw new UnsupportedOperationException();
    }
    
    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        throw new UnsupportedOperationException();
    }
    
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        throw new UnsupportedOperationException();
    }

    public abstract void setViewRoot(UIViewRoot root);

    public abstract void addMessage(String clientId, FacesMessage message);

    public abstract void release();

    public abstract void renderResponse();

    public abstract void responseComplete();

    private static ThreadLocal<FacesContext> _currentInstance = new ThreadLocal<FacesContext>();

    private static ThreadLocal<FacesContext> _firstInstance = new ThreadLocal<FacesContext>();

    public static FacesContext getCurrentInstance()
    {
        return _currentInstance.get();
    }

    protected static void setCurrentInstance(FacesContext context)
    {
        if (context == null)
        {
            _currentInstance.remove();
            _firstInstance.remove();
        }
        else
        {
            _currentInstance.set(context);
            
            if (_firstInstance.get() == null)
            {
                _firstInstance.set(context);
            }
        }
    }
}
