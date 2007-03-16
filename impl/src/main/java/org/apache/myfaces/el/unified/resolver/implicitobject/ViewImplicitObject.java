/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.el.unified.resolver.implicitobject;

import java.beans.FeatureDescriptor;
import javax.el.ELContext;
import javax.faces.component.UIViewRoot;

/**
 * Encapsulates information needed by the ImplicitObjectResolver
 *
 * @author Stan Silvert
 */
public class ViewImplicitObject extends ImplicitObject {
    
    private static final String NAME = "view".intern();
    
    /** Creates a new instance of ViewImplicitObject */
    public ViewImplicitObject() {
    }

    public Object getValue(ELContext context) {
        return facesContext(context).getViewRoot();
    }

    public String getName() {
        return NAME;
    }
    
    public Class getType() {
        return null;
    }

    public FeatureDescriptor getDescriptor() {
        return makeDescriptor(NAME, 
                             "The root object of a JSF component tree", 
                             UIViewRoot.class);
    }
    
}
