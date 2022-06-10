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
package org.apache.myfaces.renderkit.html.base;

import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlPanelGrid;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.util.lang.ArrayUtils;
import org.apache.myfaces.util.lang.StringUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

public class HtmlGridRendererBase extends HtmlRenderer
{
    private static final Logger log = Logger.getLogger(HtmlGridRendererBase.class.getName());
    
    private static final Integer[] ZERO_INT_ARRAY = new Integer[]{0};

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        // Check for npe
        super.decode(context, component);
        
        ClientBehaviorRendererUtils.decodeClientBehaviors(context, component);
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        // all work done in encodeEnd()
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException
    {
        // all work done in encodeEnd()
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, UIPanel.class);

        int columns;
        if (component instanceof HtmlPanelGrid)
        {
            columns = ((HtmlPanelGrid)component).getColumns();
        }
        else
        {
            Integer i = (Integer)component.getAttributes().get(ComponentAttrs.COLUMNS_ATTR);
            columns = i != null ? i : 0;
        }

        if (columns <= 0)
        {
            if (log.isLoggable(Level.SEVERE))
            {
                log.severe("Wrong columns attribute for PanelGrid " + 
                        component.getClientId(facesContext) + ": " + columns);
            }
            columns = 1;
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
        }

        if (behaviors != null && !behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
        }
        
        writer.startElement(HTML.TABLE_ELEM, component);
        
