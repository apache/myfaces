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
package org.apache.myfaces.util.zip;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 * 
 * Revision 1.7 2004/04/09 21:13:10 Sylvain Vieujot
 * Replace oreilly's Base64 encoder and decoder with Jakarta Commons Codec  
 */
public class ZipUtils
{
    public static final String ZIP_CHARSET = "ISO-8859-1";


    private ZipUtils() 
    {
        // hide from public access
    }


    /**
     */
    public static String unzipString(String s)
    {
        try
        {
        	Base64 base64Codec = new Base64();
            ByteArrayInputStream decodedStream = new ByteArrayInputStream( base64Codec.decode( s.getBytes(ZIP_CHARSET) ) );
            InputStream unzippedStream = new GZIPInputStream(decodedStream);

            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = unzippedStream.read()) != -1)
            {
                buf.append((char)c);
            }

            unzippedStream.close();
            decodedStream.close();

            return buf.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     */
    public static String zipString(String s)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream zos = new GZIPOutputStream(baos);
            OutputStreamWriter writer = new OutputStreamWriter(zos, ZIP_CHARSET);

            writer.write(s);

            writer.close();
            zos.close();
            baos.close();

            Base64 base64Codec = new Base64();
            return new String(base64Codec.encode( baos.toByteArray() ), ZIP_CHARSET);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


}

