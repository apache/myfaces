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
package jakarta.faces.view;

import java.beans.BeanInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.application.ViewVisitOption;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * @since 2.0
 */
public abstract class ViewDeclarationLanguage
{
    /**
     * @since 2.1
     */
    public static final String JSP_VIEW_DECLARATION_LANGUAGE_ID = "java.faces.JSP";

    /**
     * @since 2.1
     */
    public static final String FACELETS_VIEW_DECLARATION_LANGUAGE_ID = "java.faces.Facelets";
    
    public abstract void buildView(FacesContext context, UIViewRoot view) throws IOException;

    public abstract UIViewRoot createView(FacesContext context, String viewId);

    public abstract BeanInfo getComponentMetadata(FacesContext context, Resource componentResource);

    public abstract Resource getScriptComponentResource(FacesContext context, Resource componentResource);
    
    public abstract StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId); 

    public abstract ViewMetadata getViewMetadata(FacesContext context, String viewId);

    public abstract void renderView(FacesContext context, UIViewRoot view) throws IOException;

    public abstract UIViewRoot restoreView(FacesContext context, String viewId);
    
    public void retargetAttachedObjects(FacesContext context, UIComponent topLevelComponent,
                                        List<AttachedObjectHandler> handlers)
    {
        throw new UnsupportedOperationException(); 
    }

    public void retargetMethodExpressions(FacesContext context, UIComponent topLevelComponent)
    {
        throw new UnsupportedOperationException(); 
    }
    
    /**
     * 
     * @since 2.1
     * @return
     */
    public String getId()
    {
        return this.getClass().getName();
    }
    
    /**
     * 
     * @since 2.1
     * @param facesContext
     * @param viewId
     * @return
     */
    public boolean viewExists(FacesContext facesContext, String viewId)
    {
        try
        {
            return facesContext.getExternalContext().getResource(viewId) != null;
        }
        catch (MalformedURLException e)
        {
            Logger log = Logger.getLogger(ViewDeclarationLanguage.class.getName());
            if (log.isLoggable(Level.SEVERE))
            {
                log.log(Level.SEVERE, "Malformed URL viewId: "+viewId, e);
            }
        }
        return false;
    }
    
    /**
     * @since 2.2
     * @param context
     * @param taglibURI
     * @param tagName
     * @param attributes
     * @return 
     */
    public UIComponent createComponent(FacesContext context,
                                   String taglibURI,
                                   String tagName,
                                   Map<String,Object> attributes)
    {
        return null;
    }
    
    /**
     * @since 2.2
     * @param context
     * @param viewId
     * @return 
     */
    public List<String> calculateResourceLibraryContracts(FacesContext context,
                                                      String viewId)
    {
        return null;
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param path
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViews(FacesContext facesContext, String path, ViewVisitOption... options)
    {
        return getViews(facesContext, path, Integer.MAX_VALUE, options);
    }
    
    
    /**
     * 
     * @since 2.3
     * @param facesContext
     * @param path
     * @param maxDepth
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViews(FacesContext facesContext, String path, 
            int maxDepth, ViewVisitOption... options)
    {
        // Here by default we follow what spec javadoc says
        // "...This method works as if invoking it were equivalent to evaluating the expression:
        //     getViewResources(facesContext, start, Integer.MAX_VALUE, options) ..."
        // The problem here is ViewVisitOption != ResourceVisitOption. But whatever return
        // getViews must always have TOP_LEVEL_VIEWS_ONLY, because otherwise it will return 
        // everything (css, js, ...). There is ViewVisitOption.RETURN_AS_MINIMAL_IMPLICIT_OUTCOME,
        // but this is a filter on top of the stream.
        
        return facesContext.getApplication().getResourceHandler().getViewResources(
                facesContext, path, maxDepth, ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY);
    }
}