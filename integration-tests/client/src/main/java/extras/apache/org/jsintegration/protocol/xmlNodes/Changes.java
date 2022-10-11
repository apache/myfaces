/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package extras.apache.org.jsintegration.protocol.xmlNodes;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/16 15:45:19 $
 */
public class Changes {

    PartialResponse parent;
    List<Change> childs = new LinkedList<Change>();

    public Changes(PartialResponse parent) {
        this.parent = parent;
    }

    public void addChild(Change child) {
        childs.add(child);
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<changes>");
        for (Change child : childs) {
            retVal.append(child.toString());
        }
        retVal.append("</changes>");
        return retVal.toString();
    }
}
