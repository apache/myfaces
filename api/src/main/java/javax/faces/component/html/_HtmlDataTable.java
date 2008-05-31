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
package javax.faces.component.html;

import java.io.IOException;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;

/**
 * This component renders as an HTML table element.  It has as its
 * children h:column entities, which describe the columns of the table.
 * It can be decorated with facets named "header" and "footer" to
 * specify header and footer rows.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions. 
 * <p>
 * Extend standard UIData component to add support for html-specific features
 * such as CSS style attributes and event handler scripts.
 * <p>
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * </p>
 * 
 * @JSFComponent
 *   name = "h:dataTable"
 *   class = "javax.faces.component.html.HtmlDataTable"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlDataTableTag"
 *   tagSuperclass = "javax.faces.webapp.UIComponentBodyTag"
 *   desc = "h:dataTable"
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlDataTable extends UIData implements _EventProperties,
    _StyleProperties, _UniversalProperties 
{
    /**
     * @see javax.faces.component.UIData#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException
    {
        // Ensure that the "current row" is set to "no row", so that the
        // correct clientId is set for this component etc. User code may
        // have left this in some other state before rendering began...
        setRowIndex(-1);

        // Now invoke the superclass encodeBegin, which will eventually
        // execute the encodeBegin for the associated renderer.
        super.encodeBegin(context);
    }
    
    public static final String COMPONENT_TYPE = "javax.faces.HtmlDataTable";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Table";
    
    /**
     * HTML: The background color of this element.
     * 
     * @JSFProperty
     */
    public abstract String getBgcolor();

    /**
     * HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     *   defaultValue="Integer.MIN_VALUE"
     */
    public abstract int getBorder();

    /**
     * HTML: Specifies the amount of empty space between the cell border and
     * its contents.  It can be either a pixel length or a percentage.
     * 
     * @JSFProperty
     */
    public abstract String getCellpadding();

    /**
     * HTML: Specifies the amount of space between the cells of the table.
     * It can be either a pixel length or a percentage of available 
     * space.
     * 
     * @JSFProperty
     */
    public abstract String getCellspacing();

    /**
     * A comma separated list of CSS class names to apply to td elements in
     * each column.
     * 
     * @JSFProperty
     */
    public abstract String getColumnClasses();

    /**
     * The CSS class to be applied to footer cells.
     * 
     * @JSFProperty
     */
    public abstract String getFooterClass();

    /**
     * HTML: Controls what part of the frame that surrounds a table is 
     * visible.  Values include:  void, above, below, hsides, lhs, 
     * rhs, vsides, box, and border.
     * 
     * @JSFProperty
     */
    public abstract String getFrame();

    /**
     * The CSS class to be applied to header cells.
     * 
     * @JSFProperty
     */
    public abstract String getHeaderClass();

    /**
     * A comma separated list of CSS class names to apply to td elements in
     * each row.
     * 
     * @JSFProperty
     */
    public abstract String getRowClasses();

    /**
     * HTML: Controls how rules are rendered between cells.  Values include:
     * none, groups, rows, cols, and all.
     * 
     * @JSFProperty
     */
    public abstract String getRules();

    /**
     * HTML: Provides a summary of the contents of the table, for
     * accessibility purposes.
     * 
     * @JSFProperty
     */
    public abstract String getSummary();

    /**
     * HTML: Specifies the desired width of the table, as a pixel length or
     * a percentage of available space.
     * 
     * @JSFProperty
     */
    public abstract String getWidth();
    
}
