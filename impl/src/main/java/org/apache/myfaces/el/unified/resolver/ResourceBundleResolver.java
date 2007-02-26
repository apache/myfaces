/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.myfaces.el.unified.resolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

/**
 * See JSF 1.2 spec section 5.6.1.4
 *
 * @author Stan Silvert
 */
public class ResourceBundleResolver extends ELResolver {
    
    /** Creates a new instance of ResourceBundleResolver */
    public ResourceBundleResolver() {
    }

    public void setValue(ELContext context, Object base, Object property, Object value) 
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        
        if ((base == null) && (property == null)) throw new PropertyNotFoundException();

        if (!(property instanceof String)) return;
        
        ResourceBundle bundle = getResourceBundle(context, (String)property);
        
        if (bundle != null) {
            throw new PropertyNotWritableException("ResourceBundles are read-only");
        }
    }

    public boolean isReadOnly(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if (base != null) return false;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return false;
        
        ResourceBundle bundle = getResourceBundle(context, (String)property);
        
        if (bundle != null) {
            context.setPropertyResolved(true);
            return true;
        }
        
        return false;
    }

    public Object getValue(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if (base != null) return null;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return null;
        
        ResourceBundle bundle = getResourceBundle(context, (String)property);
        
        if (bundle != null) {
            context.setPropertyResolved(true);
            return bundle;
        }
        
        return null;
    }
    
    public Class<?> getType(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if (base != null) return null;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return null;
        
        ResourceBundle bundle = getResourceBundle(context, (String)property);
        
        if (bundle != null) {
            context.setPropertyResolved(true);
            return ResourceBundle.class;
        }
        
        return null;
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
       
        if (base != null) return null;
        
        // TODO fix this?
        // throw new UnsupportedOperationException("Can't implement without a list of all resource bundles??????");
        return null; // noop
    }

    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        
        if (base != null) return null;
        
        return String.class;
    }
    
    // get the FacesContext from the ELContext
    private FacesContext facesContext(ELContext context) {
        return (FacesContext)context.getContext(FacesContext.class);
    }

    private ResourceBundle getResourceBundle(ELContext context, String property) {
        FacesContext facesContext = facesContext(context);
        if (facesContext != null) {
            Application application = facesContext.getApplication();
            return application.getResourceBundle(facesContext, property);
        }
        
        return null;
    }
    
}
