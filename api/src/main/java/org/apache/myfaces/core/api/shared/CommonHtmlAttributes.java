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
package org.apache.myfaces.core.api.shared;

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.component.UIComponent;

/**
 * This is a list of the most common properties used by a Faces html component, organized by interfaces.
 */
public class CommonHtmlAttributes
{
    public static final String ATTRIBUTES_MARKED = "oam.ATTRIBUTES_MARKED";
    
    //_StyleProperties
    public static final long STYLE       = 0x1L;
    public static final long STYLECLASS  = 0x2L;
    
    //_UniversalProperties
    //_TitleProperty
    public static final long DIR         = 0x4L;
    public static final long LANG        = 0x8L;
    public static final long TITLE       = 0x10L;
    
    //_EscapeProperty
    public static final long ESCAPE      = 0x20L;

    //_DisabledClassEnabledClassProperties
    //_DisabledReadonlyProperties
    public static final long DISABLED    = 0x40L;
    public static final long ENABLED     = 0x80L;
    public static final long READONLY    = 0x100L;

    //_AccesskeyProperty
    public static final long ACCESSKEY  = 0x200L;
    
    //_AltProperty
    public static final long ALT         = 0x400L;
    
    //_ChangeSelectProperties
    public static final long ONCHANGE    = 0x800L;
    public static final long ONSELECT    = 0x1000L;
    
    //_EventProperties
    public static final long ONCLICK     = 0x2000L;
    public static final long ONDBLCLICK  = 0x4000L;
    public static final long ONMOUSEDOWN = 0x8000L;
    public static final long ONMOUSEUP   = 0x10000L;
    public static final long ONMOUSEOVER = 0x20000L;
    public static final long ONMOUSEMOVE = 0x40000L;
    public static final long ONMOUSEOUT  = 0x80000L;
    public static final long ONKEYPRESS  = 0x100000L;
    public static final long ONKEYDOWN   = 0x200000L;
    public static final long ONKEYUP     = 0x400000L;
    
    //_FocusBlurProperties
    public static final long ONFOCUS     = 0x800000L;
    public static final long ONBLUR      = 0x1000000L;

    //_LabelProperty
    public static final long LABEL       = 0x2000000L;
    
    //_LinkProperties
    public static final long CHARSET     = 0x4000000L;
    public static final long COORDS      = 0x8000000L;
    public static final long HREFLANG    = 0x10000000L;
    public static final long REL         = 0x20000000L;
    public static final long REV         = 0x40000000L;
    public static final long SHAPE       = 0x80000000L;
    public static final long TARGET      = 0x100000000L;
    public static final long TYPE        = 0x200000000L;

    //_TabindexProperty
    public static final long TABINDEX    = 0x400000000L;
    
    //Common to input fields
    public static final long ALIGN       = 0x800000000L;
    public static final long CHECKED     = 0x1000000000L;
    public static final long MAXLENGTH   = 0x2000000000L;
    public static final long SIZE        = 0x4000000000L;
    
    public static final long ROLE        = 0x8000000000L;
    
    //HTML5
    public static final long ONINPUT        = 0x1000000000L;
    public static final long ONINVALID      = 0x2000000000L;
    public static final long ONRESET        = 0x4000000000L;
    
    public static final long ONCONTEXTMENU  = 0x8000000000L;
    
    public static final long ONSUBMIT       = 0x10000000000L;

    public static final long ONWHEEL        = 0x20000000000L;
    
    public static final long ONCOPY         = 0x40000000000L;
    public static final long ONCUT          = 0x80000000000L;
    public static final long ONPASTE        = 0x100000000000L;
    
    public static final long ONDRAG         = 0x200000000000L;
    public static final long ONDRAGEND      = 0x400000000000L;
    public static final long ONDRAGENTER    = 0x800000000000L;
    public static final long ONDRAGLEAVE    = 0x1000000000000L;
    public static final long ONDRAGOVER     = 0x2000000000000L;
    public static final long ONDRAGSTART    = 0x4000000000000L;
    public static final long ONDROP         = 0x8000000000000L;
    public static final long ONSCROLL       = 0x10000000000000L;

    public static final Map<String, Long> COMMONERTIES_KEY_BY_NAME = new HashMap<String, Long>(64,1);
    
