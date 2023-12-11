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
package org.apache.myfaces.core.integrationtests.ajax.test1Protocol.responses;

import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.ViewData;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Changes;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Insert2;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Update;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

/**
 * A helper class to encapsule the table responses
 */
public class TableResponseMockups
{


    public static void execteNone(HttpServletRequest request, PrintWriter out, PartialResponse root)
    {
        boolean execute = request.getParameter("jakarta.faces.partial.execute") != null;
        boolean render = request.getParameter("jakarta.faces.partial.render") != null;

        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "result", (!execute && !render) ? """
                <div \
                id='result'>success</div>\
                """ : """
                <div \
                id='result'>fail</div>\
                """));
        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsertBody(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes, "body_row1_col1",
                "<tbody><tr><td colspan='2'>second body added</td></tr></tbody>",
                null,
                "tbody1"));
        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsertFooter(PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes, "body_row1_col1", "<tfooter>footer inserted</tfooter>",
                null,
                "tbody1"));
        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsertColumnBody(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes, "body_row1_col1", "<td id='body_row1_col1_1_" +
                (viewData.elemCnt++) + "'>inserted " + "before" + viewData.elemCnt + "</td>" +
                "<td id='body_row1_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "before " + viewData.elemCnt + "</td>",
                "body_row1_col1",
                null));
        changes.addChild(new Insert2(changes, "body_row1_col1", "<td id='body_row1_col1_1_" +
                (viewData.elemCnt++) + "'>inserted " + "after" + viewData.elemCnt +
                "</td>" + "<td id='body_row1_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "after" + viewData.elemCnt + "</td>",
                null,
                "body_row1_col2"));

        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsetColumnHead(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes,
                "head_col1", "<td id='head_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "before" + viewData.elemCnt + "</td>" +
                "<td id='head_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "before " + viewData.elemCnt + "</td>",
                "head_col1",
                null));
        changes.addChild(new Insert2(changes,
                "head_col1", "<td id='head_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "after" + viewData.elemCnt + "</td>" +
                "<td id='head_col1_1_" + (viewData.elemCnt++) + "'>inserted " +
                "after" + viewData.elemCnt + "</td>",
                null,
                "head_col2"));

        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsertRowBody(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes, "body_row1", " <tr class='insert_before' id=\"body_row1_" +
                (viewData.elemCnt++) +
                "\">\n" +
                "                <td id=\"body_row1_" + viewData.elemCnt + "_col1\">column1 in " +
                "line1 inserted before</td>\n" +
                "                <td id=\"body_row1_" + viewData.elemCnt + "_col2\">colum2 in line2 inserted " +
                "before</td>\n" +
                "            </tr>", "body_row1", null));
        changes.addChild(new Insert2(changes, "body_row2", " <tr class='insert_after' id=\"body_row1_" +
                (viewData.elemCnt++) + "\">\n" +
                "                <td id=\"body_row1_" + viewData.elemCnt + "_col1\">column1 in line1 inserted after" +
                "                </td>" +
                "                <td id=\"body_row1_" + viewData.elemCnt + "_col2\">" +
                "                  colum2 in line2 inserted after" +
                "                  <script type=\"text/javascript\">" +
                "                       document.getElementById(\"body_row1_col1\").innerHTML = " +
                "                       document.getElementById(\"body_row1_" + viewData.elemCnt + "_col1\")" +
                ".innerHTML+\"<div class='eval_result'>script " +
                "                       evaled" + (viewData.cnt++) + "</div>\"; " +
                "                  </script>" +
                "               </td>\n" +
                "            </tr>", null, "body_row1"));
        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableInsertRowHead(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Insert2(changes, "head_row1",
                " <tr class='insert_before' id=\"head_row1_" + (viewData.elemCnt++) + "\">\n" +
                "                <td id=\"head_col1_" + viewData.elemCnt + "\">column1 in line1 inserted " +
                "before</td>\n" +
                "                <td id=\"head_col2_" + viewData.elemCnt + "\">colum2 in line2 inserted before</td>\n" +
                "            </tr>", "head_row1", null));
        changes.addChild(new Insert2(changes, "head_row2", " <tr class='insert_after' id=\"head_row" +
                (viewData.elemCnt++) + "\">\n" +
                "                <td id=\"head_col1_" + viewData.elemCnt + "\">column1 in line1 inserted after" +
                "                </td>" +
                "                <td id=\"head_col2_" + viewData.elemCnt + "\">" +
                "                  colum2 in line2 inserted after" +
                "                  <script type=\"text/javascript\">" +
                "                       document.getElementById(\"head_col1_" + viewData.elemCnt + "\").innerHTML = " +
                "                       document.getElementById(\"head_col1_" + viewData.elemCnt + "\")" +
                ".innerHTML+\"<div class='eval_result'>script " +
                "                       evaled" + (viewData.cnt++) + "</div>\"; " +
                "                  </script>" +
                "               </td>\n" +
                "            </tr>", null, "head_row1"));
        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableReplaceBody(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "tbody1", "<tbody id=\"tbody1\">" +
                "<tr id=\"body_row1\">" +
                "<td id=\"body_row1_col1\"><div id=\"col1_body\">column1 in line1 replaced</div></td>" +
                "<td id=\"body_row1_col2\">colum2 in line1 replaced<script " +
                "type=\"text/javascript\">document.getElementById(\"body_row1_col1\").innerHTML = document" +
                ".getElementById(\"body_row1_col1\").innerHTML+\"<div class='eval_result'>script " +
                "evaled" + (viewData.cnt++) + "</div>\";" +
                "</script></td>" +
                "</tr>" +
                "</tbody>"));

        root.addElement(changes);
        out.println(root.toString());
    }

    public static void tableReplaceHead(ViewData viewData, PrintWriter out, PartialResponse root)
    {
        Changes changes = new Changes(root);
        changes.addChild(new Update(changes, "head1", "<thead id=\"head1\">" +
                "<tr id=\"head_row1\">" +
                "<td id=\"head_col1\"><div id=\"col1_head\">column1 in line1 replaced</div></td>" +
                "<td id=\"head_col2\">colum2 in line1 replaced<script " +
                "type=\"text/javascript\">document.getElementById(\"head_col1\").innerHTML = document" +
                ".getElementById(\"head_col1\").innerHTML+\"<div class='eval_result'>script evaled" + (viewData.cnt++)
                + "</div>\";" +
                "</script></td>" +
                "</tr>" +
                "</thead>"));

        root.addElement(changes);
        out.println(root.toString());
    }
}
