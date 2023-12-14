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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;
import java.util.Map;
import jakarta.faces.FacesException;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.view.facelets.Facelet;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.view.facelets.AbstractFacelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.tag.composite.CompositeResourceLibrary;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

/**
 * This listener must be attached to PostRestoreStateEvent so when the view is restored, 
 * the algorithm refresh the component that was created using facelets, restoring the
 * transient markup.
 *
 * @author lu4242
 */
public final class RefreshDynamicComponentListener implements ComponentSystemEventListener, StateHolder
{
    private String taglibURI;
    private String tagName;
    private Map<String,Object> attributes;
    private String baseKey;

    public RefreshDynamicComponentListener(String taglibURI, String tagName, 
        Map<String, Object> attributes, String baseKey)
    {
        this.taglibURI = taglibURI;
        this.tagName = tagName;
        this.attributes = attributes;
        this.baseKey = baseKey;
    }

    public RefreshDynamicComponentListener()
    {
    }
    
    @Override
    public void processEvent(ComponentSystemEvent event)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        FaceletViewDeclarationLanguage vdl = (FaceletViewDeclarationLanguage) 
            facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId());
        
        Facelet componentFacelet;
        FaceletFactory faceletFactory = vdl.getFaceletFactory();
            
        FaceletFactory.setInstance(faceletFactory);
        try
        {
            componentFacelet
                    = faceletFactory.compileComponentFacelet(taglibURI, tagName, attributes);
        }
        finally
        {
            FaceletFactory.setInstance(null);
        }
        try
        {
            UIComponent component = event.getComponent();
            
            facesContext.getAttributes().put(FaceletViewDeclarationLanguage.REFRESHING_TRANSIENT_BUILD,
                Boolean.TRUE);
            
            
            // Detect the relationship between parent and child, to ensure the component is properly created
            // and refreshed. In facelets this is usually done by core.FacetHandler, but since it is a 
            // dynamic component, we need to do it here before apply the handler
            UIComponent parent = component.getParent();
            String facetName = null;
            if (parent.getFacetCount() > 0 && !parent.getChildren().contains(component))
            {
                facetName = ComponentSupport.findFacetNameByComponentInstance(parent, component);
            }

            try
            {
                if (facetName != null)
                {
                    parent.getAttributes().put(org.apache.myfaces.view.facelets.tag.faces.core.FacetHandler.KEY, 
                            facetName);
                }            
                // The trick here is restore MARK_CREATED, just to allow ComponentTagHandlerDelegate to
                // find the component. Then we reset it to exclude it from facelets refresh algorithm.
                // Note oam.vf.GEN_MARK_ID helps to identify when there is a wrapper component or not,
                // and in that way identify which component is the parent.
                String markId = (String) component.getAttributes().get(FaceletViewDeclarationLanguage.GEN_MARK_ID);
                if (markId == null)
                {
                    ((AbstractFacelet) componentFacelet).applyDynamicComponentHandler(
                        facesContext, component, baseKey);
                }
                else
                {
                    try
                    {
                        component.getAttributes().put(ComponentSupport.MARK_CREATED, markId);
                        ((AbstractFacelet) componentFacelet).applyDynamicComponentHandler(
                            facesContext, component.getParent(), baseKey);
                    }
                    finally
                    {
                        component.getAttributes().put(ComponentSupport.MARK_CREATED, null);
                    }
                }
            }
            finally
            {
                if (facetName != null)
                {
                    parent.getAttributes().remove(org.apache.myfaces.view.facelets.tag.faces.core.FacetHandler.KEY);
                }
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        finally
        {
            facesContext.getAttributes().remove(
                FaceletViewDeclarationLanguage.REFRESHING_TRANSIENT_BUILD);
        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(context);

        Object[] values = new Object[4];

        Integer tagId = runtimeConfig.getIdByNamespace().get(taglibURI);
        if (tagId != null)
        {
            values[0] = tagId;
        }
        else if (taglibURI.startsWith(CompositeResourceLibrary.NAMESPACE_PREFIX))
        {
            values[0] = new Object[]{0, taglibURI.substring(CompositeResourceLibrary.NAMESPACE_PREFIX.length())};
        }
        else if(taglibURI.startsWith(CompositeResourceLibrary.JCP_NAMESPACE_PREFIX))
        {
            values[0] = new Object[]{1, taglibURI.substring(CompositeResourceLibrary.JCP_NAMESPACE_PREFIX.length())};
        }
        else if(taglibURI.startsWith(CompositeResourceLibrary.SUN_NAMESPACE_PREFIX))
        {
            values[0] = new Object[]{2, taglibURI.substring(CompositeResourceLibrary.SUN_NAMESPACE_PREFIX.length())};
        }
        else
        {
            values[0] = taglibURI;
        }
        values[1] = tagName;
        values[2] = attributes;
        values[3] = baseKey;

        return values;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Object[] values = (Object[]) state;
        if (values[0] instanceof String string)
        {
            taglibURI = string;
        }
        else if (values[0] instanceof Integer integer)
        {
            RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(context);
            taglibURI = runtimeConfig.getNamespaceById().get(integer);
        }
        else if (values[0] instanceof Object[] def)
        {
            int index = (Integer) def[0];
            String ns;
            switch (index)
            {
                case 0:
                    ns = CompositeResourceLibrary.NAMESPACE_PREFIX;
                    break;
                case 1:
                    ns = CompositeResourceLibrary.JCP_NAMESPACE_PREFIX;
                    break;
                case 2:
                    ns = CompositeResourceLibrary.SUN_NAMESPACE_PREFIX;
                    break;
                default:
                    ns = "";
                    break;
            }
            taglibURI = ns + def[1];
        }
        tagName = (String) values[1];
        attributes = (Map<String, Object>) values[2];
        baseKey = (String) values[3];
    }

    @Override
    public boolean isTransient()
    {
        return false;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
    }

}
