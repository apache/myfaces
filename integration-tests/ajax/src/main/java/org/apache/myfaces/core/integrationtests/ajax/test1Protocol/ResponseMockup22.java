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

package org.apache.myfaces.core.integrationtests.ajax.test1Protocol;

import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Changes;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Update;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *          JSF 2.2 response mockup which simulates the changed viewstate behavior
 */
public class ResponseMockup22 extends ResponseMockup
{

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String op = (String) request.getParameter("op");


        PartialResponse root = new PartialResponse();
        if (op.equals("newviewstate"))
        {
            Changes changes = new Changes(root);
            changes.addChild(new Update(changes, "jakarta.faces.ViewState",
                    "update1"));
            root.addElement(changes);
            out.println(root.toString());

        }
        else if (op.equals("newviewstate2"))
        {
            Changes changes = new Changes(root);
            changes.addChild(new Update(changes, "form2:jakarta.faces.ViewState",
                    "update2"));
            root.addElement(changes);
            out.println(root.toString());

        }
        else
        {
            super.processRequest(request, response);
        }

    }
}
