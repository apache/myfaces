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

import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * <p>Mock implementation of <code>PrintWriter</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockPrintWriter extends PrintWriter
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Return a default instance.</p>
     *
     * @param writer Temporary buffer storage for us to use
     */
    public MockPrintWriter(CharArrayWriter writer)
    {
        super(writer);
        this.caw = writer;
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Return the content that has been written to this writer.</p>
     */
    public char[] content()
    {
        return caw.toCharArray();
    }

    /**
     * <p>Reset this output stream so that it appears no content has been
     * written.</p>
     */
    public void reset()
    {
        caw.reset();
    }

    /**
     * <p>Return the number of characters that have been written to this writer.</p>
     */
    public int size()
    {
        return caw.size();
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The writer we will use for buffering.</p>
     */
    private CharArrayWriter caw = null;

    // ----------------------------------------------------- PrintWriter Methods

}
