/*
 * Copyright 2004 The Apache Software Foundation.
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
package javax.faces.context;

import javax.el.ELContext;
import javax.faces.application.FacesMessage;
import java.util.Iterator;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class FacesContext
{
    // The following concrete method was added for JSF 1.2.  It supplies a default 
    // implementation that throws UnsupportedOperationException.  
    // This allows old FacesContext implementations to still work.
    public ELContext getELContext() {
        
    	FacesContext ctx = getCurrentInstance();
    	 
    	if( ctx == null )
    		throw new NullPointerException(FacesContext.class.getName());
    	
    	ELContext elctx = ctx.getELContext();
    	
    	if ( elctx == null )
    		throw new UnsupportedOperationException();
    	
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


    private static ThreadLocal<FacesContext> _currentInstance = new ThreadLocal()
    {
        protected Object initialValue()
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
        _currentInstance.set(context);

    }
}
