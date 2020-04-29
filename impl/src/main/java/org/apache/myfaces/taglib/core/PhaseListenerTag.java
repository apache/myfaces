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
package org.apache.myfaces.taglib.core;

import java.io.Serializable;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.webapp.UIComponentELTag;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspTag;
import org.apache.myfaces.shared.util.ClassUtils;

/**
 * Register a PhaseListener instance
 * 
 * @author martin.haimberger
 * @version $Revision$ $Date$
 */
@JSFJspTag(name = "f:phaseListener", bodyContent = "empty")
public class PhaseListenerTag extends TagSupport
{
    
    private static final Logger log = Logger.getLogger(PhaseListenerTag.class.getName());
    
    /**
     * The fully qualified class name of the PhaseListener which should be created.
     */
    private ValueExpression type = null;

    /**
     * A value binding expression used to create a PhaseListener instance.
     */
    private ValueExpression binding = null;

    /**
     * Class name of the PhaseListener to be created and registered.
     */
    @JSFJspAttribute(className="jakarta.el.ValueExpression",
            deferredValueType="java.lang.String")
    public void setType(ValueExpression type)
    {
        this.type = type;
    }

    /**
     * Value binding expression that evaluates to a PhaseListener.
     */
    @JSFJspAttribute(className="jakarta.el.ValueExpression",
            deferredValueType="jakarta.faces.event.PhaseListener")
    public void setBinding(ValueExpression binding)
    {
        this.binding = binding;
    }

    @Override
    public int doStartTag() throws JspException
    {

        // JSF-Spec 1.2 9.4.9
        // Locate the one and only UIViewRoot custom action instance by walking up the tag tree
        // until you find a UIComponentELTag instance that has no parent. If the
        // getCreated() method of this instance returns true, check the binding attribute.

        Tag parent = this;
        UIComponentELTag parentTag = null;
        while ((parent = parent.getParent()) != null)
        {
            if (parent instanceof UIComponentELTag)
            {
                parentTag = (UIComponentELTag)parent;
            }
        }

        if (parentTag == null)
        {
            throw new JspException("Not nested in a UIViewRoot Error for tag with handler class: "
                    + this.getClass().getName());
        }

        if (!parentTag.getCreated())
        {
            return SKIP_BODY;
        }

        UIViewRoot root = (UIViewRoot)parentTag.getComponentInstance();

        // JSF-Spec 1.2 9.4.9
        // If binding is set, call binding.getValue() to obtain a reference to the
        // PhaseListener instance. If there is no exception thrown, and binding.getValue()
        // returned a non-null object that implements jakarta.faces.event.PhaseListener, register
        // it by calling addPhaseListener(). If there was an exception thrown, rethrow the
        // exception as a JspException.

        // If the listener instance could not be created, check the type attribute. If the type
        // attribute is set, instantiate an instance of the specified class, and register it by calling
        // addPhaseListener(). If the binding attribute was also set, store the listener instance
        // by calling binding.setValue(). If there was an exception thrown, rethrow the
        // exception as a JspException.

        PhaseListener listener = null;
        try
        {
            listener = new BindingPhaseListener(binding, type);
        }
        catch (Exception ex)
        {
            throw new JspException(ex.getMessage(), ex);
        }

        root.addPhaseListener(listener);

        return SKIP_BODY;

    }

    private static class BindingPhaseListener implements PhaseListener, Serializable
    {

        private transient PhaseListener phaseListenerCache = null;
        private ValueExpression type;
        private ValueExpression binding;
        //private final Log log = LogFactory.getLog(PhaseListenerTag.class);

        BindingPhaseListener(ValueExpression binding, ValueExpression type)
        {
            this.binding = binding;
            this.type = type;
        }

        public void afterPhase(PhaseEvent event)
        {
            PhaseListener listener = getPhaseListener();
            if (listener != null)
            {
                listener.afterPhase(event);
            }
        }

        public void beforePhase(PhaseEvent event)
        {
            PhaseListener listener = getPhaseListener();
            if (listener != null)
            {
                listener.beforePhase(event);
            }
        }

        public PhaseId getPhaseId()
        {
            PhaseListener listener = getPhaseListener();
            if (listener != null)
            {
                return listener.getPhaseId();
            }

            return null;

        }

        private PhaseListener getPhaseListener()
        {

            if (phaseListenerCache != null)
            {
                return phaseListenerCache;
            }
            else
            {
                // create PhaseListenerInstance
                phaseListenerCache = getPhaseListnerInstance(binding, type);
            }
            if (phaseListenerCache == null)
            {
                log.warning("PhaseListener will not be installed. Both binding and type are null.");

            }

            return phaseListenerCache;

        }

        private PhaseListener getPhaseListnerInstance(ValueExpression binding, ValueExpression type)
        {
            FacesContext currentFacesContext = FacesContext.getCurrentInstance();
            Object phasesInstance = null;
            if (binding != null)
            {
                phasesInstance = binding.getValue(currentFacesContext.getELContext());
            }
            else if (type != null)
            {
                try
                {
                    phasesInstance = ClassUtils.newInstance((String)type.getValue(currentFacesContext.getELContext()));
                }
                catch (FacesException ex)
                {
                    throw new AbortProcessingException(ex.getMessage(), ex);
                }
            }
            return (PhaseListener)phasesInstance;

        }

    }
}
