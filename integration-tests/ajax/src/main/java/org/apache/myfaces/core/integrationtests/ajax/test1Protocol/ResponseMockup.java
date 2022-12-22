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

import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Attribute;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Attributes;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Changes;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Delete;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.ErrorResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Eval;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Insert;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Insert2;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Update;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.responses.TableResponseMockups;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * A generic jsf simulating response servlet.
 * The reason for this is twofold. Parts of this code
 * and some tests were written even before the faces.js implementation existed in myfaces.
 * The other reason is, we can test conditions which are not yet exposed in the jsf impl
 * but allowed by protocol (insert before and after or delete for instance)
 *
 * In the end some tests will use this servlet some tests will fall back into jsf
 * with our javascript codebase as implementation
 */
public class ResponseMockup extends HttpServlet
{

    public static final String VIEW_DATA = "_viewData_";
    public static final String RESET_STATE = "reset_counters";
    public static final String EVAL_1 = "eval1";
    public static final String UPDATEINSERT_1 = "updateinsert1";
    public static final String UPDATEINSERT_2 = "updateinsert2";
    public static final String DELETE_1 = "delete1";
    public static final String VIEWSTATE = "viewstate";
    public static final String ATTRIBUTES = "attributes";
    public static final String ERRORS_TAG = "errors";
    public static final String ILLEGAL_RESPONSE = "illegalResponse";
    public static final String VIEW_BODY_REPLACE = "body";
    public static final String VIEW_BODY_REPLACE_2 = "body2";
    public static final String VIEW_ROOT_REPLACEMENT_1 = "body3";
    public static final String ILLEGAL_RESPONSE_2 = "illegalResponse";
    public static final String EXECUTE_NONE = "executenone";

    public static final String TABLE_REPLACE_HEAD = "table_replace_head";
    public static final String TABLE_REPLACE_BODY = "table_replace_body";
    public static final String TABLE_INSERT_ROW_HEAD = "table_insert_row_head";
    public static final String TABLE_INSERT_ROW_BODY = "table_insert_row_body";
    public static final String TABLE_INSERT_COLUMN_HEAD = "table_insert_column_head";
    public static final String TABLE_INSERT_COLUMN_BODY = "table_insert_column_body";
    public static final String TABLE_INSERT_FOOTER = "table_insert_footer";
    public static final String TABLE_INSERT_BODY = "table_insert_body";


    private static final String DEFAULT_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<partial-response><changes><update id=\"out1\"><![CDATA[<span id=\"out1\">2</span>]]></update><update id" +
            "=\"jakarta.faces.ViewState\"><![CDATA[j_id1:j_id3]]></update></changes></partial-response>";
    private static final String EMPTY_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<partial-response><changes></changes></partial-response>";


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws jakarta.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        //we simulate viewscoped here
        String origin = request.getParameter("origin");
        origin = (origin != null) ? origin : "";

        ViewData viewData = (ViewData) request.getSession().getAttribute(VIEW_DATA + origin);
        if (viewData == null)
        {
            viewData = resetViewData(request, origin);
        }


        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String op = request.getParameter("op");

        PartialResponse root = new PartialResponse();

