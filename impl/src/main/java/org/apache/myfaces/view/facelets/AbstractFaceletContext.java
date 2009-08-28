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
package org.apache.myfaces.view.facelets;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;


/**
 * This class contains methods that belongs to original FaceletContext shipped in
 * facelets code before 2.0, but does not take part from api, so are considered 
 * implementation details. This includes methods related to template handling
 * feature of facelets (called by ui:composition, ui:define and ui:insert).
 * 
 * The methods here are only used by the current implementation and the intention
 * is not expose it as public api.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * 
 * @since 2.0
 */
public abstract class AbstractFaceletContext extends FaceletContext
{
    public final static String COMPOSITE_COMPONENT_STACK = "org.apache.myfaces.view.facelets.COMPOSITE_COMPONENT_STACK";

    /**
     * Push the passed TemplateClient onto the stack for Definition Resolution
     * @param client
     * @see TemplateClient
     */
    public abstract void pushClient(TemplateClient client);

    /**
     * Pop the last added TemplateClient
     * @see TemplateClient
     */
    public abstract void popClient(TemplateClient client);

    public abstract void extendClient(TemplateClient client);

    /**
     * This method will walk through the TemplateClient stack to resolve and
     * apply the definition for the passed name.
     * If it's been resolved and applied, this method will return true.
     * 
     * @param parent the UIComponent to apply to
     * @param name name or null of the definition you want to apply
     * @return true if successfully applied, otherwise false
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract boolean includeDefinition(UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException;
    
    /**
     * Apply the facelet referenced by a url containing a composite component
     * definition to the current UIComponent. In other words, apply the section
     * composite:implementation in the facelet to the current component.
     * 
     * We need to do this here because DefaultFacelet is the one who has and
     * handle the current FaceletFactory instance.
     * 
     * @param parent
     * @param url
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract void applyCompositeComponent(UIComponent parent, Resource resource)
            throws IOException, FaceletException, FacesException, ELException;

    /**
     * Return the composite component being applied on the current facelet. 
     * 
     * Note this is different to UIComponent.getCurrentCompositeComponent, because a composite
     * component is added to the stack each time a composite:implementation tag handler is applied.
     * 
     * This could be used by InsertChildrenHandler and InsertFacetHandler to retrieve the current
     * composite component to be applied.
     * 
     * @param facesContext
     * @return
     */
    public abstract UIComponent getCompositeComponentFromStack();

    public abstract void pushCompositeComponentToStack(UIComponent parent);

    public abstract void popCompositeComponentToStack();
}
