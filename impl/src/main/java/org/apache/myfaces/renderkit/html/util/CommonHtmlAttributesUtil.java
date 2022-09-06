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
package org.apache.myfaces.renderkit.html.util;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.CommonHtmlAttributes;

public final class CommonHtmlAttributesUtil
{
    public static long getMarkedAttributes(UIComponent component)
    {
        return CommonHtmlAttributes.getMarkedAttributes(component);
    }

    public static boolean isIdRenderingNecessary(UIComponent component)
    {
        String id = component.getId();
        return id != null && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX);
    }

    public static void renderUniversalProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }

        if ((commonPropertiesMarked & CommonHtmlAttributes.DIR) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.DIR_ATTR, HTML.DIR_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.LANG) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.LANG_ATTR, HTML.LANG_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.TITLE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.TITLE_ATTR, HTML.TITLE_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ROLE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ROLE_ATTR, HTML.ROLE_ATTR);
        }
    }
    
    public static void renderUniversalPropertiesWithoutTitle(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.DIR) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.DIR_ATTR, HTML.DIR_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.LANG) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.LANG_ATTR, HTML.LANG_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ROLE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ROLE_ATTR, HTML.ROLE_ATTR);
        }
    }

    public static void renderStyleProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.STYLE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.STYLE_ATTR, HTML.STYLE_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.STYLECLASS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.STYLE_CLASS_ATTR, HTML.CLASS_ATTR);
        }
    }
    
    public static void renderStyleClassProperty(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.STYLECLASS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.STYLE_CLASS_ATTR, HTML.CLASS_ATTR);
        }
    }

    public static void renderEventProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONCLICK) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONCLICK_ATTR, HTML.ONCLICK_ATTR);
        }
        renderEventPropertiesWithoutOnclick(writer, commonPropertiesMarked, component);
    }
    
    public static void renderEventPropertiesWithoutOnclick(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONDBLCLICK) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONDBLCLICK_ATTR, HTML.ONDBLCLICK_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONMOUSEDOWN) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONMOUSEDOWN_ATTR, HTML.ONMOUSEDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONMOUSEUP) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONMOUSEUP_ATTR, HTML.ONMOUSEUP_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONMOUSEOVER) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONMOUSEOVER_ATTR, HTML.ONMOUSEOVER_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONMOUSEMOVE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONMOUSEMOVE_ATTR, HTML.ONMOUSEMOVE_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONMOUSEOUT) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONMOUSEOUT_ATTR, HTML.ONMOUSEOUT_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONKEYPRESS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONKEYPRESS_ATTR, HTML.ONKEYPRESS_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONKEYDOWN) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONKEYDOWN_ATTR, HTML.ONKEYDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONKEYUP) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONKEYUP_ATTR, HTML.ONKEYUP_ATTR);
        }
    }
    
    
    public static void renderChangeSelectEventProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONCHANGE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONSELECT) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONSELECT_ATTR, HTML.ONSELECT_ATTR);
        }
    }
    
    public static void renderFocusBlurEventProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONFOCUS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONFOCUS_ATTR, HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONBLUR) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONBLUR_ATTR, HTML.ONBLUR_ATTR);
        }
    }
    
    public static void renderFieldEventPropertiesWithoutOnchangeAndOnselect(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONFOCUS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONFOCUS_ATTR, HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONBLUR) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONBLUR_ATTR, HTML.ONBLUR_ATTR);
        }
    }
    
    public static void renderFieldEventPropertiesWithoutOnchange(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONFOCUS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONFOCUS_ATTR, HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONBLUR) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONBLUR_ATTR, HTML.ONBLUR_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONSELECT) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONSELECT_ATTR, HTML.ONSELECT_ATTR);
        }
    }
    
    public static void renderChangeEventProperty(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ONCHANGE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR);
        }
    }
    
    public static void renderAccesskeyTabindexProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ACCESSKEY) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ACCESSKEY_ATTR, HTML.ACCESSKEY_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.TABINDEX) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.TABINDEX_ATTR, HTML.TABINDEX_ATTR);
        }
    }

    public static void renderAltAlignProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ALIGN) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ALIGN_ATTR, HTML.ALIGN_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ALT) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ALT_ATTR, HTML.ALT_ATTR);
        }
    }

    public static void renderInputProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonHtmlAttributes.ALIGN) != 0)
        {
            HtmlRendererUtils.renderHTMLAttribute(writer, component,
                    HTML.ALIGN_ATTR, HTML.ALIGN_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.ALT) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ALT_ATTR, HTML.ALT_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.CHECKED) != 0)
        {
            HtmlRendererUtils.renderHTMLAttribute(writer, component,
                    HTML.CHECKED_ATTR, HTML.CHECKED_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.MAXLENGTH) != 0)
        {
            HtmlRendererUtils.renderHTMLAttribute(writer, component,
                    HTML.MAXLENGTH_ATTR, HTML.MAXLENGTH_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.READONLY) != 0)
        {
            HtmlRendererUtils.renderHTMLAttribute(writer, component,
                    HTML.READONLY_ATTR, HTML.READONLY_ATTR);
        }
        if ((commonPropertiesMarked & CommonHtmlAttributes.SIZE) != 0)
        {
            HtmlRendererUtils.renderHTMLAttribute(writer, component,
                    HTML.SIZE_ATTR, HTML.SIZE_ATTR);
        }        
    }
    
    public static void renderAnchorProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        if ((commonPropertiesMarked & CommonHtmlAttributes.CHARSET) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.CHARSET_ATTR, HTML.CHARSET_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.COORDS) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.COORDS_ATTR, HTML.COORDS_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.HREFLANG) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.HREFLANG_ATTR, HTML.HREFLANG_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.REL) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.REL_ATTR, HTML.REL_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.REV) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.REV_ATTR, HTML.REV_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.SHAPE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.SHAPE_ATTR, HTML.SHAPE_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.TARGET) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.TARGET_ATTR, HTML.TARGET_ATTR);
        }        
        if ((commonPropertiesMarked & CommonHtmlAttributes.TYPE) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.TYPE_ATTR, HTML.TYPE_ATTR);
        }        
    }

    public static void renderCommonPassthroughPropertiesWithoutEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderStyleProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
    }    
    
    public static void renderCommonPassthroughProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderStyleProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
        renderEventProperties(writer, commonPropertiesMarked, component);
    }

    //Methods 
    public static void renderCommonFieldEventProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderChangeSelectEventProperties(writer, commonPropertiesMarked, component);
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
    }

    public static void renderCommonFieldPassthroughPropertiesWithoutDisabled(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        renderCommonFieldEventProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderCommonFieldPassthroughPropertiesWithoutDisabledAndEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderCommonPassthroughPropertiesWithoutEvents(writer, commonPropertiesMarked, component);
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderInputPassthroughPropertiesWithoutDisabled(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderInputProperties(writer, commonPropertiesMarked, component);
        renderCommonFieldPassthroughPropertiesWithoutDisabled(writer, commonPropertiesMarked, component);
    }
    
    public static void renderInputPassthroughPropertiesWithoutDisabledAndEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderInputProperties(writer, commonPropertiesMarked, component);
        renderCommonFieldPassthroughPropertiesWithoutDisabledAndEvents(writer, commonPropertiesMarked, component);
    }

    public static void renderAnchorPassthroughProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAnchorProperties(writer, commonPropertiesMarked, component);
        renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderAnchorPassthroughPropertiesDisabled(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderAnchorPassthroughPropertiesWithoutEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAnchorProperties(writer, commonPropertiesMarked, component);
        renderStyleProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderAnchorPassthroughPropertiesDisabledWithoutEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        renderStyleProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
    }    
    
    public static void renderAnchorPassthroughPropertiesWithoutStyleAndEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAnchorProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderAnchorPassthroughPropertiesWithoutStyle(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAnchorProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
        renderEventProperties(writer, commonPropertiesMarked, component);
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
    }
    
    public static void renderAnchorPassthroughPropertiesWithoutOnclickAndStyle(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAnchorProperties(writer, commonPropertiesMarked, component);
        renderUniversalProperties(writer, commonPropertiesMarked, component);
        renderEventPropertiesWithoutOnclick(writer, commonPropertiesMarked, component);
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
    }

    public static void renderButtonPassthroughPropertiesWithoutDisabledAndEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderUniversalProperties(writer, commonPropertiesMarked, component);
        renderStyleProperties(writer, commonPropertiesMarked, component);
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        renderAltAlignProperties(writer, commonPropertiesMarked, component);
    }

    public static void renderLabelProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
        if ((commonPropertiesMarked & CommonHtmlAttributes.ACCESSKEY) != 0)
        {
            HtmlRendererUtils.renderHTMLStringAttribute(writer, component,
                    HTML.ACCESSKEY_ATTR, HTML.ACCESSKEY_ATTR);
        }
    }

    
    public static void renderLabelPassthroughProperties(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderLabelProperties(writer, commonPropertiesMarked, component);
        renderCommonPassthroughProperties(writer, commonPropertiesMarked, component);
    }

    public static void renderLabelPassthroughPropertiesWithoutEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component) throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderAccesskeyTabindexProperties(writer, commonPropertiesMarked, component);
        renderCommonPassthroughPropertiesWithoutEvents(writer, commonPropertiesMarked, component);
    }
    
    public static void renderSelectPassthroughPropertiesWithoutDisabled(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderCommonFieldPassthroughPropertiesWithoutDisabled(writer, commonPropertiesMarked, component);
    }
    
    public static void renderSelectPassthroughPropertiesWithoutDisabledAndEvents(ResponseWriter writer,
            long commonPropertiesMarked, UIComponent component)
            throws IOException
    {
        if (commonPropertiesMarked == 0)
        {
            return;
        }
        
        renderCommonFieldPassthroughPropertiesWithoutDisabledAndEvents(writer, commonPropertiesMarked, component);
    }
}
