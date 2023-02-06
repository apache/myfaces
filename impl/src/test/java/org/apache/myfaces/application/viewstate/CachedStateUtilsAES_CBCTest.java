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


package org.apache.myfaces.application.viewstate;

import org.apache.myfaces.application.viewstate.StateUtils;
import org.junit.jupiter.api.BeforeEach;

/**
 * <p>This TestCase uses the Advanced Encryption Standard with
 * Cipher Block Chaining mode and PKCS5 padding.</p>
 * <p/>
 * <p/>
 * If you are getting a SecurityException complaining about keysize,
 * you most likely need to get the unlimited strength jurisdiction
 * policy files from a place like http://java.sun.com/j2se/1.4.2/download.html .
 * </p>
 *
 * @see pom.xml <excludes>
 */

public class CachedStateUtilsAES_CBCTest extends AbstractStateUtilsTest
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        servletContext.addInitParameter(StateUtils.INIT_SECRET, BASE64_KEY_SIZE_16);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM, "AES");
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_PARAM, "CBC/PKCS5Padding");
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_IV, BASE64_KEY_SIZE_16);
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, BASE64_KEY_SIZE_8);
        StateUtils.initSecret(servletContext);
    }

}
