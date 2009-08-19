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
package org.apache.myfaces.view.facelets.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.shared_impl.util.ClassUtils;

public final class DevTools
{
    private final static String TS = "&lt;";

    private static final String DEBUG_TEMPLATE = "META-INF/rsc/myfaces-dev-debug.xml";
    private static final String ERROR_TEMPLATE = "META-INF/rsc/myfaces-dev-error.xml";
    private final static String[] IGNORE = new String[] { "parent", "rendererType" };


    private static String[] _debugParts;
    private static String[] _errorParts;

    private DevTools()
    {
    }

    public static void debugHtml(Writer writer, FacesContext faces) throws IOException
    {
        _init();
        
        Date now = new Date();
        for (int i = 0; i < _debugParts.length; i++)
        {
            if ("message".equals(_debugParts[i]))
            {
                writer.write(faces.getViewRoot().getViewId());
            }
            else if ("now".equals(_debugParts[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(_debugParts[i]))
            {
                _writeComponent(writer, faces.getViewRoot());
            }
            else if ("vars".equals(_debugParts[i]))
            {
                _writeVariables(writer, faces);
            }
            else
            {
                writer.write(_debugParts[i]);
            }
        }
    }

    public static void debugHtml(Writer writer, FacesContext faces, Exception e) throws IOException
    {
        _init();
        
        Date now = new Date();
        for (String part : _errorParts)
        {
            if ("message".equals(part))
            {
                String msg = e.getMessage();
                if (msg != null)
                {
                    writer.write(msg.replaceAll("<", TS));
                }
                else
                {
                    writer.write(e.getClass().getName());
                }
            }
            else if ("trace".equals(part))
            {
                _writeException(writer, e);
            }
            else if ("now".equals(part))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(part))
            {
                _writeComponent(writer, faces.getViewRoot());
            }
            else if ("vars".equals(part))
            {
                _writeVariables(writer, faces);
            }
            else
            {
                writer.write(part);
            }
        }
    }

    private static String _getName(UIComponent c)
    {
        String nm = c.getClass().getName();
        return nm.substring(nm.lastIndexOf('.') + 1);
    }

    private static void _init() throws IOException
    {
        if (_errorParts == null)
        {
            _errorParts = _splitTemplate(ERROR_TEMPLATE);
        }

        if (_debugParts == null)
        {
            _debugParts = _splitTemplate(DEBUG_TEMPLATE);
        }
    }

    private static boolean _isText(UIComponent c)
    {
        return (c.getClass().getName().startsWith("org.apache.myfaces.view.facelets.compiler"));
    }

    private static String[] _splitTemplate(String rsc) throws IOException
    {
        InputStream is = ClassUtils.getContextClassLoader().getResourceAsStream(rsc);
        if (is == null)
        {
            throw new FileNotFoundException(rsc);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[512];
        int read;
        while ((read = is.read(buff)) != -1)
        {
            baos.write(buff, 0, read);
        }
        
        String str = baos.toString();
        
        return str.split("@@");
    }

    private static void _writeAttributes(Writer writer, UIComponent c)
    {
        try
        {
            BeanInfo info = Introspector.getBeanInfo(c.getClass());
            
            for (PropertyDescriptor descriptor : info.getPropertyDescriptors())
            {
                if (descriptor.getWriteMethod() != null && Arrays.binarySearch(IGNORE, descriptor.getName()) < 0)
                {
                    Method getter = descriptor.getReadMethod();
                    try
                    {
                        Object value = getter.invoke(c);
                        if (value != null)
                        {
                            if (value instanceof Collection || value instanceof Map || value instanceof Iterator)
                            {
                                continue;
                            }
                            
                            writer.write(" ");
                            writer.write(descriptor.getName());
                            writer.write("=\"");
                            
                            String str;
                            if (value instanceof Expression)
                            {
                                str = ((Expression) value).getExpressionString();
                            }
                            else
                            {
                                str = value.toString();
                            }
                            
                            writer.write(str.replaceAll("<", TS));
                            writer.write("\"");
                        }
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }
                }
            }

            ValueExpression binding = c.getValueExpression("binding");
            if (binding != null)
            {
                writer.write(" binding=\"");
                writer.write(binding.getExpressionString().replaceAll("<", TS));
                writer.write("\"");
            }
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    private static void _writeVariables(Writer writer, FacesContext faces) throws IOException
    {
        ExternalContext ctx = faces.getExternalContext();
        _writeVariables(writer, ctx.getRequestParameterMap(), "Request Parameters");
        _writeVariables(writer, ctx.getRequestMap(), "Request Attributes");
        if (ctx.getSession(false) != null)
        {
            _writeVariables(writer, ctx.getSessionMap(), "Session Attributes");
        }
        
        _writeVariables(writer, ctx.getApplicationMap(), "Application Attributes");
    }

    private static void _writeVariables(Writer writer, Map<String, ? extends Object> vars, String caption) throws IOException
    {
        writer.write("<table><caption>");
        writer.write(caption);
        writer
                .write("</caption><thead><tr><th style=\"width: 10%; \">Name</th><th style=\"width: 90%; \">Value</th></tr></thead><tbody>");
        boolean written = false;
        if (!vars.isEmpty())
        {
            SortedMap<String, Object> map = new TreeMap<String, Object>(vars);
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                if (key.indexOf('.') == -1)
                {
                    writer.write("<tr><td>");
                    writer.write(key.replaceAll("<", TS));
                    writer.write("</td><td>");
                    writer.write(entry.getValue() == null ? "null" : entry.getValue().toString().replaceAll("<", TS));
                    writer.write("</td></tr>");
                    written = true;
                }
            }
        }
        
        if (!written)
        {
            writer.write("<tr><td colspan=\"2\"><em>None</em></td></tr>");
        }
        
        writer.write("</tbody></table>");
    }

    private static void _writeComponent(Writer writer, UIComponent component) throws IOException
    {
        writer.write("<dl><dt");
        if (_isText(component))
        {
            writer.write(" class=\"uicText\"");
        }
        writer.write(">");

        Map<String, UIComponent> facets = component.getFacets();

        boolean hasChildren = component.getChildCount() > 0 || !facets.isEmpty();

        _writeStart(writer, component, hasChildren);
        writer.write("</dt>");
        if (hasChildren)
        {
            if (!facets.isEmpty())
            {
                for (Map.Entry<String, UIComponent> entry : facets.entrySet())
                {
                    writer.write("<dd class=\"uicFacet\">");
                    writer.write("<span>");
                    writer.write(entry.getKey());
                    writer.write("</span>");
                    _writeComponent(writer, entry.getValue());
                    writer.write("</dd>");
                }
            }
            
            if (component.getChildCount() > 0)
            {
                for (UIComponent child : component.getChildren())
                {
                    writer.write("<dd>");
                    _writeComponent(writer, child);
                    writer.write("</dd>");
                }
            }
            
            writer.write("<dt>");
            _writeEnd(writer, component);
            writer.write("</dt>");
        }
        
        writer.write("</dl>");
    }

    private static void _writeEnd(Writer writer, UIComponent c) throws IOException
    {
        if (!_isText(c))
        {
            writer.write(TS);
            writer.write('/');
            writer.write(_getName(c));
            writer.write('>');
        }
    }

    private static void _writeException(Writer writer, Exception e) throws IOException
    {
        StringWriter str = new StringWriter(256);
        PrintWriter pstr = new PrintWriter(str);
        e.printStackTrace(pstr);
        
        pstr.close();
        
        writer.write(str.toString().replaceAll("<", TS));
    }

    private static void _writeStart(Writer writer, UIComponent c, boolean children) throws IOException
    {
        if (_isText(c))
        {
            String str = c.toString().trim();
            writer.write(str.replaceAll("<", TS));
        }
        else
        {
            writer.write(TS);
            writer.write(_getName(c));
            _writeAttributes(writer, c);
            if (children)
            {
                writer.write('>');
            }
            else
            {
                writer.write("/>");
            }
        }
    }

}
