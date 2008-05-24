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



/**
 *
 * This tag is commonly used as a child of the dataTable tag, to
 * represent a column of data. It can be decorated with "header" and
 * "footer" facets to drive the output of header and footer rows.
 * Row values are specified via its children.
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:column"
 *   type = "javax.faces.Column"
 *   family = "javax.faces.Column"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlColumnTag"
 *   tagSuperclass = "javax.faces.webapp.UIComponentBodyTag"
 *   desc = "UIData"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIColumn
        extends UIComponentBase
{
    private static final String FOOTER_FACET_NAME = "footer";
    private static final String HEADER_FACET_NAME = "header";
    
    public void setFooter(UIComponent footer)
    {
        getFacets().put(FOOTER_FACET_NAME, footer);
    }

    public UIComponent getFooter()
    {
        return (UIComponent)getFacets().get(FOOTER_FACET_NAME);
    }

    public void setHeader(UIComponent header)
    {
        getFacets().put(HEADER_FACET_NAME, header);
    }

    public UIComponent getHeader()
    {
        return (UIComponent)getFacets().get(HEADER_FACET_NAME);
    }


    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Column";
    public static final String COMPONENT_FAMILY = "javax.faces.Column";


    public UIColumn()
    {
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    //------------------ GENERATED CODE END ---------------------------------------
}
