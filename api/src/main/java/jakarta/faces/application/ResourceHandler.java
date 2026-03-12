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
package jakarta.faces.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * @since 2.0
 */
public abstract class ResourceHandler
{
    public static final String LOCALE_PREFIX = "jakarta.faces.resource.localePrefix";
    public static final String RESOURCE_EXCLUDES_DEFAULT_VALUE = ".class .jsp .jspx .properties .xhtml .groovy";
    
    /**
     * Space separated file extensions that will not be served by the default ResourceHandler implementation.
     */
    @JSFWebConfigParam(defaultValue=".class .jsp .jspx .properties .xhtml .groovy",since="2.0", group="resources")
    public static final String RESOURCE_EXCLUDES_PARAM_NAME = "jakarta.faces.RESOURCE_EXCLUDES";
    public static final String RESOURCE_IDENTIFIER = "/jakarta.faces.resource";
    
    /**
     * @since 2.2
     */
    public static final String RESOURCE_CONTRACT_XML = "jakarta.faces.contract.xml";
    
    /**
     * @since 2.2
     */
    public static final String WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME = "jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY";

    /**
     * @since 2.2
     */
    public static final String WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME = "jakarta.faces.WEBAPP_RESOURCES_DIRECTORY";

    /**
     * @since 4.0
     */
    public static final String FACES_SCRIPT_RESOURCE_NAME = "faces.js";

    /**
     * @since 4.0
     */
    public static final String FACES_SCRIPT_LIBRARY_NAME = "jakarta.faces";

    /**
     * @since 5.0
     */
    public static final String WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE = "resources";

    /**
     * @since 5.0
     */
    public static final String WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE = "contracts";

    /**
     * <p class="changed_added_5_0">
     * The boolean context parameter name to explicitly enable Content Security Policy (CSP) nonce generation.
     * When this parameter is set to {@code true}, the runtime generates a Content Security Policy (CSP) nonce value
     * for the current view.
     * The generated nonce can be obtained via {@link #getCurrentNonce(jakarta.faces.context.FacesContext)}.
     * If this context parameter is not enabled, no nonce is generated and
     * {@link #getCurrentNonce(jakarta.faces.context.FacesContext)} returns {@code null}.
     * </p>
     * <p>
     * When this parameter is set to {@code true}, the runtime will also consult {@link #CSP_POLICY_PARAM_NAME} if
     * present,
     * or fall back to {@link #DEFAULT_CSP_POLICY} to determine the exact policy sent in the
     * <code>Content-Security-Policy</code> response header.
     * </p>
     *
     * @since 5.0
     */
    public static final String ENABLE_CSP_NONCE_PARAM_NAME = "jakarta.faces.ENABLE_CSP_NONCE";

    /**
     * <p class="changed_added_5_0">
     * The string context parameter name to determine the Content Security Policy (CSP) policy to be sent in the
     * <code>Content-Security-Policy</code> response header.
     * This parameter is only consulted and applied when {@link #ENABLE_CSP_NONCE_PARAM_NAME} is explicitly set to
     * {@code true}.
     * If nonce generation is not enabled, this parameter is ignored and no <code>Content-Security-Policy</code>
     * header is added.
     * </p>
     * <p>
     * The value must be a valid CSP policy string and <strong>must</strong> include the expression
     * <code>#{nonce}</code> (e.g., <code>'nonce-#{nonce}'</code>),
     * which the runtime will substitute with the nonce value of the current view as returned by
     * {@link #getCurrentNonce(jakarta.faces.context.FacesContext)}.
     * For ajax requests, the nonce remains the same as long as the view lives. For non-ajax (full page) requests,
     * a new nonce is generated.
     * Other EL expressions besides the special <code>#{nonce}</code> placeholder will be evaluated on a per-request
     * basis using the current {@link ELContext}.
     * If this parameter is not specified, the value defined by {@link #DEFAULT_CSP_POLICY} is used.
     * </p>
     * <p>
     * Example values:
     * </p>
     * <ul>
     * <li>Default: <code>script-src 'self' 'nonce-#{nonce}' 'strict-dynamic'</code></li>
     * <li>Extended: <code>script-src 'self' 'nonce-#{nonce}' 'strict-dynamic' https://analytics.google.com</code></li>
     * <li>With expressions:
     * <code>script-src 'self' 'nonce-#{nonce}' 'strict-dynamic' #{someBean.spaceSeparatedStringOfAllowedScriptSources};
     * style-src 'self' #{someBean.spaceSeparatedStringOfAllowedStyleSources}; frame-ancestors 'none'</code></li>
     * </ul>
     *
     * @since 5.0
     */
    public static final String CSP_POLICY_PARAM_NAME = "jakarta.faces.CSP_POLICY";

