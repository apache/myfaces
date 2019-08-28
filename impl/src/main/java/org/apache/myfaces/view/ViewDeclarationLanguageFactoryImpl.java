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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguageStrategy;
import org.apache.myfaces.view.jsp.JspViewDeclarationLanguageStrategy;

/**
 * This is the default VDL factory used as of JSF 2.0, it tries to use Facelet VDL whenever possible, 
 * but fallback on JSP if required.
 * 
 * @author Simon Lessard (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 2.0
 */
public class ViewDeclarationLanguageFactoryImpl extends ViewDeclarationLanguageFactory
{
    private static final String FACELETS_1_VIEW_HANDLER = "com.sun.facelets.FaceletViewHandler";

    private static final Logger LOGGER = Logger.getLogger(ViewDeclarationLanguageFactoryImpl.class.getName());
    
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
            FacesContext context = FacesContext.getCurrentInstance();

            logWarningIfLegacyFaceletViewHandlerIsPresent(context);

            if (MyfacesConfig.getCurrentInstance(context).isSupportJSP())
            {
                _supportedLanguages = new ViewDeclarationLanguageStrategy[2];
                _supportedLanguages[0] = new FaceletViewDeclarationLanguageStrategy();
                _supportedLanguages[1] = new JspViewDeclarationLanguageStrategy();
            }
            else
            {
                _supportedLanguages = new ViewDeclarationLanguageStrategy[1];
                _supportedLanguages[0] = new FaceletViewDeclarationLanguageStrategy();
            }

            _initialized = true;
        }
    }
    
    /**
     * If the Facelets-1 ViewHandler com.sun.facelets.FaceletViewHandler is present, log a error.
     * 
     * @param context the <code>FacesContext</code>
     */
    private void logWarningIfLegacyFaceletViewHandlerIsPresent(FacesContext context)
    {
        boolean facelets1ViewHandlerPresent
                = context.getApplication().getViewHandler().getClass().getName().equals(FACELETS_1_VIEW_HANDLER);

        if (facelets1ViewHandlerPresent)
        {
            if (LOGGER.isLoggable(Level.WARNING))
            {
                LOGGER.log(Level.WARNING, "Your faces-config.xml contains the " + FACELETS_1_VIEW_HANDLER + " class."
                    + "\nYou need to remove it since it's not supported anymore since JSF 2.0");
            }
        }
    }
}
