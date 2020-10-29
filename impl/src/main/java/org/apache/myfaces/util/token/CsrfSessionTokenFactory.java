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

import jakarta.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public abstract class CsrfSessionTokenFactory
{
    /**
     * Set the default length of the random key used for the csrf session token.
     * By default is 16. 
     */
    @JSFWebConfigParam(since="2.2.0", defaultValue="16", group="state")
    public static final String RANDOM_KEY_IN_CSRF_SESSION_TOKEN_LENGTH_PARAM 
            = "org.apache.myfaces.RANDOM_KEY_IN_CSRF_SESSION_TOKEN_LENGTH";
    public static final int RANDOM_KEY_IN_CSRF_SESSION_TOKEN_LENGTH_PARAM_DEFAULT = 16;
    
    public abstract String createToken(FacesContext context);
}
