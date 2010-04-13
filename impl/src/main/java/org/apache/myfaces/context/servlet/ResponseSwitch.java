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
package org.apache.myfaces.context.servlet;

import javax.faces.context.FacesContext;

/**
 * Responses which can be enabled or disabled implement this interface.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public interface ResponseSwitch
{
    /**
     * If the current ResponseSwitch is disabled, Boolean.FALSE will be stored in the
     * FacesContext under this key, otherwise Boolean.TRUE will be stored
     * ATTENTION: this constant is duplicate in UIViewRoot.
     */
    public static final String RESPONSE_SWITCH_ENABLED = "org.apache.myfaces.RESPONSE_SWITCH_ENABLED";

    /**
     * Enables or disables the Response's Writer and OutputStream.
     * @param enabled
     */
    public void setEnabled(FacesContext facesContext, boolean enabled);
    
    public void setEnabled(boolean enabled);

    /**
     * Are the Response's Writer and OutputStream currently enabled?
     * @return
     */
    public boolean isEnabled();
    
    public boolean isEnabled(FacesContext facesContext);
    
}
