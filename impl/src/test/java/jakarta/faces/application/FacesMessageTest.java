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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FacesMessageTest
{

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage()'
     */
    @Test
    void facesMessage()
    {
        FacesMessage msg = new FacesMessage();
        assertEquals(Severity.INFO, msg.getSeverity());
        assertNull(msg.getSummary());
        assertNull(msg.getDetail());
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(String)'
     */
    @Test
    void facesMessageString()
    {
        String summary = "summary";
        FacesMessage msg = new FacesMessage(summary);
        assertEquals(Severity.INFO, msg.getSeverity());
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(String, String)'
     */
    @Test
    void facesMessageStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);
        assertEquals(Severity.INFO, msg.getSeverity());
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    @Test
    void facesMessageSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(Severity.ERROR, summary, detail);
        assertEquals(Severity.ERROR, msg.getSeverity());
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    @Test
    void facesMessageNullSeverityStringString()
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
     * Test method for 'jakarta.faces.application.FacesMessage.setSeverity(Severity)'
     */
    @Test
    void setSeverity()
    {
        FacesMessage msg = new FacesMessage();
        assertEquals(Severity.INFO, msg.getSeverity());
        msg.setSeverity(Severity.FATAL);
        assertEquals(Severity.FATAL, msg.getSeverity());
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setSeverity(Severity)'
     */
    @Test
    void setNullSeverity()
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
     * Test method for 'jakarta.faces.application.FacesMessage.setSummary(String)'
     */
    @Test
    void setSummary()
    {
        FacesMessage msg = new FacesMessage();
        String summary = "summary";
        msg.setSummary(summary);
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), summary);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.setDetail(String)'
     */
    @Test
    void setDetail()
    {
        FacesMessage msg = new FacesMessage();
        String detail = "detail";
        msg.setDetail(detail);
        assertNull(msg.getSummary());
        assertEquals(msg.getDetail(), detail);
    }

    @Test
    void severityOrdering()
    {
        // make sure they are ordered correctly from least to worst
        assertTrue(0 > Severity.INFO.compareTo(Severity.WARN));
        assertTrue(0 > Severity.WARN.compareTo(Severity.ERROR));
        assertTrue(0 > Severity.ERROR.compareTo(Severity.FATAL));
        // make sure they are ordered correctly from worts to least
        assertTrue(0 < Severity.FATAL.compareTo(Severity.ERROR));
        assertTrue(0 < Severity.ERROR.compareTo(Severity.WARN));
        assertTrue(0 < Severity.WARN.compareTo(Severity.INFO));
    }

    @Test
    void severityEquality()
    {
        // make sure they all respond as equals when they should
        assertEquals(0, Severity.INFO.compareTo(Severity.INFO));
        assertEquals(0, Severity.WARN.compareTo(Severity.WARN));
        assertEquals(0, Severity.ERROR.compareTo(Severity.ERROR));
        assertEquals(0, Severity.FATAL.compareTo(Severity.FATAL));
    }

    @Test
    void severityValues()
    {
        // Faces spec requires this list to be sorted by ordinal
        for (int i = 0, sz = FacesMessage.VALUES.size(); i < sz; i++)
        {
            FacesMessage.Severity severity = (Severity) FacesMessage.VALUES.get(i);
            assertEquals(i, severity.ordinal());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void severityValuesMap()
    {
        Map<String, FacesMessage.Severity> severityMap = (Map<String, FacesMessage.Severity>) FacesMessage.VALUES_MAP;

        for (Map.Entry<String, FacesMessage.Severity> e : severityMap.entrySet())
        {
            assertEquals(e.getKey(), e.getValue().toString());
        }
    }

    @Test
    void serialization() throws Exception
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);

        // check if properties are set correctly
        assertEquals(Severity.INFO, msg.getSeverity());
        assertEquals(msg.getSummary(), summary);
        assertEquals(msg.getDetail(), detail);
        assertEquals(false, msg.isRendered());

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
