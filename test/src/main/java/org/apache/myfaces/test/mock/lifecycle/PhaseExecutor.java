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

package org.apache.myfaces.test.mock.lifecycle;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

/**
 * Implements the PhaseExecutor for a lifecycle
 *
 * @author Nikolay Petrov
 * @since 1.0.0
 *
 */
interface PhaseExecutor
{

    /**
     * Executes a phase of the JavaServer(tm) Faces lifecycle, like UpdateModelValues.
     * The <code>execute</code> method is called by the lifecylce implementation's private
     * <code>executePhase</code>.
     * @param facesContext The <code>FacesContext</code> for the current request we are processing 
     * @return <code>true</code> if execution should be stopped
     */
    boolean execute(FacesContext facesContext);

    /**
     * Returns the <code>PhaseId</code> for which the implemented executor is invoked 
     * @return
     */
    PhaseId getPhase();
}