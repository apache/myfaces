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

import javax.faces.context.FacesContext;

/**
 *
 */
abstract class KeyFactory<K, V>
{

    /**
     * Generates a unique key per session
     *
     * @param facesContext
     * @return
     */
    public abstract K generateKey(FacesContext facesContext);

    /**
     * Encode a Key into a value that will be used as view state session token
     *
     * @param key
     * @return
     */
    public abstract V encode(K key);

    /**
     * Decode a view state session token into a key
     *
     * @param value
     * @return
     */
    public abstract K decode(V value);
    
}
