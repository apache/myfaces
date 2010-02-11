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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.config.NamedEventManager;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.util.ReflectionUtil;

/**
 * Registers a listener for a given system event class on the UIComponent associated with this tag.
 */
@JSFFaceletTag(
        name = "f:event",
        bodyContent = "empty")
public final class EventHandler extends TagHandler {
    
    private static final Class<?>[] COMPONENT_SYSTEM_EVENT_PARAMETER = new Class<?>[] { ComponentSystemEvent.class };
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    
    @JSFFaceletAttribute(name="listener",
            className="javax.el.MethodExpression",
            deferredMethodSignature=
            "public void listener(javax.faces.event.ComponentSystemEvent evt) throws javax.faces.event.AbortProcessingException")
    private TagAttribute listener;
    
    @JSFFaceletAttribute(name="type",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private TagAttribute type;
    
    public EventHandler (TagConfig tagConfig)
    {
        super (tagConfig);
        
        listener = getRequiredAttribute("listener");
        type = getRequiredAttribute("type");
    }
    
    public void apply (FaceletContext ctx, UIComponent parent) throws ELException, FacesException, FaceletException, IOException
    {
        //Apply only if we are creating a new component
        if (!ComponentHandler.isNew(parent))
        {
            return;
        }
        if (parent instanceof UIViewRoot && ((AbstractFaceletContext)ctx).isRefreshingTransientBuild())
        {
            return;
        }
        
        Class<? extends ComponentSystemEvent> eventClass = getEventClass(ctx);
        
        // Note: The listener attribute can now also take a zero-argument MethodExpression,
        // thus we need two different MethodExpressions (see MYFACES-2503 for details).
        MethodExpression methodExpOneArg 
                = listener.getMethodExpression(ctx, void.class, COMPONENT_SYSTEM_EVENT_PARAMETER);
        MethodExpression methodExpZeroArg
                = listener.getMethodExpression(ctx, void.class, EMPTY_CLASS_ARRAY);
        
        if (eventClass == PreRenderViewEvent.class)
        {
            // ensure ViewRoot for PreRenderViewEvent
            UIViewRoot viewRoot = ComponentSupport.getViewRoot(ctx, parent);
            viewRoot.subscribeToEvent(eventClass, new Listener(methodExpOneArg, methodExpZeroArg));
        }
        else
        {
            // Simply register the event on the component.
            parent.subscribeToEvent(eventClass, new Listener(methodExpOneArg, methodExpZeroArg));
        }
    }
    
    /**
     * Gets the event class defined by the tag (either in the "name" or "type" attribute).
     * 
     * @param context the Facelet context
     * @return a Class containing the event class defined by the tag.
     */
    
    @SuppressWarnings("unchecked")
    private Class<? extends ComponentSystemEvent> getEventClass (FaceletContext context)
    {
        Class<?> eventClass = null;
        String value = null;
        if (type.isLiteral())
        {
            value = type.getValue();
        }
        else
        {
            value = (String) type.getValueExpression (context, String.class).
                getValue (context.getFacesContext().getELContext());
        }
        
        Collection<Class<? extends ComponentSystemEvent>> events;
        
        // We can look up the event class by name in the NamedEventManager.
        
        events = NamedEventManager.getInstance().getNamedEvent (value);
        
        if (events == null)
        {
            try
            {
                eventClass = ReflectionUtil.forName (value);
            }
            catch (Throwable e)
            {
                throw new TagAttributeException (type, "Couldn't create event class", e);
            }
        }
        else if (events.size() > 1)
        {
            StringBuilder classNames = new StringBuilder ("[");
            Iterator<Class<? extends ComponentSystemEvent>> eventIterator = events.iterator();
            
            // TODO: The spec is somewhat vague, but I think we're supposed to throw an exception
            // here.  The @NamedEvent javadocs say that if a short name is registered to more than one
            // event class that we must throw an exception listing the short name and the classes in
            // the list _when the application makes reference to it_.  I believe processing this tag
            // qualifies as the application "making reference" to the short name.  Why the exception
            // isn't thrown when processing the @NamedEvent annotation, I don't know.  Perhaps follow
            // up with the EG to see if this is correct.
            
            while (eventIterator.hasNext())
            {
                classNames.append (eventIterator.next().getName());
                
                if (eventIterator.hasNext())
                {
                    classNames.append (", ");
                }
                else
                {
                    classNames.append ("]");
                }
            }
            
            throw new FacesException ("The event name '" + value + "' is mapped to more than one " +
                " event class: " + classNames.toString());
        }
        else
        {
            eventClass = events.iterator().next();
        }
        
        if (!ComponentSystemEvent.class.isAssignableFrom (eventClass))
        {
            throw new TagAttributeException (type, "Event class " + eventClass.getName() +
                " is not of type javax.faces.event.ComponentSystemEvent");
        }
        
        return (Class<? extends ComponentSystemEvent>) eventClass;
    }
    
    public static class Listener implements ComponentSystemEventListener, Serializable 
    {

        private static final long serialVersionUID = 7318240026355007052L;
        
        private MethodExpression methodExpOneArg;
        private MethodExpression methodExpZeroArg;
        
        public Listener()
        {
            super();
        }

        /**
         * Note: The listener attribute can now also take a zero-argument MethodExpression,
         * thus we need two different MethodExpressions (see MYFACES-2503 for details).
         * @param methodExpOneArg
         * @param methodExpZeroArg
         */
        private Listener(MethodExpression methodExpOneArg, MethodExpression methodExpZeroArg)
        {
            this.methodExpOneArg = methodExpOneArg;
            this.methodExpZeroArg = methodExpZeroArg;
        }
        
        public void processEvent(ComponentSystemEvent event)
        {
            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            try
            {
                // first try to invoke the MethodExpression with one argument
                this.methodExpOneArg.invoke(elContext, new Object[] { event });
            }
            catch (MethodNotFoundException mnfeOneArg)
            {
                try
                {
                    // if that fails try to invoke the MethodExpression with zero arguments
                    this.methodExpZeroArg.invoke(elContext, new Object[0]);
                }
                catch (MethodNotFoundException mnfeZeroArg)
                {
                    // if that fails too rethrow the original MethodNotFoundException
                    throw mnfeOneArg;
                }
            }
        }
    }
}
