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
package org.apache.myfaces.view.facelets.tag.composite;

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;

import org.apache.myfaces.view.facelets.tag.jsf.ComponentBuilderHandler;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeComponentResourceTagHandler extends ComponentHandler
    implements ComponentBuilderHandler
{
    private final Resource _resource;

    public CompositeComponentResourceTagHandler(ComponentConfig config, Resource resource)
    {
        super(config);
        _resource = resource;
    }

    @Override
    public UIComponent createComponent(FaceletContext ctx)
    {
        FacesContext faceContext = ctx.getFacesContext();
        return faceContext.getApplication().createComponent(faceContext, _resource);
    }
}
