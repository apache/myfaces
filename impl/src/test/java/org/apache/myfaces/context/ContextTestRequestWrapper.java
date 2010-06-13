/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.myfaces.context;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Context request wrapper
 * to test various aspects of a
 * of the facesContext which relies on requests!
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ContextTestRequestWrapper extends HttpServletRequestWrapper {
    Map<String, String> _paramDelegate = null;

    /**
     * Handling a normal servlet request with a new parameter map
     * @param _delegate
     * @param newRequestParameters
     */
    public ContextTestRequestWrapper(HttpServletRequest _delegate, Map<String, String> newRequestParameters) {
       super(_delegate);
       _paramDelegate = newRequestParameters;
    }

    @Override
    public String getParameter(String name) {
        return _paramDelegate.get(name);
    }

    @Override
    public Map getParameterMap() {
        return _paramDelegate;
    }

    @Override
    public Enumeration getParameterNames() {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public String[] getParameterValues(String name) {
        throw new UnsupportedOperationException("not implemented");
    }



}
