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


package org.apache.myfaces.shared.util;

import org.apache.myfaces.application.viewstate.StateUtils;

public class CachedStateUtilsDefaultTest extends AbstractStateUtilsTest
{

    public CachedStateUtilsDefaultTest(String name)
    {
        super(name);
    }
    
    // No longer necessary using junit 4 to run tests
    //public static Test suite() {
    //    return null; // keep this method or maven won't run it
    //}

    public void setUp() throws Exception
    {
        super.setUp();

        servletContext.addInitParameter(StateUtils.INIT_SECRET, BASE64_KEY_SIZE_8);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM, StateUtils.DEFAULT_ALGORITHM);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_PARAM, StateUtils.DEFAULT_ALGORITHM_PARAMS);
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, BASE64_KEY_SIZE_8);
        StateUtils.initSecret(servletContext);

    }

}
