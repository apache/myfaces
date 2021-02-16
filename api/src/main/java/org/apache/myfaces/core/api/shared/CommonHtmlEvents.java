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

public class CommonHtmlEvents
{
    public static final String EVENTS_MARKED = "oam.EVENTS_MARKED";
    
    public static final long ACTION        = 0x1L;
    public static final long CLICK         = 0x2L;
    public static final long DBLCLICK      = 0x4L;
    public static final long MOUSEDOWN     = 0x8L;
    public static final long MOUSEUP       = 0x10L;
    public static final long MOUSEOVER     = 0x20L;
    public static final long MOUSEMOVE     = 0x40L;
    public static final long MOUSEOUT      = 0x80L;
    public static final long KEYPRESS      = 0x100L;
    public static final long KEYDOWN       = 0x200L;
    public static final long KEYUP         = 0x400L;
    public static final long FOCUS         = 0x800L;
    public static final long BLUR          = 0x1000L;
    public static final long SELECT        = 0x2000L;
    public static final long CHANGE        = 0x4000L;
    public static final long VALUECHANGE   = 0x8000L;
    public static final long LOAD          = 0x10000L;
    public static final long UNLOAD        = 0x20000L;
    public static final long INPUT         = 0x40000L;
    public static final long INVALID       = 0x80000L;
    public static final long RESET         = 0x100000L;
    public static final long CONTEXTMENU   = 0x200000L;
    public static final long SUBMIT        = 0x400000L;
    public static final long WHEEL         = 0x800000L;
    public static final long COPY          = 0x1000000L;
    public static final long CUT           = 0x2000000L;
    public static final long PASTE         = 0x4000000L;
    public static final long DRAG          = 0x8000000L;
    public static final long DRAGEND       = 0x10000000L;
    public static final long DRAGENTER     = 0x20000000L;
    public static final long DRAGLEAVE     = 0x40000000L;
    public static final long DRAGOVER      = 0x80000000L;
    public static final long DRAGSTART     = 0x100000000L;
    public static final long DROP          = 0x200000000L;
    public static final long SCROLL        = 0x400000000L;

    public static final Map<String, Long> COMMON_EVENTS_KEY_BY_NAME = new HashMap<String, Long>(24,1);
    
    static
    {
        //EVENTS
        COMMON_EVENTS_KEY_BY_NAME.put("change",      CHANGE);
        COMMON_EVENTS_KEY_BY_NAME.put("select",      SELECT);
        COMMON_EVENTS_KEY_BY_NAME.put("click",       CLICK);
        COMMON_EVENTS_KEY_BY_NAME.put("dblclick",    DBLCLICK);
        COMMON_EVENTS_KEY_BY_NAME.put("mousedown",   MOUSEDOWN);
        COMMON_EVENTS_KEY_BY_NAME.put("mouseup",     MOUSEUP);
        COMMON_EVENTS_KEY_BY_NAME.put("mouseover",   MOUSEOVER);
        COMMON_EVENTS_KEY_BY_NAME.put("mousemove",   MOUSEMOVE);
        COMMON_EVENTS_KEY_BY_NAME.put("mouseout",    MOUSEOUT);
        COMMON_EVENTS_KEY_BY_NAME.put("keypress",    KEYPRESS);
        COMMON_EVENTS_KEY_BY_NAME.put("keydown",     KEYDOWN);
        COMMON_EVENTS_KEY_BY_NAME.put("keyup",       KEYUP);
        COMMON_EVENTS_KEY_BY_NAME.put("focus",       FOCUS);
        COMMON_EVENTS_KEY_BY_NAME.put("blur",        BLUR);
        COMMON_EVENTS_KEY_BY_NAME.put("load",        LOAD);
        COMMON_EVENTS_KEY_BY_NAME.put("unload",      UNLOAD);
        COMMON_EVENTS_KEY_BY_NAME.put("input",       INPUT);
        COMMON_EVENTS_KEY_BY_NAME.put("invalid",     INVALID);
        COMMON_EVENTS_KEY_BY_NAME.put("reset",       RESET);
        COMMON_EVENTS_KEY_BY_NAME.put("contextmenu", CONTEXTMENU);
        COMMON_EVENTS_KEY_BY_NAME.put("submit",      SUBMIT);
        COMMON_EVENTS_KEY_BY_NAME.put("wheel",       WHEEL);
        COMMON_EVENTS_KEY_BY_NAME.put("copy",        COPY);
        COMMON_EVENTS_KEY_BY_NAME.put("cut",         CUT);
        COMMON_EVENTS_KEY_BY_NAME.put("paste",       PASTE);
        COMMON_EVENTS_KEY_BY_NAME.put("drag",        DRAG);
        COMMON_EVENTS_KEY_BY_NAME.put("dragend",     DRAGEND);
        COMMON_EVENTS_KEY_BY_NAME.put("dragenter",   DRAGENTER);
        COMMON_EVENTS_KEY_BY_NAME.put("dragleave",   DRAGLEAVE);
        COMMON_EVENTS_KEY_BY_NAME.put("dragover",    DRAGOVER);
        COMMON_EVENTS_KEY_BY_NAME.put("dragstart",   DRAGSTART);
        COMMON_EVENTS_KEY_BY_NAME.put("drop",        DROP);
        COMMON_EVENTS_KEY_BY_NAME.put("scroll",    SCROLL);
        
        //virtual
        COMMON_EVENTS_KEY_BY_NAME.put("valueChange", VALUECHANGE);
        COMMON_EVENTS_KEY_BY_NAME.put("action", ACTION);
    }
    
    public static void markEvent(UIComponent component, String name)
    {
        Long propertyConstant = COMMON_EVENTS_KEY_BY_NAME.get(name);
        if (propertyConstant == null)
        {
            return;
        }
        Long commonPropertiesSet = (Long) component.getAttributes().get(EVENTS_MARKED);
        if (commonPropertiesSet == null)
        {
            commonPropertiesSet = 0L;
        }
        component.getAttributes().put(EVENTS_MARKED, commonPropertiesSet | propertyConstant);
    }

    public static long getMarkedEvents(UIComponent component)
    {
        Long commonEvents = (Long) component.getAttributes().get(CommonHtmlEvents.EVENTS_MARKED);
        if (commonEvents == null)
        {
            commonEvents = 0L;
        }
        return commonEvents;
    }
}
