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

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

/**
 * This class is used to encapsulate the information required to resolve
 * facelets templates.
 * 
 * Composite components require to "isolate" the inner template resolution.
 * That means, when a ui:xx tag is used, do not take into account the outer
 * templates defined.
 * 
 * The methods here are only used by the current implementation and the intention
 * is not expose it as public api.
 * 
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 947351 $ $Date: 2010-05-22 19:19:48 -0500 (Sáb, 22 May 2010) $
 * @since 2.0.1
 */
public abstract class TemplateContext
{
    /**
     * Pop the last added pushed TemplateClient
     * @see TemplateClient
     */
    public abstract TemplateManager popClient();
    
    /**
     * Push the passed TemplateClient onto the stack for Definition Resolution
     * @param client
     * @see TemplateClient
     */
    public abstract void pushClient(final AbstractFacelet owner, final TemplateClient client);
    
    /**
     * Pop the last added extended TemplateClient
     * @param client
     */
    public abstract TemplateManager popExtendedClient();
    
    public abstract void extendClient(final AbstractFacelet owner, final TemplateClient client);
    
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
    public abstract boolean includeDefinition(FaceletContext ctx, Facelet owner, UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException;
    
    public abstract TemplateManager getCompositeComponentClient();

    /**
     * Set the composite component TemplateManager instance, used to resolve
     * cc:insertChildred or cc:insertFacet usages for the current template
     * context 
     */
    public abstract void setCompositeComponentClient(TemplateManager compositeComponentClient);

}
