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
package org.apache.myfaces.config.impl.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class RenderKitImpl extends org.apache.myfaces.config.element.RenderKit implements Serializable
{
    private String id;
    private List<String> renderKitClasses;
    private List<org.apache.myfaces.config.element.Renderer> renderer;
    private List<org.apache.myfaces.config.element.ClientBehaviorRenderer> clientBehaviorRenderers;
    
    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public List<String> getRenderKitClasses()
    {
        if (renderKitClasses == null)
        {
            return Collections.emptyList();
        }
        return renderKitClasses;
    }

    public void addRenderKitClass(String renderKitClass)
    {
        if (renderKitClasses == null)
        {
            renderKitClasses = new ArrayList<>();
        }
        renderKitClasses.add(renderKitClass);
    }

    @Override
    public List<org.apache.myfaces.config.element.ClientBehaviorRenderer> getClientBehaviorRenderers ()
    {
        if (clientBehaviorRenderers == null)
        {
            return Collections.emptyList();
        }
        return clientBehaviorRenderers;
    }
    
    @Override
    public List<org.apache.myfaces.config.element.Renderer> getRenderer()
    {
        if (renderer == null)
        {
            return Collections.emptyList();
        }
        return renderer;
    }

    public void addClientBehaviorRenderer(org.apache.myfaces.config.element.ClientBehaviorRenderer renderer)
    {
        if (clientBehaviorRenderers == null)
        {
            clientBehaviorRenderers = new ArrayList<>();
        }
        clientBehaviorRenderers.add(renderer);   
    }
    
    public void addRenderer(org.apache.myfaces.config.element.Renderer value)
    {
        if (renderer == null)
        {
            renderer = new ArrayList<>();
        }
        renderer.add(value);
    }

    @Override
    public void merge(org.apache.myfaces.config.element.RenderKit renderKit)
    {
        if (!renderKit.getRenderKitClasses().isEmpty())
        {
            if (renderKitClasses == null)
            {
                renderKitClasses = new ArrayList<>();
            }
            renderKitClasses.addAll(renderKit.getRenderKitClasses());
        }
        
        if (!renderKit.getClientBehaviorRenderers().isEmpty())
        {
            if (clientBehaviorRenderers == null)
            {
                clientBehaviorRenderers = new ArrayList<>();
            }
            clientBehaviorRenderers.addAll(renderKit.getClientBehaviorRenderers());
        }

        if (!renderKit.getRenderer().isEmpty())
        {
            if (renderer == null)
            {
                renderer = new ArrayList<>();
            }
            renderer.addAll(renderKit.getRenderer());
        }
        
    }

}
