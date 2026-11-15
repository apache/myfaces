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

package org.apache.myfaces.view.facelets.tag.faces.core;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.event.AjaxBehaviorEvent;

public class AjaxListenerBean
{
    private final List<String> invocations = new ArrayList<>();

    public void oneArg(AjaxBehaviorEvent event)
    {
        invocations.add("oneArg(event)");
    }

    public void zeroArg()
    {
        invocations.add("zeroArg()");
    }

    public void both(AjaxBehaviorEvent event)
    {
        invocations.add("both(event)");
    }

    public void both()
    {
        invocations.add("both()");
    }

    public List<String> getInvocations()
    {
        return invocations;
    }
}
