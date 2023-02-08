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

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;

import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Various tests for the faces context is rendered
 * util Methods!
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class IsRenderedTest extends AbstractJsfTestCase {

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
     * tests the basic render all mechanism,
     * no render all due to defaults
     * or a renderAll which
     */
    @Test
    public void testRenderAll1() {
        FacesContext context = new FacesContextImpl(servletContext, request, response);
        
        PartialViewContext pprContext = context.getPartialViewContext();

        pprContext.setRenderAll(true);
        Assertions.assertTrue(pprContext.isRenderAll(), "override should trigger no matter which condition we have");

        context = new FacesContextImpl(servletContext, request, response);
        pprContext.setRenderAll(false);
        Assertions.assertFalse(pprContext.isRenderAll(), "override should trigger no matter which condition we have");
    }

   
}
