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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * <p>Mock implementation of the default <code>ActionListener</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockActionListener implements ActionListener
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockActionListener()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    // -------------------------------------------------- ActionListener Methods

    /**
     * <p>Process the specified <code>ActionEvent</code>.</p>
     *
     * @param event Event to be processed
     *
     * @exception AbortProcessingException if further event firing
     *  should be skipped
     */
    public void processAction(ActionEvent event)
            throws AbortProcessingException
    {
        // FIXME - provide default implementation
    }

}
