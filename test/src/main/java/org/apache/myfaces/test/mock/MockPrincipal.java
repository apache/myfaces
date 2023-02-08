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

import java.security.Principal;

/**
 * <p>Mock implementation of <code>Principal</code>.</p>
 * 
 * @since 1.0.0
 */
public class MockPrincipal implements Principal
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default Principal instance.</p>
     */
    public MockPrincipal()
    {
        this(null);
    }

    /**
     * <p>Construct a Principal with the specified name.</p>
     *
     * @param name Name for this Principal
     */
    public MockPrincipal(String name)
    {
        this.name = name;
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The name for this Principal intance.</p>
     */
    private String name = null;

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Set the name for this Principal.</p>
     *
     * @param name The new name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    // ------------------------------------------------------- Principal Methods

    @Override
    public String getName()
    {
        return this.name;
    }

}
