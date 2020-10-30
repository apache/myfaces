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
package org.apache.myfaces.webapp;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ExpressionFactory;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.jsp.JspFactory;
import org.apache.myfaces.config.MyfacesConfig;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.ResolverForJSPInitializer;
import org.apache.myfaces.el.ELResolverBuilder;
import org.apache.myfaces.el.ELResolverBuilderForJSP;
import org.apache.myfaces.el.resolver.FacesCompositeELResolver;
import org.apache.myfaces.el.resolver.FacesCompositeELResolver.Scope;
import org.apache.myfaces.util.lang.ClassUtils;

/**
 * Initializes MyFaces in a JSP 2.1 environment.
 *
 */
public class JspFacesInitializer extends AbstractFacesInitializer
{
    private static final Logger log = Logger.getLogger(JspFacesInitializer.class.getName());
    
    /**
     * Cached instance of the JspFactory to use.
     */
    private JspFactory jspFactory;
    
    @Override
    protected void initContainerIntegration(
            ServletContext servletContext, ExternalContext externalContext)
    {
        JspApplicationContext appCtx = 
            getJspFactory().getJspApplicationContext(servletContext);
        appCtx.addELContextListener(new FacesELContextListener());
        
        // check for user-specified ExpressionFactory
        ExpressionFactory expressionFactory = getUserDefinedExpressionFactory(externalContext);
        if (expressionFactory == null)
        {
            expressionFactory = appCtx.getExpressionFactory();
        }

        RuntimeConfig runtimeConfig =
            buildConfiguration(servletContext, externalContext, expressionFactory);
        
        // configure the el resolver for jsp
        configureResolverForJSP(appCtx, runtimeConfig, externalContext);
    }
    
    protected JspFactory getJspFactory()
    {
        if (jspFactory == null)
        {
            // workaround for Tomcat
            // JspFactory.getDefaultFactory() will return null unless JspRuntimeContext was initialized
            try
            {
                ClassUtils.classForName("org.apache.jasper.compiler.JspRuntimeContext");
            }
            catch (ClassNotFoundException e)
            {
                // ignore
            }
            catch (Exception ex)
            {
                log.log(Level.FINE, "An unexpected exception occured "
                        + "while loading the JspRuntimeContext.", ex);
            }

            jspFactory = JspFactory.getDefaultFactory();
        }

        return jspFactory;
    }

    /**
     * Sets the JspFactory to use. Currently, this method just simplifies
     * testing.
     * 
     * @param jspFactory
     *            the JspFactory to use
     */
    protected void setJspFactory(JspFactory jspFactory)
    {
        this.jspFactory = jspFactory;
    }

    /**
     * Register a phase listener to every lifecycle. This listener will lazy fill the el resolver for jsp as soon as the
     * first lifecycle is executed. This is necessarry to allow a faces application further setup after MyFaces has been
     * initialized. When the first request is processed no further configuation of the el resolvers is allowed.
     * 
     * @param appCtx
     * @param runtimeConfig
     */
    private void configureResolverForJSP(JspApplicationContext appCtx, RuntimeConfig runtimeConfig,
            ExternalContext externalContext)
    {
        FacesCompositeELResolver facesCompositeELResolver = new FacesCompositeELResolver(Scope.JSP);
        appCtx.addELResolver(facesCompositeELResolver);
        PhaseListener resolverForJSPInitializer = new ResolverForJSPInitializer(
                createResolverBuilderForJSP(runtimeConfig, MyfacesConfig.getCurrentInstance(externalContext)),
                facesCompositeELResolver);

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        for (Iterator<String> iter = factory.getLifecycleIds(); iter.hasNext();)
        {
            factory.getLifecycle(iter.next()).addPhaseListener(resolverForJSPInitializer);
        }
    }
    
    protected ELResolverBuilder createResolverBuilderForJSP(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig)
    {
        return new ELResolverBuilderForJSP(runtimeConfig, myfacesConfig);
    }
}
