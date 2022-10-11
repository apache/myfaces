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

package extras.apache.org.jsintegration.other;


import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */

@Named
@ViewScoped
public class MyBean2 implements Serializable {

    String helloWorld = "hello world";

    String searchTerm = "";

    String searchTerm2 = "";

    List<String> results = new LinkedList<String>();

    String testTemplate = "<div id='TPL_IDENTIFIER_PLACEHOLDER_CONTENT_ID' />";

    static int refresh=1;

    public MyBean2() {
    }

    public String getHelloWorld() {
        return helloWorld;
    }

    public void setHelloWorld(String helloWorld) {
        this.helloWorld = helloWorld;
    }

    public List<String> search(final String term)  {
        List <String> retVal = new LinkedList<String>();
        for(int cnt = 0; cnt < 10; cnt++) {
            results.add(term+cnt);
            retVal.add(term+cnt);
        }
        return retVal;

    }


    public String doSearch() {
        //System.out.println("searching"+searchTerm);
        results = search(searchTerm);
        return null;
    }


    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }


    private void readObject(ObjectInputStream ois) throws Exception {
        ois.defaultReadObject();
    }

    public  int getRefresh() {
        return refresh++;
    }

    public  void setRefresh(int refresh) {
        refresh = refresh;
    }

    public String getSearchTerm2() {
        return searchTerm2;
    }

    public void setSearchTerm2(String searchTerm2) {
        this.searchTerm2 = searchTerm2;
    }

    public String getTestTemplate() {
        return testTemplate;
    }

    public void setTestTemplate(String testTemplate) {
        this.testTemplate = testTemplate;
    }
}
