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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.servlet.ServletContext;

import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.config.FacesConfigValidator;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.annotation.DefaultLifecycleProviderFactory;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.shared_impl.context.ExceptionHandlerImpl;
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
     * If the servlet mapping for the FacesServlet is added dynamically, Boolean.TRUE 
     * is stored under this key in the ServletContext.
     * ATTENTION: this constant is duplicate in MyFacesContainerInitializer.
     */
    private static final String FACES_SERVLET_ADDED_ATTRIBUTE = "org.apache.myfaces.DYNAMICALLY_ADDED_FACES_SERVLET";

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
            // JSF 2.0: FacesInitializer now has some new methods to
            // use proper startup FacesContext and ExternalContext instances.
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            // Parse and validate the web.xml configuration file
            WebXml webXml = WebXml.getWebXml(externalContext);
            if (webXml == null) {
                if (log.isLoggable(Level.WARNING)) {
                    log.warning("Couldn't find the web.xml configuration file. "
                             + "Abort initializing MyFaces.");
                }

                return;
            } else if (webXml.getFacesServletMappings().isEmpty()) {
                // check if the FacesServlet has been added dynamically
                // in a Servlet 3.0 environment by MyFacesContainerInitializer
                Boolean mappingAdded = (Boolean) servletContext.getAttribute(FACES_SERVLET_ADDED_ATTRIBUTE);
                if (mappingAdded == null || !mappingAdded)
                {
                    if (log.isLoggable(Level.WARNING))
                    {
                        log.warning("No mappings of FacesServlet found. Abort initializing MyFaces.");
                    }
                    return;
                }
            }

            initContainerIntegration(servletContext, externalContext);

            String useEncryption = servletContext.getInitParameter(StateUtils.USE_ENCRYPTION);
            if (!"false".equals(useEncryption)) { // the default value is true
                StateUtils.initSecret(servletContext);
            }

            if (log.isLoggable(Level.INFO)) {
                log.info("ServletContext '" + servletContext.getRealPath("/") + "' initialized.");
            }

            _dispatchApplicationEvent(servletContext, PostConstructApplicationEvent.class);
            
            //initialize LifecycleProvider. 
            //if not set here, first call of getLifecycleProvider is invoked with null external context
            //and org.apache.myfaces.config.annotation.LifecycleProvider context parameter is ignored.
            //see MYFACES-2555
            DefaultLifecycleProviderFactory.getLifecycleProviderFactory().getLifecycleProvider(externalContext);
            
            // print out a very prominent log message if the project stage is != Production
            if (!facesContext.isProjectStage(ProjectStage.Production))
            {
                ProjectStage projectStage = FacesContext.getCurrentInstance().getApplication().getProjectStage();
                StringBuilder message = new StringBuilder("\n\n");
                message.append("*******************************************************************\n");
                message.append("*** WARNING: Apache MyFaces-2 is running in ");
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
    private void _dispatchApplicationEvent(ServletContext servletContext, Class<? extends SystemEvent> eventClass) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        application.publishEvent(facesContext, eventClass, Application.class, application);
    }
    
    /**
     * Cleans up all remaining resources (well, theoretically).
     */
    public void destroyFaces(ServletContext servletContext) {
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        //We need to check if the current application was initialized by myfaces
        WebXml webXml = WebXml.getWebXml(facesContext.getExternalContext());
        if (webXml == null) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("Couldn't find the web.xml configuration file. "
                         + "Abort destroy MyFaces.");
            }

            return;
        } else if (webXml.getFacesServletMappings().isEmpty()) {
            // check if the FacesServlet has been added dynamically
            // in a Servlet 3.0 environment by MyFacesContainerInitializer
            Boolean mappingAdded = (Boolean) servletContext.getAttribute(FACES_SERVLET_ADDED_ATTRIBUTE);
            if (mappingAdded == null || !mappingAdded)
            {
                if (log.isLoggable(Level.WARNING))
                {
                    log.warning("No mappings of FacesServlet found. Abort destroy MyFaces.");
                }
                return;
            }
        }
        
        _dispatchApplicationEvent(servletContext, PreDestroyApplicationEvent.class);

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
    
    public FacesContext initStartupFacesContext(ServletContext servletContext)
    {
        // We cannot use FacesContextFactory, because it is necessary to initialize 
        // before Application and RenderKit factories, so we should use different object. 
        return _createFacesContext(servletContext, true);
    }
        
    public void destroyStartupFacesContext(FacesContext facesContext)
    {
        _releaseFacesContext(facesContext);
    }
    
    public FacesContext initShutdownFacesContext(ServletContext servletContext)
    {
        return _createFacesContext(servletContext, false);
    }
        
    public void destroyShutdownFacesContext(FacesContext facesContext)
    {
        _releaseFacesContext(facesContext);
    }
    
    private FacesContext _createFacesContext(ServletContext servletContext, boolean startup)
    {
        ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, startup);
        ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
        FacesContext facesContext = new StartupFacesContextImpl(externalContext, 
                (ReleaseableExternalContext) externalContext, exceptionHandler, startup);
        
        // If getViewRoot() is called during application startup or shutdown, 
        // it should return a new UIViewRoot with its locale set to Locale.getDefault().
        UIViewRoot startupViewRoot = new UIViewRoot();
        startupViewRoot.setLocale(Locale.getDefault());
        facesContext.setViewRoot(startupViewRoot);
        
        return facesContext;
    }
    
    private void _releaseFacesContext(FacesContext facesContext)
    {        
        // make sure that the facesContext gets released.
        // This is important in an OSGi environment 
        if (facesContext != null)
        {
            facesContext.release();
        }        
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
