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
package org.apache.myfaces.test.core.mock;

import jakarta.faces.FacesException;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;

import org.apache.myfaces.view.ViewDeclarationLanguageStrategy;

public class MockMyFacesViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory
{

    private boolean _initialized;
    private ViewDeclarationLanguageStrategy[] _supportedLanguages;
    
    public MockMyFacesViewDeclarationLanguageFactory()
    {
        _initialized = false;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId)
    {
        initialize();
        
        for (ViewDeclarationLanguageStrategy strategy : _supportedLanguages)
        {
            if (strategy.handles(viewId))
            {
                return strategy.getViewDeclarationLanguage();
            }
        }
        
        throw new FacesException("Cannot find a valid PDL for view id " + viewId);
    }

    private synchronized void initialize()
    {
        if (!_initialized)
        {
            _supportedLanguages = new ViewDeclarationLanguageStrategy[2];
            _supportedLanguages[0] = new MockMyFacesFaceletViewDeclarationLanguageStrategy();
            _supportedLanguages[1] = new MockDefaultViewDeclarationLanguageStrategy(); 
            _initialized = true;
        }
    }

}
