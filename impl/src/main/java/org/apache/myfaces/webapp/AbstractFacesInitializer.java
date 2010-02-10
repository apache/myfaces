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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ExpressionFactory;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ProjectStage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;

import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.application._SystemEventServletRequest;
import org.apache.myfaces.application._SystemEventServletResponse;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.config.FacesConfigValidator;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.context.servlet.ServletExternalContextImpl;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

/**
 * Performs common initialization tasks.
 */
public abstract class AbstractFacesInitializer implements FacesInitializer {
    /**
     * The logger instance for this class.
     */
    //private static final Log log = LogFactory.getLog(AbstractFacesInitializer.class);
    private static final Logger log = Logger.getLogger(AbstractFacesInitializer.class.getName());

    /**
     * This parameter specifies the ExpressionFactory implementation to use.
     */
    @JSFWebConfigParam(since="1.2.7")
    protected static final String EXPRESSION_FACTORY = "org.apache.myfaces.EXPRESSION_FACTORY";

    /**
     * Performs all necessary initialization tasks like configuring this JSF
     * application.
     */
    public void initFaces(ServletContext servletContext) {
        try {
            if (log.isLoggable(Level.FINEST)) {
                log.finest("Initializing MyFaces");
            }

            // Some parts of the following configuration tasks have been implemented 
            // by using an ExternalContext. However, that's no problem as long as no 
            // one tries to call methods depending on either the ServletRequest or 
            // the ServletResponse.
            ExternalContext externalContext = new ServletExternalContextImpl(
                    servletContext, null, null);

            // Parse and validate the web.xml configuration file
            WebXml webXml = WebXml.getWebXml(externalContext);
            if (webXml == null) {
                if (log.isLoggable(Level.WARNING)) {
                    log.warning("Couldn't find the web.xml configuration file. "
                             + "Abort initializing MyFaces.");
                }

                return;
            } else if (webXml.getFacesServletMappings().isEmpty()) {
                if (log.isLoggable(Level.WARNING)) {
                    log.warning("No mappings of FacesServlet found. Abort initializing MyFaces.");
                }

                return;
            }

            initContainerIntegration(servletContext, externalContext);

            String useEncryption = servletContext.getInitParameter(StateUtils.USE_ENCRYPTION);
            if (!"false".equals(useEncryption)) { // the default value is true
                StateUtils.initSecret(servletContext);
            }

            if (log.isLoggable(Level.INFO)) {
                log.info("ServletContext '" + servletContext.getRealPath("/") + "' initialized.");
            }

            dispatchInitDestroyEvent(servletContext, PostConstructApplicationEvent.class);
            
            // print out a very prominent log message if the project stage is != Production
            if (!FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Production))
            {
                ProjectStage projectStage = FacesContext.getCurrentInstance().getApplication().getProjectStage();
                StringBuilder message = new StringBuilder("\n\n");
                message.append("*******************************************************************\n");
                message.append("*** WARNING: JavaServer Faces is running in ");
                message.append(projectStage.name().toUpperCase());        
                message.append(" mode.");
                int length = projectStage.name().length();
                for (int i = 0; i < 11 - length; i++)
                {
                    message.append(" ");
                }
                message.append("   ***\n");
                message.append("***                                         ");
                for (int i = 0; i < length; i++)
                {
                    message.append("^");
                }
                for (int i = 0; i < 20 - length; i++)
                {
                    message.append(" ");
                }
                message.append("***\n");
                message.append("*** Do NOT deploy to your live server(s) without changing this. ***\n");
                message.append("*** See Application#getProjectStage() for more information.     ***\n");
                message.append("*******************************************************************\n");
                log.log(Level.WARNING, message.toString());
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "An error occured while initializing MyFaces: "
                      + ex.getMessage(), ex);
        }
    }


    /**
     * Eventually we can use our plugin infrastructure for this as well
     * it would be a cleaner interception point than the base class
     * but for now this position is valid as well
     * <p/>
     * Note we add it for now here because the application factory object
     * leaves no possibility to have a destroy interceptor
     * and applications are per web application singletons
     * Note if this does not work out
     * move the event handler into the application factory
     *
     * @param servletContext the servlet context to be passed down
     * @param eventClass     the class to be passed down into the dispatching
     *                       code
     */
    private void dispatchInitDestroyEvent(Object servletContext, Class eventClass) {
        ApplicationFactory appFac = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        FacesContext fc = null;

        fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            LifecycleFactory lifeFac = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            FacesContextFactory facFac = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            fc = facFac.getFacesContext(servletContext, new _SystemEventServletRequest(), new _SystemEventServletResponse(), lifeFac.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE));
        }
        appFac.getApplication().publishEvent(fc, eventClass, Application.class, appFac.getApplication());
    }

    /**
     * Cleans up all remaining resources (well, theoretically).
     */
    public void destroyFaces(ServletContext servletContext) {
        dispatchInitDestroyEvent(servletContext, PreDestroyApplicationEvent.class);
        // TODO is it possible to make a real cleanup?
    }

    /**
     * Configures this JSF application. It's required that every
     * FacesInitializer (i.e. every subclass) calls this method during
     * initialization.
     *
     * @param servletContext    the current ServletContext
     * @param externalContext   the current ExternalContext
     * @param expressionFactory the ExpressionFactory to use
     * @return the current runtime configuration
     */
    protected RuntimeConfig buildConfiguration(ServletContext servletContext,
                                               ExternalContext externalContext, ExpressionFactory expressionFactory) {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        runtimeConfig.setExpressionFactory(expressionFactory);

        ApplicationImpl.setInitializingRuntimeConfig(runtimeConfig);

        // And configure everything
        new FacesConfigurator(externalContext).configure();

        validateFacesConfig(servletContext, externalContext);

        return runtimeConfig;
    }

    protected void validateFacesConfig(ServletContext servletContext, ExternalContext externalContext) {
        String validate = servletContext.getInitParameter(FacesConfigValidator.VALIDATE_CONTEXT_PARAM);
        if ("true".equals(validate) && log.isLoggable(Level.WARNING)) { // the default value is false
            List<String> warnings = FacesConfigValidator.validate(
                    externalContext, servletContext.getRealPath("/"));

            for (String warning : warnings) {
                log.warning(warning);
            }
        }
    }

    /**
     * Try to load user-definied ExpressionFactory. Returns <code>null</code>,
     * if no custom ExpressionFactory was specified.
     *
     * @param externalContext the current ExternalContext
     * @return User-specified ExpressionFactory, or
     *         <code>null</code>, if no no custom implementation was specified
     */
    protected static ExpressionFactory getUserDefinedExpressionFactory(ExternalContext externalContext) {
        String expressionFactoryClassName = externalContext.getInitParameter(EXPRESSION_FACTORY);
        if (expressionFactoryClassName != null
            && expressionFactoryClassName.trim().length() > 0) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Attempting to load the ExpressionFactory implementation "
                          + "you've specified: '" + expressionFactoryClassName + "'.");
            }

            return loadExpressionFactory(expressionFactoryClassName);
        }

        return null;
    }

    /**
     * Loads and instantiates the given ExpressionFactory implementation.
     *
     * @param expressionFactoryClassName the class name of the ExpressionFactory implementation
     * @return the newly created ExpressionFactory implementation, or
     *         <code>null</code>, if an error occurred
     */
    protected static ExpressionFactory loadExpressionFactory(String expressionFactoryClassName) {
        try {
            Class<?> expressionFactoryClass = Class.forName(expressionFactoryClassName);
            return (ExpressionFactory) expressionFactoryClass.newInstance();
        } catch (Exception ex) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "An error occured while instantiating a new ExpressionFactory. "
                          + "Attempted to load class '" + expressionFactoryClassName + "'.", ex);
            }
        }

        return null;
    }

    /**
     * Performs initialization tasks depending on the current environment.
     *
     * @param servletContext  the current ServletContext
     * @param externalContext the current ExternalContext
     */
    protected abstract void initContainerIntegration(
            ServletContext servletContext, ExternalContext externalContext);

}
