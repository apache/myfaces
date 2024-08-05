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

package jakarta.faces.application;

import jakarta.faces.application.FacesMessage.Severity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FacesMessageTest
{

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage()'
     */
    @Test
    public void testFacesMessage()
    {
        FacesMessage msg = new FacesMessage();
        Assertions.assertEquals(msg.getSeverity(), Severity.INFO);
        Assertions.assertNull(msg.getSummary());
        Assertions.assertNull(msg.getDetail());
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(String)'
     */
    @Test
    public void testFacesMessageString()
    {
        String summary = "summary";
        FacesMessage msg = new FacesMessage(summary);
        Assertions.assertEquals(msg.getSeverity(), Severity.INFO);
        Assertions.assertEquals(msg.getSummary(), summary);
        Assertions.assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(String, String)'
     */
    @Test
    public void testFacesMessageStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);
        Assertions.assertEquals(msg.getSeverity(), Severity.INFO);
        Assertions.assertEquals(msg.getSummary(), summary);
        Assertions.assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    @Test
    public void testFacesMessageSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(Severity.ERROR, summary, detail);
        Assertions.assertEquals(msg.getSeverity(), Severity.ERROR);
        Assertions.assertEquals(msg.getSummary(), summary);
        Assertions.assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    @Test
    public void testFacesMessageNullSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        try
        {
            new FacesMessage(null, summary, detail);
            Assertions.fail("Should have thrown an exception");
        }
        catch (NullPointerException e)
        {
        }
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setSeverity(Severity)'
     */
    @Test
    public void testSetSeverity()
    {
        FacesMessage msg = new FacesMessage();
        Assertions.assertEquals(msg.getSeverity(), Severity.INFO);
        msg.setSeverity(Severity.FATAL);
        Assertions.assertEquals(msg.getSeverity(), Severity.FATAL);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setSeverity(Severity)'
     */
    @Test
    public void testSetNullSeverity()
    {
        FacesMessage msg = new FacesMessage();
        try
        {
            msg.setSeverity(null);
            Assertions.fail("Should have thrown an exception");
        }
        catch (NullPointerException e)
        {
        }
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setSummary(String)'
     */
    @Test
    public void testSetSummary()
    {
        FacesMessage msg = new FacesMessage();
        String summary = "summary";
        msg.setSummary(summary);
        Assertions.assertEquals(msg.getSummary(), summary);
        Assertions.assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setDetail(String)'
     */
    @Test
    public void testSetDetail()
    {
        FacesMessage msg = new FacesMessage();
        String detail = "detail";
        msg.setDetail(detail);
        Assertions.assertEquals(msg.getSummary(), null);
        Assertions.assertEquals(msg.getDetail(), detail);
    }

    @Test
    public void testSeverityOrdering()
    {
        // make sure they are ordered correctly from least to worst
        Assertions.assertTrue(0 > Severity.INFO.compareTo(Severity.WARN));
        Assertions.assertTrue(0 > Severity.WARN.compareTo(Severity.ERROR));
        Assertions.assertTrue(0 > Severity.ERROR.compareTo(Severity.FATAL));
        // make sure they are ordered correctly from worts to least
        Assertions.assertTrue(0 < Severity.FATAL.compareTo(Severity.ERROR));
        Assertions.assertTrue(0 < Severity.ERROR.compareTo(Severity.WARN));
        Assertions.assertTrue(0 < Severity.WARN.compareTo(Severity.INFO));
    }

    @Test
    public void testSeverityEquality()
    {
        // make sure they all respond as equals when they should
        Assertions.assertEquals(0, Severity.INFO.compareTo(Severity.INFO));
        Assertions.assertEquals(0, Severity.WARN.compareTo(Severity.WARN));
        Assertions.assertEquals(0, Severity.ERROR.compareTo(Severity.ERROR));
        Assertions.assertEquals(0, Severity.FATAL.compareTo(Severity.FATAL));
    }

    @Test
    public void testSeverityValues()
    {
        // Faces spec requires this list to be sorted by ordinal
        for (int i = 0, sz = FacesMessage.VALUES.size(); i < sz; i++)
        {
            FacesMessage.Severity severity = (Severity) FacesMessage.VALUES.get(i);
            Assertions.assertEquals(i, severity.ordinal());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSeverityValuesMap()
    {
        Map<String, FacesMessage.Severity> severityMap = (Map<String, FacesMessage.Severity>) FacesMessage.VALUES_MAP;

        for (Map.Entry<String, FacesMessage.Severity> e : severityMap.entrySet())
        {
            Assertions.assertEquals(e.getKey(), e.getValue().toString());
        }
    }

    @Test
    public void testSerialization() throws Exception
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);

        // check if properties are set correctly
        Assertions.assertEquals(msg.getSeverity(), Severity.INFO);
        Assertions.assertEquals(msg.getSummary(), summary);
        Assertions.assertEquals(msg.getDetail(), detail);
        Assertions.assertEquals(msg.isRendered(), false);

        // serialize instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(msg);
        out.close();

        // deserialize instance
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FacesMessage deserialized = (FacesMessage) in.readObject();

        // FacesMessage properties must equal!
        Assertions.assertSame(msg.getSeverity(), deserialized.getSeverity());
        Assertions.assertEquals(msg.getSummary(), deserialized.getSummary());
        Assertions.assertEquals(msg.getDetail(), deserialized.getDetail());
        Assertions.assertEquals(msg.isRendered(), deserialized.isRendered());
    }

}
