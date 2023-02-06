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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * <p>Mock implementation of an <code>Enumeration</code> wrapper around
 * an <code>Iterator</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

class MockEnumeration implements Enumeration
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a wrapper instance.</p>
     *
     * @param iterator The <code>Iterator</code> to be wrapped
     */
    public MockEnumeration(Iterator iterator)
    {

        this.iterator = iterator;

    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The <code>Iterator</code> we are wrapping.</p>
     */
    private Iterator iterator;

    // ----------------------------------------------------- Enumeration Methods

    @Override
    public boolean hasMoreElements()
    {

        return iterator.hasNext();

    }

    @Override
    public Object nextElement()
    {

        return iterator.next();

    }

}
