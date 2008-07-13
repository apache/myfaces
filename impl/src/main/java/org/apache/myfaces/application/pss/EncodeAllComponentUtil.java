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
package org.apache.myfaces.application.pss;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Martin Haimberger
 */
public class EncodeAllComponentUtil {
    /**
     * ensure that this util class can not be instanciated
     */
    private EncodeAllComponentUtil(){}

    /**
     * Encodes a whole UI-Component Tree or a part of the tree.
     * @param context The facescontext
     * @param component The base of the tree or the part or the tree
     * @throws IOException thrown Exception
     */

    public static void encodeAll(FacesContext context, UIComponent component)
    throws IOException
    {
        if (!component.isRendered()) {
            return;
        }

        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        }
        else if (component.getChildCount() > 0) {
                Iterator kids = component.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    encodeAll(context,kid);
                }
            }

        component.encodeEnd(context);
    }
}
