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
package jakarta.faces;

import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.component.visit.VisitContextFactory;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.faces.context.ExternalContextFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.context.FlashFactory;
import jakarta.faces.context.PartialViewContextFactory;
import jakarta.faces.flow.FlowHandlerFactory;
import jakarta.faces.lifecycle.ClientWindowFactory;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import jakarta.faces.view.facelets.FaceletCacheFactory;
import jakarta.faces.view.facelets.TagHandlerDelegateFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.component.search.SearchExpressionContextFactory;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.core.api.shared.lang.ClassUtils;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public final class FactoryFinder
{
    public static final String APPLICATION_FACTORY = "jakarta.faces.application.ApplicationFactory";
    public static final String EXCEPTION_HANDLER_FACTORY = "jakarta.faces.context.ExceptionHandlerFactory";
    public static final String EXTERNAL_CONTEXT_FACTORY = "jakarta.faces.context.ExternalContextFactory";
    public static final String FACES_CONTEXT_FACTORY = "jakarta.faces.context.FacesContextFactory";
    public static final String LIFECYCLE_FACTORY = "jakarta.faces.lifecycle.LifecycleFactory";
    public static final String PARTIAL_VIEW_CONTEXT_FACTORY = "jakarta.faces.context.PartialViewContextFactory";
    public static final String RENDER_KIT_FACTORY = "jakarta.faces.render.RenderKitFactory";
    public static final String TAG_HANDLER_DELEGATE_FACTORY = "jakarta.faces.view.facelets.TagHandlerDelegateFactory";
    public static final String VIEW_DECLARATION_LANGUAGE_FACTORY = "jakarta.faces.view.ViewDeclarationLanguageFactory";
    public static final String VISIT_CONTEXT_FACTORY = "jakarta.faces.component.visit.VisitContextFactory";
    public static final String FACELET_CACHE_FACTORY = "jakarta.faces.view.facelets.FaceletCacheFactory";
    public static final String FLASH_FACTORY = "jakarta.faces.context.FlashFactory";
    public static final String FLOW_HANDLER_FACTORY = "jakarta.faces.flow.FlowHandlerFactory";
    public static final String CLIENT_WINDOW_FACTORY = "jakarta.faces.lifecycle.ClientWindowFactory";
    public static final String SEARCH_EXPRESSION_CONTEXT_FACTORY = 
            "jakarta.faces.component.search.SearchExpressionContextFactory";

    private static final Map<String, Class<?>> FACTORY_MAPPING = new HashMap<String, Class<?>>();
    private static final ClassLoader MYFACES_CLASSLOADER;
    
    private static final String INJECTION_PROVIDER_INSTANCE = "oam.spi.INJECTION_PROVIDER_KEY";
    private static final String INJECTED_BEAN_STORAGE_KEY = "org.apache.myfaces.spi.BEAN_ENTRY_STORAGE";
    private static final String BEAN_ENTRY_CLASS_NAME = "org.apache.myfaces.cdi.util.BeanEntry";

    private static final Logger LOGGER = Logger.getLogger(FactoryFinder.class.getName());
    
    
    /**
     * used as a monitor for itself and _factories. Maps in this map are used as monitors for themselves and the
     * corresponding maps in _factories.
     */
    private static Map<ClassLoader, Map<String, List<String>>> registeredFactoryNames
            = new HashMap<ClassLoader, Map<String, List<String>>>(5);

    /**
     * Maps from classLoader to another map, the container (i.e. Tomcat) will create a class loader for each web app
     * that it controls (typically anyway) and that class loader is used as the key.
     * 
     * The secondary map maps the factory name (i.e. FactoryFinder.APPLICATION_FACTORY) to actual instances that are
     * created via getFactory. The instances will be of the class specified in the setFactory method for the factory
     * name, i.e. FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MyFactory.class).
     */
    private static Map<ClassLoader, Map<String, Object>> factories
            = new HashMap<ClassLoader, Map<String, Object>>(5);

    static
    {        
        FACTORY_MAPPING.put(APPLICATION_FACTORY, ApplicationFactory.class);
        FACTORY_MAPPING.put(EXCEPTION_HANDLER_FACTORY, ExceptionHandlerFactory.class);
        FACTORY_MAPPING.put(EXTERNAL_CONTEXT_FACTORY, ExternalContextFactory.class);
        FACTORY_MAPPING.put(FACES_CONTEXT_FACTORY, FacesContextFactory.class);
        FACTORY_MAPPING.put(LIFECYCLE_FACTORY, LifecycleFactory.class);
        FACTORY_MAPPING.put(PARTIAL_VIEW_CONTEXT_FACTORY, PartialViewContextFactory.class);
        FACTORY_MAPPING.put(RENDER_KIT_FACTORY, RenderKitFactory.class);
        FACTORY_MAPPING.put(TAG_HANDLER_DELEGATE_FACTORY, TagHandlerDelegateFactory.class);
        FACTORY_MAPPING.put(VIEW_DECLARATION_LANGUAGE_FACTORY, ViewDeclarationLanguageFactory.class);
        FACTORY_MAPPING.put(VISIT_CONTEXT_FACTORY, VisitContextFactory.class);
        FACTORY_MAPPING.put(FACELET_CACHE_FACTORY, FaceletCacheFactory.class);
        FACTORY_MAPPING.put(FLASH_FACTORY, FlashFactory.class);
        FACTORY_MAPPING.put(FLOW_HANDLER_FACTORY, FlowHandlerFactory.class);
        FACTORY_MAPPING.put(CLIENT_WINDOW_FACTORY, ClientWindowFactory.class);
        FACTORY_MAPPING.put(SEARCH_EXPRESSION_CONTEXT_FACTORY, SearchExpressionContextFactory.class);
        try
        {
            ClassLoader classLoader;
            if (System.getSecurityManager() != null)
            {
                classLoader = (ClassLoader) AccessController.doPrivileged(
                        (PrivilegedExceptionAction) () -> FactoryFinder.class.getClassLoader());
            }
            else
            {
                classLoader = FactoryFinder.class.getClassLoader();
            }

            if (classLoader == null)
            {
                throw new FacesException("faces-api ClassLoader cannot be identified", null);
            }
            MYFACES_CLASSLOADER = classLoader;
        }
        catch (Exception e)
        {
            throw new FacesException("faces-api ClassLoader cannot be identified", e);
        }
    }

    // ~ Start FactoryFinderProvider Support
    
    private static Object factoryFinderProviderFactoryInstance;
    
    private static volatile boolean initialized = false;
    
    private static void initializeFactoryFinderProviderFactory()
    {
        if (!initialized)
        {
            factoryFinderProviderFactoryInstance = _FactoryFinderProviderFactory.getInstance();
            initialized = true;
        }
    }

    // ~ End FactoryFinderProvider Support

    // avoid instantiation
    FactoryFinder()
    {
    }

    /**
     * <p>
     * Create (if necessary) and return a per-web-application instance of the appropriate implementation class for the
     * specified JavaServer Faces factory class, based on the discovery algorithm described in the class description.
     * </p>
     * 
     * <p>
     * The standard factories and wrappers in Faces all implement the interface {@link FacesWrapper}. If the returned
     * <code>Object</code> is an implementation of one of the standard factories, it must be legal to cast it to an
     * instance of <code>FacesWrapper</code> and call {@link FacesWrapper#getWrapped()} on the instance.
     * </p>
     * 
     * @param factoryName
     *            Fully qualified name of the JavaServer Faces factory for which an implementation instance is requested
     * 
     * @return A per-web-application instance of the appropriate implementation class for the specified JavaServer Faces
     *         factory class
     * 
     * @throws FacesException
     *             if the web application class loader cannot be identified
     * @throws FacesException
     *             if an instance of the configured factory implementation class cannot be loaded
     * @throws FacesException
     *             if an instance of the configured factory implementation class cannot be instantiated
     * @throws IllegalArgumentException
     *             if <code>factoryname</code> does not identify a standard JavaServer Faces factory name
     * @throws IllegalStateException
     *             if there is no configured factory implementation class for the specified factory name
     * @throws NullPointerException
     *             if <code>factoryname</code> is null
     */
    public static Object getFactory(String factoryName) throws FacesException
    {
        Assert.notNull(factoryName, "factoryName");

        initializeFactoryFinderProviderFactory();
        
        if (factoryFinderProviderFactoryInstance == null)
        {
            // Do the typical stuff
            return _getFactory(factoryName);
        }
        else
        {
            try
            {
                //Obtain the FactoryFinderProvider instance for this context.
                Object ffp = _FactoryFinderProviderFactory
                        .FACTORY_FINDER_PROVIDER_FACTORY_GET_FACTORY_FINDER_METHOD
                        .invoke(factoryFinderProviderFactoryInstance, null);
                
                //Call getFactory method and pass the params
                return _FactoryFinderProviderFactory
                        .FACTORY_FINDER_PROVIDER_GET_FACTORY_METHOD.invoke(ffp, factoryName);
            }
            catch (InvocationTargetException e)
            {
                Throwable targetException = e.getCause();
                if (targetException instanceof NullPointerException)
                {
                    throw (NullPointerException) targetException;
                }
                else if (targetException instanceof FacesException)
                {
                    throw (FacesException) targetException;
                }
                else if (targetException instanceof IllegalArgumentException)
                {
                    throw (IllegalArgumentException) targetException;
                }
                else if (targetException instanceof IllegalStateException)
                {
                    throw (IllegalStateException) targetException;
                }
                else if (targetException == null)
                {
                    throw new FacesException(e);
                }
                else
                {
                    throw new FacesException(targetException);
                }
            }
            catch (Exception e)
            {
                //No Op
                throw new FacesException(e);
            }
        }
    }

    private static Object _getFactory(String factoryName) throws FacesException
    {
        ClassLoader classLoader = ClassUtils.getContextClassLoader();

        // This code must be synchronized because this could cause a problem when
        // using update feature each time of myfaces (org.apache.myfaces.CONFIG_REFRESH_PERIOD)
        // In this moment, a concurrency problem could happen
        Map<String, List<String>> factoryClassNames = null;
        Map<String, Object> factoryMap = null;

        synchronized (registeredFactoryNames)
        {
            factoryClassNames = registeredFactoryNames.get(classLoader);
            if (factoryClassNames == null)
            {
                String message
                        = "No Factories configured for this Application. This happens if the faces-initialization "
                        + "does not work at all - make sure that you properly include all configuration "
                        + "settings necessary for a basic faces application "
                        + "and that all the necessary libs are included. Also check the logging output of your "
                        + "web application and your container for any exceptions!"
                        + "\nIf you did that and find nothing, the mistake might be due to the fact "
                        + "that you use some special web-containers which "
                        + "do not support registering context-listeners via TLD files and "
                        + "a context listener is not setup in your web.xml.\n"
                        + "A typical config looks like this;\n<listener>\n"
                        + "  <listener-class>org.apache.myfaces.webapp.StartupServletContextListener</listener-class>\n"
                        + "</listener>\n";
                throw new IllegalStateException(message);
            }

            if (!factoryClassNames.containsKey(factoryName))
            {
                throw new IllegalArgumentException("no factory " + factoryName + " configured for this application.");
            }

            factoryMap = factories.computeIfAbsent(classLoader, k -> new HashMap<>());
        }

        List beanEntryStorage;
        List<String> classNames;
        Object factory;
        Object injectionProvider;
        synchronized (factoryClassNames)
        {
            beanEntryStorage = (List) factoryMap.computeIfAbsent(INJECTED_BEAN_STORAGE_KEY,
                    k -> new CopyOnWriteArrayList());
            
            factory = factoryMap.get(factoryName);
            if (factory != null)
            {
                return factory;
            }

            classNames = factoryClassNames.get(factoryName);
            
            injectionProvider = factoryMap.get(INJECTION_PROVIDER_INSTANCE);
        }

        if (injectionProvider == null)
        {
            injectionProvider = getInjectionProvider();
            synchronized (factoryClassNames)
            {
                factoryMap.put(INJECTION_PROVIDER_INSTANCE, injectionProvider);
            }
        }

        // release lock while calling out
        factory = newFactoryInstance(FACTORY_MAPPING.get(factoryName), 
            classNames.iterator(), classLoader, injectionProvider, beanEntryStorage);

        synchronized (factoryClassNames)
        {
            // check if someone else already installed the factory
            if (factoryMap.get(factoryName) == null)
            {
                factoryMap.put(factoryName, factory);
            }
        }

        return factory;
    }
    
    private static Object getInjectionProvider()
    {
        try
        {
            // Remember the first call in a webapp over FactoryFinder.getFactory(...) comes in the 
            // initialization block, so there is a startup FacesContext active and
            // also a valid startup ExternalContext. Note after that, we need to cache
            // the injection provider for the classloader, because in a normal
            // request there is no active FacesContext in the moment and this call will
            // surely fail.
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null)
            {
                Object injectionProviderFactory =
                    _FactoryFinderProviderFactory.INJECTION_PROVIDER_FACTORY_GET_INSTANCE_METHOD
                        .invoke(_FactoryFinderProviderFactory.INJECTION_PROVIDER_CLASS);
                Object injectionProvider = 
                    _FactoryFinderProviderFactory.INJECTION_PROVIDER_FACTORY_GET_INJECTION_PROVIDER_METHOD
                        .invoke(injectionProviderFactory, facesContext.getExternalContext());
                return injectionProvider;
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }
    
    private static void injectAndPostConstruct(Object injectionProvider, Object instance, List injectedBeanStorage)
    {
        if (injectionProvider != null)
        {
            try
            {
                Object creationMetaData = _FactoryFinderProviderFactory.INJECTION_PROVIDER_INJECT_METHOD.invoke(
                    injectionProvider, instance);

                addBeanEntry(instance, creationMetaData, injectedBeanStorage);

                _FactoryFinderProviderFactory.INJECTION_PROVIDER_POST_CONSTRUCT_METHOD.invoke(
                    injectionProvider, instance, creationMetaData);
            }
            catch (Exception ex)
            {
                throw new FacesException(ex);
            }
        }
    }
    
    private static void preDestroy(Object injectionProvider, Object beanEntry)
    {
        if (injectionProvider != null)
        {
            try
            {
                _FactoryFinderProviderFactory.INJECTION_PROVIDER_PRE_DESTROY_METHOD.invoke(
                    injectionProvider, getInstance(beanEntry), getCreationMetaData(beanEntry));
            }
            catch (Exception ex)
            {
                throw new FacesException(ex);
            }
        }
    }

    private static Object getInstance(Object beanEntry)
    {
        try
        {
            Method getterMethod = getMethod(beanEntry, "getInstance");
            return getterMethod.invoke(beanEntry);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static Object getCreationMetaData(Object beanEntry)
    {
        try
        {
            Method getterMethod = getMethod(beanEntry, "getCreationMetaData");
            return getterMethod.invoke(beanEntry);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static Method getMethod(Object beanEntry, String methodName) throws NoSuchMethodException
    {
        return beanEntry.getClass().getDeclaredMethod(methodName);
    }

    private static void addBeanEntry(Object instance, Object creationMetaData, List injectedBeanStorage)
    {
        try
        {
            Class<?> beanEntryClass = ClassUtils.classForName(BEAN_ENTRY_CLASS_NAME);
            Constructor beanEntryConstructor = beanEntryClass.getDeclaredConstructor(Object.class, Object.class);

            Object result = beanEntryConstructor.newInstance(instance, creationMetaData);
            injectedBeanStorage.add(result);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Object newFactoryInstance(Class<?> interfaceClass, Iterator<String> classNamesIterator,
                                             ClassLoader classLoader, Object injectionProvider,
                                             List injectedBeanStorage)
    {
        try
        {
            Object current = null;
            
            while (classNamesIterator.hasNext())
            {
                String implClassName = classNamesIterator.next();
                Class<?> implClass = null;
                try
                {
                    implClass = classLoader.loadClass(implClassName);
                }
                catch (ClassNotFoundException e)
                {
                    implClass = MYFACES_CLASSLOADER.loadClass(implClassName);
                }

                // check, if class is of expected interface type
                if (!interfaceClass.isAssignableFrom(implClass))
                {
                    throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
                }

                if (current == null)
                {
                    // nothing to decorate
                    current = implClass.newInstance();
                    injectAndPostConstruct(injectionProvider, current, injectedBeanStorage);
                }
                else
                {
                    // let's check if class supports the decorator pattern
                    try
                    {
                        Constructor<?> delegationConstructor = implClass.getConstructor(new Class[] { interfaceClass });
                        // impl class supports decorator pattern,
                        try
                        {
                            // create new decorator wrapping current
                            current = delegationConstructor.newInstance(new Object[] { current });
                            injectAndPostConstruct(injectionProvider, current, injectedBeanStorage);
                        }
                        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                        {
                            throw new FacesException(e);
                        }
                    }
                    catch (NoSuchMethodException e)
                    {
                        // no decorator pattern support
                        current = implClass.newInstance();
                        injectAndPostConstruct(injectionProvider, current, injectedBeanStorage);
                    }
                }
            }

            return current;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new FacesException(e);
        }
    }

    public static void setFactory(String factoryName, String implName)
    {
        Assert.notNull(factoryName, "factoryName");

        initializeFactoryFinderProviderFactory();
        
        if (factoryFinderProviderFactoryInstance == null)
        {
            // Do the typical stuff
            _setFactory(factoryName, implName);
        }
        else
        {
            try
            {
                //Obtain the FactoryFinderProvider instance for this context.
                Object ffp = _FactoryFinderProviderFactory
                        .FACTORY_FINDER_PROVIDER_FACTORY_GET_FACTORY_FINDER_METHOD
                        .invoke(factoryFinderProviderFactoryInstance, null);
                
                //Call getFactory method and pass the params
                _FactoryFinderProviderFactory
                        .FACTORY_FINDER_PROVIDER_SET_FACTORY_METHOD.invoke(ffp, factoryName, implName);
            }
            catch (InvocationTargetException e)
            {
                Throwable targetException = e.getCause();
                if (targetException instanceof NullPointerException)
                {
                    throw (NullPointerException) targetException;
                }
                else if (targetException instanceof FacesException)
                {
                    throw (FacesException) targetException;
                }
                else if (targetException instanceof IllegalArgumentException)
                {
                    throw (IllegalArgumentException) targetException;
                }
                else if (targetException == null)
                {
                    throw new FacesException(e);
                }
                else
                {
                    throw new FacesException(targetException);
                }
            }
            catch (Exception e)
            {
                //No Op
                throw new FacesException(e);
            }
            
        }
    }

    private static void _setFactory(String factoryName, String implName)
    {
        checkFactoryName(factoryName);

        ClassLoader classLoader = ClassUtils.getContextClassLoader();
        Map<String, List<String>> factoryClassNames = null;
        synchronized (registeredFactoryNames)
        {
            Map<String, Object> factories = FactoryFinder.factories.get(classLoader);

            if (factories != null && factories.containsKey(factoryName))
            {
                // Javadoc says ... This method has no effect if getFactory() has already been
                // called looking for a factory for this factoryName.
                return;
            }

            factoryClassNames = registeredFactoryNames.computeIfAbsent(classLoader,
                    k -> new HashMap<>());
        }

        synchronized (factoryClassNames)
        {
            List<String> classNameList = factoryClassNames.computeIfAbsent(factoryName, k -> new ArrayList<>());
            classNameList.add(implName);
        }
    }

    public static void releaseFactories() throws FacesException
    {
        initializeFactoryFinderProviderFactory();
        
        if (factoryFinderProviderFactoryInstance == null)
        {
            // Do the typical stuff
            _releaseFactories();
        }
        else
        {
            try
            {
                //Obtain the FactoryFinderProvider instance for this context.
                Object ffp = _FactoryFinderProviderFactory
                        .FACTORY_FINDER_PROVIDER_FACTORY_GET_FACTORY_FINDER_METHOD
                        .invoke(factoryFinderProviderFactoryInstance, null);
                
                //Call getFactory method and pass the params
                _FactoryFinderProviderFactory.FACTORY_FINDER_PROVIDER_RELEASE_FACTORIES_METHOD.invoke(ffp, null);
            }
            catch (InvocationTargetException e)
            {
                Throwable targetException = e.getCause();
                if (targetException instanceof FacesException)
                {
                    throw (FacesException) targetException;
                }
                else if (targetException == null)
                {
                    throw new FacesException(e);
                }
                else
                {
                    throw new FacesException(targetException);
                }
            }
            catch (Exception e)
            {
                //No Op
                throw new FacesException(e);
            }
            
        }
    }

    private static void _releaseFactories() throws FacesException
    {
        ClassLoader classLoader = ClassUtils.getContextClassLoader();

        Map<String, Object> factoryMap;
        // This code must be synchronized
        synchronized (registeredFactoryNames)
        {
            factoryMap = factories.remove(classLoader);

            // _registeredFactoryNames has as value type Map<String,List> and this must
            // be cleaned before release (for gc).
            Map<String, List<String>> factoryClassNames = registeredFactoryNames.get(classLoader);
            if (factoryClassNames != null)
            {
                factoryClassNames.clear();
            }

            registeredFactoryNames.remove(classLoader);
        }

        if (factoryMap != null)
        {
            Object injectionProvider = factoryMap.remove(INJECTION_PROVIDER_INSTANCE);
            if (injectionProvider != null)
            {
                List injectedBeanStorage = (List)factoryMap.get(INJECTED_BEAN_STORAGE_KEY);

                FacesException firstException = null;
                for (Object entry : injectedBeanStorage)
                {
                    try
                    {
                        preDestroy(injectionProvider, entry);
                    }
                    catch (FacesException e)
                    {
                        LOGGER.log(Level.SEVERE, "#preDestroy failed", e);

                        if (firstException == null)
                        {
                            firstException = e; //all preDestroy callbacks need to get invoked
                        }
                    }
                }
                injectedBeanStorage.clear();

                if (firstException != null)
                {
                    throw firstException;
                }
            }
        }
    }

    private static void checkFactoryName(String factoryName)
    {
        if (!FACTORY_MAPPING.containsKey(factoryName))
        {
            throw new IllegalArgumentException("factoryName '" + factoryName + '\'');
        }
    }
}
