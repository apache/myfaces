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

package javax.faces.application;

import junit.framework.TestCase;

import javax.faces.application.FacesMessage.Severity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class FacesMessageTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FacesMessageTest.class);
    }

    public FacesMessageTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.FacesMessage()'
     */
    public void testFacesMessage()
    {
        FacesMessage msg = new FacesMessage();
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        assertNull(msg.getSummary());
        assertNull(msg.getDetail());
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.FacesMessage(String)'
     */
    public void testFacesMessageString()
    {
        String summary = "summary";
        FacesMessage msg = new FacesMessage(summary);
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.FacesMessage(String, String)'
     */
    public void testFacesMessageStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    public void testFacesMessageSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_ERROR);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    public void testFacesMessageNullSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        try
        {
            new FacesMessage(null, summary, detail);
            fail("Should have thrown an exception");
        }
        catch (NullPointerException e)
        {
        }
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.setSeverity(Severity)'
     */
    public void testSetSeverity()
    {
        FacesMessage msg = new FacesMessage();
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        msg.setSeverity(FacesMessage.SEVERITY_FATAL);
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_FATAL);
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.setSeverity(Severity)'
     */
    public void testSetNullSeverity()
    {
        FacesMessage msg = new FacesMessage();
        try
        {
            msg.setSeverity(null);
            fail("Should have thrown an exception");
        }
        catch (NullPointerException e)
        {
        }
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.setSummary(String)'
     */
    public void testSetSummary()
    {
        FacesMessage msg = new FacesMessage();
        String summary = "summary";
        msg.setSummary(summary);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'javax.faces.application.FacesMessage.setDetail(String)'
     */
    public void testSetDetail()
    {
        FacesMessage msg = new FacesMessage();
        String detail = "detail";
        msg.setDetail(detail);
        assertEquals(msg.getSummary(), null);
        assertEquals(msg.getDetail(), detail);
    }

    public void testSeverityOrdering()
    {
        // make sure they are ordered correctly from least to worst
        assertTrue(0 > FacesMessage.SEVERITY_INFO.compareTo(FacesMessage.SEVERITY_WARN));
        assertTrue(0 > FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_ERROR));
        assertTrue(0 > FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_FATAL));
        // make sure they are ordered correctly from worts to least
        assertTrue(0 < FacesMessage.SEVERITY_FATAL.compareTo(FacesMessage.SEVERITY_ERROR));
        assertTrue(0 < FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_WARN));
        assertTrue(0 < FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_INFO));
    }

    public void testSeverityEquality()
    {
        // make sure they all respond as equals when they should
        assertEquals(0, FacesMessage.SEVERITY_INFO.compareTo(FacesMessage.SEVERITY_INFO));
        assertEquals(0, FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_WARN));
        assertEquals(0, FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_ERROR));
        assertEquals(0, FacesMessage.SEVERITY_FATAL.compareTo(FacesMessage.SEVERITY_FATAL));
    }

    public void testSeverityValues()
    {
        // JSF spec requires this list to be sorted by ordinal
        for (int i = 0, sz = FacesMessage.VALUES.size(); i < sz; i++)
        {
            FacesMessage.Severity severity = (Severity) FacesMessage.VALUES.get(i);
            assertEquals(i, severity.getOrdinal());
        }
    }

    @SuppressWarnings("unchecked")
    public void testSeverityValuesMap()
    {
        Map<String, FacesMessage.Severity> severityMap = (Map<String, FacesMessage.Severity>) FacesMessage.VALUES_MAP;

        for (Map.Entry<String, FacesMessage.Severity> e : severityMap.entrySet())
        {
            assertEquals(e.getKey(), e.getValue().toString());
        }
    }

    public void testSerialization() throws Exception
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);

        // check if properties are set correctly
        assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
        assertEquals(msg.isRendered(), false);

        // serialize instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(msg);
        out.close();

        // deserialize instance
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FacesMessage deserialized = (FacesMessage) in.readObject();

        // FacesMessage properties must equal!
        assertSame(msg.getSeverity(), deserialized.getSeverity());
        assertEquals(msg.getSummary(), deserialized.getSummary());
        assertEquals(msg.getDetail(), deserialized.getDetail());
        assertEquals(msg.isRendered(), deserialized.isRendered());
    }

}
