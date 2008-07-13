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

import java.util.Iterator;

import javax.el.ELContext;
import javax.faces.application.FacesMessage;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class FacesContext
{
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
    
    public abstract javax.faces.application.Application getApplication();

    public abstract Iterator<String> getClientIdsWithMessages();

    public abstract javax.faces.context.ExternalContext getExternalContext();

    public abstract FacesMessage.Severity getMaximumSeverity();

    public abstract Iterator<FacesMessage> getMessages();

    public abstract Iterator<FacesMessage> getMessages(String clientId);

    public abstract javax.faces.render.RenderKit getRenderKit();

    public abstract boolean getRenderResponse();

    public abstract boolean getResponseComplete();

    public abstract javax.faces.context.ResponseStream getResponseStream();

    public abstract void setResponseStream(javax.faces.context.ResponseStream responseStream);

    public abstract javax.faces.context.ResponseWriter getResponseWriter();

    public abstract void setResponseWriter(javax.faces.context.ResponseWriter responseWriter);

    public abstract javax.faces.component.UIViewRoot getViewRoot();

    public abstract void setViewRoot(javax.faces.component.UIViewRoot root);

    public abstract void addMessage(String clientId,
                                    javax.faces.application.FacesMessage message);

    public abstract void release();

    public abstract void renderResponse();

    public abstract void responseComplete();

    private static ThreadLocal<FacesContext> _currentInstance = new ThreadLocal<FacesContext>()
    {
        protected FacesContext initialValue()
        {
            return null;
        }
    };

    private static ThreadLocal<FacesContext> _firstInstance = new ThreadLocal<FacesContext>()
    {
        protected FacesContext initialValue()
        {
            return null;
        }
    };

    public static FacesContext getCurrentInstance()
    {
        return _currentInstance.get();
    }

    protected static void setCurrentInstance(javax.faces.context.FacesContext context)
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
