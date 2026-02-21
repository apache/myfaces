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
package org.apache.myfaces.view.facelets.tag.faces.html;

import java.util.Set;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class HtmlLibrary extends AbstractHtmlLibrary
{
    public final static String NAMESPACE = "jakarta.faces.html";
    public final static String JCP_NAMESPACE = "http://xmlns.jcp.org/jsf/html";
    public final static String SUN_NAMESPACE = "http://java.sun.com/jsf/html";
    public final static Set<String> NAMESPACES = Set.of(NAMESPACE, JCP_NAMESPACE, SUN_NAMESPACE);

    public final static HtmlLibrary INSTANCE = new HtmlLibrary();

    public HtmlLibrary()
    {
        super(NAMESPACE, JCP_NAMESPACE, SUN_NAMESPACE);
        
        this.addHtmlComponent("body", "jakarta.faces.OutputBody", "jakarta.faces.Body");
        
        this.addHtmlComponent("button", "jakarta.faces.HtmlOutcomeTargetButton", "jakarta.faces.Button");
        
        this.addHtmlComponent("column", "jakarta.faces.HtmlColumn", null);

        this.addHtmlComponent("commandButton", "jakarta.faces.HtmlCommandButton", "jakarta.faces.Button");

        this.addHtmlComponent("commandLink", "jakarta.faces.HtmlCommandLink", "jakarta.faces.Link");
        
        this.addHtmlComponent("commandScript", "jakarta.faces.HtmlCommandScript", "jakarta.faces.Script");

        this.addHtmlComponent("dataTable", "jakarta.faces.HtmlDataTable", "jakarta.faces.Table");

        this.addHtmlComponent("doctype", "jakarta.faces.OutputDoctype", "jakarta.faces.Doctype");
        
        this.addHtmlComponent("form", "jakarta.faces.HtmlForm", "jakarta.faces.Form");

        this.addHtmlComponent("graphicImage", "jakarta.faces.HtmlGraphicImage", "jakarta.faces.Image");
        
        this.addHtmlComponent("head", "jakarta.faces.OutputHead", "jakarta.faces.Head");
        
        this.addHtmlComponent("inputHidden", "jakarta.faces.HtmlInputHidden", "jakarta.faces.Hidden");

        this.addHtmlComponent("inputSecret", "jakarta.faces.HtmlInputSecret", "jakarta.faces.Secret");

        this.addHtmlComponent("inputText", "jakarta.faces.HtmlInputText", "jakarta.faces.Text");

        this.addHtmlComponent("inputTextarea", "jakarta.faces.HtmlInputTextarea", "jakarta.faces.Textarea");

        this.addHtmlComponent("inputFile", "jakarta.faces.HtmlInputFile", "jakarta.faces.File");
        
        this.addHtmlComponent("link", "jakarta.faces.HtmlOutcomeTargetLink", "jakarta.faces.Link");
        
        this.addHtmlComponent("message", "jakarta.faces.HtmlMessage", "jakarta.faces.Message");

        this.addHtmlComponent("messages", "jakarta.faces.HtmlMessages", "jakarta.faces.Messages");

        this.addHtmlComponent("outputFormat", "jakarta.faces.HtmlOutputFormat", "jakarta.faces.Format");

        this.addHtmlComponent("outputLabel", "jakarta.faces.HtmlOutputLabel", "jakarta.faces.Label");

        this.addHtmlComponent("outputLink", "jakarta.faces.HtmlOutputLink", "jakarta.faces.Link");
        
        this.addComponent("outputScript", "jakarta.faces.Output", "jakarta.faces.resource.Script",
                          HtmlOutputScriptHandler.class);
        
        this.addComponent("outputStylesheet", "jakarta.faces.Output", "jakarta.faces.resource.Stylesheet",
                          HtmlOutputStylesheetHandler.class);
        
        this.addHtmlComponent("outputText", "jakarta.faces.HtmlOutputText", "jakarta.faces.Text");

        this.addHtmlComponent("panelGrid", "jakarta.faces.HtmlPanelGrid", "jakarta.faces.Grid");

        this.addHtmlComponent("panelGroup", "jakarta.faces.HtmlPanelGroup", "jakarta.faces.Group");

        this.addHtmlComponent("selectBooleanCheckbox", "jakarta.faces.HtmlSelectBooleanCheckbox",
                "jakarta.faces.Checkbox");

        this.addHtmlComponent("selectManyCheckbox", "jakarta.faces.HtmlSelectManyCheckbox", "jakarta.faces.Checkbox");

        this.addHtmlComponent("selectManyListbox", "jakarta.faces.HtmlSelectManyListbox", "jakarta.faces.Listbox");

        this.addHtmlComponent("selectManyMenu", "jakarta.faces.HtmlSelectManyMenu", "jakarta.faces.Menu");

        this.addHtmlComponent("selectOneListbox", "jakarta.faces.HtmlSelectOneListbox", "jakarta.faces.Listbox");

        this.addHtmlComponent("selectOneMenu", "jakarta.faces.HtmlSelectOneMenu", "jakarta.faces.Menu");

        this.addHtmlComponent("selectOneRadio", "jakarta.faces.HtmlSelectOneRadio", "jakarta.faces.Radio");
    }

}
