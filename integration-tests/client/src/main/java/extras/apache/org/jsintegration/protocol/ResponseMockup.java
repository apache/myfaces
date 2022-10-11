/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
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
package extras.apache.org.jsintegration.protocol;

import extras.apache.org.jsintegration.protocol.xmlNodes.Attribute;
import extras.apache.org.jsintegration.protocol.xmlNodes.Attributes;
import extras.apache.org.jsintegration.protocol.xmlNodes.Changes;
import extras.apache.org.jsintegration.protocol.xmlNodes.Delete;
import extras.apache.org.jsintegration.protocol.xmlNodes.ErrorResponse;
import extras.apache.org.jsintegration.protocol.xmlNodes.Eval;
import extras.apache.org.jsintegration.protocol.xmlNodes.Insert;
import extras.apache.org.jsintegration.protocol.xmlNodes.Insert2;
import extras.apache.org.jsintegration.protocol.xmlNodes.PartialResponse;
import extras.apache.org.jsintegration.protocol.xmlNodes.Update;

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
 * @author werpu
 */
public class ResponseMockup extends HttpServlet
{

    int cnt = 0;
    int elemCnt = 0;
    int red = 0;
    String defaultResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<partial-response><changes><update id=\"out1\"><![CDATA[<span id=\"out1\">2</span>]]></update><update id" +
            "=\"jakarta.faces.ViewState\"><![CDATA[j_id1:j_id3]]></update></changes></partial-response>";

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
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String op = (String) request.getParameter("op");

        PartialResponse root = new PartialResponse();

