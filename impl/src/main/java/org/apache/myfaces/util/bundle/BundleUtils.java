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
package org.apache.myfaces.util.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class BundleUtils
{
    private static final Log log = LogFactory.getLog(BundleUtils.class);

    private BundleUtils() 
    {
        // hide from public access
    }

    public static ResourceBundle findResourceBundle(FacesContext facesContext,
                                                    String bundleName)
    {
        //TODO: Could be JSTL LocalizationContext bundle?

        //Lookup as attribute (try different scopes)
        VariableResolver vr = facesContext.getApplication().getVariableResolver();
        ResourceBundle bundle = (ResourceBundle)vr.resolveVariable(facesContext, bundleName);

        return bundle;
    }

    public static String getString(FacesContext facesContext,
                                   String bundleName, String key)
    {
        ResourceBundle bundle = findResourceBundle(facesContext, bundleName);
        if (bundle != null)
        {
            try
            {
                return bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                log.warn("Resource string '" + key + "' in bundle '" + bundleName + "' could not be found.");
                return key;
            }
        }
        else
        {
            return key;
        }
    }

}
