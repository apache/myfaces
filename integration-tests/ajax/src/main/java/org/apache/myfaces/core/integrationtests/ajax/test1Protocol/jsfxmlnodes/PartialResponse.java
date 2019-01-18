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
package org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.2 $ $Date: 2009/04/17 10:27:53 $
 */
public class PartialResponse {

    List<Object> elements = new LinkedList<Object>();

    public void addElement(Object element) {
        elements.add(element);
    }


    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        retVal.append("<partial-response>");

        for (Object element : elements) {
            retVal.append(element.toString());
        }

        retVal.append("</partial-response>");

        return retVal.toString();
    }
}
