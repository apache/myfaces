/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.impl.digester.elements;

import java.util.ArrayList;
import java.util.List;

import org.apache.myfaces.config.impl.digester.elements.Renderer;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class RenderKit
{

    private String id;
    private String renderKitClass;
    private List renderer = new ArrayList();


    public String getId()
    {
        return id;
    }


    public void setId(String id)
    {
        this.id = id;
    }


    public String getRenderKitClass()
    {
        return renderKitClass;
    }


    public void setRenderKitClass(String renderKitClass)
    {
        this.renderKitClass = renderKitClass;
    }


    public List getRenderer()
    {
        return renderer;
    }


    public void addRenderer(Renderer value)
    {
        renderer.add(value);
    }


    public void merge(RenderKit renderKit)
    {
        renderer.addAll(renderKit.getRenderer());
    }

}
