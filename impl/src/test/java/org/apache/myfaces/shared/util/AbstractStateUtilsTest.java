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
package org.apache.myfaces.shared.util;

/**
 * This TestCase is not meant to be run, it's children are.  
 * Running this TestCase directly will blow up.
 */

import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.util.DefaultSerialFactory;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

import java.io.Serializable;
import java.util.Arrays;

public abstract class AbstractStateUtilsTest extends AbstractJsfTestCase implements Serializable
{
    public AbstractStateUtilsTest(String name) {
        super(name);
    }

    protected String sensitiveString;
    private static final String TEST_DATA = "This is the test data.";
    // 76543210
    public static final String BASE64_KEY_SIZE_8 = "NzY1NDMyMTA=";
    // 7654321076543210
    public static final String BASE64_KEY_SIZE_16 = "NzY1NDMyMTA3NjU0MzIxMA==";
    // 012345678901234567890123
    public static final String BASE64_KEY_SIZE_24 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIz";


    public void setUp() throws Exception
    {
        super.setUp();
        sensitiveString = "this is my secret";
        externalContext.getApplicationMap().put(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        sensitiveString = null;
    }

    /**
     * Test for Restore View phase.
     */

    public void testConstructionString()
    {
        String constructed = StateUtils.construct(sensitiveString, externalContext);
        Object object = StateUtils.reconstruct(constructed, externalContext);
        assertTrue(object instanceof String);
        String string = (String) object;
        assertEquals(string, sensitiveString);
    }

    /**
     * Test for Restore View phase.  This method actually runs an instance of
     * StateUtilsTestCase through the construct/reconstruct process.
     */

    public void testConstruction()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        Object object = org.apache.myfaces.application.viewstate.StateUtils.reconstruct(constructed, externalContext);
        assertTrue(TEST_DATA.equals(object));
    }

    public void testSerialization()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        Object object = StateUtils.getAsObject(bytes, externalContext);
        assertTrue(TEST_DATA.equals(object));
    }

    public void testCryptography()
    {
        byte[] sensitiveBytes = sensitiveString.getBytes();
        byte[] secure = StateUtils.encrypt(sensitiveBytes, externalContext);
        byte[] insecure = StateUtils.decrypt(secure, externalContext);
        secure = StateUtils.encrypt(insecure, externalContext); // * 2
        insecure = StateUtils.decrypt(secure, externalContext);
        assertTrue(Arrays.equals(insecure, sensitiveBytes));
    }

    public void testCompression()
    {
        int size = 2049;
        byte[] orginalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(orginalBytes);
        assertTrue(lessBytes.length < orginalBytes.length);
        byte[] moreBytes = StateUtils.decompress(lessBytes);
        assertTrue(moreBytes.length > lessBytes.length);
        assertTrue(Arrays.equals(moreBytes, orginalBytes));
    }

    public void testEncoding()
    {
        byte[] orginalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(orginalBytes);
        byte[] decoded = StateUtils.decode(encoded);
        assertTrue(Arrays.equals(decoded, orginalBytes));
    }

    /**
     * Simulates testConstruction w/ corrupt data.
     */

    public void testConstructionNegative()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        constructed = constructed.substring(1, constructed.length());
        try
        {
            Object object = StateUtils.reconstruct(constructed, externalContext);
            assertFalse(TEST_DATA.equals(object));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testSerialization w/ corrput data.
     */

    public void testSerializationNegative()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        bytes[1] = (byte) 3;
        try
        {
            Object object = StateUtils.getAsObject(bytes, externalContext);
            assertFalse(TEST_DATA.equals(object));
        }
        catch (Exception e)
        {
            // do nothing
        }

    }

    /**
     * Simulates testCryptography w/ corrupt data.
     */

    public void testCryptographyNegative()
    {
        byte[] sensitiveBytes = sensitiveString.getBytes();
        byte[] secure = StateUtils.encrypt(sensitiveBytes, externalContext);
        
        secure[secure.length-5] = (byte) 1;
        try
        {
            byte[] insecure = StateUtils.decrypt(secure, externalContext);
            assertFalse(Arrays.equals(insecure, sensitiveBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testCompression w/ corrupt data.
     */

    public void testCompressionNegative()
    {
        int size = 2049;
        byte[] orginalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(orginalBytes);
        lessBytes[1] = (byte) 3;
        try
        {
            byte[] moreBytes = StateUtils.decompress(lessBytes);
            assertFalse(Arrays.equals(moreBytes, orginalBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testEncoding w/ corrupt data.
     */

    public void testEncodingNegative()
    {
        byte[] orginalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(orginalBytes);
        encoded[1] = (byte) 9;
        try
        {
            byte[] decoded = StateUtils.decode(encoded);
            assertFalse(Arrays.equals(decoded, orginalBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }


}

