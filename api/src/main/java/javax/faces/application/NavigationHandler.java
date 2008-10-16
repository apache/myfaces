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
package javax.faces.application;

import javax.faces.context.FacesContext;

/**
 * The interface for an object which determines what the "next" view to show is,
 * and then causes the ViewHandler to set that view as the current one.
 * <p>
 * The JSF specification defines the behaviour of a default implementation of
 * this interface. An alternate implementation can be used by configuring the
 * appropriate element in the application's faces-config.xml file.
 * <p>
 * See the javadoc for this class in the
 * <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * for more details.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class NavigationHandler
{
    public abstract void handleNavigation(FacesContext context,
                                          String fromAction,
                                          String outcome);
}
