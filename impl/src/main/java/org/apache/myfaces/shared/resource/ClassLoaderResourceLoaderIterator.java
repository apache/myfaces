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

package org.apache.myfaces.shared.resource;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.ResourceVisitOption;
import org.apache.myfaces.shared.util.ClassUtils;

/**
 *
 * @author lu4242
 */
public class ClassLoaderResourceLoaderIterator implements Iterator<String>
{
    private Iterator<String> delegate = null;

    public ClassLoaderResourceLoaderIterator(URL url, String basePath, 
            int maxDepth, ResourceVisitOption... options)
    {
        if (url == null)
        {
            // This library does not exists for this
            // ResourceLoader
            delegate = null;
        }
        else
        {
            if (url.getProtocol().equals("file"))
            {
                try
                {
                    File directory = new File(url.toURI());
                    delegate = new FileDepthIterator(directory, basePath, maxDepth, options);
                }
                catch (URISyntaxException e)
                {
                    Logger log = Logger.getLogger(ClassLoaderResourceLoader.class.getName()); 
                    if (log.isLoggable(Level.WARNING))
                    {
                        log.log(Level.WARNING, "url "+url.toString()+" cannot be translated to uri: "
                                +e.getMessage(), e);
                    }
                }
            }
            else if (isJarResourceProtocol(url.getProtocol()))
            {
                url = getClassLoader().getResource(basePath);

                if (url != null)
                {
                    delegate = new JarDepthIterator(url, basePath, maxDepth, options);
                }
            }
        }
    }

    @Override
    public boolean hasNext()
    {
        if (delegate != null)
        {
            return delegate.hasNext();
        }
        return false;
    }

    @Override
    public String next()
    {
        if (delegate != null)
        {
            return delegate.next();
        }
        return null;
    }

    @Override
    public void remove()
    {
        //No op
    }
    
    protected ClassLoader getClassLoader()
    {
        return ClassUtils.getContextClassLoader();
    }
 
    private static class JarDepthIterator implements Iterator<String>
    {
        private URL directory;
        private String basePath;
        private int maxDepth;
        private ResourceVisitOption[] options;
        
        private Deque<String> stack = new LinkedList<String>();
        
        Iterator<String> iterator = null;

        public JarDepthIterator(URL directory, String basePath, int maxDepth, ResourceVisitOption... options)
        {
            this.directory = directory;
            this.basePath = basePath;
            this.maxDepth = maxDepth;
            this.options = options;
            
            if (basePath.endsWith("/"))
            {
                basePath = basePath.substring(0, basePath.length()-1);
            }

            try
            {
                JarURLConnection conn = (JarURLConnection)directory.openConnection();
                // See DIGESTER-29 for related problem
                conn.setUseCaches(false);

                try
                {
                    if (conn.getJarEntry().isDirectory())
                    {
                        // Unfortunately, we have to scan all entry files
                        JarFile file = conn.getJarFile();
                        for (Enumeration<JarEntry> en = file.entries(); en.hasMoreElements();)
                        {
                            JarEntry entry = en.nextElement();
                            String entryName = entry.getName();
                            String path;

                            if (entryName.startsWith(basePath + '/'))
                            {
                                if (entryName.length() == basePath.length() + 1)
                                {
                                    // the same string, just skip it
                                    continue;
                                }
                                
                                path = entryName.substring(basePath.length(), entryName.length());
    
                                if (path.endsWith("/"))
                                {
                                    // Inner Directory
                                    continue;
                                }

                                //TODO: scan listFiles
                                int depth = ResourceLoaderUtils.getDepth(path);
                                if (depth < maxDepth)
                                {
                                    stack.add(path);
                                }
                            }
                        }
                    }
                }
                finally
                {
                    //See TRINIDAD-73
                    //just close the input stream again if
                    //by inspecting the entries the stream
                    //was let open.
                    try
                    {
                        conn.getInputStream().close();
                    }
                    catch (Exception exception)
                    {
                        // Ignored
                    }
                }
            }
            catch (IOException e)
            {
                // Just return null, because library version cannot be
                // resolved.
                Logger log = Logger.getLogger(ClassLoaderResourceLoader.class.getName()); 
                if (log.isLoggable(Level.WARNING))
                {
                    log.log(Level.WARNING, "IOException when scanning for resource in jar file:", e);
                }
            }
            iterator = stack.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        @Override
        public String next()
        {
            return iterator.next();
        }

        @Override
        public void remove()
        {
            //No op
        }
    }
    
    private static class FileDepthIterator implements Iterator<String>
    {
        private File directory;
        private String basePath;
        private int maxDepth;
        private ResourceVisitOption[] options;
        
        private Deque<File> stack = new LinkedList<File>();
        private String basePathName;

        public FileDepthIterator(File directory, String basePath, int maxDepth, ResourceVisitOption... options)
        {
            this.directory = directory;
            this.basePath = basePath;
            this.maxDepth = maxDepth;
            this.options = options;
            
            File[] list = this.directory.listFiles();
            Collections.addAll(stack, list);

            this.basePathName = this.directory.getPath().replace(File.separatorChar, '/');
        }
        
        @Override
        public boolean hasNext()
        {
            if (!stack.isEmpty())
            {
                File file = stack.peek();
                do 
                {
                    if (file.isDirectory())
                    {
                        file = stack.pop();
                        int depth = ResourceLoaderUtils.getDepth(calculatePath(file));
                        if (depth < maxDepth)
                        {
                            File[] list = file.listFiles();
                            stack.addAll(Arrays.asList(list));
                        }
                        if (!stack.isEmpty())
                        {
                            file = stack.peek();
                        }
                        else
                        {
                            file = null;
                        }
                    }
                }
                while (file != null && file.isDirectory() && !stack.isEmpty());
                
                return !stack.isEmpty();
            }
            return false;
        }

        @Override
        public String next()
        {
            if (!stack.isEmpty())
            {
                File file = stack.pop();
                do 
                {
                    if (file.isDirectory())
                    {
                        int depth = ResourceLoaderUtils.getDepth(calculatePath(file));
                        if (depth < maxDepth)
                        {
                            File[] list = file.listFiles();
                            stack.addAll(Arrays.asList(list));
                        }
                        if (!stack.isEmpty())
                        {
                            file = stack.pop();
                        }
                        else
                        {
                            file = null;
                        }
                    }
                }
                while (file != null && file.isDirectory() && !stack.isEmpty());
                if (file != null)
                {
                    // Calculate name based on url, basePath.
                    String path = calculatePath(file);
                    return path;
                }
            }
            return null;
        }
        
        private String calculatePath(File file)
        {
            return (file.getPath()).substring(this.basePathName.length()).replace(File.separatorChar, '/');
        }

        @Override
        public void remove()
        {
            //No op
        }
    }

    /**
     * <p>Determines whether the given URL resource protocol refers to a JAR file. Note that
     * BEA WebLogic and IBM WebSphere don't use the "jar://" protocol for some reason even
     * though you can treat these resources just like normal JAR files, i.e. you can ignore
     * the difference between these protocols after this method has returned.</p>
     *
     * @param protocol the URL resource protocol you want to check
     *
     * @return <code>true</code> if the given URL resource protocol refers to a JAR file,
     *          <code>false</code> otherwise
     */
    private static boolean isJarResourceProtocol(String protocol)
    {
        // Websphere uses the protocol "wsjar://" and Weblogic uses the protocol "zip://".
        return "jar".equals(protocol) || "wsjar".equals(protocol) || "zip".equals(protocol);
    }
}
