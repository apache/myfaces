/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.faces;

import junit.framework.TestCase;

public class FacesExceptionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(FacesExceptionTest.class);
    }

    public FacesExceptionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException()'
     */
    public void testFacesException() {
        FacesException e = new FacesException();
        assertNull(e.getCause());
        assertNull(e.getMessage());
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(Throwable)'
     */
    public void testFacesExceptionThrowable() {
        Throwable t = new Throwable();
        FacesException fe = new FacesException(t);
        assertEquals(t, fe.getCause());
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(String)'
     */
    public void testFacesExceptionString() {
        String m = "Message";
        FacesException e = new FacesException(m);
        assertEquals(e.getMessage(), m);
    }

    /*
     * Test method for 'javax.faces.FacesException.FacesException(String, Throwable)'
     */
    public void testFacesExceptionStringThrowable() {
        String m = "Message";
        Throwable t = new Throwable();
        FacesException fe = new FacesException(m, t);
        assertEquals(t, fe.getCause());
        assertEquals(fe.getMessage(), m);
    }

    /*
     * Test method for 'javax.faces.FacesException.getCause()'
     */
    public void testGetCause() {

    }

}
