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
package org.apache.myfaces.view.facelets.viewscope;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * A {@code @ViewScoped} CDI bean that counts its creations and destructions, used to assert that
 * the view scope of a formless (never-saved) view is destroyed at the end of the request instead
 * of lingering in the session.
 */
@Named("viewScopeLeakProbeBean")
@ViewScoped
public class ViewScopeLeakProbeBean implements Serializable
{
    public static final AtomicInteger CREATED = new AtomicInteger();
    public static final AtomicInteger DESTROYED = new AtomicInteger();

    public static void reset()
    {
        CREATED.set(0);
        DESTROYED.set(0);
    }

    @PostConstruct
    public void created()
    {
        CREATED.incrementAndGet();
    }

    @PreDestroy
    public void destroyed()
    {
        DESTROYED.incrementAndGet();
    }

    public String getValue()
    {
        return "view-scoped";
    }
}