    /**
     * <p class="changed_added_5_0">
     * The default value for the <code>Content-Security-Policy</code> response header if the
     * {@link #ENABLE_CSP_NONCE_PARAM_NAME} is set to {@code true} <strong>and</strong> the
     * {@link #CSP_POLICY_PARAM_NAME} is not provided.
     * The value is <code>{@value}</code>.
     * The runtime will substitute <code>#{nonce}</code> with the actual nonce value on each request.
     * </p>
     *
     * @since 5.0
     */
    public static final String DEFAULT_CSP_POLICY = "script-src 'self' 'nonce-#{nonce}' 'strict-dynamic'";

    private final static String RENDERED_RESOURCES_SET = "org.apache.myfaces.RENDERED_RESOURCES_SET";

    public abstract Resource createResource(String resourceName);
    
    public abstract Resource createResource(String resourceName, String libraryName);
    
    public abstract Resource createResource(String resourceName, String libraryName, String contentType);
    
    public abstract String getRendererTypeForResourceName(String resourceName);
    
    public abstract void handleResourceRequest(FacesContext context) throws IOException;
    
    public abstract boolean isResourceRequest(FacesContext context);
    
    public abstract  boolean libraryExists(String libraryName);
    
    /**
     * @since 2.2
     * @param resourceId
     * @return 
     */
    public Resource createResourceFromId(String resourceId)
    {
        return null;
    }
    
    /**
     * 
     * @since 2.2
     * @param context
     * @param resourceName
     * @return 
     */
    public ViewResource createViewResource(FacesContext context,
                                       String resourceName)
    {
        return context.getApplication().getResourceHandler().createResource(resourceName);
    }
    
    public boolean isResourceURL(java.lang.String url)
    {
        Assert.notNull(url);
        return url.contains(RESOURCE_IDENTIFIER);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param path
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViewResources(
            FacesContext facesContext, String path, ResourceVisitOption... options)
    {
        return getViewResources(facesContext, path, Integer.MAX_VALUE, options);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param path
     * @param maxDepth
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViewResources(FacesContext facesContext, 
            String path, int maxDepth, ResourceVisitOption... options)
    {
        return null;
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param resourceName
     * @param libraryName
     * @return 
     */
    public boolean isResourceRendered(FacesContext facesContext, String resourceName, String libraryName)
    {
        return getRenderedResources(facesContext).containsKey(
                libraryName != null ? libraryName+'/'+resourceName : resourceName);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param resourceName
     * @param libraryName 
     */
    public void markResourceRendered(FacesContext facesContext, String resourceName, String libraryName)
    {
        getRenderedResources(facesContext).put(
                libraryName != null ? libraryName+'/'+resourceName : resourceName, Boolean.TRUE);
    }
    
    /**
     * Return a set of already rendered resources by this renderer on the current
     * request. 
     * 
     * @param facesContext
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> getRenderedResources(FacesContext facesContext)
    {
        Map<String, Boolean> map = (Map<String, Boolean>) facesContext.getViewRoot().getTransientStateHelper()
                .getTransient(RENDERED_RESOURCES_SET);
        if (map == null)
        {
            map = new HashMap<>();
            facesContext.getViewRoot().getTransientStateHelper().putTransient(RENDERED_RESOURCES_SET,map);
        }
        return map;
    }

    /**
     * <p class="changed_added_5_0">
     * Returns the Content Security Policy (CSP) nonce for the current request, or {@code null} if CSP nonce support is
     * not enabled. When enabled via
     * {@link #ENABLE_CSP_NONCE_PARAM_NAME}, then the runtime ensures that each view has a consistent nonce value that
     * can be used to allow inline scripts to execute safely.
     * </p>
     *
     * <p>
     * The returned nonce is intended to be used:
     * </p>
     *
     * <ul>
     *   <li>As the value of the {@code nonce} attribute on rendered {@code <script>} elements.</li>
     *   <li>By the runtime to replace the <code>#{nonce}</code> expression in the {@link #CSP_POLICY_PARAM_NAME} when
     *   generating the {@code Content-Security-Policy} response header.</li>
     * </ul>
     *
     * <p>
     * Implementations must generate a unique nonce for the current view and save it in the
     * {@link jakarta.faces.component.UIViewRoot#getViewMap view map}.
     * The same nonce will be returned for the duration of the view, including ajax requests.
     * For non-ajax postbacks, a new nonce must be generated, as the full page is re-rendered with a new
     * {@code Content-Security-Policy} response header.
     * For backward compatibility, a default implementation is provided that returns {@code null}.
     * </p>
     *
     * @param context The {@link FacesContext} for this request.
     * @return a Base64-encoded CSP nonce value generated from at least 128 bits of a cryptographically secure random
     * source, or {@code null} if CSP nonce support is not enabled.
     *
     * @since 5.0
     */
    public String getCurrentNonce(FacesContext context)
    {
        return null;
    }
}
