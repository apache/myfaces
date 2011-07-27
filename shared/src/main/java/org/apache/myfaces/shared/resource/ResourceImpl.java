/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.shared.resource;

import static org.apache.myfaces.shared.renderkit.html.util.ResourceUtils.JAVAX_FACES_LIBRARY_NAME;
import static org.apache.myfaces.shared.renderkit.html.util.ResourceUtils.JSF_JS_RESOURCE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 * Default implementation for resources
 * 
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 957739 $ $Date: 2010-06-24 17:27:40 -0500 (Jue, 24 Jun 2010) $
 */
public class ResourceImpl extends Resource
{

    private ResourceMeta _resourceMeta;
    private ResourceLoader _resourceLoader;
    private ResourceHandlerSupport _resourceHandlerSupport;
    
    public ResourceImpl(ResourceMeta resourceMeta, 
            ResourceLoader resourceLoader, ResourceHandlerSupport support, String contentType)
    {
        _resourceMeta = resourceMeta;
        _resourceLoader = resourceLoader;
        _resourceHandlerSupport = support;
        setLibraryName(resourceMeta.getLibraryName());
        setResourceName(resourceMeta.getResourceName());
        setContentType(contentType);
    }
    
    public ResourceLoader getResourceLoader()
    {
        return _resourceLoader;
    }    
    
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (couldResourceContainValueExpressions())
        {
            return new ValueExpressionFilterInputStream(
                    getResourceLoader().getResourceInputStream(_resourceMeta)); 
        }
        else
        {
            return getResourceLoader().getResourceInputStream(_resourceMeta);            
        }
    }
    
    private boolean couldResourceContainValueExpressions()
    {
        if (_resourceMeta.couldResourceContainValueExpressions())
        {
            return true;
        }
        else
        {
            //By default only css resource contain value expressions
            String contentType = getContentType();
    
            return ("text/css".equals(contentType));
        }
    }

    private class ValueExpressionFilterInputStream extends InputStream
    {
        private PushbackInputStream delegate;
        
        public ValueExpressionFilterInputStream(InputStream in)
        {
            super();
            delegate = new PushbackInputStream(in,255);
        }

        @Override
        public int read() throws IOException
        {
            int c1 = delegate.read();
            
            if (c1 == -1) return -1;
            
            if ( ((char)c1) == '#')
            {
                int c2 = delegate.read();
                if (c2 == -1) return -1;
                if (((char)c2) == '{')
                {
                    //It is a value expression. We need
                    //to look for a occurrence of } to 
                    //extract the expression and evaluate it,
                    //the result should be unread.
                    List<Integer> expressionList = new ArrayList<Integer>();
                    int c3 = delegate.read();
                    while ( c3 != -1 && ((char)c3) != '}' )
                    {
                        expressionList.add(c3);
                        c3 = delegate.read();
                    }
                    
                    if (c3 == -1)
                    {
                        //get back the data, because we can't
                        //extract any value expression
                        for (int i = 0; i < expressionList.size(); i++)
                        {
                            delegate.unread(expressionList.get(i));
                        }
                        delegate.unread(c2);
                        return c1;
                    }
                    else
                    {
                        //EL expression found. Evaluate it and pushback
                        //the result into the stream
                        FacesContext context = FacesContext.getCurrentInstance();
                        ELContext elContext = context.getELContext();
                        try
                        {
                            ValueExpression ve = context.getApplication().
                                getExpressionFactory().createValueExpression(
                                        elContext,
                                        "#{"+convertToExpression(expressionList)+"}",
                                        String.class);
                            String value = (String) ve.getValue(elContext);
                            
                            for (int i = value.length()-1; i >= 0 ; i--)
                            {
                                delegate.unread((int) value.charAt(i));
                            }
                        }
                        catch(ELException e)
                        {
                            ExceptionQueuedEventContext equecontext = new ExceptionQueuedEventContext (context, e, null);
                            context.getApplication().publishEvent (context, ExceptionQueuedEvent.class, equecontext);
                            
                            Logger log = Logger.getLogger(ResourceImpl.class.getName());
                            if (log.isLoggable(Level.SEVERE))
                                log.severe("Cannot evaluate EL expression "+convertToExpression(expressionList)+ " in resource " + getLibraryName()+":"+getResourceName());
                            
                            delegate.unread(c3);
                            for (int i = expressionList.size()-1; i >= 0; i--)
                            {
                                delegate.unread(expressionList.get(i));
                            }
                            delegate.unread(c2);
                            return c1;
                        }
                        
                        //read again
                        return delegate.read();
                    }
                }
                else
                {
                    delegate.unread(c2);
                    return c1;
                }
            }
            else
            {
                //just continue
                return c1;
            }
        }
        
        private String convertToExpression(List<Integer> expressionList)
        {
            char[] exprArray = new char[expressionList.size()];
            
            for (int i = 0; i < expressionList.size(); i++)
            {
                exprArray[i] = (char) expressionList.get(i).intValue();
            }
            return String.valueOf(exprArray);
        }
    }

    @Override
    public String getRequestPath()
    {
        String path;
        if (_resourceHandlerSupport.isExtensionMapping())
        {
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/' + 
                getResourceName() + _resourceHandlerSupport.getMapping();
        }
        else
        {
            String mapping = _resourceHandlerSupport.getMapping(); 
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/' + getResourceName();
            path = (mapping == null) ? path : mapping + path;
        }
 
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String metadata = null;
        boolean useAmp = false;
        if (getLibraryName() != null)
        {
            metadata = "?ln=" + getLibraryName();
            path = path + metadata;
            useAmp = true;
            
            if (!facesContext.isProjectStage(ProjectStage.Production)
                    && JSF_JS_RESOURCE_NAME.equals(getResourceName()) 
                    && JAVAX_FACES_LIBRARY_NAME.equals(getLibraryName()))
            {
                // append &stage=?? for all ProjectStages except Production
                path = path + "&stage=" + facesContext.getApplication().getProjectStage().toString();
            }
        }
        
        return facesContext.getApplication().getViewHandler().getResourceURL(facesContext, path);
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        if (facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext))
        {
            Map<String, String> headers = new HashMap<String, String>();
            
            long lastModified;
            try
            {
                lastModified = ResourceLoaderUtils.getResourceLastModified(this.getURL());
            }
            catch (IOException e)
            {
                lastModified = -1;
            }
            
            // Here we have two cases: If the file could contain EL Expressions
            // the last modified time is the greatest value between application startup and
            // the value from file.
            if (this.couldResourceContainValueExpressions() &&
                    lastModified < _resourceHandlerSupport.getStartupTime())
            {
                lastModified = _resourceHandlerSupport.getStartupTime();
            }            
            else if (_resourceMeta instanceof AliasResourceMetaImpl &&
                lastModified < _resourceHandlerSupport.getStartupTime())
            {
                // If the resource meta is aliased, the last modified time is the greatest 
                // value between application startup and the value from file.
                lastModified = _resourceHandlerSupport.getStartupTime();
            }

            if (lastModified >= 0)
            {
                headers.put("Last-Modified", ResourceLoaderUtils.formatDateHeader(lastModified));
                
                long expires;
                if (facesContext.isProjectStage(ProjectStage.Development))
                {
                    // Force to expire now to prevent caching on development time.
                    expires = System.currentTimeMillis();
                }
                else
                {
                    expires = System.currentTimeMillis() + _resourceHandlerSupport.getMaxTimeExpires();
                }
                headers.put("Expires", ResourceLoaderUtils.formatDateHeader(expires));
            }
            
            return headers;
        }
        else
        {
            //No need to return headers 
            return Collections.emptyMap();
        }
    }

    @Override
    public URL getURL()
    {
        return getResourceLoader().getResourceURL(_resourceMeta);
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        // RFC2616 says related to If-Modified-Since header the following:
        //
        // "... The If-Modified-Since request-header field is used with a method to 
        // make it conditional: if the requested variant has not been modified since 
        // the time specified in this field, an entity will not be returned from 
        // the server; instead, a 304 (not modified) response will be returned 
        // without any message-body..."
        // 
        // This method is called from ResourceHandlerImpl.handleResourceRequest and if
        // returns false send a 304 Not Modified response.
        
        String ifModifiedSinceString = context.getExternalContext().getRequestHeaderMap().get("If-Modified-Since");
        
        if (ifModifiedSinceString == null)
        {
            return true;
        }
        
        Long ifModifiedSince = ResourceLoaderUtils.parseDateHeader(ifModifiedSinceString);
        
        if (ifModifiedSince == null)
        {
            return true;
        }
        
        Long lastModified;
        try
        {
            lastModified = ResourceLoaderUtils.getResourceLastModified(this.getURL());
        }
        catch (IOException exception)
        {
            lastModified = -1L;
        }
        
        if (lastModified >= 0)
        {
            if (this.couldResourceContainValueExpressions() &&
                    lastModified < _resourceHandlerSupport.getStartupTime())
            {
                lastModified = _resourceHandlerSupport.getStartupTime();
            }
            
            // If the lastModified date is lower or equal than ifModifiedSince,
            // the agent does not need to update.
            // Note the lastModified time is set at milisecond precision, but when 
            // the date is parsed and sent on ifModifiedSince, the exceding miliseconds
            // are trimmed. So, we have to compare trimming this from the calculated
            // lastModified time.
            if ( (lastModified-(lastModified % 1000)) <= ifModifiedSince)
            {
                return false;
            }
        }
        
        return true;
    }
}