        /*
         * field ids needed in form:
         * changesArea
         * deleteable
         * attributeChange
         *
         */
        try
        {

            if (op == null || op.isEmpty())
            {
                defaultResponse(out);
            }
            else if (op.trim().equalsIgnoreCase(RESET_STATE))
            {
                resetInternalState(request, origin, out);
            }
            else if (op.trim().toLowerCase().equals(EVAL_1))
            {
                embeddedJavascript1(out, root);
            }
            else if (op.trim().toLowerCase().equals(UPDATEINSERT_1))
            {
                updateInsert1(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(UPDATEINSERT_2))
            {
                updateInsert2(out, root);

            }
            else if (op.trim().toLowerCase().equals(DELETE_1))
            {
                delete1(out, root);
            }
            else if (op.trim().toLowerCase().equals(VIEWSTATE))
            {
                viewstateHandling(out, root);
            }
            else if (op.trim().toLowerCase().equals(ATTRIBUTES))
            {
                attributeHandling(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(ERRORS_TAG))
            {
                errors(out, root);
            }
            else if (op.trim().equals(ILLEGAL_RESPONSE))
            {
                illegalResponse(out);
            }
            else if (op.trim().toLowerCase().equals(VIEW_BODY_REPLACE))
            {
                //we omit our xml builder for now
                viewBodyWithFullHTMLResponse(out, root);
            }
            else if (op.trim().toLowerCase().equals(VIEW_BODY_REPLACE_2))
            {
                viewBodyWithOnlyBodyData(viewData, out, root);

            }
            else if (op.trim().toLowerCase().equals(VIEW_ROOT_REPLACEMENT_1))
            {
                viewRootReplacement1(out, root);

                //TODO check if still used?
            }
            else if (op.trim().toLowerCase().equals("serversideresponsewriter"))
            {
                DeferredScriptMockup scriptMockup = new DeferredScriptMockup();
                Changes changes = new Changes(root);
                changes.addChild(new Eval(changes, "alert('the output is on the server console');"));
                root.addElement(changes);
                out.println(root.toString());
                // table tests
            }
            else if (op.trim().toLowerCase().equals(ILLEGAL_RESPONSE_2))
            {
                illegalResponse2(out);
            }
            else if (op.trim().toLowerCase().equals(TABLE_REPLACE_HEAD))
            {
                TableResponseMockups.tableReplaceHead(viewData, out, root);

            }
            else if (op.trim().toLowerCase().equals(TABLE_REPLACE_BODY))
            {
                TableResponseMockups.tableReplaceBody(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_ROW_HEAD))
            {
                TableResponseMockups.tableInsertRowHead(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_ROW_BODY))
            {
                TableResponseMockups.tableInsertRowBody(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_COLUMN_HEAD))
            {
                TableResponseMockups.tableInsetColumnHead(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_COLUMN_BODY))
            {
                TableResponseMockups.tableInsertColumnBody(viewData, out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_FOOTER))
            {
                TableResponseMockups.tableInsertFooter(out, root);
            }
            else if (op.trim().toLowerCase().equals(TABLE_INSERT_BODY))
            {
                TableResponseMockups.tableInsertBody(out, root);
            }
            else if (op.trim().toLowerCase().equals(EXECUTE_NONE))
            {
                TableResponseMockups.execteNone(request, out, root);
            }

        }
        finally
        {
            out.close();
        }
    }

    private void illegalResponse2(PrintWriter out)
    {
        out.println("blablabl this is an illegal reponse, you should see an error");
    }

    private void viewRootReplacement1(PrintWriter out, PartialResponse root) throws IOException
    {
        //TODO fix this properly, still references the absolute file position
        File fIn = new File("src/main/webapp/34beta.html.html");
        FileReader fRead = new FileReader(fIn);
        BufferedReader reader = new BufferedReader(fRead);
        String line = null;
        StringBuilder replacement = new StringBuilder();
        do
        {
            line = reader.readLine();
            if (line != null)
            {
                replacement.append(line);
                replacement.append("\n");
            }
        } while (line != null);
        Changes changes = new Changes(root);
        root.addElement(changes);
        changes.addChild(new Update(changes, "jakarta.faces.ViewRoot", replacement.toString()));
        out.println(root.toString());
    }

    private void viewBodyWithOnlyBodyData(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        //we omit our xml builder for now
        StringBuilder replacement = new StringBuilder();

        replacement.append("<body class=\"tundra\"> ");
        replacement.append("    <div id=\"myfaces.logging\"><div id = \"centerDiv\">\n");
        replacement.append("        <h1>Selenium Test for body change done</h1>\n");
        for (int cnt = 0; cnt < 20; cnt++)
        {
            replacement.append("        <div id = \"testResults" + cnt + "\"></div>\n");
        }
        replacement.append("            <h3>Body replacement test successful</h3>\n");
        replacement.append("    </div>");
        replacement.append("</body>");

        Changes changes = new Changes(root);
        root.addElement(changes);
        changes.addChild(new Update(changes, "jakarta.faces.ViewBody", replacement.toString()));
        out.println(root.toString());
    }

    private void viewBodyWithFullHTMLResponse(PrintWriter out, PartialResponse root)
    {
        StringBuilder replacement = new StringBuilder();

        replacement.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                "<head>");
        replacement.append("    <title></title>");
        replacement.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        replacement.append("    <script type=\"text/javascript\" src=\"./myfaces/_impl/core/_Runtime.js\"></script>");

        replacement.append("    <script type=\"text/javascript\" src=\"./myfaces/_impl/_util/_Lang.js\"></script>");
        replacement.append("</head>" +
                "<body class=\"tundra\">  <div id=\"myfaces.logging\">\n" +
                "    </div>" +
                "    <div id = \"centerDiv\">\n" +
                "        <h1>Selenium Test for body change done</h1>\n" +
                "        <div id = \"testResults\">\n" +
                "            <h3>Body replacement test  successful</h3>" +
                "<div id='scriptreceiver'></div>\n" +
                "        " +
                "   <script type='text/javascript'>var b = true && true; document.getElementById" +
                "('scriptreceiver').innerHTML=" +
                "'hello from embedded script & in the body'; " +
                "</script>            </div>" +
                "    </div>" +
                "</body>" +
                "</html>");

        Changes changes = new Changes(root);
        root.addElement(changes);
        changes.addChild(new Update(changes, "jakarta.faces.ViewBody", replacement.toString()));
        out.println(root.toString());
    }

