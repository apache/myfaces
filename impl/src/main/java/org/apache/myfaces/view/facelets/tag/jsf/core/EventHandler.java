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
import java.util.Collection;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttributes;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.config.NamedEventManager;
import org.apache.myfaces.view.facelets.util.ReflectionUtil;

/**
 * Registers a listener for a given system event class on the UIComponent associated with this tag.
 */
@JSFFaceletTag(
        name = "f:event",
        bodyContent = "empty")
@JSFFaceletAttributes(attributes={
        @JSFFaceletAttribute(name="name",
                className="javax.el.ValueExpression",
                deferredValueType="java.lang.String"),
        @JSFFaceletAttribute(name="listener",
                className="javax.el.MethodExpression",
                deferredMethodSignature=
                "public void listener(javax.faces.event.ComponentSystemEvent evt) throws javax.faces.event.AbortProcessingException")
})
public final class EventHandler extends TagHandler {
    private TagAttribute listener;
    private TagAttribute name;
    private TagAttribute type;
    
    public EventHandler (TagConfig tagConfig)
    {
        super (tagConfig);
        
        listener = getRequiredAttribute ("listener");
        name = getAttribute ("name");
        type = getAttribute ("type");
        
        // TODO: is this right?  The spec isn't entirely clear, but it seems to me that one or the other
        // attribute must be defined, despite the fact that the docs say "name" is required.
        
        if ((name == null) && (type == null)) {
            throw new TagException (this.tag, "One of the 'name' or 'type' attributes must be defined");
        }
        
        else if ((name != null) && (type != null)) {
            throw new TagException (this.tag, "Both the 'name' and 'type' attributes cannot be defined");
        }
    }
    
    @Override
    public void apply (FaceletContext ctx, UIComponent parent) throws ELException, FacesException, FaceletException, IOException
    {
        Class<? extends ComponentSystemEvent> eventClass = getEventClass (ctx);
        MethodExpression methodExp = listener.getMethodExpression(ctx, void.class, new Class<?>[] {
            ComponentSystemEvent.class });
        
        // Simply register the event on the component.
        
        parent.subscribeToEvent (eventClass, new Listener (ctx.getFacesContext().getELContext(), methodExp));
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
        ValueExpression valueExp = ((name != null) ? name.getValueExpression (context, String.class) :
            type.getValueExpression (context, String.class));
        String value = (String) valueExp.getValue (context.getFacesContext().getELContext());
        
        if (name != null) {
            Collection<Class<? extends ComponentSystemEvent>> events;
            
            // We can look up the event class by name in the NamedEventManager.
            
            events = NamedEventManager.getInstance().getNamedEvent (value);
            
            if (events.size() > 1) {
                StringBuilder classNames = new StringBuilder ("[");
                Iterator<Class<? extends ComponentSystemEvent>> eventIterator = events.iterator();
                
                // TODO: The spec is somewhat vague, but I think we're supposed to throw an exception
                // here.  The @NamedEvent javadocs say that if a short name is registered to more than one
                // event class that we must throw an exception listing the short name and the classes in
                // the list _when the application makes reference to it_.  I believe processing this tag
                // qualifies as the application "making reference" to the short name.  Why the exception
                // isn't thrown when processing the @NamedEvent annotation, I don't know.  Perhaps follow
                // up with the EG to see if this is correct.
                
                while (eventIterator.hasNext()) {
                    classNames.append (eventIterator.next().getName());
                    
                    if (eventIterator.hasNext()) {
                        classNames.append (", ");
                    }
                    
                    else {
                        classNames.append ("]");
                    }
                }
                
                throw new FacesException ("The event name '" + value + "' is mapped to more than one " +
                    " event class: " + classNames.toString());
            }
            
            else {
                eventClass = events.iterator().next();
            }
        }
        
        else {
            // Must have been defined via the "type" attribute, so instantiate the class.
            
            try {
                eventClass = ReflectionUtil.forName (value);
            }
        
            catch (Throwable e) {
                throw new TagAttributeException ((name != null) ? name : type, "Couldn't create event class", e);
            }
        }
        
        if (!ComponentSystemEvent.class.isAssignableFrom (eventClass)) {
            throw new TagAttributeException ((name != null) ? name : type, "Event class " + eventClass.getName() +
                " is not of type javax.faces.event.ComponentSystemEvent");
        }
        
        return (Class<? extends ComponentSystemEvent>) eventClass;
    }
    
    private class Listener implements ComponentSystemEventListener {
        private ELContext elContext;
        private MethodExpression methodExp;
        
        private Listener (ELContext elContext, MethodExpression methodExp)
        {
            this.elContext = elContext;
            this.methodExp = methodExp;
        }
        
        @Override
        public void processEvent (ComponentSystemEvent event)
        {
            this.methodExp.invoke(elContext, new Object[] { event });
        }
    }
}
