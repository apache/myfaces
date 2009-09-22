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
package javax.faces.event;

import javax.faces.component.UIComponent;

/**
 *
 * @author Jan-Kees van Andel 
 * @since 2.0
 *
 * TODO this class seems deprecated it is no longer in the latest
 * spec (26.06.2009) as it seems, please clear this up
 */
public class InitialStateEvent extends ComponentSystemEvent
{

    /**
     * This event marks that the component is in its initial state
     * happens after the AfterRestore Event
     * 
     * @param component the affected component
     * @throws NullPointerException the component argument is null!
     * FIXME There seems to be an errata in the SPEC here the spec
     * clearly says NPE while the base class throws an illegal argument
     * exception, either we have to wrap the base class in a delegate
     * or the spec is faulty here, I do not wrap for now
     * and file a bug report!
     * 
     *
     */
    public InitialStateEvent(UIComponent component)
    {
        super(component);
    }

    /**
     * Set a new component on the event.
     *
     * @param newComponent The new component.
     */
    public void setComponent(UIComponent newComponent)
    {
        source = newComponent;
    }
}