        if (component instanceof ClientBehaviorHolder)
        {
            if (!behaviors.isEmpty())
            {
                HtmlRendererUtils.writeIdAndName(writer, component, facesContext);
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
            }
            long commonPropertiesMarked = 0L;
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
            }
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderEventProperties(writer, 
                        commonPropertiesMarked, component);
            }
            else
            {
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                           commonPropertiesMarked,
                           CommonHtmlEventsUtil.getMarkedEvents(component), component, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, component, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.TABLE_ATTRIBUTES);
                CommonHtmlAttributesUtil.renderCommonPassthroughPropertiesWithoutEvents(writer, 
                        commonPropertiesMarked, component);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                        HTML.TABLE_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
            }
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.TABLE_ATTRIBUTES);
                CommonHtmlAttributesUtil.renderCommonPassthroughProperties(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);
            }
        }

        writer.flush();

        HtmlRendererUtils.renderTableCaption(facesContext, writer, component);

        // theader and tfooter are rendered before the tbody
        renderHeaderOrFooter(facesContext, writer, component, columns, true);   //Header facet
        renderHeaderOrFooter(facesContext, writer, component, columns, false);  //Footer facet
        
        renderChildren(facesContext, writer, component, columns);

        writer.endElement(HTML.TABLE_ELEM);
    }

    

    protected void renderHeaderOrFooter(FacesContext context,
                                      ResponseWriter writer,
                                      UIComponent component,
                                      int columns,
                                      boolean header)
        throws IOException
    {
        UIComponent facet = component.getFacet(header ? "header" : "footer");
        if (facet == null)
        {
            return;
        }

        writer.startElement(header ? HTML.THEAD_ELEM : HTML.TFOOT_ELEM, null);
                // component);
        writer.startElement(HTML.TR_ELEM, null); // component);
        writer.startElement(header ? HTML.TH_ELEM : HTML.TD_ELEM, null); // component);

        String styleClass = (component instanceof HtmlPanelGrid)
            ? (header
                ? ((HtmlPanelGrid)component).getHeaderClass()
                : ((HtmlPanelGrid)component).getFooterClass())
            : (header
                ? (String)component.getAttributes().get(ComponentAttrs.HEADER_CLASS_ATTR)
                : (String)component.getAttributes().get(ComponentAttrs.FOOTER_CLASS_ATTR));
        if (styleClass != null)
        {
            writer.writeAttribute(HTML.CLASS_ATTR, styleClass,
                                  header 
                                          ? ComponentAttrs.HEADER_CLASS_ATTR
                                          : ComponentAttrs.FOOTER_CLASS_ATTR);
        }

        if (header)
        {
            writer.writeAttribute(HTML.SCOPE_ATTR, HTML.SCOPE_COLGROUP_VALUE, null);
        }

        writer.writeAttribute(HTML.COLSPAN_ATTR, Integer.toString(columns), null);

        //RendererUtils.renderChild(context, facet);
        facet.encodeAll(context);

        writer.endElement(header ? HTML.TH_ELEM : HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEM);
        writer.endElement(header ? HTML.THEAD_ELEM : HTML.TFOOT_ELEM);
    }

    protected int childAttributes(FacesContext context,
            ResponseWriter writer,
            UIComponent component,
            int columnIndex)
        throws IOException
    {
        // subclasses can override this method to add attributes to the table cell <td> tag
        return columnIndex;
    }

    protected void renderChildren(FacesContext context,
                                ResponseWriter writer,
                                UIComponent component,
                                int columns)
        throws IOException
    {
        String columnClasses;
        String rowClasses;
        if (component instanceof HtmlPanelGrid)
        {
            columnClasses = ((HtmlPanelGrid)component).getColumnClasses();
            rowClasses = ((HtmlPanelGrid)component).getRowClasses();
        }
        else
        {
            columnClasses = (String)component.getAttributes().get(ComponentAttrs.COLUMN_CLASSES_ATTR);
            rowClasses = (String)component.getAttributes().get(ComponentAttrs.ROW_CLASSES_ATTR);
        }

        String[] columnClassesArray = (columnClasses == null)
            ? ArrayUtils.EMPTY_STRING_ARRAY
            : StringUtils.trim(StringUtils.splitShortString(columnClasses, ','));
        int columnClassesCount = columnClassesArray.length;

        String[] rowClassesArray = (rowClasses == null)
            ? org.apache.myfaces.util.lang.ArrayUtils.EMPTY_STRING_ARRAY
            : StringUtils.trim(StringUtils.splitShortString(rowClasses, ','));
        int rowClassesCount = rowClassesArray.length;

        int childCount = getChildCount(component);
        if (childCount > 0)
        {
            // get the row indizes for which a new TBODY element should be created
            Integer[] bodyrows = null;
            String bodyrowsAttr = (String) component.getAttributes().get(ComponentAttrs.BODYROWS_ATTR);
            if(bodyrowsAttr != null && !bodyrowsAttr.isEmpty())
            {   
                String[] bodyrowsString = StringUtils.trim(StringUtils.splitShortString(bodyrowsAttr, ','));
                // parsing with no exception handling, because of Faces-spec: 
                // "If present, this must be a comma separated list of integers."
                bodyrows = new Integer[bodyrowsString.length];
                for(int i = 0; i < bodyrowsString.length; i++) 
                {
                    bodyrows[i] = Integer.valueOf(bodyrowsString[i]);
                }
                
            }
            else
            {
                bodyrows = ZERO_INT_ARRAY;
            }
            int bodyrowsCount = 0;
            int rowIndex = -1;
            int columnIndex = 0;
            int rowClassIndex = 0;
            boolean rowStarted = false;
            for (int i = 0, size = component.getChildCount(); i < size; i++)
            {
                UIComponent child = component.getChildren().get(i);
                if (child.isRendered())
                {
                    if (columnIndex == 0)
                    {
                        rowIndex++;
                        
                        if (rowStarted)
                        {
                            //do we have to close the last row?
                            writer.endElement(HTML.TR_ELEM);
                        }
                        
                        // is the current row listed in the bodyrows attribute
                        if (ArrayUtils.contains(bodyrows, rowIndex)) 
                        {
                            // close any preopened TBODY element first
                            if(bodyrowsCount != 0) 
                            {
                                writer.endElement(HTML.TBODY_ELEM);
                            }
                            writer.startElement(HTML.TBODY_ELEM, null); // component); 
                            bodyrowsCount++;
                        }
                        
                        //start of new/next row
                        writer.startElement(HTML.TR_ELEM, null); // component);
                        String rowClass = null;
                        if (component instanceof HtmlPanelGrid)
                        {
                            rowClass = ((HtmlPanelGrid) component).getRowClass();
                        }
                        if (rowClassIndex < rowClassesCount) 
                        {
                            if (rowClass == null) 
                            {
                                rowClass = rowClassesArray[rowClassIndex];
                            }
                            else
                            {
                                rowClass = rowClass + ' ' + rowClassesArray[rowClassIndex];
                            }
                        }
                        if (rowClass != null)
                        {
                            writer.writeAttribute(HTML.CLASS_ATTR, rowClass, null);
                        }
                        rowStarted = true;
                        rowClassIndex++;
                        if (rowClassIndex == rowClassesCount)
                        {
                            rowClassIndex = 0;
                        }
                    }

                    writer.startElement(HTML.TD_ELEM, null); // component);
                    if (columnIndex < columnClassesCount)
                    {
                        writer.writeAttribute(HTML.CLASS_ATTR, columnClassesArray[columnIndex], null);
                    }
                    columnIndex = childAttributes(context, writer, child, columnIndex);

                    child.encodeAll(context);
                    writer.endElement(HTML.TD_ELEM);

                    columnIndex++;
                    if (columnIndex >= columns)
                    {
                        columnIndex = 0;
                    }
                }
            }

            if (rowStarted)
            {
                if (columnIndex > 0)
                {
                    Level level = context.isProjectStage(ProjectStage.Production)
                            ? Level.FINE
                            : Level.WARNING;
                    if (log.isLoggable(level))
                    {
                        log.log(level, "PanelGrid " + ComponentUtils.getPathToComponent(component)
                                + " has not enough children. Child count should be a " 
                                + "multiple of the columns attribute.");
                    }

                    //Render empty columns, so that table is correct
                    for ( ; columnIndex < columns; columnIndex++)
                    {
                        writer.startElement(HTML.TD_ELEM, null); // component);
                        if (columnIndex < columnClassesCount)
                        {
                            writer.writeAttribute(HTML.CLASS_ATTR, columnClassesArray[columnIndex], null);
                        }
                        writer.endElement(HTML.TD_ELEM);
                    }
                }
                writer.endElement(HTML.TR_ELEM);
                
                // close any preopened TBODY element first
                if(bodyrowsCount != 0) 
                {
                    writer.endElement(HTML.TBODY_ELEM);
                }
            }
        }
    }

}
