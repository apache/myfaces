/*
 * 
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
 * Various tests for the faces context is rendered
 * util Methods!
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class IsRenderedTest extends AbstractJsfTestCase {

    public IsRenderedTest() {
        super("IsRenderedTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * testcase for isExecute non on the faces context
     * 
     */
    public void testIsExecuteNone() {
        //EXECUTE NONE according to spec only true if:
        //PARTIAL_EXECUTE_PARAM_NAME is present and has the value of NO_PARTIAL_PHASE_CLIENT_IDS.
        //otherwise it is false!

        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, FacesContext.NO_PARTIAL_PHASE_CLIENT_IDS);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);

        assertTrue("Execute none set", context.isExecuteNone());

        requestParamMap = new HashMap<String,String>();
        wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        context = new FacesContextImpl(servletContext, wrapper, response);
        assertFalse("Execute none not set", context.isExecuteNone());

        requestParamMap = new HashMap<String,String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME,"pebkac");
        wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        context = new FacesContextImpl(servletContext, wrapper, response);
        assertFalse("Execute none wrong value", context.isExecuteNone());
    }

    /**
     * testcase for the isRenderedNone testcase
     *
     */
    public void testIsRenderedNone() {

        //RENDER NONE according to spec only true if:
        //PARTIAL_RENDER_PARAM_NAME is present and has the value of NO_PARTIAL_PHASE_CLIENT_IDS.
        //otherwise it is false!

        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_RENDER_PARAM_NAME, FacesContext.NO_PARTIAL_PHASE_CLIENT_IDS);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);

        assertTrue("Render none set", context.isRenderNone());

        requestParamMap = new HashMap<String,String>();
        wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        context = new FacesContextImpl(servletContext, wrapper, response);
        assertFalse("Render none not set", context.isRenderNone());

        requestParamMap = new HashMap<String,String>();
        requestParamMap.put(FacesContext.PARTIAL_RENDER_PARAM_NAME,"pebkac");
        wrapper = new ContextTestRequestWrapper(request, requestParamMap);
        context = new FacesContextImpl(servletContext, wrapper, response);
        assertFalse("Render none wrong value", context.isRenderNone());

    }
}
