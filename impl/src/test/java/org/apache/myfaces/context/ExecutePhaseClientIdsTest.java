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

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;

import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ExecutePhaseClientIdsTest extends AbstractFacesTestCase {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
         super.setUp();
         FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
         "org.apache.myfaces.context.PartialViewContextFactoryImpl");    
         FactoryFinder.setFactory (FactoryFinder.EXCEPTION_HANDLER_FACTORY,
         "org.apache.myfaces.context.ExceptionHandlerFactoryImpl");
     }

    /**
     * Empty String as request param
     * has to result in an empty list
     */
     @Test
    public void testRequestParams1() {
        String empty = "    \n \t  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME, empty);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();

        Assertions.assertTrue(pprContext.getExecuteIds().isEmpty());
    }

    /**
     * no request param, has to result in an empty list
     */
    @Test
    public void testRequestParams2() {
        Map<String, String> requestParamMap = new HashMap<String, String>();
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();

        Assertions.assertTrue(pprContext.getExecuteIds().isEmpty());
    }

    /**
     * NO_PARTIAL_PHASE_CLIENT_IDS as request param, has to result in an empty list
     */
    /*
    public void testRequestParams4() {
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME, 
                            PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();

        Assertions.assertTrue(pprContext.getExecuteIds().isEmpty());
    }*/

    /**
     * list with one element has to result in a list with one element
     */
    @Test
    public void testRequestParams5() {
        String params = " view1:panel1:_component1  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME, params);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();

        Assertions.assertTrue(pprContext.getExecuteIds().size() == 1);
        Assertions.assertTrue(pprContext.getExecuteIds().iterator().next().equals("view1:panel1:_component1"));
    }

    /**
     * test on a full blown list containing various
     * blank chars
     */
    @Test
    public void testRequestParams6() {
        String params = " view1:panel1:_component1 view1:panel1:_component2 \n  component3 component4  ";
        Map<String, String> requestParamMap = new HashMap<String, String>();
        requestParamMap.put(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME, params);
        ContextTestRequestWrapper wrapper = new ContextTestRequestWrapper(request, requestParamMap);

        FacesContext context = new FacesContextImpl(servletContext, wrapper, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();
        
        Assertions.assertTrue(pprContext.getExecuteIds().size() == 4);

        // FIXME: Latest spec uses a Collection so order is not garanteed
//        Assertions.assertTrue("Value match", pprContext.getExecuteIds().get(0).equals("view1:panel1:_component1"));
//        Assertions.assertTrue("Value match", pprContext.getExecuteIds().get(2).equals("component3"));
//
//
//        Assertions.assertTrue("Value match", pprContext.getExecuteIds().get(3).equals("component4"));
    }
}
