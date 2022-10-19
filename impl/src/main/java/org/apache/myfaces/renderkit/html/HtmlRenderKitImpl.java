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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.RendererWrapper;
import jakarta.faces.render.ResponseStateManager;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderKit;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.renderkit.LazyRenderKit;
import org.apache.myfaces.renderkit.ContentTypeUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.util.lang.ClassUtils;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderKit(renderKitId = "HTML_BASIC")
public class HtmlRenderKitImpl extends RenderKit implements LazyRenderKit
{
    private static final Logger log = Logger.getLogger(HtmlRenderKitImpl.class.getName());

    // ~ Instance fields ----------------------------------------------------------------------------

    private Map<String, Map<String, Renderer>> _renderers;
    private ResponseStateManager _responseStateManager;
    private Map<String, ClientBehaviorRenderer> _clientBehaviorRenderers;
    private MyfacesConfig myfacesConfig;
    
    // ~ Constructors -------------------------------------------------------------------------------

    public HtmlRenderKitImpl()
    {
        _renderers = new ConcurrentHashMap<>(64, 0.75f, 1);
        _responseStateManager = new HtmlResponseStateManager();
        _clientBehaviorRenderers = new HashMap<>();
        myfacesConfig = MyfacesConfig.getCurrentInstance();
    }

    // ~ Methods ------------------------------------------------------------------------------------

    @Override
    public void addClientBehaviorRenderer(String type, ClientBehaviorRenderer renderer)
    {
        Assert.notNull(type, "type");
        Assert.notNull(renderer, "renderer");
        
        _clientBehaviorRenderers.put(type, renderer);
    }
    
    @Override
    public ClientBehaviorRenderer getClientBehaviorRenderer(String type)
    {
        Assert.notNull(type, "type");
        
        return _clientBehaviorRenderers.get(type);
    }
    
    @Override
    public Iterator<String> getClientBehaviorRendererTypes()
    {
        return _clientBehaviorRenderers.keySet().iterator();
    }
    
    @Override
    public Renderer getRenderer(String componentFamily, String rendererType)
    {
        Assert.notNull(componentFamily, "componentFamily");
        Assert.notNull(rendererType, "rendererType");
        
        Map <String,Renderer> familyRendererMap = _renderers.get(componentFamily); 
        Renderer renderer = null;
        if (familyRendererMap != null)
        {
            renderer = familyRendererMap.get(rendererType);
        }
        if (renderer == null)
        {
            log.warning("Unsupported component-family/renderer-type: " + componentFamily + '/' + rendererType);
        }
        if (renderer instanceof LazyRendererWrapper)
        {
            renderer = ((LazyRendererWrapper)renderer).getWrapped();
            familyRendererMap.put(rendererType, renderer);
        }
        return renderer;
    }

