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
package org.apache.myfaces.view.facelets.pss.acid.managed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 */
@Named("forEachBean")
@SessionScoped
public class ForEachBean implements Serializable
{
    private List<String> items1;
    
    public ForEachBean()
    {
        items1 = new ArrayList<String>();
        items1.add("a");
        items1.add("b");
        items1.add("c");
    }

    /**
     * @return the items1
     */
    public List<String> getItems1()
    {
        return items1;
    }
    
    public void addFirst()
    {
        items1.add(0, "z");
    }

    public void addMiddle()
    {
        items1.add(2, "x");
    }

    public void removeLast()
    {
        items1.remove(items1.size()-1);
    }
}
