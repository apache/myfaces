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

import junit.framework.TestCase;

import jakarta.faces.application.FacesMessage.Severity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class FacesMessageTest
{

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage()'
     */
    @Test
    public void testFacesMessage()
    {
        FacesMessage msg = new FacesMessage();
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        Assert.assertNull(msg.getSummary());
        Assert.assertNull(msg.getDetail());
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(String)'
     */
    @Test
    public void testFacesMessageString()
    {
        String summary = "summary";
        FacesMessage msg = new FacesMessage(summary);
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        Assert.assertEquals(msg.getSummary(), summary);
        Assert.assertEquals(msg.getDetail(), summary);
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
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        Assert.assertEquals(msg.getSummary(), summary);
        Assert.assertEquals(msg.getDetail(), detail);
    }

    /*
     * Test method for 'jakarta.faces.application.FacesMessage.FacesMessage(Severity, String, String)'
     */
    @Test
    public void testFacesMessageSeverityStringString()
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_ERROR);
        Assert.assertEquals(msg.getSummary(), summary);
        Assert.assertEquals(msg.getDetail(), detail);
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
            Assert.fail("Should have thrown an exception");
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
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        msg.setSeverity(FacesMessage.SEVERITY_FATAL);
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_FATAL);
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
            Assert.fail("Should have thrown an exception");
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
        Assert.assertEquals(msg.getSummary(), summary);
        Assert.assertEquals(msg.getDetail(), summary);
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
        Assert.assertEquals(msg.getSummary(), null);
        Assert.assertEquals(msg.getDetail(), detail);
    }

    @Test
    public void testSeverityOrdering()
    {
        // make sure they are ordered correctly from least to worst
        Assert.assertTrue(0 > FacesMessage.SEVERITY_INFO.compareTo(FacesMessage.SEVERITY_WARN));
        Assert.assertTrue(0 > FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_ERROR));
        Assert.assertTrue(0 > FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_FATAL));
        // make sure they are ordered correctly from worts to least
        Assert.assertTrue(0 < FacesMessage.SEVERITY_FATAL.compareTo(FacesMessage.SEVERITY_ERROR));
        Assert.assertTrue(0 < FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_WARN));
        Assert.assertTrue(0 < FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_INFO));
    }

    @Test
    public void testSeverityEquality()
    {
        // make sure they all respond as equals when they should
        Assert.assertEquals(0, FacesMessage.SEVERITY_INFO.compareTo(FacesMessage.SEVERITY_INFO));
        Assert.assertEquals(0, FacesMessage.SEVERITY_WARN.compareTo(FacesMessage.SEVERITY_WARN));
        Assert.assertEquals(0, FacesMessage.SEVERITY_ERROR.compareTo(FacesMessage.SEVERITY_ERROR));
        Assert.assertEquals(0, FacesMessage.SEVERITY_FATAL.compareTo(FacesMessage.SEVERITY_FATAL));
    }

    @Test
    public void testSeverityValues()
    {
        // Faces spec requires this list to be sorted by ordinal
        for (int i = 0, sz = FacesMessage.VALUES.size(); i < sz; i++)
        {
            FacesMessage.Severity severity = (Severity) FacesMessage.VALUES.get(i);
            Assert.assertEquals(i, severity.getOrdinal());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSeverityValuesMap()
    {
        Map<String, FacesMessage.Severity> severityMap = (Map<String, FacesMessage.Severity>) FacesMessage.VALUES_MAP;

        for (Map.Entry<String, FacesMessage.Severity> e : severityMap.entrySet())
        {
            Assert.assertEquals(e.getKey(), e.getValue().toString());
        }
    }

    @Test
    public void testSerialization() throws Exception
    {
        String summary = "summary";
        String detail = "detail";
        FacesMessage msg = new FacesMessage(summary, detail);

        // check if properties are set correctly
        Assert.assertEquals(msg.getSeverity(), FacesMessage.SEVERITY_INFO);
        Assert.assertEquals(msg.getSummary(), summary);
        Assert.assertEquals(msg.getDetail(), detail);
        Assert.assertEquals(msg.isRendered(), false);

        // serialize instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(msg);
        out.close();

        // deserialize instance
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FacesMessage deserialized = (FacesMessage) in.readObject();

        // FacesMessage properties must equal!
        Assert.assertSame(msg.getSeverity(), deserialized.getSeverity());
        Assert.assertEquals(msg.getSummary(), deserialized.getSummary());
        Assert.assertEquals(msg.getDetail(), deserialized.getDetail());
        Assert.assertEquals(msg.isRendered(), deserialized.isRendered());
    }

}
