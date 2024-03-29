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
package org.apache.myfaces.view.facelets.impl;

import java.io.IOException;
import jakarta.faces.component.UIOutput;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
    Basic test that verifies programmmatic facelets work. See spec issue: https://github.com/jakartaee/faces/issues/1581
    Facelet code is generated by ProgrammaticViewBean.
 */
public class ProgrammaticViewTest extends org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase   //extends FaceletTestCase
{
    @Test
    public void testProgrammaticView() throws IOException
    {
        startViewRequest("/test.xhtml");
        processLifecycleExecuteAndRender();

        //Grab by ID
        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("messageId");
        String result1 = out.getValue().toString();

        Assertions.assertTrue(result1.contains("Success!"));

        //Grab by ID
        UIOutput cdiOut = (UIOutput) facesContext.getViewRoot().findComponent("cdiId");
        String result2 = cdiOut.getValue().toString();

        Assertions.assertTrue(result2.contains("CDI Bean Name: Test1581Bean"));
    }


}
