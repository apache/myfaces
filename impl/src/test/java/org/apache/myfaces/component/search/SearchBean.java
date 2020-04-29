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

package org.apache.myfaces.component.search;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.RequestScoped;

/**
 *
 * @author lu4242
 */
@ManagedBean(name = "searchBean")
@RequestScoped
public class SearchBean
{
    
    private List<RowData> model;
    
    public SearchBean()
    {
        
    }
    
    @PostConstruct
    public void init()
    {
        setModel(new ArrayList<RowData>());
        getModel().add(new RowData("text1","style1"));
        getModel().add(new RowData("text2","style2"));
        getModel().add(new RowData("text3","style3"));
        getModel().add(new RowData("text4","style4"));        
    }

    /**
     * @return the model
     */
    public List<RowData> getModel()
    {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(List<RowData> model)
    {
        this.model = model;
    }
    
    public static class RowData
    {
        private String text;
        
        private List<String> nested;

        public RowData(String text, String style)
        {
           super();
            this.text = text;
            this.style = style;
            this.nested = new ArrayList<String>();
            this.nested.add("A");
            this.nested.add("B");
        }

        private String style;
        
        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public String getStyle()
        {
            return style;
        }

        public void setStyle(String style)
        {
            this.style = style;
        }

        /**
         * @return the nested
         */
        public List<String> getNested()
        {
            return nested;
        }

        /**
         * @param nested the nested to set
         */
        public void setNested(List<String> nested)
        {
            this.nested = nested;
        }
    }
}
