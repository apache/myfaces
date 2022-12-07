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

package extras.apache.org.jsintegration.protocol.xmlNodes;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */


/**
 *
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.2 $ $Date: 2009/04/17 10:53:30 $
 */
public class Insert2 implements Change {

    String id = "";
    String before = "";
    String after = "";
    String data = "";

    public Insert2(Changes parent, String id, String data, String before, String after) {
        super();
        this.id = id;
        this.before = before;
        this.after = after;
        this.data = data;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("<insert>");
        if (before != null && !before.trim().equals("")) {
            builder.append("<before id='");
            builder.append(before);
            builder.append("'>");
        } else {
            builder.append("<after id='");
            builder.append(after);
            builder.append("' >");
        }
        builder.append("<![CDATA[");
        builder.append(data);
        builder.append("]]>");
        if (before != null && !before.trim().equals("")) {
            builder.append("</before>");
        } else {
            builder.append("</after>");
        }
        builder.append("</insert>");

        return builder.toString();
    }
}

