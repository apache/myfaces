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

import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

/**
 * Tests the facesContext isAjaxRequest
 * with all its sideconditions
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class TestIsAjaxRequest extends AbstractJsfTestCase {
    Map<String, String> requestParameterMap = null;
    FacesContext context = null;

    public TestIsAjaxRequest() {
        super("TestIsAjaxRequest");
    }

    public void setUp() throws Exception {
        super.setUp();

        //requestParameterMap = new HashMap<String, String>();
        //ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParameterMap);
        FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
            "org.apache.myfaces.context.PartialViewContextFactoryImpl");     
        FactoryFinder.setFactory (FactoryFinder.EXCEPTION_HANDLER_FACTORY,
        "org.apache.myfaces.context.ExceptionHandlerFactoryImpl");
        context = new FacesContextImpl(servletContext, request, response);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        //requestParameterMap.clear();
    }

    public void testNoEntry() {

        assertFalse("no ajax request found", context.getPartialViewContext().isAjaxRequest());
    }

    public void testEntry() {
        request.addHeader("Faces-Request", "partial/ajax");
        request.addParameter("javax.faces.partial.ajax","true");
        assertTrue("no ajax request found", context.getPartialViewContext().isAjaxRequest());
    }

}
