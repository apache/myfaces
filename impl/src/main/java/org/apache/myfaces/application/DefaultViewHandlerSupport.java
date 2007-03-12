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
package org.apache.myfaces.application;

import java.util.List;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultViewHandlerSupport implements ViewHandlerSupport
{
    private static final String SERVLET_MAPPING = DefaultViewHandlerSupport.class.getName() + ".ServletMapping";

    private static final Log log = LogFactory.getLog(DefaultViewHandlerSupport.class);

    public String calculateViewId(FacesContext context, String viewId)
    {
        ServletMapping mapping = calculateServletMapping(context);
        if (mapping == null || mapping.isExtensionMapping())
        {
            viewId = applyDefaultSuffix(context, viewId);
        }
        else if (mapping != null && viewId != null && mapping.getUrlPattern().startsWith(viewId))
        {
            throw new InvalidViewIdException(viewId);
        }
        return viewId;
    }

    public String calculateActionURL(FacesContext context, String viewId)
    {
        if (viewId == null || !viewId.startsWith("/"))
            throw new IllegalArgumentException("ViewId must start with a '/': " + viewId);

        ServletMapping mapping = calculateServletMapping(context);
        ExternalContext externalContext = context.getExternalContext();
        String contextPath = externalContext.getRequestContextPath();
        StringBuilder builder = new StringBuilder(contextPath);
        if (mapping != null)
        {
            if (mapping.isExtensionMapping())
            {
                String contextSuffix = getContextSuffix(context);
                if (viewId.endsWith(contextSuffix))
                {
                    builder.append(viewId.substring(0, viewId.indexOf(contextSuffix)));
                    builder.append(mapping.getExtension());
                }
                else
                {
                    builder.append(viewId);
                }
            }
            else
            {
                builder.append(mapping.getPrefix());
                builder.append(viewId);
            }
        }
        else
        {
            builder.append(viewId);
        }
        String calculatedActionURL = builder.toString();
        if (log.isTraceEnabled())
        {
            log.trace("Calculated actionURL: '" + calculatedActionURL + "' for viewId: '" + viewId + "'");
        }
        return calculatedActionURL;
    }

    protected ServletMapping calculateServletMapping(FacesContext context)
    {
        ExternalContext externalContext = context.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        ServletMapping mapping = null;
        if (requestMap.containsKey(externalContext))
        {
            mapping = (ServletMapping) requestMap.get(SERVLET_MAPPING);
        }
        else
        {
            String servletPath = externalContext.getRequestServletPath();
            String requestPathInfo = externalContext.getRequestPathInfo();

            WebXml webxml = WebXml.getWebXml(externalContext);
            List mappings = webxml.getFacesServletMappings();

            if (requestPathInfo == null)
            {
                // might be extension mapping
                for (int i = 0, size = mappings.size(); i < size && mapping == null; i++)
                {
                    ServletMapping servletMapping = (ServletMapping) mappings.get(i);
                    String urlpattern = servletMapping.getUrlPattern();
                    String extension = urlpattern.substring(1, urlpattern.length());
                    if (servletPath.endsWith(extension))
                    {
                        mapping = servletMapping;
                    }
                    else if (servletPath.equals(urlpattern))
                    {
                        // path mapping with no pathInfo for the current request
                        mapping = servletMapping;
                    }
                }
            }
            else
            {
                // path mapping
                for (int i = 0, size = mappings.size(); i < size && mapping == null; i++)
                {

                    ServletMapping servletMapping = (ServletMapping) mappings.get(i);
                    String urlpattern = servletMapping.getUrlPattern();
                    urlpattern = urlpattern.substring(0, urlpattern.length() - 2);
                    // servletPath starts with "/" except in the case where the
                    // request is matched with the "/*" pattern, in which case
                    // it is the empty string (see Servlet Sepc 2.3 SRV4.4)
                    if (servletPath.equals(urlpattern))
                    {
                        mapping = servletMapping;
                    }
                }
            }

            // handle cases as best possible where servletPath is not a faces servlet,
            // such as when coming through struts-faces
            if (mapping == null && mappings.size() > 0)
            {
                mapping = (ServletMapping) mappings.get(0);
            }

            if (mapping == null && log.isWarnEnabled())
                log.warn("no faces servlet mappings found");

            requestMap.put(SERVLET_MAPPING, mapping);
        }
        return mapping;
    }

    protected String getContextSuffix(FacesContext context)
    {
        String defaultSuffix = context.getExternalContext().getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
        if (defaultSuffix == null)
        {
            defaultSuffix = ViewHandler.DEFAULT_SUFFIX;
        }
        return defaultSuffix;
    }

    protected String applyDefaultSuffix(FacesContext context, String viewId)
    {
        String defaultSuffix = getContextSuffix(context);
        if (!viewId.endsWith(defaultSuffix))
        {
            StringBuilder builder = new StringBuilder(viewId);
            int index = viewId.lastIndexOf('.');
            if (index != -1)
            {
                builder.replace(index, viewId.length(), defaultSuffix);
            }
            else
            {
                builder.append(defaultSuffix);
            }
            viewId = builder.toString();
            if (log.isTraceEnabled())
            {
                log.trace("view id after applying the context suffix: " + viewId);
            }
        }
        return viewId;
    }
}
