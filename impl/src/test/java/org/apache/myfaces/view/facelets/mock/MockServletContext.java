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
package org.apache.myfaces.view.facelets.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class MockServletContext extends
        org.apache.myfaces.test.mock.MockServletContext
{

    private static Logger log = Logger.getLogger(MockServletContext.class.getName());

    protected final URI base;

    public MockServletContext(URI base)
    {
        this.base = base;
        File f = new File(base);
        if (!f.exists())
        {
            throw new IllegalArgumentException("File: " + base.getPath()
                    + " doesn't exist");
        }
    }

    public Set getResourcePaths(String path)
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            File f = new File(uri);
            if (f.exists() && f.isDirectory())
            {
                File[] c = f.listFiles();
                Set s = new HashSet();
                int start = f.getAbsolutePath().length();
                for (int i = 0; i < c.length; i++)
                {
                    s.add(c[i].getAbsolutePath().substring(start));
                }
                return s;
            }
        }
        return Collections.EMPTY_SET;
    }

    public URL getResource(String path) throws MalformedURLException
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            File f = new File(uri);
            if (f.exists())
            {
                return uri.toURL();
            }
        }
        return null;
    }

    public InputStream getResourceAsStream(String path)
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            try
            {
                File f = new File(uri);
                if (f.exists())
                {
                    return uri.toURL().openStream();
                }
            }
            catch (MalformedURLException e)
            {
                this.log.severe(e.getMessage());
                return null;
            }
            catch (IOException e)
            {
                this.log.severe(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            File f = new File(uri);
            if (f.exists())
            {
                try
                {
                    return new MockRequestDispatcher(uri.toURL());
                }
                catch (MalformedURLException e)
                {
                    this.log.severe(e.getMessage());
                    return null;
                }
            }

        }
        return null;
    }

    public String getRealPath(String path)
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            File f = new File(uri);
            if (f.exists())
            {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    private final URI resolve(String path)
    {
        if (path == null)
        {
            throw new NullPointerException("Path cannot be null");
        }
        if (path.charAt(0) == '/')
        {
            if (path.length() > 1)
            {
                return this.base.resolve(path.substring(1));
            }
            return this.base;
        }
        return null;
    }

}
