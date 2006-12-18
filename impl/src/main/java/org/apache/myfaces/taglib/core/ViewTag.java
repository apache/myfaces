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
package org.apache.myfaces.taglib.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.util.LocaleUtils;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKitFactory;
import javax.faces.webapp.UIComponentELTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import java.util.Locale;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Bruno Aranda (JSR-252)
 * @version $Revision$ $Date$
 */
public class ViewTag
        extends UIComponentELTag
{
    private static final Log log = LogFactory.getLog(ViewTag.class);

    public String getComponentType()
    {
        return UIViewRoot.COMPONENT_TYPE;
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _locale;
    private ValueExpression _renderKitId;

    private MethodExpression _beforePhase;
    private MethodExpression _afterPhase;

    public void setLocale(ValueExpression locale)
    {
        _locale = locale;
    }

    public void setRenderKitId(ValueExpression renderKitId)
    {
        _renderKitId = renderKitId;
    }

    public void setBeforePhase(MethodExpression beforePhase)
    {
        _beforePhase = beforePhase;
    }

    public void setAfterPhase(MethodExpression afterPhase)
    {
        _afterPhase = afterPhase;
    }

    public int doStartTag() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doStartTag");

        int retVal = super.doStartTag();

        FacesContext facesContext = FacesContext.getCurrentInstance();

        Config.set(pageContext.getRequest(),
                       Config.FMT_LOCALE,
                       facesContext.getViewRoot().getLocale());

        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doStartTag");
        return retVal;
    }

    public int doEndTag() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doEndTag");
        int retVal = super.doEndTag();

        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doEndTag");
        return retVal;
    }

    public int doAfterBody() throws JspException
    {
        if (log.isTraceEnabled()) log.trace("entering ViewTag.doAfterBody");

        UIComponent verbatimComp = createVerbatimComponentFromBodyContent();
        
        if (verbatimComp != null)
        {
            FacesContext.getCurrentInstance().getViewRoot().getChildren()
                .add(verbatimComp);
        }
        /*
        BodyContent bodyContent = getBodyContent();

        if (bodyContent != null)
        {

            FacesContext facesContext = FacesContext.getCurrentInstance();

            StateManager stateManager = facesContext.getApplication().getStateManager();
            StateManager.SerializedView serializedView
                    = stateManager.saveSerializedView(facesContext);
            if (serializedView != null)
            {
                //until now we have written to a buffer
                ResponseWriter bufferWriter = facesContext.getResponseWriter();
                bufferWriter.flush();
                //now we switch to real output
                ResponseWriter realWriter = bufferWriter.cloneWithWriter(getBodyContent().getEnclosingWriter());
                facesContext.setResponseWriter(realWriter);

                String bodyStr = bodyContent.getString();
                if ( stateManager.isSavingStateInClient(facesContext) )
                {
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
                }
                else
                {
                    realWriter.write( bodyStr );
                }
            }
            else
            {
                bodyContent.writeOut(getBodyContent().getEnclosingWriter());
            }

        }
        */

        if (log.isTraceEnabled()) log.trace("leaving ViewTag.doAfterBody");
        return EVAL_PAGE;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        UIViewRoot viewRoot = (UIViewRoot) component;

        // locale
        if (_locale != null)
        {
            Locale locale;
            if (_locale.isLiteralText())
            {
                locale = LocaleUtils.toLocale(_locale.getValue(elContext).toString());
            }
            else
            {
                component.setValueExpression("locale", _locale);

                Object localeValue = _locale.getValue(elContext);
                
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
            viewRoot.setLocale(locale);
            Config.set(pageContext.getRequest(),
                       Config.FMT_LOCALE,
                       locale);
        }

        // renderkitId
        if (_renderKitId != null)
        {
            if (_renderKitId.isLiteralText())
            {
                viewRoot.setRenderKitId(_renderKitId.getValue(elContext).toString());
            }
            else
            {
                viewRoot.setValueExpression("renderKitId", _renderKitId);
                viewRoot.setRenderKitId(null);
            }
        }
        else if (viewRoot.getRenderKitId() == null)
        {
            String defaultRenderKitId = facesContext.getApplication().getDefaultRenderKitId();
            viewRoot.setRenderKitId(defaultRenderKitId);
        }
        else
        {
            viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        }

        // beforePhase
        if (_beforePhase != null)
        {
            if (_beforePhase.isLiteralText())
            {
                throw new FacesException("Invalid method expression for attribute 'beforePhase' in the view tag: "
                        +_beforePhase.getExpressionString());
            }
            else
            {
                viewRoot.setBeforePhaseListener(_beforePhase);
            }
        }

        // afterPhase
        if (_afterPhase != null)
        {
            if (_afterPhase.isLiteralText())
            {
                throw new FacesException("Invalid method expression for attribute 'beforePhase' in the view tag: "
                        +_afterPhase.getExpressionString());
            }
            else
            {
                viewRoot.setAfterPhaseListener(_afterPhase);
            }
        }
    }
}
