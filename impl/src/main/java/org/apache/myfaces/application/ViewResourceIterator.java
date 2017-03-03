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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.faces.application.ResourceVisitOption;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared.resource.ContractResourceLoader;
import org.apache.myfaces.shared.resource.ResourceHandlerSupport;
import org.apache.myfaces.shared.resource.ResourceLoader;

/**
 *
 * @author lu4242
 */
public class ViewResourceIterator implements Iterator<String>
{
    private ResourceHandlerSupport support;
    private Deque<ResourceLoader> stack = new LinkedList<ResourceLoader>();
    private Deque<String> basePathStack = new LinkedList<String>();
    private Iterator<String> currentIterator = null;
    private String origBasePath;
    private int maxDepth;
    private ResourceVisitOption[] options;
    private FacesContext facesContext;

    /**
     * register a set 
     */
    private Set<String> pathSet;

    public ViewResourceIterator(FacesContext facesContext, ResourceHandlerSupport support, 
            String localePrefix, List<String> contracts, String contractPreferred, 
            String path, int maxDepth, ResourceVisitOption... options) 
    {
        this.support = support;
        this.origBasePath = path;
        this.maxDepth = maxDepth;
        this.options = options;
        this.facesContext = facesContext;

        String basePath = this.origBasePath.endsWith("/") ? this.origBasePath : this.origBasePath+"/";
        
        if (contractPreferred != null)
        {
            for (ContractResourceLoader loader : support.getContractResourceLoaders())
            {
                if (localePrefix != null)
                {
                    stack.add(loader);
                    basePathStack.add(basePath+localePrefix+"/"+contractPreferred);
                }
                stack.add(loader);
                basePathStack.add(basePath+"/"+contractPreferred);
            }
        }
        if (!contracts.isEmpty())
        {
            for (ContractResourceLoader loader : support.getContractResourceLoaders())
            {
                for (String contract : contracts)
                {
                    if (localePrefix != null)
                    {
                        stack.add(loader);
                        basePathStack.add(basePath+localePrefix+"/"+contract);
                    }
                    stack.add(loader);
                    basePathStack.add(basePath+contract);
                }
            }
        }
        
        for (ResourceLoader loader : support.getViewResourceLoaders())
        {
            if (localePrefix != null)
            {
                stack.add(loader);
                basePathStack.add(basePath+localePrefix);
            }
            stack.add(loader);
            basePathStack.add(basePath);
        }
    }

    @Override
    public boolean hasNext()
    {
        boolean next = false;
        do 
        {
            if (currentIterator == null)
            {
                ResourceLoader loader = null;
                String basePath = null;
                do
                {
                    do
                    {
                        loader = (ResourceLoader) stack.pop();
                        basePath = basePathStack.pop();
                        
                        if (loader != null)
                        {
                            currentIterator = loader.iterator(facesContext, basePath, maxDepth, options);
                        }
                    } 
                    while (currentIterator == null && !stack.isEmpty());
                    if (currentIterator != null)
                    {
                        boolean hasNext = currentIterator.hasNext();
                        if (!hasNext)
                        {
                            currentIterator = null;
                        }
                    }
                }
                while (currentIterator == null && !stack.isEmpty());
                if (currentIterator == null)
                {
                    return false;
                }
            }
            next = currentIterator.hasNext();
            if (!next && !stack.isEmpty())
            {
                currentIterator = null;
            }
        }
        while (currentIterator == null);
        return next;
    }

    @Override
    public String next()
    {
        if (hasNext())
        {
            if (currentIterator != null)
            {
                return currentIterator.next();
            }
            else
            {
                //Should not happen, return null
                return null;
            }
        }
        else
        {
            return null;
        }
        /*
        if (currentIterator == null)
        {
            ResourceLoader loader = (ResourceLoader) stack.pop();
            if (loader != null)
            {
                currentIterator = loader.iterator(facesContext, basePath, maxDepth, options);
            }
            else
            {
                return null;
            }
        }
        return currentIterator.next();
        */
    }
}
