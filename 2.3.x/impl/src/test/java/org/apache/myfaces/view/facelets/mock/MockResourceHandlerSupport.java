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
package org.apache.myfaces.view.facelets.mock;

import org.apache.myfaces.application.DefaultResourceHandlerSupport;
import org.apache.myfaces.shared.resource.ClassLoaderResourceLoader;
import org.apache.myfaces.shared.resource.ResourceLoader;

/**
 * Redirect resource request to the directory where the test class is,
 * to make easier test composite components.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 */
public class MockResourceHandlerSupport extends DefaultResourceHandlerSupport
{
    private ResourceLoader[] _resourceLoaders;
    
    private Class _referenceClass;
    
    public MockResourceHandlerSupport(Class class1)
    {
        super();
        _referenceClass = class1;
    }

    public ResourceLoader[] getResourceLoaders()
    {
        if (_resourceLoaders == null)
        {
            //The ExternalContextResourceLoader has precedence over
            //ClassLoaderResourceLoader, so it goes first.
            _resourceLoaders = new ResourceLoader[] {
                    new ClassLoaderResourceLoader(getDirectory())
            };
        }
        return _resourceLoaders;
    }
    
    private String getDirectory()
    {
        return _referenceClass.getName().substring(0,
                _referenceClass.getName().lastIndexOf('.')).replace('.', '/');
    }
}
