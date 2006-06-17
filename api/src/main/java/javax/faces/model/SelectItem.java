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
package javax.faces.model;

import java.io.Serializable;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class SelectItem implements Serializable
{
    private static final long serialVersionUID = 8841094741464512226L;
    // FIELDS
    private Object _value;
    private String _label;
    private String _description;
    private boolean _disabled;
    private boolean escape;

	// CONSTRUCTORS
    public SelectItem()
    {
    }

    public SelectItem(Object value)
    {
        if (value == null) throw new NullPointerException("value");
        _value = value;
        _label = value.toString();
        _description = null;
        _disabled = false;
    }

    public SelectItem(Object value, String label)
    {
        if (value == null) throw new NullPointerException("value");
        if (label == null) throw new NullPointerException("label");
        _value = value;
        _label = label;
        _description = null;
        _disabled = false;
    }

    public SelectItem(Object value, String label, String description)
    {
        if (value == null) throw new NullPointerException("value");
        if (label == null) throw new NullPointerException("label");
        _value = value;
        _label = label;
        _description = description;
        _disabled = false;
    }

    public SelectItem(Object value, String label, String description, boolean disabled)
    {
        if (value == null) throw new NullPointerException("value");
        if (label == null) throw new NullPointerException("label");
        _value = value;
        _label = label;
        _description = description;
        _disabled = disabled;
    }

    public SelectItem(Object value, String label, String description, boolean disabled, boolean escape)
    {
        if (value == null) throw new NullPointerException("value");
        if (label == null) throw new NullPointerException("label");
        _value = value;
        _label = label;
        _description = description;
        _disabled = disabled;
        this.escape = escape;
    }
    
	// METHODS
    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public boolean isDisabled()
    {
        return _disabled;
    }

    public void setDisabled(boolean disabled)
    {
        _disabled = disabled;
    }

    public String getLabel()
    {
        return _label;
    }

    public void setLabel(String label)
    {
        if (label == null) throw new NullPointerException("label");
        _label = label;
    }

    public Object getValue()
    {
        return _value;
    }

    public void setValue(Object value)
    {
        if (value == null) throw new NullPointerException("value");
        _value = value;
    }

    public boolean isEscape()
    {
        return escape;
    }

    public void setEscape(boolean escape)
    {
        this.escape = escape;
    }
    
}
