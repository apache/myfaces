/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.impl.digester.elements;

import java.util.List;
import java.util.ArrayList;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class Factory
{

    private List applicationFactory = new ArrayList();
    private List facesContextFactory = new ArrayList();
    private List lifecycleFactory = new ArrayList();
    private List renderkitFactory = new ArrayList();

    public void addApplicationFactory(String factory)
    {
        applicationFactory.add(factory);
    }


    public void addFacesContextFactory(String factory)
    {
        facesContextFactory.add(factory);
    }


    public void addLifecycleFactory(String factory)
    {
        lifecycleFactory.add(factory);
    }


    public void addRenderkitFactory(String factory)
    {
        renderkitFactory.add(factory);
    }


    public List getApplicationFactory()
    {
        return applicationFactory;
    }


    public List getFacesContextFactory()
    {
        return facesContextFactory;
    }


    public List getLifecycleFactory()
    {
        return lifecycleFactory;
    }


    public List getRenderkitFactory()
    {
        return renderkitFactory;
    }
}
