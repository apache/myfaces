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
package org.apache.myfaces.core.extensions.quarkus.showcase.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.annotation.View;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.inject.Named;

@Named
@RequestScoped
public class EventController
{

    private int called = 0;
    private int calledForView = 0;

    public void observe(@Observes PreRenderViewEvent event)
    {
        called++;
    }

    public void observeForView(@Observes @View("/index.xhtml") PreRenderViewEvent event)
    {
        calledForView++;
    }

    public int getCalled()
    {
        return called;
    }

    public void setCalled(int called)
    {
        this.called = called;
    }

    public int getCalledForView()
    {
        return calledForView;
    }

    public void setCalledForView(int calledForView)
    {
        this.calledForView = calledForView;
    }
}
