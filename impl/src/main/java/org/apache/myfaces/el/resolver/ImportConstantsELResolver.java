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
package org.apache.myfaces.el.resolver;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewMetadata;
import org.apache.myfaces.renderkit.html.ImportConstantsRenderer;
import org.apache.myfaces.util.ConstantsCollector;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public final class ImportConstantsELResolver extends ELResolver
{
    private static final String IMPORT_CONSTANTS = "oam.importConstants";

    @Override
    public Object getValue(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return null;
        }

        final FacesContext facesContext = facesContext(context);
        if (facesContext == null)
        {
            return null;
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot == null)
        {
            return null;
        }

        Map<String, String> importConstantsMap = (Map<String, String>)
                viewRoot.getTransientStateHelper().getTransient(IMPORT_CONSTANTS);
        if (importConstantsMap == null)
        {
            Collection<UIImportConstants> uiImportConstants = ViewMetadata.getImportConstants(viewRoot);
            if (uiImportConstants != null && !uiImportConstants.isEmpty())
            {
                importConstantsMap = ImportConstantsRenderer.toVarTypeMap(uiImportConstants);
            } 
            else
            {
                importConstantsMap = Collections.emptyMap();
            }
            if (!FaceletViewDeclarationLanguage.isBuildingViewMetadata(facesContext))
            {
                viewRoot.getTransientStateHelper().putTransient(IMPORT_CONSTANTS, importConstantsMap);
            }
        }

        if (importConstantsMap != null && !importConstantsMap.isEmpty())
        {
            String type = importConstantsMap.get((String)property);
            if (type != null)
            {
                Map<String, Object> constantsMap = ConstantsCollector.collectConstants(
                        FacesContext.getCurrentInstance(),
                        type);
                if (!constantsMap.isEmpty())
                {
                    context.setPropertyResolved(true);
                    return constantsMap;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        return null;
    }

    @Override
    public void setValue(ELContext elc, Object o, Object o1, Object o2)
            throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        //No op
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base,
            final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context, final Object base)
    {
        return base == null ? Object.class : null;
    }

    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

}
