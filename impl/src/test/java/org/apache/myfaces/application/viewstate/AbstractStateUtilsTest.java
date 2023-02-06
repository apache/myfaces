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
package org.apache.myfaces.application.viewstate;

/**
 * This TestCase is not meant to be run, it's children are.  
 * Running this TestCase directly will blow up.
 */

import org.apache.myfaces.spi.impl.DefaultSerialFactory;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

import java.io.Serializable;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStateUtilsTest extends AbstractJsfTestCase implements Serializable
{
    protected String sensitiveString;
    private static final String TEST_DATA = "This is the test data.";
    // 76543210
    public static final String BASE64_KEY_SIZE_8 = "NzY1NDMyMTA=";
    // 7654321076543210
    public static final String BASE64_KEY_SIZE_16 = "NzY1NDMyMTA3NjU0MzIxMA==";
    // 012345678901234567890123
    public static final String BASE64_KEY_SIZE_24 = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIz";


    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        sensitiveString = "this is my secret";
        externalContext.getApplicationMap().put(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        sensitiveString = null;
    }

    /**
     * Test for Restore View phase.
     */
    @Test
    public void testConstructionString()
    {
        String constructed = StateUtils.construct(sensitiveString, externalContext);
        Object object = StateUtils.reconstruct(constructed, externalContext);
        Assertions.assertTrue(object instanceof String);
        String string = (String) object;
        Assertions.assertEquals(string, sensitiveString);
    }

    /**
     * Test for Restore View phase.  This method actually runs an instance of
     * StateUtilsTestCase through the construct/reconstruct process.
     */
    @Test
    public void testConstruction()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        Object object = org.apache.myfaces.application.viewstate.StateUtils.reconstruct(constructed, externalContext);
        Assertions.assertTrue(TEST_DATA.equals(object));
    }

    @Test
    public void testSerialization()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        Object object = StateUtils.getAsObject(bytes, externalContext);
        Assertions.assertTrue(TEST_DATA.equals(object));
    }

    @Test
    public void testCryptography()
    {
        byte[] sensitiveBytes = sensitiveString.getBytes();
        byte[] secure = StateUtils.encrypt(sensitiveBytes, externalContext);
        byte[] insecure = StateUtils.decrypt(secure, externalContext);
        secure = StateUtils.encrypt(insecure, externalContext); // * 2
        insecure = StateUtils.decrypt(secure, externalContext);
        Assertions.assertTrue(Arrays.equals(insecure, sensitiveBytes));
    }

    @Test
    public void testCompression()
    {
        int size = 2049;
        byte[] orginalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(orginalBytes);
        Assertions.assertTrue(lessBytes.length < orginalBytes.length);
        byte[] moreBytes = StateUtils.decompress(lessBytes);
        Assertions.assertTrue(moreBytes.length > lessBytes.length);
        Assertions.assertTrue(Arrays.equals(moreBytes, orginalBytes));
    }

    @Test
    public void testEncoding()
    {
        byte[] orginalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(orginalBytes);
        byte[] decoded = StateUtils.decode(encoded);
        Assertions.assertTrue(Arrays.equals(decoded, orginalBytes));
    }

    /**
     * Simulates testConstruction w/ corrupt data.
     */
    @Test
    public void testConstructionNegative()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        constructed = constructed.substring(1);
        try
        {
            Object object = StateUtils.reconstruct(constructed, externalContext);
            Assertions.assertFalse(TEST_DATA.equals(object));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testSerialization w/ corrput data.
     */
    @Test
    public void testSerializationNegative()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        bytes[1] = (byte) 3;
        try
        {
            Object object = StateUtils.getAsObject(bytes, externalContext);
            Assertions.assertFalse(TEST_DATA.equals(object));
        }
        catch (Exception e)
        {
            // do nothing
        }

    }

    /**
     * Simulates testCryptography w/ corrupt data.
     */
    @Test
    public void testCryptographyNegative()
    {
        byte[] sensitiveBytes = sensitiveString.getBytes();
        byte[] secure = StateUtils.encrypt(sensitiveBytes, externalContext);
        
        secure[secure.length-5] = (byte) 1;
        try
        {
            byte[] insecure = StateUtils.decrypt(secure, externalContext);
            Assertions.assertFalse(Arrays.equals(insecure, sensitiveBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testCompression w/ corrupt data.
     */
    @Test
    public void testCompressionNegative()
    {
        int size = 2049;
        byte[] orginalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(orginalBytes);
        lessBytes[1] = (byte) 3;
        try
        {
            byte[] moreBytes = StateUtils.decompress(lessBytes);
            Assertions.assertFalse(Arrays.equals(moreBytes, orginalBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Simulates testEncoding w/ corrupt data.
     */
    @Test
    public void testEncodingNegative()
    {
        byte[] orginalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(orginalBytes);
        encoded[1] = (byte) 9;
        try
        {
            byte[] decoded = StateUtils.decode(encoded);
            Assertions.assertFalse(Arrays.equals(decoded, orginalBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }


}

