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
package org.apache.myfaces.el.unified.resolver;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.ManagedBean;

/**
 * See JSF 1.2 spec section 5.6.1.2
 * 
 * @author Stan Silvert
 */
public class ManagedBeanResolver extends ELResolver
{
    private static final Log log = LogFactory.getLog(ManagedBeanResolver.class);
    private static final String BEANS_UNDER_CONSTRUCTION = "org.apache.myfaces.el.unified.resolver.managedbean.beansUnderConstruction";

    // adapted from Manfred's JSF 1.1 VariableResolverImpl
    protected static final Map<String, Scope> s_standardScopes = new HashMap<String, Scope>(16);

    static
    {
        s_standardScopes.put("request", new Scope()
        {
            public void put(ExternalContext extContext, String name, Object obj)
            {
                extContext.getRequestMap().put(name, obj);
            }
        });
        
        s_standardScopes.put("session", new Scope()
        {
            public void put(ExternalContext extContext, String name, Object obj)
            {
                extContext.getSessionMap().put(name, obj);
            }
        });
        
        s_standardScopes.put("application", new Scope()
        {
            public void put(ExternalContext extContext, String name, Object obj)
            {
                extContext.getApplicationMap().put(name, obj);
            }
        });
        
        s_standardScopes.put("none", new Scope()
        {
            public void put(ExternalContext extContext, String name, Object obj)
            {
                // do nothing
            }
        });
    }

    /**
     * Stores all scopes defined for this instance of <code>VariableResolver</code>
     * <p>
     * Can store instances of <code>Scope</code> which have the ability to dynamically resolve against ExternalContext
     * for put operations.
     * </p>
     * <p>
     * WARNING: this implementation is not serialized as it is thread safe because it does not update/add to _scopes
     * after object initialization. If you need to add your own scopes, either extend and add more in an initialization
     * block, or add proper sychronization
     * </p>
     */
    protected final Map<String, Scope> _scopes = new HashMap<String, Scope>(16);
    {
        _scopes.putAll(s_standardScopes);
    }

    /**
     * RuntimeConfig is instantiated once per servlet and never changes--we can safely cache it
     */
    private RuntimeConfig runtimeConfig;

    private ManagedBeanBuilder beanBuilder = new ManagedBeanBuilder();

    /** Creates a new instance of ManagedBeanResolver */
    public ManagedBeanResolver()
    {
    }

    public void setValue(final ELContext context, final Object base, final Object property, final Object value)
                                                                                                               throws NullPointerException,
                                                                                                               PropertyNotFoundException,
                                                                                                               PropertyNotWritableException,
                                                                                                               ELException
    {

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException();
        }

    }

    public boolean isReadOnly(final ELContext context, final Object base, final Object property) throws NullPointerException,
                                                                                                PropertyNotFoundException,
                                                                                                ELException
    {

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException();
        }

        return false;
    }

    public Object getValue(final ELContext context, final Object base, final Object property) throws NullPointerException,
                                                                                             PropertyNotFoundException,
                                                                                             ELException
    {

        if (base != null)
            return null;

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }

        final ExternalContext extContext = externalContext(context);

        if (extContext == null)
            return null;
        if (extContext.getRequestMap().containsKey(property))
            return null;
        if (extContext.getSessionMap().containsKey(property))
            return null;
        if (extContext.getApplicationMap().containsKey(property))
            return null;

        if (!(property instanceof String))
            return null;

        final String strProperty = (String) property;

        final ManagedBean managedBean = runtimeConfig(context).getManagedBean(strProperty);
        Object beanInstance = null;
        if (managedBean != null)
        {
            FacesContext facesContext = facesContext(context);
            context.setPropertyResolved(true);
            beanInstance = createManagedBean(managedBean, facesContext);
        }

        return beanInstance;
    }

    // Create a managed bean. If the scope of the bean is "none" then
    // return it right away. Otherwise store the bean in the appropriate
    // scope and return null.
    //
    // adapted from Manfred's JSF 1.1 VariableResolverImpl
    @SuppressWarnings("unchecked")
    private Object createManagedBean(final ManagedBean managedBean, final FacesContext facesContext) throws ELException
    {

        final ExternalContext extContext = facesContext.getExternalContext();
        final Map<String, Object> requestMap = extContext.getRequestMap();

        // check for cyclic references
        List<String> beansUnderConstruction = (List<String>)requestMap.get(BEANS_UNDER_CONSTRUCTION);
        if (beansUnderConstruction == null)
        {
            beansUnderConstruction = new ArrayList<String>();
            requestMap.put(BEANS_UNDER_CONSTRUCTION, beansUnderConstruction);
        }

        final String managedBeanName = managedBean.getManagedBeanName();
        if (beansUnderConstruction.contains(managedBeanName))
        {
            throw new ELException("Detected cyclic reference to managedBean " + managedBeanName);
        }

        beansUnderConstruction.add(managedBeanName);

        Object obj = null;
        try
        {
            obj = beanBuilder.buildManagedBean(facesContext, managedBean);
        }
        finally
        {
            beansUnderConstruction.remove(managedBeanName);
        }

        putInScope(managedBean, extContext, obj);

        return obj;
    }

    private void putInScope(final ManagedBean managedBean, final ExternalContext extContext, final Object obj)
    {

        final String managedBeanName = managedBean.getManagedBeanName();

        if (obj == null)
        {
            if (log.isDebugEnabled())
                log.debug("Variable '" + managedBeanName + "' could not be resolved.");
        }
        else
        {

            final String scopeKey = managedBean.getManagedBeanScope();

            // find the scope handler object
            final Scope scope = (Scope) _scopes.get(scopeKey);
            if (scope == null)
            {
                log.error("Managed bean '" + managedBeanName + "' has illegal scope: " + scopeKey);
            }
            else
            {
                scope.put(extContext, managedBeanName, obj);
            }
        }

    }

    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

    private static ExternalContext externalContext(final ELContext context)
    {
        final FacesContext facesContext = facesContext(context);

        return facesContext != null ? facesContext.getExternalContext() : null;
    }

    public Class<?> getType(final ELContext context, final Object base, final Object property) throws NullPointerException,
                                                                                              PropertyNotFoundException,
                                                                                              ELException
    {

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException();
        }

        return null;
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base)
    {

        if (base != null)
            return null;

        final ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>();

        final Map<String, ManagedBean> managedBeans = runtimeConfig(context).getManagedBeans();
        for (Map.Entry<String, ManagedBean> managedBean : managedBeans.entrySet())
        {
            descriptors.add(makeDescriptor(managedBean.getKey(), managedBean.getValue()));
        }

        return descriptors.iterator();
    }

    private static FeatureDescriptor makeDescriptor(final String beanName, final ManagedBean managedBean)
    {
        final FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
        fd.setValue(ELResolver.TYPE, managedBean.getManagedBeanClass());
        fd.setName(beanName);
        fd.setDisplayName(beanName);
        fd.setShortDescription(managedBean.getDescription());
        fd.setExpert(false);
        fd.setHidden(false);
        fd.setPreferred(true);
        return fd;
    }

    protected RuntimeConfig runtimeConfig(final ELContext context)
    {
        final FacesContext facesContext = facesContext(context);

        // application-level singleton - we can safely cache this
        if (this.runtimeConfig == null)
        {
            this.runtimeConfig = RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
        }

        return runtimeConfig;
    }

    public Class<?> getCommonPropertyType(final ELContext context, final Object base)
    {

        if (base != null)
            return null;

        return Object.class;
    }

    interface Scope
    {
        public void put(ExternalContext extContext, String name, Object obj);
    }
}
