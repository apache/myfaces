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
package org.apache.myfaces.view.facelets.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.faces.context.ExternalContext;

import org.apache.myfaces.config.MetaInfResourceCache;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockServletContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4767: in containers which expose jars through a virtual file system (e.g. JBoss VFS), the
 * META-INF/ URL is neither a JarURLConnection nor a directory on disk, so its entries are read as a zip
 * stream whose entry names are relative to the URL it was opened on - not to a classpath root.
 */
public class ClasspathTest
{
    private static final String JAR = "vfstest:/content/app.war/WEB-INF/lib/custom.jar/";

    @Test
    public void testStreamEntriesRelativeToMetaInf() throws Exception
    {
        // opening META-INF/ itself yields its children (like VFS does for a directory)
        Map<String, byte[]> streams = new HashMap<>();
        streams.put(JAR + "META-INF/", zip("custom.taglib.xml", "custom.faces-config.xml"));

        Classpath.ResourceEntries entries = Classpath.searchResourceEntries(
                new VfsClassLoader(streams, JAR + "META-INF/"), "META-INF/");

        assertContains(entries, JAR + "META-INF/custom.taglib.xml");
        assertContains(entries, JAR + "META-INF/custom.faces-config.xml");
    }

    @Test
    public void testStreamEntriesRelativeToJarRoot() throws Exception
    {
        // META-INF/ cannot be read as a zip, the scan falls back to the enclosing jar
        Map<String, byte[]> streams = new HashMap<>();
        streams.put(JAR, zip("META-INF/custom.taglib.xml", "org/acme/Foo.class"));

        Classpath.ResourceEntries entries = Classpath.searchResourceEntries(
                new VfsClassLoader(streams, JAR + "META-INF/"), "META-INF/");

        assertContains(entries, JAR + "META-INF/custom.taglib.xml");
        Assertions.assertEquals(1, entries.getUrls().size(), entries.getUrls().toString());
    }

    @Test
    public void testFindResourcesViaMetaInfResourceCache() throws Exception
    {
        // the path the taglib / faces-config providers take: filter the cached scan by suffix
        Map<String, byte[]> streams = new HashMap<>();
        streams.put(JAR + "META-INF/", zip("custom.taglib.xml", "custom.faces-config.xml", "MANIFEST.MF"));

        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new VfsClassLoader(streams, JAR + "META-INF/"));
        try
        {
            ExternalContext context = new MockExternalContext(new MockServletContext(), null, null);

            Collection<URL> taglibs = MetaInfResourceCache.findResources(context, ".taglib.xml");
            Assertions.assertEquals(1, taglibs.size(), taglibs.toString());
            Assertions.assertEquals(JAR + "META-INF/custom.taglib.xml", taglibs.iterator().next().toExternalForm());

            Collection<URL> configs = MetaInfResourceCache.findResources(context, ".faces-config.xml");
            Assertions.assertEquals(1, configs.size(), configs.toString());
            Assertions.assertEquals(JAR + "META-INF/custom.faces-config.xml",
                    configs.iterator().next().toExternalForm());
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private static void assertContains(Classpath.ResourceEntries entries, String url)
    {
        Assertions.assertTrue(entries.getUrls().stream().anyMatch(u -> u.toExternalForm().equals(url)),
                url + " not found in " + entries.getUrls() + " / " + entries.getNames());
    }

    private static byte[] zip(String... names) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes))
        {
            for (String name : names)
            {
                zip.putNextEntry(new ZipEntry(name));
                zip.write("<x/>".getBytes());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    /**
     * Exposes a single jar as META-INF/ only (no META-INF/MANIFEST.MF), served through a non-jar
     * protocol whose paths do not exist on disk.
     */
    private static class VfsClassLoader extends ClassLoader
    {
        private final URLStreamHandler handler;
        private final String metaInf;

        VfsClassLoader(Map<String, byte[]> streams, String metaInf)
        {
            super(null);
            this.metaInf = metaInf;
            this.handler = new URLStreamHandler()
            {
                @Override
                protected URLConnection openConnection(URL u)
                {
                    return new URLConnection(u)
                    {
                        @Override
                        public void connect()
                        {
                        }

                        @Override
                        public InputStream getInputStream() throws IOException
                        {
                            byte[] content = streams.get(u.toExternalForm());
                            if (content == null)
                            {
                                throw new IOException("not readable: " + u);
                            }
                            return new ByteArrayInputStream(content);
                        }
                    };
                }
            };
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            if ("META-INF/".equals(name))
            {
                return Collections.enumeration(Collections.singletonList(new URL(null, metaInf, handler)));
            }
            return Collections.emptyEnumeration();
        }
    }
}
