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
package org.apache.myfaces.test;

import org.junit.jupiter.api.Assertions;

/**
 * Provides various assert calls which can be used for tests.
 */
public class MyFacesAsserts
{
    protected MyFacesAsserts()
    {
    }

    /**
     * Asserts that the execution of the {@link TestRunner#run()} method will throw the <code>expected</code>
     * exception
     * 
     * @param expected
     *            the expected Exception
     * @param testCase
     *            the testcase to run
     */
    public static void assertException(Class<? extends Throwable> expected, TestRunner testCase)
    {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(testCase);
        try
        {
            testCase.run();
        }
        catch (Throwable e)
        {
            if (expected.isAssignableFrom(e.getClass()))
            {
                return;
            }
        }

        Assertions.fail(expected.getName() + " expected");
    }

}