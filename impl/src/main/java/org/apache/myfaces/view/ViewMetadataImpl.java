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
package org.apache.myfaces.view;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewMetadata;

/**
 * This class represents the default implementation of javax.faces.view.ViewMetaData.
 */

public class ViewMetadataImpl extends ViewMetadata
{
    private String viewID;
    
    public ViewMetadataImpl (String viewID)
    {
        this.viewID = viewID;
    }
    
    @Override
    public UIViewRoot createMetadataView (FacesContext context)
    {
        // TODO implement.  This will require supporting Facelet parser/compiler logic
        // to extract metadata.
        
        return null;
    }

    @Override
    public String getViewId ()
    {
        return this.viewID;
    }
}
