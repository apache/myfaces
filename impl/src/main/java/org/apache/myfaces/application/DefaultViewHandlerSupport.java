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

import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.lifecycle.CheckedViewIdsCache;
import org.apache.myfaces.util.SharedStringBuilder;
import org.apache.myfaces.util.ExternalContextUtils;
import org.apache.myfaces.util.StringUtils;
import org.apache.myfaces.util.ViewProtectionUtils;

/**
 * A ViewHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports javax.servlet, and uses a standard web.xml file.
 */
public class DefaultViewHandlerSupport implements ViewHandlerSupport
{
    /**
     * Identifies the FacesServlet mapping in the current request map.
     */
    private static final String CACHED_SERVLET_MAPPING =
        DefaultViewHandlerSupport.class.getName() + ".CACHED_SERVLET_MAPPING";

    private static final Logger log = Logger.getLogger(DefaultViewHandlerSupport.class.getName());
    
    private static final String VIEW_HANDLER_SUPPORT_SB = "oam.viewhandler.SUPPORT_SB";
    
    private final String[] _faceletsViewMappings;
    private final String[] _contextSuffixes;
    private final String _faceletsContextSufix;
    private final boolean _initialized;
    private CheckedViewIdsCache checkedViewIdsCache = null;
    
    public DefaultViewHandlerSupport()
    {
        _faceletsViewMappings = null;
        _contextSuffixes = null;
        _faceletsContextSufix = null;
        _initialized = false;
    }
    
    public DefaultViewHandlerSupport(FacesContext facesContext)
    {
        _faceletsViewMappings = getFaceletsViewMappings(facesContext);
        _contextSuffixes = getContextSuffix(facesContext);
        _faceletsContextSufix = getFaceletsContextSuffix(facesContext);
        _initialized = true;
    }

    @Override
    public String deriveLogicalViewId(FacesContext context, String viewId)
    {
        //If no viewId found, don't try to derive it, just continue.
        if (viewId == null)
        {
            return null;
        }

        FacesServletMapping mapping = getFacesServletMapping(context);
        if (mapping == null || mapping.isExtensionMapping())
        {
            viewId = handleSuffixMapping(context, viewId);
        }
        else if (mapping.isExactMapping())
        {
            if (viewId.equals(mapping.getExact()))
            {
                viewId = handleSuffixMapping(context, viewId + ".jsf");
            }
        }
        else if (mapping.isPrefixMapping())
        {
            viewId = handlePrefixMapping(viewId, mapping.getPrefix());
            
            // A viewId that is equals to the prefix mapping on servlet mode is
            // considered invalid, because jsp vdl will use RequestDispatcher and cause
            // a loop that ends in a exception. Note in portlet mode the view
            // could be encoded as a query param, so the viewId could be valid.
            if (viewId != null && viewId.equals(mapping.getPrefix()) &&
                !ExternalContextUtils.isPortlet(context.getExternalContext()))
            {
                throw new InvalidViewIdException();
            }

            // In JSF 2.3 some changes were done in the VDL to avoid the jsp vdl
            // RequestDispatcher redirection (only accept viewIds with jsp extension).
            // If we have this case
            if (viewId != null && viewId.equals(mapping.getPrefix()))
            {
                viewId = handleSuffixMapping(context, viewId+".jsf");
            }
        }
        else if (mapping.getUrlPattern().startsWith(viewId))
        {
            throw new InvalidViewIdException(viewId);
        }

        return viewId; // return null if no physical resource exists
    }
    
    @Override
    public String deriveViewId(FacesContext context, String viewId)
    {
        //If no viewId found, don't try to derive it, just continue.
        if (viewId == null)
        {
            return null;
        }
        FacesServletMapping mapping = getFacesServletMapping(context);
        if (mapping == null || mapping.isExtensionMapping())
        {
            viewId = handleSuffixMapping(context, viewId);
        }
        else if (mapping.isExactMapping())
        {
            if (viewId.equals(mapping.getExact()))
            {
                viewId = handleSuffixMapping(context, viewId + ".jsf");
            }
        }
        else if (mapping.isPrefixMapping())
        {
            viewId = handlePrefixMapping(viewId, mapping.getPrefix());

            if (viewId != null)
            {
                // A viewId that is equals to the prefix mapping on servlet mode is
                // considered invalid, because jsp vdl will use RequestDispatcher and cause
                // a loop that ends in a exception. Note in portlet mode the view
                // could be encoded as a query param, so the viewId could be valid.
                //if (viewId.equals(mapping.getPrefix()) &&
                //    !ExternalContextUtils.isPortlet(context.getExternalContext()))
                //{
                //    throw new InvalidViewIdException();
                //}
            
                // In JSF 2.3 some changes were done in the VDL to avoid the jsp vdl
                // RequestDispatcher redirection (only accept viewIds with jsp extension).
                // If we have this case
                if (viewId != null && viewId.equals(mapping.getPrefix()))
                {
                    viewId = handleSuffixMapping(context, viewId+".jsf");
                }

                return (checkViewExists(context,viewId) ? viewId : null);
            }
        }
        else if (mapping.getUrlPattern().startsWith(viewId))
        {
            throw new InvalidViewIdException(viewId);
        }
        else
        {
            if(viewId != null)
            {
                return (checkViewExists(context,viewId) ? viewId : null);
            }
        }

        return viewId;    // return null if no physical resource exists
    }

