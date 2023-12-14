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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.util.lang.ClassUtils;

/**
 * <p>Utility class with methods that support getting a recursive list of
 * classes starting with a specific package name.</p>
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class PackageInfo
{
    private static final Logger LOG = Logger.getLogger(PackageInfo.class.getName());

    /**
     * <p>Return an array of all classes, visible to our application class loader,
     * in the specified Java package.</p>
     *
     * @param pckgname Package name used to select matching classes
     *
     * @throws ClassNotFoundException
     */
    public static Class[] getClasses(final String pckgname) throws ClassNotFoundException
    {
        List<Class> classes = new ArrayList<>();
        
        Enumeration resources;
        ClassLoader cld;
        String path;
        try
        {
            // convert the package name to a path
            path = pckgname.replace('.', '/');

            cld = ClassUtils.getContextClassLoader();
            if (cld == null)
            {
                throw new ClassNotFoundException("Can't get class loader.");
            }

            // find the entry points to the classpath
            resources = cld.getResources(path);
            if (resources == null || !resources.hasMoreElements())
            {
                throw new ClassNotFoundException("No resource for " + path);
            }
        }
        catch (NullPointerException | IOException e)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package", e);
        }

        // iterate through all resources containing the package in question
        while (resources.hasMoreElements())
        {
            URL resource = (URL) resources.nextElement();
            URLConnection connection = null;
            try
            {
                connection = resource.openConnection();
            }
            catch (IOException e)
            {
                throw new ClassNotFoundException(pckgname + " does not appear to be a valid package", e);
            }

            if (connection instanceof JarURLConnection juc)
            {
                JarFile jarFile = null;
                try
                {
                    jarFile = juc.getJarFile();
                }
                catch (IOException e)
                {
                    throw new ClassNotFoundException(pckgname + " does not appear to be a valid package", e);
                }
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry jarEntry = entries.nextElement();
                    String entryName = jarEntry.getName();
                    if (!entryName.startsWith(path))
                    {
                        continue;
                    }
                    if (!entryName.toLowerCase().endsWith(".class"))
                    {
                        continue;
                    }
                    String className = filenameToClassname(entryName);
                    loadClass(classes, cld, className);
                }
            }
            else
            {
                // iterate trhough all the children starting with the package name
                File file;
                try
                {
                    file = new File(connection.getURL().toURI());
                }
                catch (URISyntaxException e)
                {
                    LOG.log(Level.WARNING, "error loading directory " + connection, e);
                    continue;
                }

                listFilesRecursive(classes, file, cld, pckgname);
            }
        }

        if (classes.size() < 1)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
        }

        Class[] resolvedClasses = new Class[classes.size()];
        classes.toArray(resolvedClasses);
        return resolvedClasses;

    }

    /**
     * <p>Convert a filename to a classname.</p>
     *
     * @param entryName Filename to be converted
     */
    protected static String filenameToClassname(String entryName)
    {
        return entryName.substring(0, entryName.length() - 6).replace('/', '.');
    }

    /**
     * <p>Load the class <code>className</code> using the classloader
     * <code>cld</code>, and add it to the list.</p>
     *
     * @param classes List of matching classes being accumulated
     * @param cld ClassLoader from which to load the specified class
     * @param className Name of the class to be loaded
     */
    protected static void loadClass(List<Class> classes, ClassLoader cld, String className)
    {
        try
        {
            classes.add(cld.loadClass(className));
        }
        catch (NoClassDefFoundError | ClassNotFoundException e)
        {
            LOG.log(Level.WARNING, "error loading class " + className, e);
        }
    }

    /**
     * <p>Traverse a directory structure starting at <code>base</code>, adding
     * matching files to the specified list.</p>
     *
     * @param classes List of matching classes being accumulated
     * @param base Base file from which to recurse
     * @param cld ClassLoader being searched for matching classes
     * @param pckgname Package name used to select matching classes
     */
    protected static void listFilesRecursive(final List<Class> classes,
            final File base, final ClassLoader cld, final String pckgname)
    {
        base.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                if (file.isDirectory())
                {
                    listFilesRecursive(classes, file, cld, pckgname + '.' + file.getName());
                    return false;
                }
                if (!file.getName().toLowerCase().endsWith(".class"))
                {
                    return false;
                }

                String className = filenameToClassname(pckgname + '.' + file.getName());
                loadClass(classes, cld, className);

                return false;
            }
        });
    }
}
