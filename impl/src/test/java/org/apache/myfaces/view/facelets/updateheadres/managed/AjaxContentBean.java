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
package org.apache.myfaces.view.facelets.updateheadres.managed;

import java.io.Serializable;

import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("ajaxContentBean")
@ViewScoped
public class AjaxContentBean implements Serializable
{
    private String page = "ajaxContent1";
    
    private String text;

    public String getPage()
    {
        return page;
    }

    public void setPage(String page)
    {
        this.page = page;
    }
    
    public void setPage1(ActionEvent event)
    {
        this.page = "ajaxContent1";
    }
    
    public void setPage2(ActionEvent event)
    {
        this.page = "ajaxContent2";
    }
    
    public void setPage3(ActionEvent event)
    {
        this.page = "ajaxContent3";
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
