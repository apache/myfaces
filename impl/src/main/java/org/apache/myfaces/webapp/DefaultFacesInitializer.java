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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.config.FacesConfigValidator;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.context.servlet.ServletExternalContextImpl;
import org.apache.myfaces.el.ResolverForJSPInitializer;
import org.apache.myfaces.el.unified.ELResolverBuilder;
import org.apache.myfaces.el.unified.ResolverBuilderForJSP;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver.Scope;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultFacesInitializer implements FacesInitializer
{
    private static final Log log = LogFactory.getLog(DefaultFacesInitializer.class);
    private JspFactory _jspFactory;

    protected ELResolverBuilder createResolverBuilderForJSP(RuntimeConfig runtimeConfig)
    {
        return new ResolverBuilderForJSP(runtimeConfig);
    }

    public void initFaces(ServletContext servletContext)
    {
        try
        {
            log.trace("Initializing MyFaces");

            // Load the configuration
            ExternalContext externalContext = new ServletExternalContextImpl(servletContext, null, null);

            // parse web.xml
            WebXml webXml = WebXml.getWebXml(externalContext);
            if(webXml.getFacesServletMappings().isEmpty())
            {
                log.warn("No mappings of FacesServlet found. Abort initializing MyFaces.");
                return;
            }


            // TODO: this Class.forName will be removed when Tomcat fixes a bug
            // also, we should then be able to remove jasper.jar from the deployment
            try
            {
                Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
            }
            catch (ClassNotFoundException e)
            {
                // ignore
            }
            catch (Exception e) {
                log.debug(e.getMessage(), e);
            }

            JspFactory jspFactory = getJspFactory();
            if (log.isDebugEnabled())
            {
                log.debug("jspfactory = " + jspFactory);
            }
            JspApplicationContext appCtx = jspFactory.getJspApplicationContext(servletContext);

            appCtx.addELContextListener(new FacesELContextListener());

            RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
            runtimeConfig.setExpressionFactory(appCtx.getExpressionFactory());

            ApplicationImpl.setInitializingRuntimeConfig(runtimeConfig);

            // And configure everything
            new FacesConfigurator(externalContext).configure();

            validateFacesConfigIfNecessary(servletContext, externalContext);

            // configure the el resolver for jsp
            configureResolverForJSP(appCtx, runtimeConfig);

            if (! "false".equals(servletContext.getInitParameter(StateUtils.USE_ENCRYPTION)))
                StateUtils.initSecret(servletContext);

            log.info("ServletContext '" + servletContext.getRealPath("/") + "' initialized.");
        }
        catch (Exception ex)
        {
            log.error("Error initializing MyFaces: " + ex.getMessage(), ex);
        }
    }

    protected JspFactory getJspFactory()
    {
        if (_jspFactory == null)
        {
            return JspFactory.getDefaultFactory();
        }
        return _jspFactory;
    }
    
    /**
     * @param jspFactory the jspFactory to set
     */
    public void setJspFactory(JspFactory jspFactory)
    {
        _jspFactory = jspFactory;
    }

    /**
     * Register a phase listener to every lifecycle. This listener will lazy fill the el resolver for jsp as soon as the
     * first lifecycle is executed. This is necessarry to allow a faces application further setup after MyFaces has been
     * initialized. When the first request is processed no further configuation of the el resolvers is allowed.
     * 
     * @param appCtx
     * @param runtimeConfig
     */
    private void configureResolverForJSP(JspApplicationContext appCtx, RuntimeConfig runtimeConfig)
    {
        FacesCompositeELResolver facesCompositeELResolver = new FacesCompositeELResolver(Scope.JSP);
        appCtx.addELResolver(facesCompositeELResolver);
        PhaseListener resolverForJSPInitializer = new ResolverForJSPInitializer(
                createResolverBuilderForJSP(runtimeConfig), facesCompositeELResolver);

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        for (Iterator<String> iter = factory.getLifecycleIds(); iter.hasNext();)
        {
            factory.getLifecycle(iter.next()).addPhaseListener(resolverForJSPInitializer);
        }
    }

    protected void validateFacesConfigIfNecessary(ServletContext servletContext, ExternalContext externalContext)
    {
        if ("true".equals(servletContext.getInitParameter(FacesConfigValidator.VALIDATE_CONTEXT_PARAM)))
        {
            List<String> list = FacesConfigValidator.validate(externalContext, servletContext.getRealPath("/"));

            Iterator<String> iterator = list.iterator();

            while (iterator.hasNext())
                log.warn(iterator.next());

        }
    }

    public void destroyFaces(ServletContext servletContext)
    {
        // TODO is it possible to make a real cleanup?
    }

}
