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
package org.apache.myfaces.view.facelets.component.rowstate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

/**
 * Bean of rowStatePreservedNewRow.xhtml, see {@link RowStatePreservedNewRowTest}.
 */
@Named("rowStateNewRowBean")
@SessionScoped
public class RowStateNewRowBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<String> tableRows = new ArrayList<>(List.of("0", "1"));
    private final List<String> repeatRows = new ArrayList<>(List.of("0", "1"));

    /**
     * Adds an input with only an id, so that nothing is in its state helper initially.
     */
    public void addInput(ComponentSystemEvent event)
    {
        UIComponent cell = event.getComponent();
        if (cell.getChildCount() == 0)
        {
            HtmlInputText input = new HtmlInputText();
            input.setId("in");
            cell.getChildren().add(input);
        }
    }

    public void addRow()
    {
        tableRows.add(String.valueOf(tableRows.size()));
        repeatRows.add(String.valueOf(repeatRows.size()));
    }

    public List<String> getTableRows()
    {
        return tableRows;
    }

    public List<String> getRepeatRows()
    {
        return repeatRows;
    }
}
