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

package javax.faces;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FacesExceptionTest
{

    public FacesExceptionTest()
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException()'
     */
    @Test
    public void testFacesException()
    {
        FacesException e = new FacesException();
        Assert.assertNull(e.getCause());
        Assert.assertNull(e.getMessage());
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(Throwable)'
     */
    @Test
    public void testFacesExceptionThrowable()
    {
        Throwable t = new Throwable();
        FacesException fe = new FacesException(t);
        Assert.assertEquals(t, fe.getCause());
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(String)'
     */
    @Test
    public void testFacesExceptionString()
    {
        String m = "Message";
        FacesException e = new FacesException(m);
        Assert.assertEquals(e.getMessage(), m);
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(String, Throwable)'
     */
    @Test
    public void testFacesExceptionStringThrowable()
    {
        String m = "Message";
        Throwable t = new Throwable();
        FacesException fe = new FacesException(m, t);
        Assert.assertEquals(t, fe.getCause());
        Assert.assertEquals(fe.getMessage(), m);
    }

    /*
     * Test method for 'javax.faces.FacesException.getCause()'
     */
    @Test
    public void testGetCause()
    {

    }

}