    @Override
    public void addRenderer(String componentFamily, String rendererType, Renderer renderer)
    {
        Assert.notNull(componentFamily, "componentFamily");
        Assert.notNull(rendererType, "rendererType");
        Assert.notNull(renderer, "renderer");

        _put(componentFamily, rendererType, renderer);

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("add Renderer family = " + componentFamily + " rendererType = " + rendererType
                    + " renderer class = " + renderer.getClass().getName());
        }
    }
    
    @Override
    public void addRenderer(String componentFamily, String rendererType, String rendererClass)
    {
        Assert.notNull(componentFamily, "componentFamily");
        Assert.notNull(rendererType, "rendererType");
        Assert.notNull(rendererClass, "rendererClass");

        _put(componentFamily, rendererType, new LazyRendererWrapper(rendererClass));

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("add Renderer family = " + componentFamily + " rendererType = " + rendererType
                    + " renderer class = " + rendererClass);
        }
    }

    /**
     * Put the renderer on the double map
     * 
     * @param componentFamily
     * @param rendererType
     * @param renderer
     */
    synchronized private void _put(String componentFamily, String rendererType, Renderer renderer)
    {
        Map <String,Renderer> familyRendererMap = _renderers.get(componentFamily);
        if (familyRendererMap == null)
        {
            familyRendererMap = new ConcurrentHashMap<>(8, 0.75f, 1);
            _renderers.put(componentFamily, familyRendererMap);
        }
        else
        {
            if (familyRendererMap.get(rendererType) != null)
            {
                // this is not necessarily an error, but users do need to be
                // very careful about jar processing order when overriding
                // some component's renderer with an alternate renderer.
                log.fine("Overwriting renderer with family = " + componentFamily +
                   " rendererType = " + rendererType +
                   " renderer class = " + renderer.getClass().getName());
            }
        }
        familyRendererMap.put(rendererType, renderer);
    }

    @Override
    public ResponseStateManager getResponseStateManager()
    {
        return _responseStateManager;
    }
    
    /**
     * @since Faces 2.0
     */
    @Override
    public Iterator<String> getComponentFamilies()
    {
        //return _families.keySet().iterator();
        return _renderers.keySet().iterator();
    }
    
    /**
     * @since Faces 2.0
     */
    @Override
    public Iterator<String> getRendererTypes(String componentFamily)
    {
        //Return an Iterator over the renderer-type entries for the given component-family.
        Map<String, Renderer> map = _renderers.get(componentFamily);
        if (map != null)
        {
            return map.keySet().iterator();
        }

        //If the specified componentFamily is not known to this RenderKit implementation, return an empty Iterator
        return Collections.<String>emptySet().iterator();
    }

    @Override
    public ResponseWriter createResponseWriter(Writer writer, String contentTypeListString, String characterEncoding)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String selectedContentType = null;
        String writerContentType = null;
        boolean isAjaxRequest = facesContext.getPartialViewContext().isAjaxRequest();
        String contentTypeListStringFromAccept = null;

        // To detect the right contentType, we need to check if the request is an ajax request or not.
        // If it is an ajax request, HTTP Accept header content type will be set for the ajax itself, which
        // is application/xml or text/xml. In that case, there are two response writers
        // (PartialResponseWriterImpl and HtmlResponseWriterImpl),
        
        //1. if there is a passed contentTypeListString, it takes precedence over accept header
        if (contentTypeListString != null)
        {
            selectedContentType = ContentTypeUtils.chooseWriterContentType(contentTypeListString, 
                    ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES, 
                    isAjaxRequest ? ContentTypeUtils.AJAX_XHTML_ALLOWED_CONTENT_TYPES :
                                    ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES);
        }

        //2. If no selectedContentType
        //   try to derive it from accept header
        if (selectedContentType == null)
        {
            contentTypeListStringFromAccept = 
                ContentTypeUtils.getContentTypeFromAcceptHeader(facesContext);
            
            if (contentTypeListStringFromAccept != null)
            {
                selectedContentType = ContentTypeUtils.chooseWriterContentType(contentTypeListStringFromAccept,
                        ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES, 
                        isAjaxRequest ? ContentTypeUtils.AJAX_XHTML_ALLOWED_CONTENT_TYPES :
                                        ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES);
            }
        }

        //3. if no selectedContentType was derived, set default from the param 
        if (selectedContentType == null)
        {
            if (contentTypeListString == null && contentTypeListStringFromAccept == null)
            {
                //If no contentTypeList, return the default
                selectedContentType = myfacesConfig.getDefaultResponseWriterContentTypeMode();
            }
            else
            {
                // If a contentTypeList was passed and we don't have direct matches, we still need
                // to check if */* is found and if that so return the default, otherwise throw
                // exception.
                if (contentTypeListString != null)
                {
                    String[] contentTypes = ContentTypeUtils.splitContentTypeListString(contentTypeListString);
                    if (ContentTypeUtils.containsContentType(ContentTypeUtils.ANY_CONTENT_TYPE, contentTypes))
                    {
                        selectedContentType = myfacesConfig.getDefaultResponseWriterContentTypeMode();
                    }
                }
                
                if (selectedContentType == null)
                {
                    if (contentTypeListStringFromAccept != null)
                    {
                        String[] contentTypes = ContentTypeUtils.splitContentTypeListString(
                                contentTypeListStringFromAccept);
                        if (ContentTypeUtils.containsContentType(ContentTypeUtils.ANY_CONTENT_TYPE, contentTypes))
                        {
                            selectedContentType = myfacesConfig.getDefaultResponseWriterContentTypeMode();
                        }
                    }
                    else if (isAjaxRequest)
                    {
                        // If is an ajax request, contentTypeListStringFromAccept == null and 
                        // contentTypeListString != null, contentTypeListString should not be taken 
                        // into account, because the final content type in this case is for PartialResponseWriter 
                        // implementation. In this case rfc2616-sec14 takes precedence:
                        // 
                        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
                        // 14.1 Accept 
                        // If no Accept header field is present, then it is assumed that the client 
                        // accepts all media types.
                        selectedContentType = myfacesConfig.getDefaultResponseWriterContentTypeMode();
                    }
                    if (selectedContentType == null)
                    {
                        // Note this case falls when contentTypeListStringFromAccept == null and 
                        // contentTypeListString != null, but since this not an ajax request, 
                        // contentTypeListString should be taken strictly and throw IllegalArgumentException
                        throw new IllegalArgumentException(
                                "ContentTypeList does not contain a supported content type: "
                                        + ((contentTypeListString != null) ? 
                                                contentTypeListString : contentTypeListStringFromAccept) );
                    }
                }
            }
        }
        if (isAjaxRequest)
        {
            // If HTTP Accept header has application/xml or text/xml, that does not means the writer
            // content type mode should be set to application/xhtml+xml.
            writerContentType = selectedContentType.contains(ContentTypeUtils.XHTML_CONTENT_TYPE) ?
                    ContentTypeUtils.XHTML_CONTENT_TYPE : ContentTypeUtils.HTML_CONTENT_TYPE;
        }
        else
        {
            writerContentType = ContentTypeUtils.isXHTMLContentType(selectedContentType) ? 
                    ContentTypeUtils.XHTML_CONTENT_TYPE : ContentTypeUtils.HTML_CONTENT_TYPE;
        }
        
        if (characterEncoding == null)
        {
            characterEncoding = ContentTypeUtils.DEFAULT_CHAR_ENCODING;
        }

        if (myfacesConfig.isEarlyFlushEnabled())
        {
            return new EarlyFlushHtmlResponseWriterImpl(writer, selectedContentType, characterEncoding, 
                myfacesConfig.isWrapScriptContentWithXmlCommentTag(), writerContentType);
        }
        else
        {
            return new HtmlResponseWriterImpl(writer, selectedContentType, characterEncoding, 
                myfacesConfig.isWrapScriptContentWithXmlCommentTag(), writerContentType);
        }
    }

    @Override
    public ResponseStream createResponseStream(OutputStream outputStream)
    {
        return new MyFacesResponseStream(outputStream);
    }

    private static class MyFacesResponseStream extends ResponseStream
    {
        private OutputStream output;

        public MyFacesResponseStream(OutputStream output)
        {
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException
        {
            output.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException
        {
            output.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            output.write(b, off, len);
        }

        @Override
        public void flush() throws IOException
        {
            output.flush();
        }

        @Override
        public void close() throws IOException
        {
            output.close();
        }
    }
    
    private static class LazyRendererWrapper extends RendererWrapper
    {
        private String rendererClass;
        private Renderer delegate;
        
        public LazyRendererWrapper(String rendererClass)
        {
            this.rendererClass = rendererClass;
        }

        @Override
        public Renderer getWrapped()
        {
            if (delegate == null)
            {
                delegate = (Renderer) ClassUtils.newInstance(
                    ClassUtils.simpleClassForName(rendererClass));
            }
            return delegate;
        }
    }
}
