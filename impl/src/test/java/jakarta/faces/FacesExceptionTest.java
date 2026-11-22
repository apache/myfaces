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

package jakarta.faces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import  org.junit.jupiter.api.AfterEach;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FacesExceptionTest
{

    public FacesExceptionTest()
    {
    }

    @BeforeEach
    void setUp() throws Exception
    {
    }

    @AfterEach
    void tearDown() throws Exception
    {
    }

    /*
     * Test method for 'jakarta.faces.FacesException.FacesException()'
     */
    @Test
    void facesException()
    {
        FacesException e = new FacesException();
        assertNull(e.getCause());
        assertNull(e.getMessage());
    }

    /*
     * Test method for 'jakarta.faces.FacesException.FacesException(Throwable)'
     */
    @Test
    void facesExceptionThrowable()
    {
        Throwable t = new Throwable();
        FacesException fe = new FacesException(t);
        assertEquals(t, fe.getCause());
    }

    /*
     * Test method for 'jakarta.faces.FacesException.FacesException(String)'
     */
    @Test
    void facesExceptionString()
    {
        String m = "Message";
        FacesException e = new FacesException(m);
        assertEquals(e.getMessage(), m);
    }

    /*
     * Test method for 'jakarta.faces.FacesException.FacesException(String, Throwable)'
     */
    @Test
    void facesExceptionStringThrowable()
    {
        String m = "Message";
        Throwable t = new Throwable();
        FacesException fe = new FacesException(m, t);
        assertEquals(t, fe.getCause());
        assertEquals(fe.getMessage(), m);
    }

    /*
     * Test method for 'jakarta.faces.FacesException.getCause()'
     */
    @Test
    void getCause()
    {

    }

}
