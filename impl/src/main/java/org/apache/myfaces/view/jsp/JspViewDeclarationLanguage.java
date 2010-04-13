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
package org.apache.myfaces.view.jsp;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.myfaces.application.jsp.ServletViewResponseWrapper;
import org.apache.myfaces.context.servlet.ResponseSwitch;
import org.apache.myfaces.shared_impl.view.JspViewDeclarationLanguageBase;
import org.apache.myfaces.util.ExternalContextUtils;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-22 13:55:12 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class JspViewDeclarationLanguage extends JspViewDeclarationLanguageBase
{
    //private static final Log log = LogFactory.getLog(JspViewDeclarationLanguage.class);
    public static final Logger log = Logger.getLogger(JspViewDeclarationLanguage.class.getName());
    /**
     * 
     */
    public JspViewDeclarationLanguage()
    {
        if (log.isLoggable(Level.FINEST))
            log.finest("New JspViewDeclarationLanguage instance created");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        ExternalContext externalContext = context.getExternalContext();

        if (context.getPartialViewContext().isPartialRequest())
        {
            // try to get (or create) a ResponseSwitch and turn off the output
            Object origResponse = context.getExternalContext().getResponse();
            ResponseSwitch responseSwitch = ExternalContextUtils.getResponseSwitch(origResponse);
            if (responseSwitch == null)
            {
                // no ResponseSwitch installed yet - create one 
                responseSwitch = ExternalContextUtils.createResponseSwitch(origResponse);
                if (responseSwitch != null)
                {
                    // install the ResponseSwitch
                    context.getExternalContext().setResponse(responseSwitch);
                }
            }
            if (responseSwitch != null)
            {
                responseSwitch.setEnabled(context, false);
            }
        }
        
        ServletResponse response = (ServletResponse) externalContext.getResponse();
        ServletRequest request = (ServletRequest) externalContext.getRequest();
        
        Locale locale = view.getLocale();
        response.setLocale(locale);
        Config.set(request, Config.FMT_LOCALE, context.getViewRoot().getLocale());

        String viewId = view.getViewId();
        ServletViewResponseWrapper wrappedResponse = new ServletViewResponseWrapper((HttpServletResponse) response);

        externalContext.setResponse(wrappedResponse);
        try
        {
            externalContext.dispatch(viewId);
        }
        finally
        {
            externalContext.setResponse(response);
        }

        boolean errorResponse = wrappedResponse.getStatus() < 200 || wrappedResponse.getStatus() > 299;
        if (errorResponse)
        {
            wrappedResponse.flushToWrappedResponse();
            return;
        }

        //Skip this step if we are rendering an ajax request, because no content outside
        //f:view tag should be output.
        if (!context.getPartialViewContext().isPartialRequest())
        {
            // store the wrapped response in the request, so it is thread-safe
            setAfterViewTagResponseWrapper(externalContext, wrappedResponse);
        }
    }

    @Override
    protected void sendSourceNotFound(FacesContext context, String message)
    {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        try
        {
            context.responseComplete();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
        catch (IOException ioe)
        {
            throw new FacesException(ioe);
        }
    }

}
