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
package org.apache.myfaces.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;

import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguageStrategy;

/**
 * This is the default VDL factory used as of JSF 2.0, it tries to use Facelet VDL whenever possible.
 * 
 * @author Simon Lessard (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 2.0
 */
public class ViewDeclarationLanguageFactoryImpl extends ViewDeclarationLanguageFactory
{    
    private volatile boolean _initialized;
    private volatile ViewDeclarationLanguageStrategy[] _supportedLanguages;
    
    private volatile List<ViewDeclarationLanguage> _supportedVDLs = null;

    public ViewDeclarationLanguageFactoryImpl()
    {
        _initialized = false;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId)
    {
        if (!_initialized)
        {
            initialize();
        }
        
        for (ViewDeclarationLanguageStrategy strategy : _supportedLanguages)
        {
            if (strategy.handles(viewId))
            {
                return strategy.getViewDeclarationLanguage();
            }
        }
        
        return null;
    }

    @Override
    public List<ViewDeclarationLanguage> getAllViewDeclarationLanguages()
    {
        if (!_initialized)
        {
            initialize();
        }
        
        if (_supportedVDLs == null)
        {
            List<ViewDeclarationLanguage> list = new ArrayList<>(_supportedLanguages.length);
            for (ViewDeclarationLanguageStrategy strategy : _supportedLanguages)
            {        
                list.add(strategy.getViewDeclarationLanguage());
            }
            _supportedVDLs = Collections.unmodifiableList(list);
        }
        return _supportedVDLs;
    }
    
    /**
     * Initialize the supported view declaration languages.
     */
    private synchronized void initialize()
    {
        if (!_initialized)
        {
            _supportedLanguages = new ViewDeclarationLanguageStrategy[]
            {
                new FaceletViewDeclarationLanguageStrategy()
            };

            _initialized = true;
        }
    }
}
