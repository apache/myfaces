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
package javax.faces.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIMessages
        extends UIComponentBase
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Messages";
    public static final String COMPONENT_FAMILY = "javax.faces.Messages";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Messages";
    private static final boolean DEFAULT_GLOBALONLY = false;
    private static final boolean DEFAULT_SHOWDETAIL = false;
    private static final boolean DEFAULT_SHOWSUMMARY = true;

    private Boolean _globalOnly = null;
    private Boolean _showDetail = null;
    private Boolean _showSummary = null;

    public UIMessages()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setGlobalOnly(boolean globalOnly)
    {
        _globalOnly = Boolean.valueOf(globalOnly);
    }

    public boolean isGlobalOnly()
    {
        if (_globalOnly != null) return _globalOnly.booleanValue();
        ValueBinding vb = getValueBinding("globalOnly");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_GLOBALONLY;
    }

    public void setShowDetail(boolean showDetail)
    {
        _showDetail = Boolean.valueOf(showDetail);
    }

    public boolean isShowDetail()
    {
        if (_showDetail != null) return _showDetail.booleanValue();
        ValueBinding vb = getValueBinding("showDetail");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_SHOWDETAIL;
    }

    public void setShowSummary(boolean showSummary)
    {
        _showSummary = Boolean.valueOf(showSummary);
    }

    public boolean isShowSummary()
    {
        if (_showSummary != null) return _showSummary.booleanValue();
        ValueBinding vb = getValueBinding("showSummary");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_SHOWSUMMARY;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[4];
        values[0] = super.saveState(context);
        values[1] = _globalOnly;
        values[2] = _showDetail;
        values[3] = _showSummary;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _globalOnly = (Boolean)values[1];
        _showDetail = (Boolean)values[2];
        _showSummary = (Boolean)values[3];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