    static
    {
        COMMONERTIES_KEY_BY_NAME.put("style",      STYLE);
        
        COMMONERTIES_KEY_BY_NAME.put("styleClass", STYLECLASS);
        
        //_UniversalProperties
        //_TitleProperty
        COMMONERTIES_KEY_BY_NAME.put("dir",        DIR);
        COMMONERTIES_KEY_BY_NAME.put("lang",       LANG);
        COMMONERTIES_KEY_BY_NAME.put("title",      TITLE);
        
        //_EscapeProperty
        COMMONERTIES_KEY_BY_NAME.put("escape",     ESCAPE);

        //_DisabledClassEnabledClassProperties
        //_DisabledReadonlyProperties
        COMMONERTIES_KEY_BY_NAME.put("disabled",   DISABLED);
        COMMONERTIES_KEY_BY_NAME.put("enabled",    ENABLED);
        COMMONERTIES_KEY_BY_NAME.put("readonly",   READONLY);

        //_AccesskeyProperty
        COMMONERTIES_KEY_BY_NAME.put("accesskey",  ACCESSKEY);
        
        //_AltProperty
        COMMONERTIES_KEY_BY_NAME.put("alt",        ALT);
        
        //_ChangeSelectProperties
        COMMONERTIES_KEY_BY_NAME.put("onchange",   ONCHANGE);
        COMMONERTIES_KEY_BY_NAME.put("onselect",   ONSELECT);
        
        //_EventProperties
        COMMONERTIES_KEY_BY_NAME.put("onclick",    ONCLICK);
        COMMONERTIES_KEY_BY_NAME.put("ondblclick", ONDBLCLICK);
        COMMONERTIES_KEY_BY_NAME.put("onmousedown",ONMOUSEDOWN);
        COMMONERTIES_KEY_BY_NAME.put("onmouseup",  ONMOUSEUP);
        COMMONERTIES_KEY_BY_NAME.put("onmouseover",ONMOUSEOVER);
        COMMONERTIES_KEY_BY_NAME.put("onmousemove",ONMOUSEMOVE);
        COMMONERTIES_KEY_BY_NAME.put("onmouseout", ONMOUSEOUT);
        COMMONERTIES_KEY_BY_NAME.put("onkeypress", ONKEYPRESS);
        COMMONERTIES_KEY_BY_NAME.put("onkeydown",  ONKEYDOWN);
        COMMONERTIES_KEY_BY_NAME.put("onkeyup",    ONKEYUP);
        
        //_FocusBlurProperties
        COMMONERTIES_KEY_BY_NAME.put("onfocus",    ONFOCUS);
        COMMONERTIES_KEY_BY_NAME.put("onblur",     ONBLUR);

        //_LabelProperty
        COMMONERTIES_KEY_BY_NAME.put("label",      LABEL);
        
        //_LinkProperties
        COMMONERTIES_KEY_BY_NAME.put("charset",    CHARSET);
        COMMONERTIES_KEY_BY_NAME.put("coords",     COORDS);
        COMMONERTIES_KEY_BY_NAME.put("hreflang",   HREFLANG);
        COMMONERTIES_KEY_BY_NAME.put("rel",        REL);
        COMMONERTIES_KEY_BY_NAME.put("rev",        REV);
        COMMONERTIES_KEY_BY_NAME.put("shape",      SHAPE);
        COMMONERTIES_KEY_BY_NAME.put("target",     TARGET);
        COMMONERTIES_KEY_BY_NAME.put("type",       TYPE);

        //_TabindexProperty
        COMMONERTIES_KEY_BY_NAME.put("tabindex",   TABINDEX);

        //Common to input fields
        COMMONERTIES_KEY_BY_NAME.put("align",      ALIGN);
        COMMONERTIES_KEY_BY_NAME.put("checked",    CHECKED);
        COMMONERTIES_KEY_BY_NAME.put("maxlength",  MAXLENGTH);
        COMMONERTIES_KEY_BY_NAME.put("size",       SIZE);
        
        // HTML5 role
        COMMONERTIES_KEY_BY_NAME.put("role",   ROLE);

        // HTML 5
        COMMONERTIES_KEY_BY_NAME.put("oninput",       ONINPUT);  
        COMMONERTIES_KEY_BY_NAME.put("oninvalid",     ONINVALID);  
        COMMONERTIES_KEY_BY_NAME.put("onreset",       ONRESET);  

        COMMONERTIES_KEY_BY_NAME.put("oncontextmenu", ONCONTEXTMENU);  

        COMMONERTIES_KEY_BY_NAME.put("onsubmit",      ONSUBMIT);
        
        COMMONERTIES_KEY_BY_NAME.put("onwheel",       ONWHEEL);
        
        COMMONERTIES_KEY_BY_NAME.put("oncopy",        ONCOPY);  
        COMMONERTIES_KEY_BY_NAME.put("oncut",         ONCUT);  
        COMMONERTIES_KEY_BY_NAME.put("onpaste",       ONPASTE);
        
        COMMONERTIES_KEY_BY_NAME.put("ondrag",        ONDRAG); 
        COMMONERTIES_KEY_BY_NAME.put("ondragend",     ONDRAGEND); 
        COMMONERTIES_KEY_BY_NAME.put("ondragenter",   ONDRAGENTER); 
        COMMONERTIES_KEY_BY_NAME.put("ondragleave",   ONDRAGLEAVE); 
        COMMONERTIES_KEY_BY_NAME.put("ondragover",    ONDRAGOVER); 
        COMMONERTIES_KEY_BY_NAME.put("ondragstart",   ONDRAGSTART); 
        COMMONERTIES_KEY_BY_NAME.put("ondrop",        ONDROP); 
        COMMONERTIES_KEY_BY_NAME.put("onscroll",      ONSCROLL);         
    }

    public static void markAttribute(UIComponent component, String name)
    {
        Long propertyConstant = COMMONERTIES_KEY_BY_NAME.get(name);
        if (propertyConstant == null)
        {
            return;
        }
        Long commonPropertiesSet = (Long) component.getAttributes().get(ATTRIBUTES_MARKED);
        if (commonPropertiesSet == null)
        {
            commonPropertiesSet = 0L;
        }
        component.getAttributes().put(ATTRIBUTES_MARKED, commonPropertiesSet | propertyConstant);
    }
    
    public static void markAttribute(UIComponent component, long propertyConstant)
    {
        Long commonPropertiesSet = (Long) component.getAttributes().get(ATTRIBUTES_MARKED);
        if (commonPropertiesSet == null)
        {
            commonPropertiesSet = 0L;
        }
        component.getAttributes().put(ATTRIBUTES_MARKED, commonPropertiesSet | propertyConstant);
    }
    
    public static long getMarkedAttributes(UIComponent component)
    {
        Long commonProperties = (Long) component.getAttributes().get(ATTRIBUTES_MARKED);
        if (commonProperties == null)
        {
            commonProperties = 0L;
        }
        return commonProperties;
    }
}
