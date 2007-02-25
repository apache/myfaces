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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.MyfacesStateManager;
import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlLinkRendererBase;
import org.apache.myfaces.shared_impl.util.LocaleUtils;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentBodyTag;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewTag
        extends UIComponentBodyTag
{
    private static final Log log = LogFactory.getLog(ViewTag.class);
    private static final String PARTIAL_STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.PARTIAL_STATE_SAVING_METHOD";
    private static final String PARTIAL_STATE_SAVING_METHOD_ON = "true";
    private static final String PARTIAL_STATE_SAVING_METHOD_OFF = "false";

    private Boolean _partialStateSaving = null;

    private boolean isPartialStateSavingOn(javax.faces.context.FacesContext context)
    {
        if(context == null) throw new NullPointerException("context");
        if (_partialStateSaving != null) return _partialStateSaving.booleanValue();
        String stateSavingMethod = context.getExternalContext().getInitParameter(PARTIAL_STATE_SAVING_METHOD_PARAM_NAME);
        if (stateSavingMethod == null)
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("No partial state saving method defined, assuming default partial state saving methode off.");
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_ON))
        {
            _partialStateSaving = Boolean.TRUE;
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_OFF))
        {
            _partialStateSaving = Boolean.FALSE;
        }
        else
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("Illegal partial state saving method '" + stateSavingMethod + "', default partial state saving will be used (partial state saving off).");
        }
        return _partialStateSaving.booleanValue();
    }


    public String getComponentType()
    {
        return UIViewRoot.COMPONENT_TYPE;
    }

    public String getRendererType()
    {
        return null;
    }

    private String _locale;

    public void setLocale(String locale)
    {
        _locale = locale;
    }

    public int doStartTag() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doStartTag");
        super.doStartTag();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResponseWriter responseWriter = facesContext.getResponseWriter();
        try
        {
            responseWriter.startDocument();
        }
        catch (IOException e)
        {
            log.error("Error writing startDocument", e);
            throw new JspException(e);
        }

        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doStartTag");
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    protected boolean isSuppressed()
    {
        return true;
    }

    public int doEndTag() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doEndTag");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResponseWriter responseWriter = facesContext.getResponseWriter();

        try
        {
            responseWriter.endDocument();
        }
        catch (IOException e)
        {
            log.error("Error writing endDocument", e);
            throw new JspException(e);
        }

        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doEndTag");
        return super.doEndTag();
    }

    public int doAfterBody() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doAfterBody");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!isPartialStateSavingOn(facesContext))
        {
            try
            {
                BodyContent bodyContent = getBodyContent();
                if (bodyContent != null)
                {
                    StateManager stateManager = facesContext.getApplication().getStateManager();
                    StateManager.SerializedView serializedView
                            = stateManager.saveSerializedView(facesContext);

                    //until now we have written to a buffer
                    ResponseWriter bufferWriter = facesContext.getResponseWriter();
                    bufferWriter.flush();
                    //now we switch to real output
                    ResponseWriter realWriter = bufferWriter.cloneWithWriter(getPreviousOut());
                    facesContext.setResponseWriter(realWriter);

                    String bodyStr = bodyContent.getString();
                  /*
                    do this always - even with server-side-state saving
                    if ( stateManager.isSavingStateInClient(facesContext) )
                    { */

                    int form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER);
                    int url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER);
                    int lastMarkerEnd = 0;
                    while (form_marker != -1 || url_marker != -1)
                    {
                        if (url_marker == -1 || (form_marker != -1 && form_marker < url_marker))
                        {
                            //replace form_marker
                            realWriter.write(bodyStr, lastMarkerEnd, form_marker - lastMarkerEnd);
                            stateManager.writeState(facesContext, serializedView);
                            lastMarkerEnd = form_marker + JspViewHandlerImpl.FORM_STATE_MARKER_LEN;
                            form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER, lastMarkerEnd);
                        }
                        else
                        {
                            //replace url_marker
                            realWriter.write(bodyStr, lastMarkerEnd, url_marker - lastMarkerEnd);
                            if (stateManager instanceof MyfacesStateManager)
                            {
                                ((MyfacesStateManager)stateManager).writeStateAsUrlParams(facesContext,
                                                                                          serializedView);
                            }
                            else
                            {
                                log.error("Current StateManager is no MyfacesStateManager and does not support saving state in url parameters.");
                            }
                            lastMarkerEnd = url_marker + HtmlLinkRendererBase.URL_STATE_MARKER_LEN;
                            url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER, lastMarkerEnd);
                        }
                    }
                    realWriter.write(bodyStr, lastMarkerEnd, bodyStr.length() - lastMarkerEnd);

                    /* change over to do this always - even with server-side state saving
                    }
                    else
                    {
                        realWriter.write( bodyStr );
                    } */

                    /* before, this was done when getSerializedView was null
                    }
                    else
                    {
                        bodyContent.writeOut(getPreviousOut());
                    }*/
                }
            }
            catch (IOException e)
            {
                log.error("Error writing body content", e);
                throw new JspException(e);
            }
        }
        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doAfterBody");
        return super.doAfterBody();
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        if (_locale != null)
        {
            Locale locale;
            if (UIComponentTag.isValueReference(_locale))
            {
                FacesContext context = FacesContext.getCurrentInstance();
                ValueBinding vb = context.getApplication().createValueBinding(_locale);
                Object localeValue = vb.getValue(context);
                if (localeValue instanceof Locale)
                {
                    locale = (Locale) localeValue;
                }
                else if (localeValue instanceof String)
                {
                    locale = LocaleUtils.toLocale((String) localeValue);
                }
                else
                {
                     if (localeValue != null)
                     {
                         throw new IllegalArgumentException(
                                 "Locale or String class expected. Expression: " + _locale
                                 + ". Return class: " + localeValue.getClass().getName());
                     }
                     else
                     {
                         throw new IllegalArgumentException(
                                 "Locale or String class expected. Expression: " + _locale
                                 + ". Return value null");
                     }
                }
            }
            else
            {
                locale = LocaleUtils.toLocale(_locale);
            }
            ((UIViewRoot)component).setLocale(locale);
            Config.set((ServletRequest)getFacesContext().getExternalContext().getRequest(),
                       Config.FMT_LOCALE,
                       locale);
        }
    }
}
