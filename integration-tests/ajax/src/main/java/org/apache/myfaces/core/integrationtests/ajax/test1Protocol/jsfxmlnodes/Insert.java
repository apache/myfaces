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

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.2 $ $Date: 2009/04/17 10:53:30 $
 */
public class Insert implements Change {

    String id = "";
    String before = "";
    String after = "";
    String data = "";

    public Insert(Changes parent, String id, String data, String before, String after) {
        super();
        this.id = id;
        this.before = before;
        this.after = after;
        this.data = data;
    }

    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<insert id='");
        builder.append(id);
        builder.append("' ");
        if (before != null && !before.trim().equals("")) {
            builder.append(" before='");
            builder.append(before);
            builder.append("' >");
        } else {
            builder.append(" after='");
            builder.append(after);
            builder.append("' >");
        }
        builder.append("<![CDATA[");
        builder.append(data);
        builder.append("]]>");
        builder.append("</insert>");

        return builder.toString();
    }
}
