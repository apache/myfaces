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
package javax.faces;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.ApplicationFactory;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKitFactory;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class FactoryFinder
{
    public static final String APPLICATION_FACTORY = "javax.faces.application.ApplicationFactory";
    public static final String FACES_CONTEXT_FACTORY = "javax.faces.context.FacesContextFactory";
    public static final String LIFECYCLE_FACTORY = "javax.faces.lifecycle.LifecycleFactory";
    public static final String RENDER_KIT_FACTORY = "javax.faces.render.RenderKitFactory";
    
    /**
     * @since 2.0
     */
    public static final String EXCEPTION_HANDLER_FACTORY = "javax.faces.context.ExceptionHandlerFactory";

    /**
     * used as a monitor for itself and _factories.
     * Maps in this map are used as monitors for themselves and the corresponding maps in _factories.
     */
    private static Map<ClassLoader, Map<String, List<String>>> _registeredFactoryNames = 
        new HashMap<ClassLoader, Map<String, List<String>>>();
    
    /**
     * Maps from classLoader to another map, the container (i.e. Tomcat) will create a class loader for
     * each web app that it controls (typically anyway) and that class loader is used as the key.
     *
     * The secondary map maps the factory name (i.e. FactoryFinder.APPLICATION_FACTORY) to actual instances
     * that are created via getFactory. The instances will be of the class specified in the setFactory method
     * for the factory name, i.e. FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MyFactory.class).
     */
    private static Map<ClassLoader, Map<String, Object>> _factories = 
        new HashMap<ClassLoader, Map<String, Object>>();

    private static final Set<String> VALID_FACTORY_NAMES = new HashSet<String>();
    private static final Map<String, Class<?>> ABSTRACT_FACTORY_CLASSES = new HashMap<String, Class<?>>();
    
    static
    {
        VALID_FACTORY_NAMES.add(APPLICATION_FACTORY);
        VALID_FACTORY_NAMES.add(FACES_CONTEXT_FACTORY);
        VALID_FACTORY_NAMES.add(LIFECYCLE_FACTORY);
        VALID_FACTORY_NAMES.add(RENDER_KIT_FACTORY);
        VALID_FACTORY_NAMES.add(EXCEPTION_HANDLER_FACTORY);

        ABSTRACT_FACTORY_CLASSES.put(APPLICATION_FACTORY, ApplicationFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(FACES_CONTEXT_FACTORY, FacesContextFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(LIFECYCLE_FACTORY, LifecycleFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(RENDER_KIT_FACTORY, RenderKitFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(EXCEPTION_HANDLER_FACTORY, ExceptionHandlerFactory.class);
    }


  // avoid instantiation
  FactoryFinder() {
  }

  /**
   * <p>Create (if necessary) and return a per-web-application instance of the appropriate 
   * implementation class for the specified JavaServer Faces factory class, based on the 
   * discovery algorithm described in the class description.</p>
   * 
   * <p>The standard factories and wrappers in JSF all implement the interface {@link FacesWrapper}. 
   * If the returned <code>Object</code> is an implementation of one of the standard factories, 
   * it must be legal to cast it to an instance of <code>FacesWrapper</code> and call 
   * {@link FacesWrapper#getWrapped()} on the instance.</p>
   * 
   * @param factoryName Fully qualified name of the JavaServer Faces factory for which an 
   *                    implementation instance is requested
   *                    
   * @return A per-web-application instance of the appropriate implementation class for the 
   *         specified JavaServer Faces factory class
   *         
   * @throws FacesException if the web application class loader cannot be identified 
   * @throws FacesException if an instance of the configured factory implementation class 
   *         cannot be loaded 
   * @throws FacesException if an instance of the configured factory implementation class 
   *         cannot be instantiated 
   * @throws IllegalArgumentException if <code>factoryname</code> does not identify a standard 
   *         JavaServer Faces factory name 
   * @throws IllegalStateException if there is no configured factory implementation class 
   *         for the specified factory name 
   * @throws NullPointerException if <code>factoryname</code> is null
   */
  public static Object getFactory(String factoryName) throws FacesException
  {
        if(factoryName == null)
        {
            throw new NullPointerException("factoryName may not be null");
        }

        ClassLoader classLoader = getClassLoader();

        //This code must be synchronized because this could cause a problem when
        //using update feature each time of myfaces (org.apache.myfaces.CONFIG_REFRESH_PERIOD)
        //In this moment, a concurrency problem could happen
        Map<String, List<String>> factoryClassNames = null;
        Map<String, Object> factoryMap = null;
        
        synchronized(_registeredFactoryNames)
        {
            factoryClassNames = _registeredFactoryNames.get(classLoader);

            if (factoryClassNames == null)
            {
                String message = "No Factories configured for this Application. This happens if the faces-initialization "+
                    "does not work at all - make sure that you properly include all configuration settings necessary for a basic faces application " +
                    "and that all the necessary libs are included. Also check the logging output of your web application and your container for any exceptions!" +
                    "\nIf you did that and find nothing, the mistake might be due to the fact that you use some special web-containers which "+
                    "do not support registering context-listeners via TLD files and " +
                    "a context listener is not setup in your web.xml.\n" +
                    "A typical config looks like this;\n<listener>\n" +
                    "  <listener-class>org.apache.myfaces.webapp.StartupServletContextListener</listener-class>\n" +
                    "</listener>\n";
                throw new IllegalStateException(message);
            }

            if (! factoryClassNames.containsKey(factoryName))
            {
                throw new IllegalArgumentException("no factory " + factoryName + " configured for this application.");
            }

            factoryMap = _factories.get(classLoader);

            if (factoryMap == null)
            {
                factoryMap = new HashMap<String, Object>();
                _factories.put(classLoader, factoryMap);
            }
        }
        
        List<String> classNames = null;
        Object factory = null;
        synchronized (factoryClassNames)
        {
            factory = factoryMap.get(factoryName);
            if (factory != null)
            {
                return factory;
            }
            
            classNames = factoryClassNames.get(factoryName);
        }
        
        //release lock while calling out
        factory = newFactoryInstance(ABSTRACT_FACTORY_CLASSES.get(factoryName), classNames.iterator(), classLoader);
        
        synchronized (factoryClassNames)
        {
            //check if someone else already installed the factory
            if (factoryMap.get(factoryName) == null)
            {
                factoryMap.put(factoryName, factory);
            }            
        }
        
        return factory;
    }

    private static Object newFactoryInstance(Class<?> interfaceClass, Iterator<String> classNamesIterator, ClassLoader classLoader)
    {
        try
        {
            Object current = null;

            while (classNamesIterator.hasNext())
            {
                String implClassName = classNamesIterator.next();
                Class<?> implClass = classLoader.loadClass(implClassName);

                // check, if class is of expected interface type
                if (!interfaceClass.isAssignableFrom(implClass))
                {
                    throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
                }

                if (current == null)
                {
                    // nothing to decorate
                    current = implClass.newInstance();
                }
                else
                {
                    // let's check if class supports the decorator pattern
                    try
                    {
                        Constructor<?> delegationConstructor = implClass.getConstructor(new Class[]{interfaceClass});
                        // impl class supports decorator pattern,
                        try
                        {
                            // create new decorator wrapping current
                            current = delegationConstructor.newInstance(new Object[]{current});
                        }
                        catch (InstantiationException e)
                        {
                            throw new FacesException(e);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new FacesException(e);
                        }
                        catch (InvocationTargetException e)
                        {
                            throw new FacesException(e);
                        }
                    }
                    catch (NoSuchMethodException e)
                    {
                        // no decorator pattern support
                        current = implClass.newInstance();
                    }
                }
            }

            return current;
        }
        catch (ClassNotFoundException e)
        {
            throw new FacesException(e);
        }
        catch (InstantiationException e)
        {
            throw new FacesException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new FacesException(e);
        }
    }


    public static void setFactory(String factoryName, String implName)
    {
        checkFactoryName(factoryName);

        ClassLoader classLoader = getClassLoader();
        Map<String, List<String>> factoryClassNames = null;
        synchronized(_registeredFactoryNames)
        {
            Map<String, Object> factories = _factories.get(classLoader);

            if (factories != null && factories.containsKey(factoryName)) {
                // Javadoc says ... This method has no effect if getFactory() has already been
                // called looking for a factory for this factoryName.
                return;
            }

            factoryClassNames = _registeredFactoryNames.get(classLoader);

            if (factoryClassNames == null)
            {
                factoryClassNames = new HashMap<String, List<String>>();
                _registeredFactoryNames.put(classLoader, factoryClassNames);
            }
        }
        
        synchronized (factoryClassNames)
        {
            List<String> classNameList = factoryClassNames.get(factoryName);

            if (classNameList == null) 
            {
                classNameList = new ArrayList<String>();
                factoryClassNames.put(factoryName, classNameList);
            }
            
            classNameList.add(implName);
        }
    }


    public static void releaseFactories() throws FacesException
    {
        ClassLoader classLoader = getClassLoader();

        //This code must be synchronized
        synchronized(_registeredFactoryNames)
        {
            _factories.remove(classLoader);            
            
            // _registeredFactoryNames has as value type Map<String,List> and this must
            //be cleaned before release (for gc).
            Map<String, List<String>> factoryClassNames = _registeredFactoryNames.get(classLoader);
            if (factoryClassNames != null)
            {
                factoryClassNames.clear();
            }
            
            _registeredFactoryNames.remove(classLoader);
        }
    }

    private static void checkFactoryName(String factoryName)
    {
        if (! VALID_FACTORY_NAMES.contains(factoryName))
        {
            throw new IllegalArgumentException("factoryName '" + factoryName + "'");
        }
    }
    
    private static ClassLoader getClassLoader()
    {
        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null)
            {
                throw new FacesException("web application class loader cannot be identified", null);
            }
            return classLoader;
        }
        catch (Exception e)
        {
            throw new FacesException("web application class loader cannot be identified", e);
        }
    }
}
