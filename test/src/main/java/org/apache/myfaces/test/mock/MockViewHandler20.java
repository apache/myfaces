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
package org.apache.myfaces.test.mock;

import java.util.List;
import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * Mock for a JSF 2.0 ViewHandler.
 * This ViewHandler is used by MockApplication.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class MockViewHandler20 extends MockViewHandler
{

    @Override
    public String getBookmarkableURL(FacesContext context, String viewId,
            Map<String, List<String>> parameters, boolean includeViewParams)
    {
        // the standard impl only calls getActionURL(context, viewId)
        // but we want to include the parameters too

        String actionEncodedViewId = getActionURL(context, viewId);
        ExternalContext externalContext = context.getExternalContext();
        String bookmarkEncodedURL = externalContext.encodeBookmarkableURL(
                actionEncodedViewId, parameters);
        return externalContext.encodeActionURL(bookmarkEncodedURL);
    }

}
