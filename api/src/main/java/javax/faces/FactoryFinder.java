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


import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKitFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
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

    private static Map _registeredFactoryNames = new HashMap();
    /**
     * Maps from classLoader to another map, the container (i.e. Tomcat) will create a class loader for
     * each web app that it controls (typically anyway) and that class loader is used as the key.
     *
     * The secondary map maps the factory name (i.e. FactoryFinder.APPLICATION_FACTORY) to actual instances
     * that are created via getFactory. The instances will be of the class specified in the setFactory method
     * for the factory name, i.e. FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MyFactory.class).
     */
    private static Map _factories = new HashMap();

    private static final Set VALID_FACTORY_NAMES = new HashSet();
    private static final Map ABSTRACT_FACTORY_CLASSES = new HashMap();
    static {
        VALID_FACTORY_NAMES.add(APPLICATION_FACTORY);
        VALID_FACTORY_NAMES.add(FACES_CONTEXT_FACTORY);
        VALID_FACTORY_NAMES.add(LIFECYCLE_FACTORY);
        VALID_FACTORY_NAMES.add(RENDER_KIT_FACTORY);

        ABSTRACT_FACTORY_CLASSES.put(APPLICATION_FACTORY, ApplicationFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(FACES_CONTEXT_FACTORY, FacesContextFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(LIFECYCLE_FACTORY, LifecycleFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(RENDER_KIT_FACTORY, RenderKitFactory.class);
    }


  // avoid instantiation
  FactoryFinder() {
  }

  public static Object getFactory(String factoryName)
            throws FacesException
    {
        if(factoryName == null)
            throw new NullPointerException("factoryName may not be null");

        ClassLoader classLoader = getClassLoader();
        
        //This code must be synchronized because this could cause a problem when
        //using update feature each time of myfaces (org.apache.myfaces.CONFIG_REFRESH_PERIOD)
        //In this moment, a concurrency problem could happen
        synchronized(_registeredFactoryNames)
        {        
            Map factoryClassNames = (Map) _registeredFactoryNames.get(classLoader);
    
            if (factoryClassNames == null)
            {
                String message = "No Factories configured for this Application. This happens if the faces-initialization "+
                "does not work at all - make sure that you properly include all configuration settings necessary for a basic faces application " +
                "and that all the necessary libs are included. <br/>\n "+
                "Make sure that the JSF-API (myfaces-api-xxx.jar) is not included twice on the classpath - if not, the factories might be configured in the wrong class instance. <br/>\n"+
                "If you use Tomcat, it might also pay off to clear your /[tomcat-installation]/work/ directory - Tomcat sometimes stumbles over its own tld-cache stored there, and doesn't load the StartupServletContextListener. <br/>\n"+
                "Also check the logging output of your web application and your container for any exceptions! \n<br/>" +
                "If you did that and find nothing, the mistake might be due to the fact that you use one of the very few web-containers which "+
                "do not support registering context-listeners via TLD files and " +
                "a context listener is not setup in your web.xml. <br/>\n" +
                "Add the following lines to your web.xml file to work around this issue : <br/>\n&lt;listener&gt;\n<br/>" +
                "  &lt;listener-class&gt;org.apache.myfaces.webapp.StartupServletContextListener&lt;/listener-class&gt;\n<br/>" +
                "&lt;/listener&gt;";
    
                throw new IllegalStateException(message);
            }
    
            if (! factoryClassNames.containsKey(factoryName)) {
                throw new IllegalArgumentException("no factory " + factoryName + " configured for this application.");
            }
    
            Map factoryMap = (Map) _factories.get(classLoader);
    
            if (factoryMap == null) {
                factoryMap = new HashMap();
                _factories.put(classLoader, factoryMap);
            }
            Object factory = factoryMap.get(factoryName);
    
            if (factory == null) {
                List classNames = (List) factoryClassNames.get(factoryName);
                factory = newFactoryInstance((Class)ABSTRACT_FACTORY_CLASSES.get(factoryName), classNames.iterator(), classLoader);
                factoryMap.put(factoryName, factory);
                return factory;
            }
            else
            {
                return factory;
            }
        }
    }


    private static Object newFactoryInstance(Class interfaceClass, Iterator classNamesIterator, ClassLoader classLoader)
    {
        try
        {
            Object current = null;

            while (classNamesIterator.hasNext())
            {
                String implClassName = (String) classNamesIterator.next();
                Class implClass = classLoader.loadClass(implClassName);

                // check, if class is of expected interface type
                if (!interfaceClass.isAssignableFrom(implClass))
                {
                    throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
                }

                if (current == null)
                {
                    // nothing to decorate
                    current = implClass.newInstance();
                } else
                {
                    // let's check if class supports the decorator pattern
                    try
                    {
                        Constructor delegationConstructor = implClass.getConstructor(new Class[]{interfaceClass});
                        // impl class supports decorator pattern,
                        try
                        {
                            // create new decorator wrapping current
                            current = delegationConstructor.newInstance(new Object[]{current});
                        } catch (InstantiationException e)
                        {
                            throw new FacesException(e);
                        } catch (IllegalAccessException e)
                        {
                            throw new FacesException(e);
                        } catch (InvocationTargetException e)
                        {
                            throw new FacesException(e);
                        }
                    } catch (NoSuchMethodException e)
                    {
                        // no decorator pattern support
                        current = implClass.newInstance();
                    }
                }
            }

            return current;
        } catch (ClassNotFoundException e)
        {
            throw new FacesException(e);
        } catch (InstantiationException e)
        {
            throw new FacesException(e);
        } catch (IllegalAccessException e)
        {
            throw new FacesException(e);
        }
    }


    public static void setFactory(String factoryName,
                                  String implName)
    {
        checkFactoryName(factoryName);

        ClassLoader classLoader = getClassLoader();
        synchronized(_registeredFactoryNames)
        {
            Map factories = (Map) _factories.get(classLoader);

            if (factories != null && factories.containsKey(factoryName)) {
                // Javadoc says ... This method has no effect if getFactory() has already been
                // called looking for a factory for this factoryName.
                return;
            }

            Map factoryClassNames = (Map) _registeredFactoryNames.get(classLoader);

            if (factoryClassNames == null)
            {
                factoryClassNames = new HashMap();
                _registeredFactoryNames.put(classLoader, factoryClassNames);
            }

            List classNameList = (List) factoryClassNames.get(factoryName);

            if (classNameList == null) {
                classNameList = new ArrayList();
                factoryClassNames.put(factoryName, classNameList);
            }
            classNameList.add(implName);
        }
    }


    public static void releaseFactories()
            throws FacesException
    {
        ClassLoader classLoader = getClassLoader();
        
        //This code must be synchronized
        synchronized(_registeredFactoryNames)
        {
            _factories.remove(classLoader);            
            
            // _registeredFactoryNames has as value type Map<String,List> and this must
            //be cleaned before release (for gc).
            Map factoryClassNames = _registeredFactoryNames.get(classLoader);
            if (factoryClassNames != null) factoryClassNames.clear();
            _registeredFactoryNames.remove(classLoader);
        }
    }

    private static void checkFactoryName(String factoryName)
    {
        if (! VALID_FACTORY_NAMES.contains(factoryName)) {
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
