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
package org.apache.myfaces.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.myfaces.util.lang.FastByteArrayInputStream;

public abstract class SerialFactory
{
    public byte[] toByteArray(Object object) throws IOException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            try (ObjectOutputStream oos = getObjectOutputStream(baos))
            {
                oos.writeObject(object);
                oos.flush();

                return baos.toByteArray();
            }
        }
    }
    
    public Object toObject(byte[] bytes) throws IOException, PrivilegedActionException, ClassNotFoundException
    {
        try (InputStream bias = new FastByteArrayInputStream(bytes))
        {
            try (ObjectInputStream ois = getObjectInputStream(bias))
            {
                if (System.getSecurityManager() != null)
                {
                    return AccessController.doPrivileged((PrivilegedExceptionAction) ois::readObject);
                }

                return ois.readObject();
            }
        }
    }

    protected abstract ObjectOutputStream getObjectOutputStream(OutputStream outputStream) throws IOException;

    protected abstract ObjectInputStream getObjectInputStream(InputStream inputStream) throws IOException;
}
