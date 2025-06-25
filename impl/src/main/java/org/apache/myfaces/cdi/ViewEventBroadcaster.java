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
package org.apache.myfaces.cdi;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.util.lang.Lazy;

public class ViewEventBroadcaster implements SystemEventListener
{
    private Lazy<Event<Object>> globalEvent = new Lazy<>(() ->
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        BeanManager beanManager = CDIUtils.getBeanManager(facesContext);
        return beanManager == null ? null : beanManager.getEvent();
    });

    public ViewEventBroadcaster()
    {
    }

    @Override
    public boolean isListenerForSource(Object source)
    {
        return source instanceof UIViewRoot;
    }

    @Override
    public void processEvent(SystemEvent e) throws AbortProcessingException
    {
        Event<Object> globalEvent = this.globalEvent.get();
        if (globalEvent == null)
        {
            return;
        }

        // view-bound event
        UIViewRoot view = (UIViewRoot) e.getSource();
        globalEvent.select(View.Literal.of(view.getViewId())).fire(e);

        // this is not needed as the CDI impl automatically handles this above
        // globalEvent.fire(e);
    }
}
