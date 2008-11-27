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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.shale.test.base.AbstractJsfTestCase;

/**
 * °
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ExecutePhaseClientIdsTest extends AbstractJsfTestCase {
     public ExecutePhaseClientIdsTest() {
        super("ExecutePhaseClientIdsTest");
    }

    /**
     * Empty String as request param
     * has to result in an empty list
     */
    public void testRequestParams1() {
        String empty = "    \n \t  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, empty);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        assertTrue(context.getExecutePhaseClientIds().isEmpty());
    }

    /**
     * no request param, has to result in an empty list
     */
    public void testRequestParams2() {
        String empty = "";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        assertTrue(context.getExecutePhaseClientIds().isEmpty());
    }

    /**
     * NO_PARTIAL_PHASE_CLIENT_IDS as request param, has to result in an empty list
     */
    public void testRequestParams4() {
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, FacesContext.NO_PARTIAL_PHASE_CLIENT_IDS);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        assertTrue(context.getExecutePhaseClientIds().isEmpty());
    }

    /**
     * list with one element has to result in a list with one element
     */
    public void testRequestParams5() {
        String params = " view1:panel1:_component1  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, params);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        assertTrue("Length must be one",context.getExecutePhaseClientIds().size() == 1);
        assertTrue("Value match",context.getExecutePhaseClientIds().get(0).equals("view1:panel1:_component1"));
    }

    /**
     * test on a full blown list containing various
     * blank chars
     */
    public void testRequestParams6() {
        String params = " view1:panel1:_component1,view1:panel1:_component2 \n , component3, component4  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, params);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        assertTrue("Length must be four",context.getExecutePhaseClientIds().size() == 4);

        assertTrue("Value match",context.getExecutePhaseClientIds().get(0).equals("view1:panel1:_component1"));
        assertTrue("Value match",context.getExecutePhaseClientIds().get(2).equals("component3"));


        assertTrue("Value match",context.getExecutePhaseClientIds().get(3).equals("component4"));
    }

    /**
     * priority the setter has higer priority
     * than the request query
     */
    public void testSetter1() {
        List<String> executePhaseClientIds = new LinkedList<String>();
        executePhaseClientIds.add("component1");
        executePhaseClientIds.add("component2");
        String params = " view1:panel1:_component1,view1:panel1:_component2 \n , component3, component4  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(FacesContext.PARTIAL_EXECUTE_PARAM_NAME, params);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        context.setExecutePhaseClientIds(executePhaseClientIds);
        assertTrue(context.getExecutePhaseClientIds().size() == 2);

    }
}
