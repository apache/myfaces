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
package org.apache.myfaces.view.facelets.tag.composite;

import org.apache.myfaces.view.facelets.tag.AbstractTagLibrary;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeLibrary extends AbstractTagLibrary
{
    public final static String NAMESPACE = "jakarta.faces.composite";
    public final static String JCP_NAMESPACE = "http://xmlns.jcp.org/jsf/composite";
    public final static String SUN_NAMESPACE = "http://java.sun.com/jsf/composite";
    
    public CompositeLibrary()
    {
        super(NAMESPACE, JCP_NAMESPACE, SUN_NAMESPACE);

        addTagHandler("actionSource", ActionSourceHandler.class);

        addTagHandler("attribute", AttributeHandler.class);
        
        addTagHandler("clientBehavior", ClientBehaviorHandler.class);
        
        addTagHandler("editableValueHolder", EditableValueHolderHandler.class);
        
        addTagHandler("extension", ExtensionHandler.class);
        
        addTagHandler("facet", FacetHandler.class);
        
        addTagHandler("implementation", ImplementationHandler.class);
        
        addTagHandler("insertChildren", InsertChildrenHandler.class);
        
        addTagHandler("insertFacet", InsertFacetHandler.class);
        
        addTagHandler("interface", InterfaceHandler.class);
        
        addComponent("renderFacet", "jakarta.faces.Output", 
                "jakarta.faces.CompositeFacet", RenderFacetHandler.class);

        addTagHandler("valueHolder", ValueHolderHandler.class);
    }

}
