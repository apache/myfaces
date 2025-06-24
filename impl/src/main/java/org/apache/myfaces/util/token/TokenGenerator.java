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
package org.apache.myfaces.util.token;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lu4242
 */
public class TokenGenerator
{
    private final AtomicLong seed;
    
    private static Logger log = Logger.getLogger(TokenGenerator.class.getName());

    // TODO -- make a web parameter or it would be nice 
    // to consolidate RANDOM_KEY_IN_CSRF_SESSION_TOKEN_SECURE_RANDOM_ALGORITM, 
    // and RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITHM
    private String[] supportedAlgorithmsList = {"SHA256DRBG","DRBG","SHA1PRNG"};

    public TokenGenerator()
    {
        seed = new AtomicLong(generateSeed());
    }
    
    private long generateSeed()
    {
        SecureRandom rng = null;
        for(String algorithm :supportedAlgorithmsList)
        {
            if(rng == null) 
            {
                try
                {
                    rng = SecureRandom.getInstance(algorithm);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // ignore -- log will next if rng is null
                }
            }
        }

        if(rng == null)
        {
            log.log(Level.WARNING, Arrays.toString(supportedAlgorithmsList) + " is not supported either." + 
                "Attempting to use default implmenentation.");

            rng = new SecureRandom();
        }

        // use 48 bits for strength and fill them in
        byte[] randomBytes = new byte[6];
        rng.nextBytes(randomBytes);

        // convert to a long
        return new BigInteger(randomBytes).longValue();
    }
    
    /**
     * Get the next token to be assigned to this request
     * 
     * @return
     */
    public String getNextToken()
    {
        // atomically increment the value
        long nextToken = seed.incrementAndGet();

        // convert using base 36 because it is a fast efficient subset of base-64
        return Long.toString(nextToken, 36);
    }

    public AtomicLong getSeed()
    {
        return seed;
    }
}