    private void illegalResponse(PrintWriter out)
    {
        out.println(">>>> xxxx >YYYY-!->>>");
    }

    private void errors(PrintWriter out, PartialResponse root)
    {
        root.addElement(new ErrorResponse(root, "Error1", "Error1 Text"));
        root.addElement(new ErrorResponse(root, "Error2", "Error2 Text"));

        out.println(root.toString());
    }

    private void attributeHandling(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        Attributes attr = new Attributes(changes, "attributeChange");
        viewData.red += 10;
        attr.addAttribute(new Attribute("style",
                "color:rgb(" + ((viewData.red) % 255) + ",100,100);"));
        attr.addAttribute(new Attribute("style", "border:1px solid black"));
        attr.addAttribute(new Attribute("onclick",
                "document.getElementById('evalarea4').innerHTML = 'attributes onclick succeeded';"));

        changes.addChild(attr);
        root.addElement(changes);
        out.println(root.toString());
    }

    private void viewstateHandling(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "jakarta.faces.ViewState", "hello world"));
        root.addElement(changes);
        out.println(root.toString());
    }

    private void delete1(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Delete(changes, "deleteable"));
        root.addElement(changes);
        out.println(root.toString());
    }

    private void updateInsert2(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "changesArea",
                "<div id='changesArea'>update succeeded 1</div><script type='text/javascript'>" +
                        "document.getElementById('evalarea2').innerHTML='embedded script at update succeed';" +
                        "</script>"));
        changes.addChild(new Insert2(changes, "inserted1",
                "<div id='insertbefore'>insert2 before succeeded " +
                "should display before test1</div><script type='text/javascript'>" +
                        "document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';" +
                        "</script>", "changesArea", null));
        changes.addChild(new Insert2(changes, "inserted2", "<div  id='insertafter'>insert2 after succeeded " +
                "should display after test1</div>", null, "changesArea"));
        root.addElement(changes);
        out.println(root.toString());
    }

    private void updateInsert1(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "changesArea",
                "<div id='changesArea'>update succeeded " + (viewData.cnt++) + "</div>" +
                        "<script type='text/javascript'>" +
                        "document.getElementById('evalarea2').innerHTML='embedded script at update succeed';" +
                        "</script>"));
        changes.addChild(new Insert(changes, "inserted1",
                "<div id='insertbefore'>insert before succeeded should display before test1</div>" +
                        "<script type='text/javascript'>" +
                        "document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';</script>",
                "changesArea", null));
        changes.addChild(new Insert(changes, "inserted2",
                "<div  id='insertafter'>insert after succeeded should display after test1</div>",
                null, "changesArea"));
        root.addElement(changes);
        out.println(root.toString());
    }

    private void embeddedJavascript1(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Eval(changes,
                "document.getElementById('evalarea1').innerHTML = 'eval test succeeded';"));
        root.addElement(changes);
        out.println(root.toString());
    }

    private void resetInternalState(HttpServletRequest request, String origin, PrintWriter out)
    {
        resetViewData(request, origin);
        out.println(EMPTY_RESPONSE);
    }

    private void defaultResponse(PrintWriter out)
    {
        out.println(DEFAULT_RESPONSE);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws jakarta.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>


    private ViewData resetViewData(HttpServletRequest request, String origin)
    {
        ViewData viewData;
        viewData = new ViewData();
        request.getSession().setAttribute(VIEW_DATA + origin, viewData);
        return viewData;
    }

}
