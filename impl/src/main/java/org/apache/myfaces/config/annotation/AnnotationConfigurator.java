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
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.NoneScoped;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.FacesComponent;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesRenderer;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.validator.FacesValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.FacesConfigDispenser;
import org.apache.myfaces.config.NamedEventManager;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.view.facelets.util.Classpath;

/**
 * Configure all annotations that needs to be defined at startup.
 * 
 * <ul>
 * <li>{@link javax.faces.component.FacesComponent}</li>
 * <li>{@link javax.faces.convert.FacesConverter}</li>
 * <li>{@link javax.faces.validator.FacesValidator}</li>
 * <li>{@link javax.faces.render.FacesRenderer}</li>
 * <li>{@link javax.faces.bean.ManagedBean}</li>
 * <li>{@link javax.faces.bean.ManagedProperty}</li>
 * <li>PENDING:</li>
 * <li>{@link javax.faces.render.FacesBehaviorRenderer}</li>
 * </ul>
 * <p>
 * Some parts copied from org.apache.shale.tiger.view.faces.LifecycleListener2
 * </p>
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class AnnotationConfigurator
{
    private static final Log log = LogFactory
            .getLog(AnnotationConfigurator.class);

    private static final String META_INF_PREFIX = "META-INF/";

    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";
    
    private static final String STANDARD_FACES_CONFIG_RESOURCE = "META-INF/standard-faces-config.xml";
    
    /**
     * <p> Servlet context init parameter which defines which packages to scan
     * for beans, separated by commas.</p>
     */
    public static final String SCAN_PACKAGES = "org.apache.myfaces.annotation.SCAN_PACKAGES";

    /**
     * <p>Context relative path to the default <code>faces-config.xml</code>
     * resource for a JavaServer Faces application.</p>
     */
    private static final String FACES_CONFIG_DEFAULT = "/WEB-INF/faces-config.xml";

    /**
     * <p>Resource path used to acquire implicit resources buried
     * inside application JARs.</p>
     */
    private static final String FACES_CONFIG_IMPLICIT = "META-INF/faces-config.xml";

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
    
    private final ExternalContext _externalContext;
    
    private final _ClassByteCodeAnnotationFilter _filter;    

    private static Set<String> byteCodeAnnotationsNames;

    static
    {
        Set<String> bcan = new HashSet<String>(10, 1f);
        bcan.add("Lorg/apache/myfaces/test/annotations/component/FacesComponent;");
        bcan.add("Lorg/apache/myfaces/test/annotations/converter/FacesConverter;");
        bcan.add("Lorg/apache/myfaces/test/annotations/validator/FacesValidator;");
        bcan.add("Lorg/apache/myfaces/test/annotations/render/FacesRenderer;");
        bcan.add("Lorg/apache/myfaces/test/annotations/bean/ManagedBean;");

        byteCodeAnnotationsNames = Collections.unmodifiableSet(bcan);
    }

    public AnnotationConfigurator(ExternalContext externalContext)
    {
        _externalContext = externalContext;
        _filter = new _ClassByteCodeAnnotationFilter();
    }

    public void configure(final Application application, 
            final FacesConfigDispenser<FacesConfig> dispenser,
            boolean metadataComplete) throws FacesException
    {
        List<Class> classes;
        
        if (metadataComplete)
        {
            //Read only annotations available myfaces-impl
            List<JarFile> archives = null;
            try
            {                
                //Also scan jar including META-INF/standard-faces-config.xml
                //(myfaces-impl jar file)
                JarFile jarFile = getMyfacesImplJarFile();
                if (jarFile != null)
                {
                    classes = archiveClasses(_externalContext, jarFile);
                    for (Class clazz : classes)
                    {
                        configureClass(application, dispenser, clazz);
                    }
                }
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
            return;
        }

        // Scan annotation available in org.apache.myfaces.annotation.SCAN_PACKAGES 
        // init param
        String scanPackages = _externalContext.getInitParameter(SCAN_PACKAGES);
        if (scanPackages != null)
        {
            // Scan the classes configured by the scan_packages context parameter
            try
            {
                classes = packageClasses(_externalContext, scanPackages);
            }
            catch (ClassNotFoundException e)
            {
                throw new FacesException(e);
            }
            catch (IOException e)
            {
                throw new FacesException(e);
            }
        }
        else
        {
            // Scan the classes in /WEB-INF/classes for interesting annotations
            try
            {
                classes = webClasses(_externalContext);
            }
            catch (ClassNotFoundException e)
            {
                throw new FacesException(e);
            }
        }

        try
        {
            for (Class clazz : classes)
            {
                configureClass(application, dispenser, clazz);
            }
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        // Scan the classes in /WEB-INF/lib for interesting annotations
        List<JarFile> archives = null;
        try
        {
            archives = webArchives(_externalContext);

            System.out.println("Receiving " + archives.size()
                    + " jar files to check");
            for (JarFile archive : archives)
            {
                classes = archiveClasses(_externalContext, archive);
                for (Class clazz : classes)
                {
                    configureClass(application, dispenser, clazz);
                }
            }
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
        
        // Scan annotations available myfaces-impl
        try
        {                
            //Also scan jar including META-INF/standard-faces-config.xml
            //(myfaces-impl jar file)
            JarFile jarFile = getMyfacesImplJarFile();
            if (jarFile != null)
            {
                classes = archiveClasses(_externalContext, jarFile);
                for (Class clazz : classes)
                {
                    configureClass(application, dispenser, clazz);
                }
            }
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    private JarFile getMyfacesImplJarFile() throws IOException
    {
        URL url = getClassLoader().getResource(STANDARD_FACES_CONFIG_RESOURCE);
        return getJarFile(url);
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
     * <p>Return a list of the classes defined within the given packages
     * If there are no such classes, a zero-length list will be returned.</p>
     *
     * @param scanPackages the package configuration
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     * @exception IOException if an input/output error occurs
     */
    private List<Class> packageClasses(final ExternalContext externalContext,
            final String scanPackages) throws ClassNotFoundException, IOException
    {

        List<Class> list = new ArrayList<Class>();

        String[] scanPackageTokens = scanPackages.split(",");
        for (String scanPackageToken : scanPackageTokens)
        {
            if (scanPackageToken.toLowerCase().endsWith(".jar"))
            {
                URL jarResource = externalContext.getResource(WEB_LIB_PREFIX
                        + scanPackageToken);
                String jarURLString = "jar:" + jarResource.toString() + "!/";
                URL url = new URL(jarURLString);
                JarFile jarFile = ((JarURLConnection) url.openConnection())
                        .getJarFile();

                list.addAll(archiveClasses(externalContext, jarFile));
            }
            else
            {
                _PackageInfo.getInstance().getClasses(list, scanPackageToken);
            }
        }
        return list;
    }

    /**
     * <p>Return a list of the JAR archives defined under the
     * <code>/WEB-INF/lib</code> directory of this web application
     * that contain a <code>META-INF/faces-config.xml</code> resource
     * (even if that resource is empty).  If there are no such JAR archives,
     * a zero-length list will be returned.</p>
     *
     * @param externalContext <code>ExternalContext</code> instance for
     *  this application
     *
     * @exception IOException if an input/output error occurs
     */
    private List<JarFile> webArchives(ExternalContext externalContext)
            throws IOException
    {

        List<JarFile> list = new ArrayList<JarFile>();
        Set<String> paths = externalContext.getResourcePaths(WEB_LIB_PREFIX);

        if (paths.isEmpty())
        {
            //This usually happens when maven-jetty-plugin is used
            //Scan jars looking for paths including META-INF/faces-config.xml
            Iterator<URL> it = ClassUtils.getResources(FACES_CONFIG_IMPLICIT,
                    this);
            while (it.hasNext())
            {
                URL url = it.next();
                JarFile jarFile = getJarFile(url);
                if (jarFile != null)
                {
                    list.add(jarFile);
                }
            }
            
            //Scan files inside META-INF ending with .faces-config.xml
            URL[] urls = Classpath.search(getClassLoader(), META_INF_PREFIX, FACES_CONFIG_SUFFIX);
            for (int i = 0; i < urls.length; i++)
            {
                JarFile jarFile = getJarFile(urls[i]);
                if (jarFile != null)
                {
                    list.add(jarFile);
                }
            }
        }
        else
        {
            //Scan jars available on path
            for (Object pathObject : paths)
            {
                String path = (String) pathObject;
                if (!path.endsWith(".jar"))
                {
                    continue;
                }
                URL url = externalContext.getResource(path);
                String jarURLString = "jar:" + url.toString() + "!/";
                url = new URL(jarURLString);
                JarFile jarFile = ((JarURLConnection) url.openConnection())
                        .getJarFile();
                // Skip this JAR file if it does not have a META-INF/faces-config.xml
                // resource (even if that resource is empty)
                JarEntry signal = jarFile.getJarEntry(FACES_CONFIG_IMPLICIT);
                if (signal == null)
                {
                    //Look for files starting with META-INF/ and ending with .faces-config.xml
                    for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();)
                    {
                        JarEntry entry = e.nextElement();
                        String name = entry.getName(); 
                        if (name.startsWith(META_INF_PREFIX) &&
                                name.endsWith(FACES_CONFIG_SUFFIX))
                        {
                            signal = entry;
                            break;
                        }
                    }
                }
                if (signal == null)
                {
                    if (log.isTraceEnabled())
                    {
                        log
                                .trace("Skip JAR file "
                                        + path
                                        + " because it has no META-INF/faces-config.xml resource");
                    }
                    continue;
                }
                list.add(jarFile);
            }
        }
        return list;
    }

    /**
     * <p>Return a list of classes to examine from the specified JAR archive.
     * If this archive has no classes in it, a zero-length list is returned.</p>
     *
     * @param context <code>ExternalContext</code> instance for
     *  this application
     * @param jar <code>JarFile</code> for the archive to be scanned
     *
     * @exception ClassNotFoundException if a located class cannot be loaded
     */
    private List<Class> archiveClasses(ExternalContext context, JarFile jar)
            throws ClassNotFoundException
    {

        // Accumulate and return a list of classes in this JAR file
        List<Class> list = new ArrayList<Class>();
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
                couldContainAnnotation = _filter
                        .couldContainAnnotationsOnClassDef(in,
                                byteCodeAnnotationsNames);
            }
            catch (IOException e)
            {
                // Include this class - we can't scan this class using
                // the filter, but it could be valid, so we need to
                // load it using the classLoader. Anyway, log a debug
                // message.
                couldContainAnnotation = true;
                if (log.isDebugEnabled())
                {
                    log.debug("IOException when filtering class " + name
                            + " for annotations");
                }
            }
            finally
            {
                if (in != null)
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        // No Op
                    }
            }

            if (couldContainAnnotation)
            {
                name = name.substring(0, name.length() - 6); // Trim ".class"
                Class clazz = null;
                try
                {
                    clazz = loader.loadClass(name.replace('/', '.'));
                }
                catch (NoClassDefFoundError e)
                {
                    ; // Skip this class - we cannot analyze classes we cannot load
                }
                catch (Exception e)
                {
                    ; // Skip this class - we cannot analyze classes we cannot load
                }
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
    private List<Class> webClasses(ExternalContext externalContext)
            throws ClassNotFoundException
    {
        List<Class> list = new ArrayList<Class>();
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
    private void webClasses(ExternalContext externalContext, String prefix,
            List<Class> list) throws ClassNotFoundException
    {

        ClassLoader loader = getClassLoader();

        Set<String> paths = externalContext.getResourcePaths(prefix);
        if (log.isTraceEnabled())
        {
            log.trace("webClasses(" + prefix + ") - Received " + paths.size()
                    + " paths to check");
        }
        System.out.println("webClasses(" + prefix + ") - Received "
                + paths.size() + " paths to check");

        String path = null;

        if (paths.isEmpty())
        {
            if (log.isWarnEnabled())
            {
                log
                        .warn("AnnotationConfigurator does not found classes "
                                + "for annotations in "
                                + prefix
                                + " ."
                                + " This could happen because maven jetty plugin is used"
                                + " (goal jetty:run). Try configure "
                                + SCAN_PACKAGES + " init parameter "
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
                        in = new DataInputStream(externalContext
                                .getResourceAsStream(path));
                        couldContainAnnotation = _filter
                                .couldContainAnnotationsOnClassDef(in,
                                        byteCodeAnnotationsNames);
                    }
                    catch (IOException e)
                    {
                        // Include this class - we can't scan this class using
                        // the filter, but it could be valid, so we need to
                        // load it using the classLoader. Anyway, log a debug
                        // message.
                        couldContainAnnotation = true;
                        if (log.isDebugEnabled())
                        {
                            log.debug("IOException when filtering class " + path
                                    + " for annotations");
                        }
                    }
                    finally
                    {
                        if (in != null)
                            try
                            {
                                in.close();
                            }
                            catch (IOException e)
                            {
                                // No Op
                            }
                    }

                    if (couldContainAnnotation)
                    {
                        //Load it and add it to list for later processing
                        path = path.substring(WEB_CLASSES_PREFIX.length()); // Strip prefix
                        path = path.substring(0, path.length() - 6); // Strip suffix
                        path = path.replace('/', '.'); // Convert to FQCN

                        Class clazz = null;
                        try
                        {
                            clazz = loader.loadClass(path);
                        }
                        catch (NoClassDefFoundError e)
                        {
                            ; // Skip this class - we cannot analyze classes we cannot load
                        }
                        catch (Exception e)
                        {
                            ; // Skip this class - we cannot analyze classes we cannot load
                        }
                        if (clazz != null)
                        {
                            list.add(clazz);
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * 
     * @param application
     * @param dispenser
     * @param clazz
     */
    protected void configureClass( Application application, FacesConfigDispenser<FacesConfig> dispenser, Class clazz)
    {
        if (log.isTraceEnabled())
        {
            log.trace("registerClass(" + clazz.getName() + ")");
        }

        FacesComponent comp = (FacesComponent) clazz
                .getAnnotation(FacesComponent.class);
        if (comp != null)
        {
            if (log.isTraceEnabled())
            {
                log.trace("addComponent(" + comp.value() + ","
                        + clazz.getName() + ")");
            }
            
            //If there is a previous entry on Application Configuration Resources,
            //the entry there takes precedence
            if (dispenser.getComponentClass(comp.value()) == null)
            {
                application.addComponent(comp.value(), clazz.getName());                
            }
        }
        
        FacesConverter conv = (FacesConverter) clazz
                .getAnnotation(FacesConverter.class);
        if (conv != null)
        {
            if (log.isTraceEnabled())
            {
                log.trace("addConverter(" + conv.value() + ","
                        + clazz.getName() + ")");
            }
            //If there is a previous entry on Application Configuration Resources,
            //the entry there takes precedence
            if (!Object.class.equals(conv.forClass()))
            {
                application.addConverter(conv.forClass(), clazz.getName());
            }
            if (dispenser.getConverterClassById(conv.value()) == null &&
                    conv.value() != null && !"".equals(conv.value()))
            {
                application.addConverter(conv.value(), clazz.getName());
            }
        }
        
        FacesValidator val = (FacesValidator) clazz
        .getAnnotation(FacesValidator.class);
        if (val != null)
        {
            if (log.isTraceEnabled())
            {
                log.trace("addValidator(" + val.value() + "," + clazz.getName()
                        + ")");
            }
            //If there is a previous entry on Application Configuration Resources,
            //the entry there takes precedence
            if (dispenser.getValidatorClass(val.value()) == null)
            {
                application.addValidator(val.value(), clazz.getName());
                if (val.isDefault())
                {
                    application.addDefaultValidatorId(val.value());
                }
            }
        }

        FacesRenderer rend = (FacesRenderer) clazz
                .getAnnotation(FacesRenderer.class);
        if (rend != null)
        {
            String renderKitId = rend.renderKitId();
            if (renderKitId == null)
            {
                renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }
            if (log.isTraceEnabled())
            {
                log.trace("addRenderer(" + renderKitId + ", "
                        + rend.componentFamily() + ", " + rend.rendererType()
                        + ", " + clazz.getName() + ")");
            }
            try
            {
                RenderKit rk = renderKitFactory().getRenderKit(null,
                        renderKitId);
                if (rk == null)
                {
                    if (log.isErrorEnabled())
                    {
                        log.error("RenderKit "+renderKitId+" not found when adding @FacesRenderer annotation");
                    }
                    throw new FacesException("RenderKit "+renderKitId+" not found when adding @FacesRenderer annotation");
                }
                rk.addRenderer(rend.componentFamily(), rend.rendererType(),
                        (Renderer) clazz.newInstance());
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
        }
        
        javax.faces.bean.ManagedBean bean = 
            (javax.faces.bean.ManagedBean) clazz.getAnnotation(javax.faces.bean.ManagedBean.class);
        
        if (bean != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Class '" + clazz.getName() + "' has an @ManagedBean annotation");
            }
            RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);
            org.apache.myfaces.config.impl.digester.elements.ManagedBean mbc =
                new org.apache.myfaces.config.impl.digester.elements.ManagedBean();
            mbc.setName(bean.name());
            mbc.setBeanClass(clazz.getName());
            
            ApplicationScoped appScoped = (ApplicationScoped) clazz.getAnnotation(ApplicationScoped.class);
            if (appScoped != null)
            {
                mbc.setScope("application");
            }            
            NoneScoped noneScoped = (NoneScoped) clazz.getAnnotation(NoneScoped.class);
            if (noneScoped != null)
            {
                mbc.setScope("none");
            }            
            RequestScoped requestScoped = (RequestScoped) clazz.getAnnotation(RequestScoped.class);
            if (requestScoped != null)
            {
                mbc.setScope("request");
            }            
            SessionScoped sessionScoped = (SessionScoped) clazz.getAnnotation(SessionScoped.class);
            if (sessionScoped != null)
            {
                mbc.setScope("session");
            }            
            ViewScoped viewScoped = (ViewScoped) clazz.getAnnotation(ViewScoped.class);
            if (viewScoped != null)
            {
                mbc.setScope("view");
            }            
            CustomScoped customScoped = (CustomScoped) clazz.getAnnotation(CustomScoped.class);
            if (customScoped != null)
            {
                mbc.setScope(customScoped.value());
            }
            Field[] fields = fields(clazz);
            for (Field field : fields)
            {
                if (log.isTraceEnabled())
                {
                    log.trace("  Scanning field '" + field.getName() + "'");
                }
                javax.faces.bean.ManagedProperty property = (javax.faces.bean.ManagedProperty) field
                        .getAnnotation(javax.faces.bean.ManagedProperty.class);
                if (property != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("  Field '" + field.getName()
                                + "' has a @ManagedProperty annotation");
                    }
                    org.apache.myfaces.config.impl.digester.elements.ManagedProperty mpc = 
                        new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
                    String name = property.name();
                    if ((name == null) || "".equals(name))
                    {
                        name = field.getName();
                    }
                    mpc.setPropertyName(name);
                    mpc.setPropertyClass(field.getType().getName()); // FIXME - primitives, arrays, etc.
                    mpc.setValue(property.value());
                    mbc.addProperty(mpc);
                    continue;
                }
            }
            runtimeConfig.addManagedBean(mbc.getManagedBeanName(), mbc);
        }
        
        NamedEvent namedEvent = (NamedEvent) clazz.getAnnotation (NamedEvent.class);
        
        if (namedEvent != null) {
            // Can only apply @NamedEvent to ComponentSystemEvent subclasses.
            
            if (!ComponentSystemEvent.class.isAssignableFrom (clazz)) {
                // Just log this.  We'll catch it later in the runtime.
                
                if (log.isWarnEnabled()) {
                    log.warn (clazz.getName() + " is annotated with @javax.faces.event.NamedEvent, but does " +
                        "not extend javax.faces.event.ComponentSystemEvent");
                }
                
                return;
            }
            
            // Have to register @NamedEvent annotations with the NamedEventManager class since
            // we need to get access to this info later and can't from the dispenser (it's not a
            // singleton).
            
            NamedEventManager.getInstance().addNamedEvent (namedEvent.shortName(),
                (Class<? extends ComponentSystemEvent>) clazz);
        }
        
        // TODO: All annotations scanned at startup must be configured here!
        //FacesBehaviorRenderer
    }
    
    /**
     * <p>Return an array of all <code>Field</code>s reflecting declared
     * fields in this class, or in any superclass other than
     * <code>java.lang.Object</code>.</p>
     *
     * @param clazz Class to be analyzed
     */
    private Field[] fields(Class clazz) {

        Map<String,Field> fields = new HashMap<String,Field>();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
        return (Field[]) fields.values().toArray(new Field[fields.size()]);

    }

    /**
     * <p>The render kit factory for this application.</p>
     */
    private RenderKitFactory rkFactory = null;

    /**
     * <p>Return the <code>RenderKitFactory</code> for this application.</p>
     */
    private RenderKitFactory renderKitFactory()
    {

        if (rkFactory == null)
        {
            rkFactory = (RenderKitFactory) FactoryFinder
                    .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return rkFactory;

    }

    private ClassLoader getClassLoader()
    {
        ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null)
        {
            loader = this.getClass().getClassLoader();
        }
        return loader;
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
}
