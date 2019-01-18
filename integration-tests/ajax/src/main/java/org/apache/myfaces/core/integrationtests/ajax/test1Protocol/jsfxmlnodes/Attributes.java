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
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/16 15:45:19 $
 */
public class Attributes implements Change {

    String id = "";
    List<Attribute> attributes = new LinkedList<Attribute>();

    public Attributes(Changes parent, String id) {
        this.id = id;
    }

    public void addAttribute(Attribute attr) {
        attributes.add(attr);
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<attributes id='");
        retVal.append(id);
        retVal.append("'>");
        for (Attribute attribute : attributes) {
            retVal.append(attribute.toString());
        }
        retVal.append("</attributes>");

        return retVal.toString();
    }
}
