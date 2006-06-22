/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.myfaces.el;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.VariableResolver;
import javax.faces.FacesException;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.ManagedBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class VariableResolverImpl
    extends VariableResolver
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Log log              = LogFactory.getLog(VariableResolverImpl.class);
    private static final String BEANS_UNDER_CONSTRUCTION = "org.apache.myfaces.config.beansUnderConstruction";

    //~ Instance fields ----------------------------------------------------------------------------

    public static final Map s_standardImplicitObjects = new HashMap(32);
    static {
        s_standardImplicitObjects.put(
            "applicationScope",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getApplicationMap();
                }
            });
        s_standardImplicitObjects.put(
            "cookie",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestCookieMap();
                }
            });
        s_standardImplicitObjects.put(
            "facesContext",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext;
                }
            });
        s_standardImplicitObjects.put(
            "header",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestHeaderMap();
                }
            });
        s_standardImplicitObjects.put(
            "headerValues",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestHeaderValuesMap();
                }
            });
        s_standardImplicitObjects.put(
            "initParam",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getInitParameterMap();
                }
            });
        s_standardImplicitObjects.put(
            "param",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestParameterMap();
                }
            });
        s_standardImplicitObjects.put(
            "paramValues",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestParameterValuesMap();
                }
            });
        s_standardImplicitObjects.put(
            "requestScope",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getRequestMap();
                }
            });
        s_standardImplicitObjects.put(
            "sessionScope",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getExternalContext().getSessionMap();
                }
            });
        s_standardImplicitObjects.put(
            "view",
            new ImplicitObject()
            {
                public Object get(FacesContext facesContext)
                {
                    return facesContext.getViewRoot();
                }
            });
    }

    /**
     * Stores all implicit objects defined for this instance of <code>VariableResolver</code>
     * <p>
     * Can store instances of <code>ImplicitObject</code> which have the ability to
     * dynamically resolve against FacesContext. Can also store any other object
     * which itself is the value for the implicit object (this in effect will be
     * a static object).
     * </p>
     * <p>
     * WARNING: this implementation is not serialized as it is thread safe because
     *          it does not update/add to _implicitObjects after object initialization.
     *          If you need to add your own implicit objects, either extend and add more
     *          in an initialization block, or add proper sychronization
     * </p>
     */
    protected final Map _implicitObjects = new HashMap(32);
    {
        _implicitObjects.putAll(s_standardImplicitObjects);
    }

    protected static final Map s_standardScopes = new HashMap(16);
    static {
        s_standardScopes.put(
            "request",
            new Scope()
            {
                public void put(ExternalContext extContext, String name, Object obj)
                {
                    extContext.getRequestMap().put(name, obj);
                }
            });
        s_standardScopes.put(
            "session",
            new Scope()
            {
                public void put(ExternalContext extContext, String name, Object obj)
                {
                    extContext.getSessionMap().put(name, obj);
                }
            });
        s_standardScopes.put(
            "application",
            new Scope()
            {
                public void put(ExternalContext extContext, String name, Object obj)
                {
                    extContext.getApplicationMap().put(name, obj);
                }
            });
        s_standardScopes.put(
            "none",
            new Scope()
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
     * Can store instances of <code>Scope</code> which have the ability to
     * dynamically resolve against ExternalContext for put operations.
     * </p>
     * <p>
     * WARNING: this implementation is not serialized as it is thread safe because
     *          it does not update/add to _scopes after object initialization.
     *          If you need to add your own scopes, either extend and add more
     *          in an initialization block, or add proper sychronization
     * </p>
     */
    protected final Map _scopes = new HashMap(16);
    {
        _scopes.putAll(s_standardScopes);
    }

    /**
     * RuntimeConfig is instantiated once per servlet and never changes--we can
     * safely cache it
     */
    private RuntimeConfig _runtimeConfig;

    private ManagedBeanBuilder beanBuilder = new ManagedBeanBuilder();


    //~ Methods ---------------------------------------------------------------

    public Object resolveVariable(FacesContext facesContext, String name)
    {
        if ((name == null) || (name.length() == 0))
        {
            throw new ReferenceSyntaxException("Varible name is null or empty");
        }

        // Implicit objects
        Object implicitObject = _implicitObjects.get(name);
        if (implicitObject != null)
        {
            if (implicitObject instanceof ImplicitObject)
            {
                // a complex runtime object
                return ((ImplicitObject) implicitObject).get(facesContext);
            }
            else
            {
                // a simple object
                return implicitObject;
            }
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        // Request context
        Map    requestMap = externalContext.getRequestMap();
        Object obj = requestMap.get(name);
        if (obj != null)
        {
            return obj;
        }

        // Session context
        obj = externalContext.getSessionMap().get(name);
        if (obj != null)
        {
            return obj;
        }

        // Application context
        obj = externalContext.getApplicationMap().get(name);
        if (obj != null)
        {
            return obj;
        }

        // ManagedBean
        ManagedBean mbc = getRuntimeConfig(facesContext).getManagedBean(name);

        if (mbc != null)
        {

            // check for cyclic references
            List beansUnderConstruction = (List)requestMap.get(BEANS_UNDER_CONSTRUCTION);
            if (beansUnderConstruction == null) {
                beansUnderConstruction = new ArrayList();
                requestMap.put(BEANS_UNDER_CONSTRUCTION, beansUnderConstruction);
            }

            String managedBeanName = mbc.getManagedBeanName();
            if (beansUnderConstruction.contains(managedBeanName)) {
                throw new FacesException( "Detected cyclic reference to managedBean " + mbc.getManagedBeanName());
            }

            beansUnderConstruction.add(managedBeanName);
            try {
                obj = beanBuilder.buildManagedBean(facesContext, mbc);
            } finally {
                beansUnderConstruction.remove(managedBeanName);
            }

            // put in scope
            String scopeKey = mbc.getManagedBeanScope();

            // find the scope handler object
            Scope scope = (Scope) _scopes.get(scopeKey);
            if (scope == null)
            {
                log.error("Managed bean '" + name + "' has illegal scope: "
                    + scopeKey);
            }
            else
            {
                scope.put(externalContext, name, obj);
            }

            if(obj==null && log.isDebugEnabled())
            {
                log.debug("Variable '" + name + "' could not be resolved.");
            }

            return obj;
        }

        if(log.isDebugEnabled())
        {
            log.debug("Variable '" + name + "' could not be resolved.");
        }
        
        return null;
    }

    protected RuntimeConfig getRuntimeConfig(FacesContext facesContext)
    {
        if (_runtimeConfig == null)
        {
            _runtimeConfig = RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
        }
        return _runtimeConfig;
    }
}


interface ImplicitObject
{
    //~ Methods ---------------------------------------------------------------

    public Object get(FacesContext facesContext);
}


interface Scope
{
    //~ Methods ---------------------------------------------------------------

    public void put(ExternalContext extContext, String name, Object obj);
}
