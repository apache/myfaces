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
package org.apache.myfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;

import org.apache.myfaces.application.ResourceHandlerSupport;

/**
 * Default implementation for resources
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
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
    public InputStream getInputStream()
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
        String contentType = getContentType();

        return ("text/css".equals(contentType) || 
            "text/javascript".equals(contentType) || 
            "application/x-javascript".equals(contentType) );
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
            path = _resourceHandlerSupport.getMapping() + 
                ResourceHandler.RESOURCE_IDENTIFIER + '/' + getResourceName();
        }
 
        String metadata = null;
        if (getLibraryName() != null)
        {
            metadata = "?ln=" + getLibraryName();
            path = path + metadata;
        }
                
        return FacesContext.getCurrentInstance().getApplication().
            getViewHandler().getResourceURL(
                    FacesContext.getCurrentInstance(), path);
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        // TODO: Read the HTTP documentation to see how we can enhance this
        //part. For now, use always an empty map. 
        return Collections.emptyMap();
    }

    @Override
    public URL getURL()
    {
        return getResourceLoader().getResourceURL(_resourceMeta);
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        // TODO: When and How we can return safely false?
        return true;
    }

}
