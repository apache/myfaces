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
package org.apache.myfaces.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 * Resolver for Flash object 
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FlashELResolver extends ELResolver
{

    private final static String FLASH = "flash".intern();

    private final static String KEEP = "keep".intern();

    private final static String NOW = "now".intern();

    public FlashELResolver()
    {
        super();
    }

    @Override
    public void setValue(ELContext context, Object base, Object property,
            Object value) throws NullPointerException,
            PropertyNotFoundException, PropertyNotWritableException,
            ELException
    {
        if (property == null)
            throw new PropertyNotFoundException();
        if (!(property instanceof String))
            return;

        String strProperty = castAndIntern(property);

        if (FLASH.equals(strProperty))
        {
            throw new PropertyNotWritableException();
        }
        else if (base instanceof Flash)
        {
            context.setPropertyResolved(true);
            try
            {
                ((Flash) base).put(strProperty, value);
            }
            catch (UnsupportedOperationException e)
            {
                throw new PropertyNotWritableException(e);
            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
            throw new PropertyNotFoundException();
        if (!(property instanceof String))
            return false;

        String strProperty = castAndIntern(property);

        if (FLASH.equals(strProperty))
        {
            context.setPropertyResolved(true);
            return true;
        }
        else if (base instanceof Flash) {
            context.setPropertyResolved(true);
        }

        return false;
    }

    @Override
    public Object getValue(ELContext elContext, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
            throw new PropertyNotFoundException();
        if (!(property instanceof String))
            return null;

        String strProperty = castAndIntern(property);

        if (base == null)
        {
            if (FLASH.equals(strProperty))
            {
                //Access to flash object
                elContext.setPropertyResolved(true);
                Flash flash = externalContext(elContext).getFlash();
                //This is just to make sure after this point
                //we are not in "keep" promotion.
                setDoKeepPromotion(false);
                
                // Note that after this object is returned, Flash.get() and Flash.put()
                // methods are called from javax.el.MapELResolver, since 
                // Flash is instance of Map.
                return flash;
            }
        }
        else if (base instanceof Flash)
        {
            Flash flash = (Flash) base;
            if (KEEP.equals(strProperty))
            {
                setDoKeepPromotion(true);
                // Since we returned a Flash instance getValue will 
                // be called again but this time the property name
                // to be resolved will be called, so we can do keep
                // promotion.
                return base;
            }
            else if (NOW.equals(strProperty))
            {
                //Prevent invalid syntax #{flash.keep.now.someKey}
                if (!isDoKeepPromotion())
                {
                    // According to the javadoc of Flash.putNow() and 
                    // Flash.keep(), this is an alias to requestMap, used
                    // as a "buffer" to promote vars to flash scope using
                    // "keep" method
                    elContext.setPropertyResolved(true);
                    return externalContext(elContext).getRequestMap();
                }
            }
            else if (isDoKeepPromotion())
            {
                //Resolve property calling get or keep
                elContext.setPropertyResolved(true);
                //Obtain the value on requestMap if any
                Object value = externalContext(elContext).getRequestMap().get(strProperty);
                //promote it to flash scope
                flash.keep(strProperty);
                return value;
            }
            else
            {
                //Just get the value
                elContext.setPropertyResolved(true);
                return flash.get(strProperty);
            }
        }
        return null;
    }
    
    /**
     * This var indicate if we are inside a keep operation
     * or not. We go into keep status in two cases:
     * 
     * - A direct call to Flash.keep(String key)
     * - A lookup to keep map using a value expression #{flash.keep.someKey}.
     *   This occur when the ELResolver try to get the keep object.
     *   
     * Note that when "keep" is resolved by FlashELResolver,
     * we need a way to comunicate that the current lookup is 
     * for keep promotion.
     * 
     * This var do the job.
     */
    private static ThreadLocal<Boolean> _keepStatus = 
        new ThreadLocal<Boolean>()
        {
            @Override
            protected Boolean initialValue()
            {
                return Boolean.FALSE;
            }
        };

    private static boolean isDoKeepPromotion()
    {
        return _keepStatus.get();
    }

    private static void setDoKeepPromotion(boolean value)
    {
        _keepStatus.set(Boolean.valueOf(value));
    }
    
    // get the FacesContext from the ELContext
    protected FacesContext facesContext(ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

    protected ExternalContext externalContext(ELContext context)
    {
        return facesContext(context).getExternalContext();
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
            throw new PropertyNotFoundException();
        if (!(property instanceof String))
            return null;

        String strProperty = castAndIntern(property);

        if (FLASH.equals(strProperty))
        {
            context.setPropertyResolved(true);
        }
        else if (base instanceof Flash) {
            context.setPropertyResolved(true);
            Object obj = ((Flash) base).get(property);
            return (obj != null) ? obj.getClass() : null;
        }

        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
            Object base)
    {
        ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>(
                1);

        descriptors.add(makeDescriptor(FLASH,
                "Represents the current flash scope", Object.class));

        if (base instanceof Flash) {
            Iterator itr = ((Flash) base).keySet().iterator();
            Object key;
            FeatureDescriptor desc;
            while (itr.hasNext()) {
                key = itr.next();
                desc = makeDescriptor(key.toString(), key.toString(), key.getClass());
                descriptors.add(desc);
            }
        }
        return descriptors.iterator();
    }

    protected FeatureDescriptor makeDescriptor(String name, String description,
            Class<?> elResolverType)
    {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
        fd.setValue(ELResolver.TYPE, elResolverType);
        fd.setName(name);
        fd.setDisplayName(name);
        fd.setShortDescription(description);
        fd.setExpert(false);
        fd.setHidden(false);
        fd.setPreferred(true);
        return fd;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (base == null)
            return null;

        if (base instanceof Flash)
        {
            return Object.class;
        }
        else if (FLASH.equals(base.toString()))
        {
            return Object.class;
        }

        return null;
    }

    protected String castAndIntern(Object o)
    {
        String s = (String) o;
        return s.intern();
    }

}
