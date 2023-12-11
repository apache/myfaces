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
package org.apache.myfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.apache.myfaces.util.lang.StringUtils;

public class ResourceLoaderUtils
{
    private static final DateTimeFormatter HTTP_RESPONSE_DATE_HEADER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(ZoneId.of("GMT"));

    private static final DateTimeFormatter[] HTTP_REQUEST_DATE_HEADER = {
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(ZoneId.of("GMT")),
            DateTimeFormatter.ofPattern("EEE MMMM d HH:mm:ss yyyy", Locale.US).withZone(ZoneId.of("GMT")) };
 
    public static String formatDateHeader(long value)
    {
        Instant instant = Instant.ofEpochMilli(value);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, HTTP_RESPONSE_DATE_HEADER.getZone());
        return HTTP_RESPONSE_DATE_HEADER.format(zdt);
    }
    
    public static Long parseDateHeader(String value)
    {
        for (DateTimeFormatter formatter : HTTP_REQUEST_DATE_HEADER)
        {
            try
            {
                ZonedDateTime zdt = ZonedDateTime.parse(value, formatter);
                return zdt.toInstant().toEpochMilli();
            }
            catch (DateTimeParseException e)
            {
                // all fine
            }
        }

        return null;
    }

    public static long getResourceLastModified(URL url) throws IOException
    {
        long lastModified;

        InputStream is = null;
        try
        {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection jarUrlConnection)
            { 
                URL jarFileUrl = jarUrlConnection.getJarFileURL(); 
                URLConnection jarFileConnection = jarFileUrl.openConnection();
                is = jarFileConnection.getInputStream();
                lastModified = jarFileConnection.getLastModified(); 
            }
            else
            {
                is = connection.getInputStream();
                lastModified = connection.getLastModified();
            }
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    // Ignored
                }
            }
        }

        return lastModified;
    }

    public static int getDepth(String path)
    {
        int depth = 0;
        String [] paths = StringUtils.splitShortString(path, '/');
        if (paths == null)
        {
            return 0;
        }
        for (String p : paths)
        {
            if (p != null && p.length() > 0)
            {
                depth++;
            }
        }
        return depth;
    }

    public static boolean isDirectory(String path)
    {
        return path.startsWith("/") && path.endsWith("/");
    }
}
