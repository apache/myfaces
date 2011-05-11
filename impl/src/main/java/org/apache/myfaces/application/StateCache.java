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
package org.apache.myfaces.application;

import javax.faces.context.FacesContext;

/**
 *  
 * 
 * @author Leonardo Uribe
 *
 */
public abstract class StateCache<K, V>
{

    /**
     * Put the state on the cache, to can be restored later.
     * 
     * @param facesContext
     * @param serializedView
     */
    public abstract K saveSerializedView(FacesContext facesContext, V serializedView);
    
    /**
     * 
     * @param facesContext
     * @param viewId The viewId of the view to be restored
     * @param viewState A token usually retrieved from a call to ResponseStateManager.getState that will be
     *                  used to identify or restore the state.
     * @return
     */
    public abstract V restoreSerializedView(FacesContext facesContext, String viewId, K viewState);

    /**
     * 
     * @param facesContext
     * @param state The state that will be used to derive the token returned.
     * @return A token (usually encoded on javax.faces.ViewState input hidden field) that will be passed to 
     *         ResponseStateManager.writeState or ResponseStateManager.getViewState to be 
     *         output to the client.
     */
    //public abstract K encodeSerializedState(FacesContext facesContext, Object serializedView);
}
