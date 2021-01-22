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

package org.apache.myfaces.test.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.ReadListener;

import jakarta.servlet.ServletInputStream;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: MockServletInputStream.java 804043 2009-08-13 22:08:44Z lu4242 $
 * @since 1.0.0
 */
public class MockServletInputStream extends ServletInputStream
{

    private final InputStream source;

    public MockServletInputStream()
    {
        this.source = new ByteArrayInputStream(new byte[0]);
    }

    public MockServletInputStream(InputStream source)
    {
        this.source = source;
    }

    public int read() throws IOException
    {
        return this.source.read();
    }

    @Override
    public boolean isFinished()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setReadListener(ReadListener rl)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
