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

import org.junit.jupiter.api.BeforeEach;

/**
 * This TestCase uses Blowfish in Electronic CodeBook mode
 * with PKCS5 padding.
 * <p/>
 * <p/>
 * If you are getting a SecurityException complaining about keysize,
 * you most likely need to get the unlimited strength jurisdiction
 * policy files from a place like http://java.sun.com/j2se/1.4.2/download.html .
 * </p>
 */

public class StateUtilsBlowfish_ECBTest extends AbstractStateUtilsTest
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        servletContext.addInitParameter(StateUtils.INIT_SECRET, BASE64_KEY_SIZE_16);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM, "Blowfish");
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_PARAM, "ECB/PKCS5Padding");
        servletContext.addInitParameter(StateUtils.INIT_SECRET_KEY_CACHE, "false");
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, AbstractStateUtilsTest.BASE64_KEY_SIZE_8);
        StateUtils.initSecret(servletContext);// should do nothing

    }

}