    @Override
    public String calculateActionURL(FacesContext context, String viewId)
    {
        if (viewId == null || !viewId.startsWith("/"))
        {
            throw new IllegalArgumentException("ViewId must start with a '/': " + viewId);
        }

        FacesServletMapping mapping = getFacesServletMapping(context);
        ExternalContext externalContext = context.getExternalContext();
        String contextPath = externalContext.getRequestContextPath();
        StringBuilder builder = SharedStringBuilder.get(context, VIEW_HANDLER_SUPPORT_SB);
        // If the context path is root, it is not necessary to append it, otherwise
        // and extra '/' will be set.
        if (contextPath != null && !(contextPath.length() == 1 && contextPath.charAt(0) == '/') )
        {
            builder.append(contextPath);
        }
        
        
        // In JSF 2.3 we could have cases where the viewId can be bound to an url-pattern that is not
        // prefix or suffix, but exact mapping. In this part we need to take the viewId and check if
        // the viewId is bound or not with a mapping.
        if (mapping != null && mapping.isExactMapping())
        {
            String exactMappingViewId = calculatePrefixedExactMapping(context, viewId);
            if (exactMappingViewId != null && !exactMappingViewId.isEmpty())
            {
                // if the current exactMapping already matches the requested viewId -> same view, skip....
                if (!mapping.getExact().equals(exactMappingViewId))
                {
                    // different viewId -> lets try to lookup a exact mapping
                    mapping = FacesServletMappingUtils.getExactMapping(context, exactMappingViewId);

                    // no exactMapping for the requested viewId available BUT the current view is a exactMapping
                    // we need a to search for a prefix or extension mapping
                    if (mapping == null)
                    {
                        mapping = FacesServletMappingUtils.getGenericPrefixOrSuffixMapping(context);
                        if (mapping == null)
                        {
                            throw new IllegalStateException(
                                    "No generic (either prefix or suffix) servlet-mapping found for FacesServlet."
                                    + "This is required serve views, that are not exact mapped.");
                        }
                    }
                }
            }
        }

        if (mapping != null)
        {
            if (mapping.isExactMapping())
            {
                builder.append(mapping.getExact());
            }
            else if (mapping.isExtensionMapping())
            {
                //See JSF 2.0 section 7.5.2 
                String[] contextSuffixes = _initialized ? _contextSuffixes : getContextSuffix(context); 
                boolean founded = false;
                for (String contextSuffix : contextSuffixes)
                {
                    if (viewId.endsWith(contextSuffix))
                    {
                        builder.append(viewId.substring(0, viewId.indexOf(contextSuffix)));
                        builder.append(mapping.getExtension());
                        founded = true;
                        break;
                    }
                }
                if (!founded)
                {   
                    //See JSF 2.0 section 7.5.2
                    // - If the argument viewId has an extension, and this extension is mapping, 
                    // the result is contextPath + viewId
                    //
                    // -= Leonardo Uribe =- It is evident that when the page is generated, the derived 
                    // viewId will end with the 
                    // right contextSuffix, and a navigation entry on faces-config.xml should use such id,
                    // this is just a workaroud
                    // for usability. There is a potential risk that change the mapping in a webapp make 
                    // the same application fail,
                    // so use viewIds ending with mapping extensions is not a good practice.
                    if (viewId.endsWith(mapping.getExtension()))
                    {
                        builder.append(viewId);
                    }
                    else if(viewId.lastIndexOf('.') != -1 )
                    {
                        builder.append(viewId.substring(0, viewId.lastIndexOf('.')));
                        builder.append(contextSuffixes[0]);
                    }
                    else
                    {
                        builder.append(viewId);
                        builder.append(contextSuffixes[0]);
                    }
                }
            }
            else if (mapping.isPrefixMapping())
            {
                builder.append(mapping.getPrefix());
                builder.append(viewId);
            }
        }
        else
        {
            builder.append(viewId);
        }
        
        
        //JSF 2.2 check view protection.
        if (ViewProtectionUtils.isViewProtected(context, viewId))
        {
            int index = builder.indexOf("?");
            if (index >= 0)
            {
                builder.append('&');
            }
            else
            {
                builder.append('?');
            }
            builder.append(ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM);
            builder.append('=');
            ResponseStateManager rsm = context.getRenderKit().getResponseStateManager();
            builder.append(rsm.getCryptographicallyStrongTokenFromSession(context));
        }
        
        String calculatedActionURL = builder.toString();
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Calculated actionURL: '" + calculatedActionURL + "' for viewId: '" + viewId + '\'');
        }
        return calculatedActionURL;
    }
    
    private String calculatePrefixedExactMapping(FacesContext context, String viewId)
    {
        String[] contextSuffixes = _initialized ? _contextSuffixes : getContextSuffix(context); 
        String prefixedExactMapping = null;
        for (String contextSuffix : contextSuffixes)
        {
            if (viewId.endsWith(contextSuffix))
            {
                prefixedExactMapping = viewId.substring(0, viewId.length() - contextSuffix.length());
                break;
            }
        }
        return prefixedExactMapping == null ? viewId : prefixedExactMapping;
    }
            

    /**
     * Read the web.xml file that is in the classpath and parse its internals to
     * figure out how the FacesServlet is mapped for the current webapp.
     */
    protected FacesServletMapping getFacesServletMapping(FacesContext context)
    {
        Map<Object, Object> attributes = context.getAttributes();

        // Has the mapping already been determined during this request?
        FacesServletMapping mapping = (FacesServletMapping) attributes.get(CACHED_SERVLET_MAPPING);
        if (mapping == null)
        {
            ExternalContext externalContext = context.getExternalContext();
            mapping = FacesServletMappingUtils.calculateFacesServletMapping(
                    context,
                    externalContext.getRequestServletPath(),
                    externalContext.getRequestPathInfo(),
                    true);

            attributes.put(CACHED_SERVLET_MAPPING, mapping);
        }
        return mapping;
    }

    protected String[] getContextSuffix(FacesContext context)
    {
        String defaultSuffix = context.getExternalContext().getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
        if (defaultSuffix == null)
        {
            defaultSuffix = ViewHandler.DEFAULT_SUFFIX;
        }
        return StringUtils.splitShortString(defaultSuffix, ' ');
    }
    
    protected String getFaceletsContextSuffix(FacesContext context)
    {
        String defaultSuffix = context.getExternalContext().getInitParameter(ViewHandler.FACELETS_SUFFIX_PARAM_NAME);
        if (defaultSuffix == null)
        {
            defaultSuffix = ViewHandler.DEFAULT_FACELETS_SUFFIX;
        }
        return defaultSuffix;
    }
    
    
    
    protected String[] getFaceletsViewMappings(FacesContext context)
    {
        String faceletsViewMappings= context.getExternalContext().getInitParameter(
                ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME);
        if(faceletsViewMappings == null)    //consider alias facelets.VIEW_MAPPINGS
        {
            faceletsViewMappings= context.getExternalContext().getInitParameter("facelets.VIEW_MAPPINGS");
        }
        
        return faceletsViewMappings == null ? null : StringUtils.splitShortString(faceletsViewMappings, ';');
    }

    /**
     * Return the normalized viewId according to the algorithm specified in 7.5.2 
     * by stripping off any number of occurrences of the prefix mapping from the viewId.
     * <p>
     * For example, both /faces/view.xhtml and /faces/faces/faces/view.xhtml would both return view.xhtml
     * </p>
     */
    protected String handlePrefixMapping(String viewId, String prefix)
    {
        // If prefix mapping (such as "/faces/*") is used for FacesServlet, 
        // normalize the viewId according to the following
        // algorithm, or its semantic equivalent, and return it.
               
        // Remove any number of occurrences of the prefix mapping from the viewId. 
        // For example, if the incoming value was /faces/faces/faces/view.xhtml 
        // the result would be simply view.xhtml.
        
        if ("".equals(prefix))
        {
            // if prefix is an empty string (Spring environment), we let it be "//"
            // in order to prevent an infinite loop in uri.startsWith(-emptyString-).
            // Furthermore a prefix of "//" is just another double slash prevention.
            prefix = "//";
        }
        else
        {
            // need to make sure its really /faces/* and not /facesPage.xhtml
            prefix = prefix + '/'; 
        }
        
        String uri = viewId;
        while (uri.startsWith(prefix) || uri.startsWith("//")) 
        {
            if (uri.startsWith(prefix))
            {
                // cut off only /faces, leave the trailing '/' char for the next iteration
                uri = uri.substring(prefix.length() - 1);
            }
            else
            {
                // uri starts with '//' --> cut off the leading slash, leaving
                // the second slash to compare for the next iteration
                uri = uri.substring(1);
            }
        }

        return uri;
    }
    
    /**
     * Return the viewId with any non-standard suffix stripped off and replaced with
     * the default suffix configured for the specified context.
     * <p>
     * For example, an input parameter of "/foo.jsf" may return "/foo.jsp".
     * </p>
     */
    protected String handleSuffixMapping(FacesContext context, String requestViewId)
    {
        String[] faceletsViewMappings = _initialized ? _faceletsViewMappings : getFaceletsViewMappings(context);
        String[] jspDefaultSuffixes = _initialized ? _contextSuffixes : getContextSuffix(context);
        
        int slashPos = requestViewId.lastIndexOf('/');
        int extensionPos = requestViewId.lastIndexOf('.');
        
        StringBuilder builder = SharedStringBuilder.get(context, VIEW_HANDLER_SUPPORT_SB);
        
        //Try to locate any resource that match with the expected id
        for (String defaultSuffix : jspDefaultSuffixes)
        {
            builder.setLength(0);
            builder.append(requestViewId);
           
            if (extensionPos > -1 && extensionPos > slashPos)
            {
                builder.replace(extensionPos, requestViewId.length(), defaultSuffix);
            }
            else
            {
                builder.append(defaultSuffix);
            }

            String candidateViewId = builder.toString();
            
            if (faceletsViewMappings != null && faceletsViewMappings.length > 0 )
            {
                for (String mapping : faceletsViewMappings)
                {
                    if (mapping.startsWith("/"))
                    {
                        continue;   //skip this entry, its a prefix mapping
                    }
                    if (mapping.equals(candidateViewId))
                    {
                        return candidateViewId;
                    }
                    if (mapping.startsWith(".")) //this is a wildcard entry
                    {
                        builder.setLength(0); //reset/reuse the builder object 
                        builder.append(candidateViewId); 
                        builder.replace(candidateViewId.lastIndexOf('.'), candidateViewId.length(), mapping);
                        String tempViewId = builder.toString();
                        if (checkViewExists(context, tempViewId))
                        {
                            return tempViewId;
                        }
                    }
                }
            }

            // forced facelets mappings did not match or there were no entries in faceletsViewMappings array
            if (checkViewExists(context,candidateViewId))
            {
                return candidateViewId;
            }
        
        }
        
        //jsp suffixes didn't match, try facelets suffix
        String faceletsDefaultSuffix = _initialized ? _faceletsContextSufix : this.getFaceletsContextSuffix(context);
        if (faceletsDefaultSuffix != null)
        {
            for (String defaultSuffix : jspDefaultSuffixes)
            {
                if (faceletsDefaultSuffix.equals(defaultSuffix))
                {
                    faceletsDefaultSuffix = null;
                    break;
                }
            }
        }
        if (faceletsDefaultSuffix != null)
        {
            builder.setLength(0);
            builder.append(requestViewId);
            
            if (extensionPos > -1 && extensionPos > slashPos)
            {
                builder.replace(extensionPos, requestViewId.length(), faceletsDefaultSuffix);
            }
            else
            {
                builder.append(faceletsDefaultSuffix);
            }
            
            String candidateViewId = builder.toString();
            if (checkViewExists(context,candidateViewId))
            {
                return candidateViewId;
            }
        }

        // Otherwise, if a physical resource exists with the name requestViewId let that value be viewId.
        if (checkViewExists(context,requestViewId))
        {
            return requestViewId;
        }
        
        //Otherwise return null.
        return null;
    }
    
    protected boolean checkViewExists(FacesContext facesContext, String viewId)
    {
        if (checkedViewIdsCache == null)
        {
            checkedViewIdsCache = CheckedViewIdsCache.getInstance(facesContext);
        }
        
        try
        {
            Boolean resourceExists = null;
            if (checkedViewIdsCache.isEnabled())
            {
                resourceExists = checkedViewIdsCache.get(viewId);
            }

            if (resourceExists == null)
            {
                ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler()
                        .getViewDeclarationLanguage(facesContext, viewId);
                if (vdl != null)
                {
                    resourceExists = vdl.viewExists(facesContext, viewId);
                }
                else
                {
                    // Fallback to default strategy
                    resourceExists = facesContext.getExternalContext().getResource(viewId) != null;
                }

                if (checkedViewIdsCache.isEnabled())
                {
                    checkedViewIdsCache.put(viewId, resourceExists);
                }
            }

            return resourceExists;
        }
        catch (MalformedURLException e)
        {
            //ignore and move on
        }     
        return false;
    }

}
