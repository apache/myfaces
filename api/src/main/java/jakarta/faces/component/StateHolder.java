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
package jakarta.faces.component;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 * <p>
 * Since Faces 5.0: an object attached to a component, such as a converter, validator, listener or behavior, which
 * implements only this interface, is saved in every view state with the full result of its {@link #saveState}
 * method, because the runtime cannot tell whether it changed after the build of the view. To save only its changes
 * after the build of the view, it should implement {@link PartialStateHolder} instead.
 * </p>
 */
public interface StateHolder
{
    public java.lang.Object saveState(jakarta.faces.context.FacesContext context);

    public void restoreState(jakarta.faces.context.FacesContext context,
                             java.lang.Object state);

    public boolean isTransient();

    public void setTransient(boolean newTransientValue);
}
