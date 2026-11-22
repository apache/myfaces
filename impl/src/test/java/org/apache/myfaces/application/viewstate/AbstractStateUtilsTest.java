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
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStateUtilsTest extends AbstractFacesTestCase implements Serializable
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
    public void constructionString()
    {
        String constructed = StateUtils.construct(sensitiveString, externalContext);
        Object object = StateUtils.reconstruct(constructed, externalContext);
        assertInstanceOf(String.class, object);
        String string = (String) object;
        assertEquals(string, sensitiveString);
    }

    /**
     * Test for Restore View phase.  This method actually runs an instance of
     * StateUtilsTestCase through the construct/reconstruct process.
     */
    @Test
    public void construction()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        Object object = org.apache.myfaces.application.viewstate.StateUtils.reconstruct(constructed, externalContext);
        assertEquals(TEST_DATA, object);
    }

    @Test
    public void serialization()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        Object object = StateUtils.getAsObject(bytes, externalContext);
        assertEquals(TEST_DATA, object);
    }

    @Test
    public void cryptography()
    {
        byte[] sensitiveBytes = sensitiveString.getBytes();
        byte[] secure = StateUtils.encrypt(sensitiveBytes, externalContext);
        byte[] insecure = StateUtils.decrypt(secure, externalContext);
        secure = StateUtils.encrypt(insecure, externalContext); // * 2
        insecure = StateUtils.decrypt(secure, externalContext);
        assertTrue(Arrays.equals(insecure, sensitiveBytes));
    }

    @Test
    public void compression()
    {
        int size = 2049;
        byte[] originalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(originalBytes);
        assertTrue(lessBytes.length < originalBytes.length);
        byte[] moreBytes = StateUtils.decompress(lessBytes);
        assertTrue(moreBytes.length > lessBytes.length);
        assertTrue(Arrays.equals(moreBytes, originalBytes));
    }

    @Test
    public void encoding()
    {
        byte[] originalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(originalBytes);
        byte[] decoded = StateUtils.decode(encoded);
        assertTrue(Arrays.equals(decoded, originalBytes));
    }

    /**
     * Simulates testConstruction w/ corrupt data.
     */
    @Test
    public void constructionNegative()
    {
        String constructed = StateUtils.construct(TEST_DATA, externalContext);
        constructed = constructed.substring(1);
        try
        {
            Object object = StateUtils.reconstruct(constructed, externalContext);
            assertNotEquals(TEST_DATA, object);
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
    public void serializationNegative()
    {
        byte[] bytes = StateUtils.getAsByteArray(TEST_DATA, externalContext);
        bytes[1] = (byte) 3;
        try
        {
            Object object = StateUtils.getAsObject(bytes, externalContext);
            assertNotEquals(TEST_DATA, object);
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
    public void cryptographyNegative()
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
    @Test
    public void compressionNegative()
    {
        int size = 2049;
        byte[] originalBytes = new byte[size];
        byte[] lessBytes = StateUtils.compress(originalBytes);
        lessBytes[1] = (byte) 3;
        try
        {
            byte[] moreBytes = StateUtils.decompress(lessBytes);
            assertFalse(Arrays.equals(moreBytes, originalBytes));
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
    public void encodingNegative()
    {
        byte[] originalBytes = sensitiveString.getBytes();
        byte[] encoded = StateUtils.encode(originalBytes);
        encoded[1] = (byte) 9;
        try
        {
            byte[] decoded = StateUtils.decode(encoded);
            assertFalse(Arrays.equals(decoded, originalBytes));
        }
        catch (Exception e)
        {
            // do nothing
        }
    }


}

