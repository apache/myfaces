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
package org.apache.myfaces.config.annotation;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.inject.spi.BeanManager;

import jakarta.faces.FacesException;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.validator.FacesValidator;

import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.spi.AnnotationProviderFactory;
import org.apache.myfaces.view.facelets.util.Classpath;

/**
 * 
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public class DefaultAnnotationProvider extends AnnotationProvider
{
    private static final Logger log = Logger.getLogger(DefaultAnnotationProvider.class.getName());

    /**
     * <p>Prefix path used to locate web application classes for this
     * web application.</p>
     */
    private static final String WEB_CLASSES_PREFIX = "/WEB-INF/classes/";
    
    /**
     * <p>Prefix path used to locate web application libraries for this
     * web application.</p>
     */
    private static final String WEB_LIB_PREFIX = "/WEB-INF/lib/";
    
    private static final String META_INF_PREFIX = "META-INF/";

    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";

    /**
     * <p>Resource path used to acquire implicit resources buried
     * inside application JARs.</p>
     */
    private static final String FACES_CONFIG_IMPLICIT = "META-INF/faces-config.xml";

    /**
     * This set contains the annotation names that this AnnotationConfigurator is able to scan
     * in the format that is read from .class file.
     */
    private static final Set<String> JSF_ANNOTATION_NAMES;
    
    private static final Set<Class<? extends Annotation>> JSF_ANNOTATION_CLASSES;

    static
    {
        Set<String> bcan = new HashSet<>(10, 1f);
        bcan.add("Ljakarta/faces/component/FacesComponent;");
        bcan.add("Ljakarta/faces/component/behavior/FacesBehavior;");
        bcan.add("Ljakarta/faces/convert/FacesConverter;");
        bcan.add("Ljakarta/faces/validator/FacesValidator;");
        bcan.add("Ljakarta/faces/render/FacesRenderer;");
        bcan.add("Ljakarta/faces/event/NamedEvent;");
        //bcan.add("Ljakarta/faces/event/ListenerFor;");
        //bcan.add("Ljakarta/faces/event/ListenersFor;");
        bcan.add("Ljakarta/faces/render/FacesBehaviorRenderer;");
        JSF_ANNOTATION_NAMES = Collections.unmodifiableSet(bcan);

        Set<Class<? extends Annotation>> ancl = new HashSet<>(10, 1f);
        ancl.add(FacesComponent.class);
        ancl.add(FacesBehavior.class);
        ancl.add(FacesConverter.class);
        ancl.add(FacesValidator.class);
        ancl.add(FacesRenderer.class);
        ancl.add(NamedEvent.class);
        ancl.add(FacesBehaviorRenderer.class);
        JSF_ANNOTATION_CLASSES = Collections.unmodifiableSet(ancl);
    }

    public DefaultAnnotationProvider()
    {
        super();
    }
    
    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(ExternalContext ctx)
    {
        //1. Use CDI
        BeanManager beanManager = CDIUtils.getBeanManager(ctx);
        CdiAnnotationProviderExtension extension = CDIUtils.getOptional(beanManager,
                CdiAnnotationProviderExtension.class);
        if (extension != null)
        {
            return extension.getMap();
        }

        Map<Class<? extends Annotation>, Set<Class<?>>> map = new HashMap<>();
        Collection<Class<?>> classes = null;

        //2. Scan for annotations on /WEB-INF/classes
        try
        {
            classes = getAnnotatedWebInfClasses(ctx);
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }

        for (Class<?> clazz : classes)
        {
            processClass(map, clazz);
        }
        
        //3. Scan for annotations on classpath
        try
        {
            AnnotationProvider provider
                    = AnnotationProviderFactory.getAnnotationProviderFactory(ctx).getAnnotationProvider(ctx);
            classes = getAnnotatedMetaInfClasses(ctx, provider.getBaseUrls(ctx));
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        
        for (Class<?> clazz : classes)
        {
            processClass(map, clazz);
        }
        
        return map;
    }
    
    @Override
    public Set<URL> getBaseUrls(ExternalContext context) throws IOException
    {
        ClassLoader cl = ClassUtils.getCurrentLoader(this);

        Set<URL> urlSet = new HashSet<>();

        //This usually happens when maven-jetty-plugin is used
        //Scan jars looking for paths including META-INF/faces-config.xml
        Enumeration<URL> resources = cl.getResources(FACES_CONFIG_IMPLICIT);
        while (resources.hasMoreElements())
        {
            urlSet.add(resources.nextElement());
        }

        //Scan files inside META-INF ending with .faces-config.xml
        URL[] urls = Classpath.search(cl, META_INF_PREFIX, FACES_CONFIG_SUFFIX);
        Collections.addAll(urlSet, urls);

        return urlSet;
    }

    protected Collection<Class<?>> getAnnotatedMetaInfClasses(ExternalContext ctx, Set<URL> urls)
    {
        if (urls != null && !urls.isEmpty())
        {
            List<Class<?>> list = new ArrayList<>();
            for (URL url : urls)
            {
                try
                {
                    JarFile jarFile = getJarFile(url);
                    if (jarFile != null)
                    {
                        archiveClasses(jarFile, list);
                    }
                }
                catch(IOException e)
                {
                    log.log(Level.SEVERE, "cannot scan jar file for annotations:"+url, e);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    protected Collection<Class<?>> getAnnotatedWebInfClasses(ExternalContext ctx) throws IOException
    {
        String scanPackages = MyfacesConfig.getCurrentInstance(ctx).getScanPackages();
        if (scanPackages != null)
        {
            try
            {
                return packageClasses(ctx, scanPackages);
            }
            catch (ClassNotFoundException | IOException e)
            {
                throw new FacesException(e);
            }
        }
        else
        {
            return webClasses(ctx);
        }
    }
    
    /**
     * <p>Return a list of the classes defined within the given packages
     * If there are no such classes, a zero-length list will be returned.</p>
     *
     * @param scanPackages the package configuration
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     * @exception IOException if an input/output error occurs
     */
    private List<Class<?>> packageClasses(final ExternalContext externalContext, final String scanPackages)
            throws ClassNotFoundException, IOException
    {
        List<Class<?>> list = new ArrayList<>();

        String[] scanPackageTokens = scanPackages.split(",");
        for (String scanPackageToken : scanPackageTokens)
        {
            if (scanPackageToken.toLowerCase().endsWith(".jar"))
            {
                URL jarResource = externalContext.getResource(WEB_LIB_PREFIX + scanPackageToken);
                String jarURLString = "jar:" + jarResource.toString() + "!/";
                URL url = new URL(jarURLString);
                JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();

                archiveClasses(jarFile, list);
            }
            else
            {
                Class[] classes = PackageInfo.getClasses(scanPackageToken);
                for (Class c : classes)
                {
                    list.add(c);                    
                }
            }
        }
        return list;
    }    
    
    /**
     * <p>Return a list of classes to examine from the specified JAR archive.
     * If this archive has no classes in it, a zero-length list is returned.</p>
     *
     * @param jar <code>JarFile</code> for the archive to be scanned
     * @param list <code>List</code> list of classes
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     */
    private List<Class<?>> archiveClasses(JarFile jar, List<Class<?>> list)
    {
        // Accumulate and return a list of classes in this JAR file
        ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null)
        {
            loader = this.getClass().getClassLoader();
        }
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory())
            {
                continue; // This is a directory
            }
            String name = entry.getName();
            if (name.startsWith("META-INF/"))
            {
                continue; // Attribute files
            }
            if (!name.endsWith(".class"))
            {
                continue; // This is not a class
            }

            DataInputStream in = null;
            boolean couldContainAnnotation = false;
            try
            {
                in = new DataInputStream(jar.getInputStream(entry));
                couldContainAnnotation = ClassByteCodeAnnotationFilter.couldContainAnnotationsOnClassDef(in,
                        JSF_ANNOTATION_NAMES);
            }
            catch (IOException e)
            {
                // Include this class - we can't scan this class using
                // the filter, but it could be valid, so we need to
                // load it using the classLoader. Anyway, log a debug
                // message.
                couldContainAnnotation = true;
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("IOException when filtering class " + name + " for annotations");
                }
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        // No Op
                    }
                }
            }

            if (couldContainAnnotation)
            {
                name = name.substring(0, name.length() - 6); // Trim ".class"
                Class<?> clazz = null;
                try
                {
                    clazz = loader.loadClass(name.replace('/', '.'));
                }
                catch (NoClassDefFoundError | Exception e)
                {
                    // Skip this class - we cannot analyze classes we cannot load
                }
                // Skip this class - we cannot analyze classes we cannot load
                if (clazz != null)
                {
                    list.add(clazz);
                }
            }
        }
        return list;

    }
    
    /**
     * <p>Return a list of the classes defined under the
     * <code>/WEB-INF/classes</code> directory of this web
     * application.  If there are no such classes, a zero-length list
     * will be returned.</p>
     *
     * @param externalContext <code>ExternalContext</code> instance for
     *  this application
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     */
    private List<Class<?>> webClasses(ExternalContext externalContext)
    {
        List<Class<?>> list = new ArrayList<>();
        webClasses(externalContext, WEB_CLASSES_PREFIX, list);
        return list;
    }

    /**
     * <p>Add classes found in the specified directory to the specified
     * list, recursively calling this method when a directory is encountered.</p>
     *
     * @param externalContext <code>ExternalContext</code> instance for
     *  this application
     * @param prefix Prefix specifying the "directory path" to be searched
     * @param list List to be appended to
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     */
    private void webClasses(ExternalContext externalContext, String prefix, List<Class<?>> list)
    {
        ClassLoader loader = ClassUtils.getCurrentLoader(this);

        Set<String> paths = externalContext.getResourcePaths(prefix);
        if (paths == null)
        {
            return; //need this in case there is no WEB-INF/classes directory
        }
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("webClasses(" + prefix + ") - Received " + paths.size() + " paths to check");
        }

        String path = null;

        if (paths.isEmpty())
        {
            if (log.isLoggable(Level.WARNING))
            {
                log.warning("AnnotationConfigurator does not found classes "
                            + "for annotations in "
                            + prefix
                            + " ."
                            + " This could happen because maven jetty plugin is used"
                            + " (goal jetty:run). Try configure "
                            + MyfacesConfig.SCAN_PACKAGES + " init parameter "
                            + "or use jetty:run-exploded instead.");
            }
        }
        else
        {
            for (Object pathObject : paths)
            {
                path = (String) pathObject;
                if (path.endsWith("/"))
                {
                    webClasses(externalContext, path, list);
                }
                else if (path.endsWith(".class"))
                {
                    DataInputStream in = null;
                    boolean couldContainAnnotation = false;
                    try
                    {
                        in = new DataInputStream(externalContext.getResourceAsStream(path));
                        couldContainAnnotation = ClassByteCodeAnnotationFilter.couldContainAnnotationsOnClassDef(in,
                                JSF_ANNOTATION_NAMES);
                    }
                    catch (IOException e)
                    {
                        // Include this class - we can't scan this class using
                        // the filter, but it could be valid, so we need to
                        // load it using the classLoader. Anyway, log a debug
                        // message.
                        couldContainAnnotation = true;
                        if (log.isLoggable(Level.FINE))
                        {
                            log.fine("IOException when filtering class " + path + " for annotations");
                        }
                    }
                    finally
                    {
                        if (in != null)
                        {
                            try
                            {
                                in.close();
                            }
                            catch (IOException e)
                            {
                                // No Op
                            }
                        }
                    }

                    if (couldContainAnnotation)
                    {
                        //Load it and add it to list for later processing
                        path = path.substring(WEB_CLASSES_PREFIX.length()); // Strip prefix
                        path = path.substring(0, path.length() - 6); // Strip suffix
                        path = path.replace('/', '.'); // Convert to FQCN

                        Class<?> clazz = null;
                        try
                        {
                            clazz = loader.loadClass(path);
                        }
                        catch (NoClassDefFoundError | Exception e)
                        {
                            // Skip this class - we cannot analyze classes we cannot load
                        }
                        // Skip this class - we cannot analyze classes we cannot load
                        if (clazz != null)
                        {
                            list.add(clazz);
                        }
                    }
                }
            }
        }
    }
    
    private JarFile getJarFile(URL url) throws IOException
    {
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);

        JarFile jarFile;
        if (conn instanceof JarURLConnection)
        {
            jarFile = ((JarURLConnection) conn).getJarFile();
        }
        else
        {
            jarFile = _getAlternativeJarFile(url);
        }
        return jarFile;
    }
    

    /**
     * taken from org.apache.myfaces.view.facelets.util.Classpath
     * 
     * For URLs to JARs that do not use JarURLConnection - allowed by the servlet spec - attempt to produce a JarFile
     * object all the same. Known servlet engines that function like this include Weblogic and OC4J. This is not a full
     * solution, since an unpacked WAR or EAR will not have JAR "files" as such.
     */
    private static JarFile _getAlternativeJarFile(URL url) throws IOException
    {
        String urlFile = url.getFile();

        // Trim off any suffix - which is prefixed by "!/" on Weblogic
        int separatorIndex = urlFile.indexOf("!/");

        // OK, didn't find that. Try the less safe "!", used on OC4J
        if (separatorIndex == -1)
        {
            separatorIndex = urlFile.indexOf('!');
        }

        if (separatorIndex != -1)
        {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            // And trim off any "file:" prefix.
            if (jarFileUrl.startsWith("file:"))
            {
                jarFileUrl = jarFileUrl.substring("file:".length());
            }

            return new JarFile(jarFileUrl);
        }

        return null;
    }

    
    private void processClass(Map<Class<? extends Annotation>,Set<Class<?>>> map, Class<?> clazz)
    {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation anno : annotations)
        {
            Class<? extends Annotation> annotationClass = anno.annotationType();
            if (JSF_ANNOTATION_CLASSES.contains(annotationClass))
            {
                Set<Class<?>> set = map.computeIfAbsent(annotationClass, k -> new HashSet<>());
                set.add(clazz);
            }
        }
    }
}
