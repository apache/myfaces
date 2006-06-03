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
package org.apache.myfaces.taglib.html;

import org.apache.myfaces.shared_impl.taglib.html.HtmlInputTextTagBase;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlInputTextTag
        extends HtmlInputTextTagBase
{
	
	//special attribut added to jsf 1.2
    private String _autocomplete;

    public void release()
    {
        super.release();
        _autocomplete = null;
    }
    
    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, org.apache.myfaces.shared_impl.renderkit.html.HTML.AUTOCOMPLETE_ATTR, _autocomplete);
    }

    public void setAutocomplete(String autocomplete)
    {
        _autocomplete = autocomplete;
    }
	
    public String getComponentType()
    {
        return HtmlInputText.COMPONENT_TYPE;
    }

    public String getRendererType()
    {
        return "javax.faces.Text";
    }
}
