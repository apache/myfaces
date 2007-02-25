/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.myfaces.application.pss;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Martin Haimberger
 */
public class TreeStructComponent
        implements Serializable
{

    final public static byte STATE_IS_TEMPLATE_STATE = 1;
    final public static byte STATE_IS_NEW_STATE = 2;
    final public static byte STATE_IS_NEW_COMPONENT = 3;
    final public static byte STATE_IS_RESTORED = 4;

    private static final long serialVersionUID = 5069177074684737231L;
    private Integer _componentClass;
    private String _componentId;
    private TreeStructComponent[] _children = null;    // Array of children
    private Object[] _facets = null;                   // Array of Array-tuples with Facetname and TreeStructComponent
    private Object _componentState = null;
    private byte status = 0;
    private boolean _transient;

    public boolean isTransient() {
        return _transient;
    }

    public void setTransient(boolean _transient) {
        this._transient = _transient;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Object get_componentState() {
        return _componentState;
    }

    public void set_componentState(Object _componentState) {
        this._componentState = _componentState;
    }

    TreeStructComponent(Integer componentClass, String componentId,Object componentState, boolean isTransient)
    {
        _componentClass = componentClass;
        _componentId = componentId;
         set_componentState(componentState);
        _transient = isTransient;

    }

    public Integer getComponentClass()
    {
        return _componentClass;
    }

    public String getComponentId()
    {
        return _componentId;
    }

    void setChildren(TreeStructComponent[] children)
    {
        _children = children;
    }

    TreeStructComponent[] getChildren()
    {
        return _children;
    }

    Object[] getFacets()
    {
        return _facets;
    }

    void setFacets(Object[] facets)
    {
        _facets = facets;
    }

    public TreeStructComponent clone(TreeStructComponent org)
    {
        if (org == null)
        {
            return new TreeStructComponent(getComponentClass(),getComponentId(),get_componentState(),isTransient());
        }
        return new TreeStructComponent(org.getComponentClass(),org.getComponentId(),org.get_componentState(),org.isTransient());
    }

}
