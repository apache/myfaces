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

package extras.apache.org.jsintegration.table;



import jakarta.annotation.ManagedBean;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */

@Named
@ViewScoped
public class TableBean implements Serializable
{
    List<Entry> entries = new LinkedList<Entry>();

    String input1;
    String input2;

    String lineInput1;
    String lineInput2;


   public class Entry implements  Serializable{
        String field1;
        String field2;

        Entry(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }
    }


    public TableBean() {
        for (int cnt = 0; cnt < 100; cnt++) {
            entries.add(new Entry("field1" + cnt, "field2" + cnt));
      }
    }
   

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getLineInput1() {
        return lineInput1;
    }

    public void setLineInput1(String lineInput1) {
        this.lineInput1 = lineInput1;
    }

    public String getLineInput2() {
        return lineInput2;
    }

    public void setLineInput2(String lineInput2) {
        this.lineInput2 = lineInput2;
    }

    

    public String getInput1() {
        return input1;
    }

    public void setInput1(String input1) {
        this.input1 = input1;
    }

    public String getInput2() {
        return input2;
    }

    public void setInput2(String input2) {
        this.input2 = input2;
    }

    public String doTableSubmit() {
        return null;
    }

    public String doTableSubmit2() {
        return null;
    }
}
