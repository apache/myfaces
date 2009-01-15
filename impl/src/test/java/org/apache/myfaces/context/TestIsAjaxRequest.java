/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.myfaces.context;

import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.shale.test.base.AbstractJsfTestCase;

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

        requestParameterMap = new HashMap<String, String>();
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParameterMap);
        context = new FacesContextImpl(servletContext, wrapper, response);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        requestParameterMap.clear();
    }

    public void testNoEntry() {

        assertFalse("no ajax request found", context.getPartialViewContext().isAjaxRequest());
    }

    public void testEntry() {
        requestParameterMap.put("javax.faces.partial.ajax", "yess");

        assertTrue("no ajax request found", context.getPartialViewContext().isAjaxRequest());
    }



}
