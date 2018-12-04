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
package org.apache.myfaces.view.facelets.util;

import org.apache.myfaces.util.FastWriter;
import org.junit.Assert;
import org.junit.Test;

public class FastWriterTest 
{
    // Test FastWriter.write(String str, int off, int len)
    @Test
    public void testFastWriterWriteString() throws Exception
    {
        String sampleStringToWrite = "Test String to write";
        
        FastWriter fw = new FastWriter();
        fw.write(sampleStringToWrite, 0, sampleStringToWrite.length());

        // fw.toString() should be: Test String to write
        Assert.assertEquals(fw.toString(), sampleStringToWrite);
    }
    
    @Test
    public void testFastWriterWriteStringOffSet() throws Exception
    {
        int offSet = 5;
        String sampleStringToWrite = "Test String to write";
        
        FastWriter fw = new FastWriter();
        fw.write(sampleStringToWrite, offSet, sampleStringToWrite.length() - offSet);

        // fw.toString() should be: String to write
        Assert.assertEquals(fw.toString(), sampleStringToWrite.substring(offSet));
    }
    
    @Test
    public void testFastWriterWriteStringLength() throws Exception
    {
       String sampleStringToWrite = "Test String to write";
       int length = sampleStringToWrite.substring(0, sampleStringToWrite.indexOf(' ')).length();
       
       FastWriter fw = new FastWriter();
       fw.write(sampleStringToWrite, 0, length);

       // fw.toString() should be: Test
       Assert.assertEquals(fw.toString(), sampleStringToWrite.substring(0,length));
    }
}