        /**
         * field ids needed in form:
         * changesArea
         * deleteable
         * attributeChange
         *
         */
        try
        {

            if (StringUtils.isBlank(op))
            {
                out.println(defaultResponse);
            } else if (op.trim().toLowerCase().equals("eval1"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Eval(changes, "document.getElementById('evalarea1').innerHTML = 'eval test succeeded';"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("updateinsert1"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "changesArea", "<div id='changesArea'>update succeeded " + (cnt++) + "</div><script type='text/javascript'>document.getElementById('evalarea2').innerHTML='embedded script at update succeed';</script>"));
                changes.addChild(new Insert(changes, "inserted1", "<div id='insertbefore'>insert before succeeded should display before test1</div><script type='text/javascript'>document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';</script>", "changesArea", null));
                changes.addChild(new Insert(changes, "inserted2", "<div  id='insertafter'>insert after succeeded should display after test1</div>", null, "changesArea"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("updateinsert2"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "changesArea", "<div id='changesArea'>update succeeded " + (cnt++) + "</div><script type='text/javascript'>document.getElementById('evalarea2').innerHTML='embedded script at update succeed';</script>"));
                changes.addChild(new Insert2(changes, "inserted1", "<div id='insertbefore'>insert2 before succeeded " +
                        "should display before test1</div><script type='text/javascript'>document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';</script>", "changesArea", null));
                changes.addChild(new Insert2(changes, "inserted2", "<div  id='insertafter'>insert2 after succeeded " +
                        "should display after test1</div>", null, "changesArea"));
                root.addElement(changes);
                out.println(root.toString());

            } else if (op.trim().toLowerCase().equals("delete1"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Delete(changes, "deleteable"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("viewstate"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "jakarta.faces.ViewState", "hello world"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("attributes"))
            {
                Changes changes = new Changes(root);
                Attributes attr = new Attributes(changes, "attributeChange");
                attr.addAttribute(new Attribute("style", "color:rgb(" + ((red += 10) % 255) + ",100,100);"));
                attr.addAttribute(new Attribute("style", "border:1px solid black"));
                attr.addAttribute(new Attribute("onclick", "document.getElementById('evalarea4').innerHTML = 'attributes onclick succeeded';"));

                changes.addChild(attr);
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("errors"))
            {
                root.addElement(new ErrorResponse(root, "Error1", "Error1 Text wanted error"));
                root.addElement(new ErrorResponse(root, "Error2", "Error2 Text wanted error"));

                out.println(root.toString());
            } else if (op.trim().equals("illegalResponse"))
            {
                out.println(">>>> xxxx >YYYY-!->>>");
            } else if (op.trim().toLowerCase().equals("body"))
            {
                //we omit our xml builder for now
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
            } else if (op.trim().toLowerCase().equals("body2"))
            {
                //we omit our xml builder for now
                StringBuilder replacement = new StringBuilder();

                replacement.append("<body class=\"tundra\"> " +
                        "    <div id=\"myfaces.logging\"><div id = \"centerDiv\">\n" +
                        "        <h1>Selenium Test for body change done</h1>\n");
                for (int cnt = 0; cnt < 300; cnt++)
                {
                    replacement.append("        <div id = \"testResults" + cnt + "\" ></div>\n");
                }
                replacement.append("            <h3>Body replacement test successful</h3>\n" +
                        //             "   <script type='text/javascript'>alert('hello from embedded script in replacement body');</script>            </div>" +
                        "    </div>" +
                        "</body>");

                Changes changes = new Changes(root);
                root.addElement(changes);
                changes.addChild(new Update(changes, "jakarta.faces.ViewBody", replacement.toString()));
                out.println(root.toString());

            } else if (op.trim().toLowerCase().equals("body3"))
            {

                File fIn = new File("/Users/werpu/development/workspace/TestRI20/TestRI20/src/main/webapp/34beta.html.html");
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

            } else if (op.trim().toLowerCase().equals("serversideresponsewriter"))
            {
                DeferredScriptMockup scriptMockup = new DeferredScriptMockup();
                Changes changes = new Changes(root);
                changes.addChild(new Eval(changes, "alert('the output is on the server console');"));
                root.addElement(changes);
                out.println(root.toString());
                // table tests
            } else if (op.trim().toLowerCase().equals("illegalResponse"))
            {
                out.println("blablabl this is an illegal reponse, you should see an error");
            } else if (op.trim().toLowerCase().equals("table_replace_head"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "head1", "<thead id=\"head1\">" +
                        "<tr id=\"head_row1\">" +
                        "<td id=\"head_col1\"><div id=\"col1_head\">column1 in line1 replaced</div></td>" +
                        "<td id=\"head_col2\">colum2 in line1 replaced<script " +
                        "type=\"text/javascript\">document.getElementById(\"head_col1\").innerHTML = document" +
                        ".getElementById(\"head_col1\").innerHTML+\"<div class='eval_result'>script evaled" + (cnt++)
                        + "</div>\";" +
                        "</script></td>" +
                        "</tr>" +
                        "</thead>"));

                root.addElement(changes);
                out.println(root.toString());

            } else if (op.trim().toLowerCase().equals("table_replace_body"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "tbody1", "<tbody id=\"tbody1\">" +
                        "<tr id=\"body_row1\">" +
                        "<td id=\"body_row1_col1\"><div id=\"col1_body\">column1 in line1 replaced</div></td>" +
                        "<td id=\"body_row1_col2\">colum2 in line1 replaced<script " +
                        "type=\"text/javascript\">document.getElementById(\"body_row1_col1\").innerHTML = document" +
                        ".getElementById(\"body_row1_col1\").innerHTML+\"<div class='eval_result'>script " +
                        "evaled" + (cnt++) + "</div>\";" +
                        "</script></td>" +
                        "</tr>" +
                        "</tbody>"));

                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_row_head"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "head_row1", " <tr class='insert_before' id=\"head_row1_" + (elemCnt++) + "\">\n" +
                        "                <td id=\"head_col1_" + elemCnt + "\">column1 in line1 inserted " +
                        "before</td>\n" +
                        "                <td id=\"head_col2_" + elemCnt + "\">colum2 in line2 inserted before</td>\n" +
                        "            </tr>", "head_row1", null));
                changes.addChild(new Insert2(changes, "head_row2", " <tr class='insert_after' id=\"head_row" +
                        (elemCnt++) + "\">\n" +
                        "                <td id=\"head_col1_" + elemCnt + "\">column1 in line1 inserted after" +
                        "                </td>" +
                        "                <td id=\"head_col2_" + elemCnt + "\">" +
                        "                  colum2 in line2 inserted after" +
                        "                  <script type=\"text/javascript\">" +
                        "                       document.getElementById(\"head_col1_" + elemCnt + "\").innerHTML = " +
                        "                       document.getElementById(\"head_col1_" + elemCnt + "\")" +
                        ".innerHTML+\"<div class='eval_result'>script " +
                        "                       evaled" + (cnt++) + "</div>\"; " +
                        "                  </script>" +
                        "               </td>\n" +
                        "            </tr>", null, "head_row1"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_row_body"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "body_row1", " <tr class='insert_before' id=\"body_row1_" +
                        (elemCnt++) +
                        "\">\n" +
                        "                <td id=\"body_row1_" + elemCnt + "_col1\">column1 in " +
                        "line1 inserted before</td>\n" +
                        "                <td id=\"body_row1_" + elemCnt + "_col2\">colum2 in line2 inserted " +
                        "before</td>\n" +
                        "            </tr>", "body_row1", null));
                changes.addChild(new Insert2(changes, "body_row2", " <tr class='insert_after' id=\"body_row1_" +
                        (elemCnt++) + "\">\n" +
                        "                <td id=\"body_row1_" + elemCnt + "_col1\">column1 in line1 inserted after" +
                        "                </td>" +
                        "                <td id=\"body_row1_" + elemCnt + "_col2\">" +
                        "                  colum2 in line2 inserted after" +
                        "                  <script type=\"text/javascript\">" +
                        "                       document.getElementById(\"body_row1_col1\").innerHTML = " +
                        "                       document.getElementById(\"body_row1_" + elemCnt + "_col1\")" +
                        ".innerHTML+\"<div class='eval_result'>script " +
                        "                       evaled" + (cnt++) + "</div>\"; " +
                        "                  </script>" +
                        "               </td>\n" +
                        "            </tr>", null, "body_row1"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_column_head"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "head_col1", "<td id='head_col1_1_" + (elemCnt++) + "'>inserted " +
                        "before" + elemCnt + "</td>" + "<td id='head_col1_1_" + (elemCnt++) + "'>inserted " +
                        "before " + elemCnt + "</td>",
                        "head_col1",
                        null));
                changes.addChild(new Insert2(changes, "head_col1", "<td id='head_col1_1_" + (elemCnt++) + "'>inserted " +
                        "after" + elemCnt + "</td>" + "<td id='head_col1_1_" + (elemCnt++) + "'>inserted " +
                        "after" + elemCnt + "</td>",
                        null,
                        "head_col2"));

                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_column_body"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "body_row1_col1", "<td id='body_row1_col1_1_" + (elemCnt++) +
                        "'>inserted " +
                        "before" + elemCnt + "</td>" + "<td id='body_row1_col1_1_" + (elemCnt++) + "'>inserted " +
                        "before " + elemCnt + "</td>",
                        "body_row1_col1",
                        null));
                changes.addChild(new Insert2(changes, "body_row1_col1", "<td id='body_row1_col1_1_" + (elemCnt++) +
                        "'>inserted " +
                        "after" + elemCnt + "</td>" + "<td id='body_row1_col1_1_" + (elemCnt++) + "'>inserted " +
                        "after" + elemCnt + "</td>",
                        null,
                        "body_row1_col2"));

                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_footer"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "body_row1_col1", "<tfooter>footer inserted</tfooter>",
                        null,
                        "tbody1"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("table_insert_body"))
            {
                Changes changes = new Changes(root);
                changes.addChild(new Insert2(changes, "body_row1_col1",
                        "<tbody><tr><td colspan='2'>second body added</td></tr></tbody>",
                        null,
                        "tbody1"));
                root.addElement(changes);
                out.println(root.toString());
            } else if (op.trim().toLowerCase().equals("executenone"))
            {
                boolean execute = request.getParameter("jakarta.faces.partial.execute") != null;
                boolean render = request.getParameter("jakarta.faces.partial.render") != null;

                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "result",(!execute && !render) ?  "<div " +
                        "id='result'>success</div>" :  "<div " +
                                                "id='result'>fail</div>"));
                root.addElement(changes);
                out.println(root.toString());
            }

        }
        finally
        {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws jakarta.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
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
}
