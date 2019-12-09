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
package org.apache.myfaces.ee;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.NamedEvent;
import javax.faces.model.FacesDataModel;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.servlet.annotation.HandlesTypes;

// TODO: backward compatibility for TomEE
@HandlesTypes({
        FacesBehavior.class,
        FacesBehaviorRenderer.class,
        FacesComponent.class,
        FacesConverter.class,
        FacesRenderer.class,
        FacesValidator.class,
        FacesDataModel.class,
        ListenerFor.class,
        ListenersFor.class,
        NamedEvent.class,
        ResourceDependencies.class,
        ResourceDependency.class,
        UIComponent.class,
        Converter.class,
        Renderer.class,
        Validator.class
    })
@Deprecated
public class MyFacesContainerInitializer extends org.apache.myfaces.webapp.MyFacesContainerInitializer
{
    
}
