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
package javax.faces.component.html;

import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlPanelGroup extends UIPanel
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlPanelGroup";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Group";

    private String _style = null;
    private String _styleClass = null;
    private String layout;

    public HtmlPanelGroup()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public void setStyle(String style)
    {
        _style = style;
    }

    public String getStyle()
    {
        if (_style != null) return _style;
        ValueBinding vb = getValueBinding("style");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setStyleClass(String styleClass)
    {
        _styleClass = styleClass;
    }

    public String getStyleClass()
    {
        if (_styleClass != null) return _styleClass;
        ValueBinding vb = getValueBinding("styleClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[4];
        values[0] = super.saveState(context);
        values[1] = _style;
        values[2] = _styleClass;
        values[3] = layout;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _style = (String)values[1];
        _styleClass = (String)values[2];
        layout = (String)values[3];
    }
    //------------------ GENERATED CODE END ---------------------------------------
    
    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public String getLayout()
    {
        if (layout != null) return layout;
        ValueBinding vb = getValueBinding("layout");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }
}
