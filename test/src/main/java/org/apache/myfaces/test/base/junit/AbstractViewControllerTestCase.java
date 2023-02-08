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

package org.apache.myfaces.test.base.junit;

import java.util.Iterator;

import org.junit.jupiter.api.Assertions;

/**
 * <p>Abstract base class for testing <code>ViewController</code>
 * implementations.</p>
 *
 * <p><strong>WARNING</strong> - If you choose to subclass this class, be sure
 * your <code>setUp()</code> and <code>tearDown()</code> methods call
 * <code>super.setUp()</code> and <code>super.tearDown()</code> respectively,
 * and that you implement your own <code>suite()</code> method that exposes
 * the test methods for your test case.</p>
 * 
 * @since 1.0.0
 */
public abstract class AbstractViewControllerTestCase extends
        AbstractJsfTestCase
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this test case.</p>
     */
    public AbstractViewControllerTestCase()
    {
        super();
    }

    // ---------------------------------------------------- Overall Test Methods

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>Test that the specified number of messages have been queued on the
     * <code>FacesContext</code> instance, without regard to matching a
     * particular client identifier.</p>
     *
     * @param expected The expected number of messages
     */
    protected void checkMessageCount(int expected)
    {

        int actual = 0;
        Iterator messages = facesContext.getMessages();
        while (messages.hasNext())
        {
            messages.next();
            actual++;
        }
        Assertions.assertEquals(expected, actual, "Complete message count");

    }

    /**
     * <p>Test that the specified number of messages have been queued on the
     * <code>FacesContext</code> instance, for the specified client id.</p>
     *
     * @param clientId Client identifier of the component for which to
     *  count queued messages
     * @param expected The expected number of messages
     */
    protected void checkMessageCount(String clientId, int expected)
    {

        int actual = 0;
        Iterator messages = facesContext.getMessages(clientId);
        while (messages.hasNext())
        {
            messages.next();
            actual++;
        }
        Assertions.assertSame(expected, actual, "Complete message count");

    }

}
