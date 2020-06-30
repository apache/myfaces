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

import org.apache.myfaces.application.viewstate.StateUtils;

/**
 * This TestCase uses the the default algorithm/mode/padding of
 * StateUtils.
 */

public class StateUtilsDefaultTest extends AbstractStateUtilsTest
{
    public void setUp() throws Exception
    {
        super.setUp();

        servletContext.addInitParameter(StateUtils.INIT_SECRET, BASE64_KEY_SIZE_8);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM, StateUtils.DEFAULT_ALGORITHM);
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_PARAM, StateUtils.DEFAULT_ALGORITHM_PARAMS);
        servletContext.addInitParameter(StateUtils.INIT_SECRET_KEY_CACHE, "false");
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, AbstractStateUtilsTest.BASE64_KEY_SIZE_8);
        StateUtils.initSecret(servletContext);// should do nothing

    }
}
