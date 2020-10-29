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

import jakarta.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.view.ViewDeclarationLanguageStrategy;

public class MockDefaultViewDeclarationLanguageStrategy 
    implements ViewDeclarationLanguageStrategy
{
    
    private ViewDeclarationLanguage _language;

    public MockDefaultViewDeclarationLanguageStrategy()
    {
        super();
        _language = new MockDefaultViewDeclarationLanguage();
    }

    public ViewDeclarationLanguage getViewDeclarationLanguage()
    {
        return _language;
    }

    public boolean handles(String viewId)
    {
        return true;
    }

    @Override
    public String getMinimalImplicitOutcome(String viewId)
    {
        return viewId.substring(0, viewId.lastIndexOf('.'));
    }
}
