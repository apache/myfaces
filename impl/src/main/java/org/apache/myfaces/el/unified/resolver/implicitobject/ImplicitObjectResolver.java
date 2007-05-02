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

package org.apache.myfaces.el.unified.resolver.implicitobject;

import javax.el.*;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * See JSF 1.2 spec sections 5.6.1.1 and 5.6.2.1
 *
 * @author Stan Silvert
 */
public class ImplicitObjectResolver extends ELResolver {
    
    private List<ImplicitObject> implicitObjects;
    
    /**
     * Static factory for an ELResolver for resolving implicit objects in JSPs. 
     * See JSF 1.2 spec section 5.6.1.1
     */
    public static ELResolver makeResolverForJSP() {
        List<ImplicitObject> forJSPList = new ArrayList<ImplicitObject>(2);
        forJSPList.add(new FacesContextImplicitObject());
        forJSPList.add(new ViewImplicitObject());
        return new ImplicitObjectResolver(forJSPList);
    }
    
    /**
     * Static factory for an ELResolver for resolving implicit objects in all of Faces. 
     * See JSF 1.2 spec section 5.6.1.2
     */
    public static ELResolver makeResolverForFaces() {
        List<ImplicitObject> forFacesList = new ArrayList<ImplicitObject>(14);
        forFacesList.add(new ApplicationImplicitObject());
        forFacesList.add(new ApplicationScopeImplicitObject());
        forFacesList.add(new CookieImplicitObject());
        forFacesList.add(new FacesContextImplicitObject());
        forFacesList.add(new HeaderImplicitObject());
        forFacesList.add(new HeaderValuesImplicitObject());
        forFacesList.add(new InitParamImplicitObject());
        forFacesList.add(new ParamImplicitObject());
        forFacesList.add(new ParamValuesImplicitObject());
        forFacesList.add(new RequestImplicitObject());
        forFacesList.add(new RequestScopeImplicitObject());
        forFacesList.add(new SessionImplicitObject());
        forFacesList.add(new SessionScopeImplicitObject());
        forFacesList.add(new ViewImplicitObject());
        return new ImplicitObjectResolver(forFacesList);        
    }
    
    
    private ImplicitObjectResolver() {
        super();
        this.implicitObjects = new ArrayList<ImplicitObject>();
    }
    
    /** Creates a new instance of ImplicitObjectResolverForJSP */
    private ImplicitObjectResolver(List<ImplicitObject> implicitObjects) {
        this();
        this.implicitObjects = implicitObjects;
    }

    public void setValue(ELContext context, Object base, Object property, Object value) 
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        
        if (base != null) return;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return;
        
        String strProperty = castAndIntern(property);
        
        for (ImplicitObject obj: implicitObjects) {
            if (strProperty.equals(obj.getName())) {
                throw new PropertyNotWritableException();
            }
        }
    }
    
    public boolean isReadOnly(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if (base != null) return false;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return false;
        
        String strProperty = castAndIntern(property);
        
        for (ImplicitObject obj: implicitObjects) {
            if (strProperty.equals(obj.getName())) {
                context.setPropertyResolved(true);
                return true;
            }
        }
        
        return false;
    }

    public Object getValue(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {

        if (base != null) return null;
        if (property == null) throw new PropertyNotFoundException();      
        if (!(property instanceof String)) return null;
            
        String strProperty = castAndIntern(property);

        for (ImplicitObject obj : implicitObjects) {
            if (strProperty.equals(obj.getName())) {
                context.setPropertyResolved(true);
                return obj.getValue(context);
            }
        }
        
        return null;
    }

    public Class<?> getType(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if (base != null) return null;
        if (property == null) throw new PropertyNotFoundException();
        if (!(property instanceof String)) return null;
        
        String strProperty = castAndIntern(property);
        
        for (ImplicitObject obj: implicitObjects) {
            if (strProperty.equals(obj.getName())) {
                context.setPropertyResolved(true);
            }
        }
        
        return null;
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        if (base != null) return null;

        ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>(implicitObjects.size());
        
        for (ImplicitObject obj: implicitObjects) {
            descriptors.add(obj.getDescriptor());
         }
        
        return descriptors.iterator();
    }

    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) return null;
        
        return String.class;
    }
    
    protected String castAndIntern(Object o) {
        String s = (String)o;
        return s.intern();
    }
    
}
