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
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.ErrorResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Update;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class JSF21Simulation extends HttpServlet
{

    static int timeoutReqs = 0;
    static int queuesizeReqs = 0;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        process(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/xml;charset=UTF-8");

        PrintWriter out = response.getWriter();
        try
        {
            org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse root
                    = new PartialResponse();
            if (request.getParameter("op") != null && request.getParameter("op").equals("timeout"))
            {
                renderTimeout(out, root);
            }
            else if (request.getParameter("op") != null && request.getParameter("op").equals("cleardelay"))
            {
                timeoutReqs = 0;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "delayoutput",
                        "<div id='delayoutput'>Number of requests so far " + timeoutReqs + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            }
            else if (request.getParameter("op") != null && request.getParameter("op").equals("delay"))
            {
                timeoutReqs++;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "delayoutput",
                        "<div id='delayoutput'>Number of requests so far " + timeoutReqs + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            }
            else if (request.getParameter("op") != null && request.getParameter("op").equals("queuesize"))
            {
                queuesizeReqs++;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "queuesizeoutput",
                        "<div id='queuesizeoutput'>Number of requests so far " + queuesizeReqs + "  </div>"));
                root.addElement(changes);
                sleep(300);
                out.println(root.toString());
                out.flush();
            }
            else if (request.getParameter("op") != null && request.getParameter("op").equals("pps"))
            {
                queuesizeReqs++;
                Changes changes = new Changes(root);
                boolean validPPS = request.getParameter("ppsControl") != null
                        && request.getParameter("queuesizecontrol") == null;
                String validPPSString = (validPPS)? "is a valid partial page submit" : "is a full post submit";
                changes.addChild(new Update(changes, "ppsoutput",
                        "<div id='ppsoutput'>" + validPPSString + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            }
            else if (request.getParameter("op") != null && request.getParameter("op").equals("pps2"))
            {
                queuesizeReqs++;
                Changes changes = new Changes(root);
                boolean validPPS = request.getParameter("ppsControl3") != null
                        && request.getParameter("queuesizecontrol") == null;
                String validPPSString = (validPPS)? "is a valid partial page submit" : "is a full post submit";
                changes.addChild(new Update(changes, "ppsoutput2",
                        "<div id='ppsoutput2'>" + validPPSString + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            }




        }
        finally
        {
            out.close();

        }

    }

    private void renderTimeout(PrintWriter out, PartialResponse root)
    {
        sleep(3000);

        root.addElement(new ErrorResponse(root,
                "This error should be displayed only if you run the long running request", "NoTrigger"));
        out.println(root.toString());
        out.flush();

    }

    private void sleep(int len)
    {
        try
        {
            Thread.sleep(len);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
