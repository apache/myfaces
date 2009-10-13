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

/**
 * A ViewHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports javax.servlet, and uses a standard web.xml file.
 *
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultViewHandlerSupport implements ViewHandlerSupport
{
    /**
     * Identifies the FacesServlet mapping in the current request map.
     */
    private static final String CACHED_SERVLET_MAPPING =
        DefaultViewHandlerSupport.class.getName() + ".CACHED_SERVLET_MAPPING";

    //private static final Log log = LogFactory.getLog(DefaultViewHandlerSupport.class);
    private static final Logger log = Logger.getLogger(DefaultViewHandlerSupport.class.getName());

    public String calculateViewId(FacesContext context, String viewId)
    {
        FacesServletMapping mapping = getFacesServletMapping(context);
        if (mapping == null || mapping.isExtensionMapping())
        {
            viewId = handleSuffixMapping(context, viewId);
        }
        else if(mapping.isPrefixMapping())
        {
            viewId = handlePrefixMapping(viewId,mapping.getPrefix());
        }
        else if (viewId != null && mapping.getUrlPattern().startsWith(viewId))
        {
            throw new InvalidViewIdException(viewId);
        }

        if(viewId != null)
        {
            return (checkResourceExists(context,viewId) ? viewId : null);
        }

        return null;    // return null if no physical resource exists
    }

    public String calculateActionURL(FacesContext context, String viewId)
    {
        if (viewId == null || !viewId.startsWith("/"))
        {
            throw new IllegalArgumentException("ViewId must start with a '/': " + viewId);
        }

        FacesServletMapping mapping = getFacesServletMapping(context);
        ExternalContext externalContext = context.getExternalContext();
        String contextPath = externalContext.getRequestContextPath();
        StringBuilder builder = new StringBuilder(contextPath);
        if (mapping != null)
        {
            if (mapping.isExtensionMapping())
            {
                String[] contextSuffixes = getContextSuffix(context); 
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
                    if(viewId.lastIndexOf(".") != -1 )
                    {
                        builder.append(viewId.substring(0,viewId.lastIndexOf(".")));
                    }
                    else
                    {
                        builder.append(viewId);
                    }
                    builder.append(contextSuffixes[0]);
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
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Calculated actionURL: '" + calculatedActionURL + "' for viewId: '" + viewId + "'");
        }
        return calculatedActionURL;
    }

    /**
     * Read the web.xml file that is in the classpath and parse its internals to
     * figure out how the FacesServlet is mapped for the current webapp.
     */
    protected FacesServletMapping getFacesServletMapping(FacesContext context)
    {
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

        // Has the mapping already been determined during this request?
        if (!requestMap.containsKey(CACHED_SERVLET_MAPPING))
        {
            ExternalContext externalContext = context.getExternalContext();
            FacesServletMapping mapping =
                calculateFacesServletMapping(
                    externalContext.getRequestServletPath(),
                    externalContext.getRequestPathInfo());

            requestMap.put(CACHED_SERVLET_MAPPING, mapping);
        }

        return (FacesServletMapping) requestMap.get(CACHED_SERVLET_MAPPING);
    }

    /**
     * Determines the mapping of the FacesServlet in the web.xml configuration
     * file. However, there is no need to actually parse this configuration file
     * as runtime information is sufficient.
     *
     * @param servletPath The servletPath of the current request
     * @param pathInfo    The pathInfo of the current request
     * @return the mapping of the FacesServlet in the web.xml configuration file
     */
    protected static FacesServletMapping calculateFacesServletMapping(
        String servletPath, String pathInfo)
    {
        if (pathInfo != null)
        {
            // If there is a "extra path", it's definitely no extension mapping.
            // Now we just have to determine the path which has been specified
            // in the url-pattern, but that's easy as it's the same as the
            // current servletPath. It doesn't even matter if "/*" has been used
            // as in this case the servletPath is just an empty string according
            // to the Servlet Specification (SRV 4.4).
            return FacesServletMapping.createPrefixMapping(servletPath);
        }
        else
        {
            // In the case of extension mapping, no "extra path" is available.
            // Still it's possible that prefix-based mapping has been used.
            // Actually, if there was an exact match no "extra path"
            // is available (e.g. if the url-pattern is "/faces/*"
            // and the request-uri is "/context/faces").
            int slashPos = servletPath.lastIndexOf('/');
            int extensionPos = servletPath.lastIndexOf('.');
            if (extensionPos > -1 && extensionPos > slashPos)
            {
                String extension = servletPath.substring(extensionPos);
                return FacesServletMapping.createExtensionMapping(extension);
            }
            else
            {
                // There is no extension in the given servletPath and therefore
                // we assume that it's an exact match using prefix-based mapping.
                return FacesServletMapping.createPrefixMapping(servletPath);
            }
        }
    }

    protected String[] getContextSuffix(FacesContext context)
    {
        String defaultSuffix = context.getExternalContext().getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
        if (defaultSuffix == null)
        {
            defaultSuffix = ViewHandler.DEFAULT_SUFFIX;
        }
        return defaultSuffix.split(" ");
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
        String faceletsViewMappings= context.getExternalContext().getInitParameter(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME);
        if(faceletsViewMappings == null)    //consider alias facelets.VIEWMAPPINGS
        {
            faceletsViewMappings= context.getExternalContext().getInitParameter("facelets.VIEWMAPPINGS");
        }
        
        return faceletsViewMappings == null ? null : faceletsViewMappings.split(";");
    }

    /**
     * Return the normalized viewId according to the algorithm specified in 7.5.2 
     * by stripping off any number of occurrences of the prefix mapping from the viewId.
     * <p/>
     * For example, both /faces/view.xhtml and /faces/faces/faces/view.xhtml would both return view.xhtml
     * F 
     */
    protected String handlePrefixMapping(String viewId, String prefix)
    {
        /*  If prefix mapping (such as "/faces/*") is used for FacesServlet, normalize the viewId according to the following
            algorithm, or its semantic equivalent, and return it.
               
            Remove any number of occurrences of the prefix mapping from the viewId. For example, if the incoming value
            was /faces/faces/faces/view.xhtml the result would be simply view.xhtml.
         */
        String uri = viewId;
        prefix = prefix + '/';  //need to make sure its really /faces/* and not /facesPage.xhtml
        while (uri.startsWith(prefix)) 
        {
            uri = uri.substring(prefix.length() - 1);    //cut off only /faces, leave the trailing '/' char for the next iteration
        }
        //now delete any remaining leading '/'
        // TODO: CJH: I don't think this is correct, considering that getActionURL() expects everything to
        // start with '/', and in the suffix case we only mess with the suffix and leave leading
        // slashes alone.  Please review...
        /*if(uri.startsWith("/"))
        {
            uri = uri.substring(1);
        }*/
        
        return uri;
    }
    
    /**
     * Return the viewId with any non-standard suffix stripped off and replaced with
     * the default suffix configured for the specified context.
     * <p/>
     * For example, an input parameter of "/foo.jsf" may return "/foo.jsp".
     */
    protected String handleSuffixMapping(FacesContext context, String requestViewId)
    {
        String[] faceletsViewMappings = getFaceletsViewMappings(context);
        String[] jspDefaultSuffixes = getContextSuffix(context);
        
        int slashPos = requestViewId.lastIndexOf('/');
        int extensionPos = requestViewId.lastIndexOf('.');
        
        //Try to locate any resource that match with the expected id
        for (String defaultSuffix : jspDefaultSuffixes)
        {
            StringBuilder builder = new StringBuilder(requestViewId);
           
            if (extensionPos > -1 && extensionPos > slashPos)
            {
                builder.replace(extensionPos, requestViewId.length(), defaultSuffix);
            }
            else
            {
                builder.append(defaultSuffix);
            }
            String candidateViewId = builder.toString();
            
            if( faceletsViewMappings != null && faceletsViewMappings.length > 0 )
            {
                for (String mapping : faceletsViewMappings)
                {
                    if(mapping.startsWith("/"))
                    {
                        continue;   //skip this entry, its a prefix mapping
                    }
                    if(mapping.equals(candidateViewId))
                    {
                        return candidateViewId;
                    }
                    if(mapping.startsWith(".")) //this is a wildcard entry
                    {
                        builder.setLength(0); //reset/reuse the builder object 
                        builder.append(candidateViewId); 
                        builder.replace(candidateViewId.lastIndexOf('.'), candidateViewId.length(), mapping);
                        String tempViewId = builder.toString();
                        if(checkResourceExists(context,tempViewId))
                            return tempViewId;
                    }
                }
            }

            // forced facelets mappings did not match or there were no entries in faceletsViewMappings array
            if(checkResourceExists(context,candidateViewId))
                return candidateViewId;
                       
        }
        
        //jsp suffixes didn't match, try facelets suffix
        String faceletsDefaultSuffix = this.getFaceletsContextSuffix(context);
        StringBuilder builder = new StringBuilder(requestViewId);
        
        if (extensionPos > -1 && extensionPos > slashPos)
        {
            builder.replace(extensionPos, requestViewId.length(), faceletsDefaultSuffix);
        }
        else
        {
            builder.append(faceletsDefaultSuffix);
        }
        
        String candidateViewId = builder.toString();
        if(checkResourceExists(context,candidateViewId))
            return candidateViewId;

        if(checkResourceExists(context,requestViewId))
            return requestViewId;
        
        return null;
    }
    
    protected boolean checkResourceExists(FacesContext context, String viewId)
    {
        try
        {
            if (context.getExternalContext().getResource(viewId) != null)
            {
                return true;
            }                                 
        }
        catch(MalformedURLException e)
        {
            //ignore and move on
        }     
        return false;
    }

    /**
     * Represents a mapping entry of the FacesServlet in the web.xml
     * configuration file.
     */
    protected static class FacesServletMapping
    {

        /**
         * The path ("/faces", for example) which has been specified in the
         * url-pattern of the FacesServlet mapping.
         */
        private String prefix;

        /**
         * The extension (".jsf", for example) which has been specified in the
         * url-pattern of the FacesServlet mapping.
         */
        private String extension;

        /**
         * Creates a new FacesServletMapping object using prefix mapping.
         *
         * @param path The path ("/faces", for example) which has been specified
         *             in the url-pattern of the FacesServlet mapping.
         * @return a newly created FacesServletMapping
         */
        public static FacesServletMapping createPrefixMapping(String path)
        {
            FacesServletMapping mapping = new FacesServletMapping();
            mapping.setPrefix(path);
            return mapping;
        }

        /**
         * Creates a new FacesServletMapping object using extension mapping.
         *
         * @param path The extension (".jsf", for example) which has been
         *             specified in the url-pattern of the FacesServlet mapping.
         * @return a newly created FacesServletMapping
         */
        public static FacesServletMapping createExtensionMapping(
            String extension)
        {
            FacesServletMapping mapping = new FacesServletMapping();
            mapping.setExtension(extension);
            return mapping;
        }

        /**
         * Returns the path ("/faces", for example) which has been specified in
         * the url-pattern of the FacesServlet mapping. If this mapping is based
         * on an extension, <code>null</code> will be returned. Note that this
         * path is not the same as the specified url-pattern as the trailing
         * "/*" is omitted.
         *
         * @return the path which has been specified in the url-pattern
         */
        public String getPrefix()
        {
            return prefix;
        }

        /**
         * Sets the path ("/faces/", for example) which has been specified in
         * the url-pattern.
         *
         * @param path The path which has been specified in the url-pattern
         */
        public void setPrefix(String path)
        {
            this.prefix = path;
        }

        /**
         * Returns the extension (".jsf", for example) which has been specified
         * in the url-pattern of the FacesServlet mapping. If this mapping is
         * not based on an extension, <code>null</code> will be returned.
         *
         * @return the extension which has been specified in the url-pattern
         */
        public String getExtension()
        {
            return extension;
        }

        /**
         * Sets the extension (".jsf", for example) which has been specified in
         * the url-pattern of the FacesServlet mapping.
         *
         * @param extension The extension which has been specified in the url-pattern
         */
        public void setExtension(String extension)
        {
            this.extension = extension;
        }

        /**
         * Indicates whether this mapping is based on an extension (e.g.
         * ".jsp").
         *
         * @return <code>true</code>, if this mapping is based is on an
         *         extension, <code>false</code> otherwise
         */
        public boolean isExtensionMapping()
        {
            return extension != null;
        }

        /**
         * Indicates whether this mapping is based on a prefix (e.g.
         * /faces/*").
         *
         * @return <code>true</code>, if this mapping is based is on a
         *         prefix, <code>false</code> otherwise
         */
        public boolean isPrefixMapping()
        {
            return prefix != null;
        }
        
        /**
         * Returns the url-pattern entry for this servlet mapping.
         *
         * @return the url-pattern entry for this servlet mapping
         */
        public String getUrlPattern()
        {
            if (isExtensionMapping())
            {
                return "*" + extension;
            }
            else
            {
                return prefix + "/*";
            }
        }

    }
}
