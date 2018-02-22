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
package org.apache.myfaces.view.facelets.pss.acid.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostRestoreStateEvent;

/**
 *
 * @author lu4242
 */
@ListenerFor(systemEventClass = PostRestoreStateEvent.class)
@FacesComponent(namespace = "http://my.namespace.com/components",
        createTag = true, tagName = "simpleComponent2",
        value = "com.myapp.UISimpleComponent2")
public class UISimpleComponent2 extends UIInput
{

    public UISimpleComponent2()
    {
        setRendererType(null);
    }

    @Override
    public String getFamily()
    {

        return "com.myapp";
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException
    {
        // Check it doesn't throw StackOverflowException
        super.processEvent(event);
    }

    @Override
    public Object saveState(FacesContext context)
    {
        String value = (String) this.getValue();
        
        return super.saveState(context); 
    }

    
}
