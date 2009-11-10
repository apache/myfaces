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
package javax.faces.webapp;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.component.UIComponent;
import javax.el.Expression;
import javax.el.ValueExpression;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jacob Hookom (ICLA with ASF filed)
 */
final class _ErrorPageWriter
{

    //private static final Log log = LogFactory.getLog(_ErrorPageWriter.class);
    private static final Logger log = Logger.getLogger(_ErrorPageWriter.class.getName());

    private final static String TS = "&lt;";

    private static final String ERROR_TEMPLATE = "META-INF/rsc/myfaces-dev-error.xml";

    @JSFWebConfigParam(defaultValue="META-INF/rsc/myfaces-dev-error.xml", since="1.2.4")
    private static final String ERROR_TEMPLATE_RESOURCE = "org.apache.myfaces.ERROR_TEMPLATE_RESOURCE";

    private static String[] ERROR_PARTS;

    private static final String DEBUG_TEMPLATE = "META-INF/rsc/myfaces-dev-debug.xml";

    @JSFWebConfigParam(defaultValue="META-INF/rsc/myfaces-dev-debug.xml", since="1.2.4")
    private static final String DEBUG_TEMPLATE_RESOURCE = "org.apache.myfaces.DEBUG_TEMPLATE_RESOURCE";

    private static String[] DEBUG_PARTS;
    
    private static final String REGEX_PATTERN = ".*?\\Q,Id:\\E\\s*(\\S+)\\s*\\].*?";

    public _ErrorPageWriter()
    {
        super();
    }

    private static String getErrorTemplate(FacesContext context)
    {
        String errorTemplate = context.getExternalContext().getInitParameter(ERROR_TEMPLATE_RESOURCE);
        if (errorTemplate != null)
        {
            return errorTemplate;
        }
        return ERROR_TEMPLATE;
    }

    private static String getDebugTemplate(FacesContext context)
    {
        String debugTemplate = context.getExternalContext().getInitParameter(DEBUG_TEMPLATE_RESOURCE);
        if (debugTemplate != null)
        {
            return debugTemplate;
        }
        return DEBUG_TEMPLATE;
    }

    private static void init(FacesContext context) throws IOException
    {
        if (ERROR_PARTS == null)
        {
            ERROR_PARTS = splitTemplate(getErrorTemplate(context));
        }

        if (DEBUG_PARTS == null)
        {
            DEBUG_PARTS = splitTemplate(getDebugTemplate(context));
        }
    }

    private static String[] splitTemplate(String rsc) throws IOException
    {
        InputStream is = null;

        if(System.getSecurityManager()!=null){
            try{
                Object cl = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws PrivilegedActionException {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
                is = ((ClassLoader)cl).getResourceAsStream(rsc);
            }catch(PrivilegedActionException pae){
                throw new FacesException(pae);
            }
        }else{
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(rsc);
        }

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

    private static List<String> getErrorId(Throwable e)
    {
        String message = e.getMessage();

        if (message == null)
            return null;

        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(REGEX_PATTERN);
        Matcher matcher = pattern.matcher(message);

        while (matcher.find())
        {
            list.add(matcher.group(1));
        }
        if (list.size() > 0)
            return list;
        return null;
    }

    public static void writeCause(Writer writer, Throwable ex) throws IOException
    {
        String msg = ex.getMessage();
        while (ex.getCause() != null)
        {
            ex = ex.getCause();
            if (ex.getMessage() != null)
                msg = ex.getMessage();
        }

        if (msg != null)
        {
            msg = ex.getClass().getName() + " - " + msg;
            writer.write(msg.replaceAll("<", TS));
        }
        else
        {
            writer.write(ex.getClass().getName());
        }
    }

    public static void debugHtml(Writer writer, FacesContext faces, Throwable e) throws IOException
    {
        init(faces);
        Date now = new Date();
        for (int i = 0; i < ERROR_PARTS.length; i++)
        {
            if ("message".equals(ERROR_PARTS[i]))
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
            else if ("trace".equals(ERROR_PARTS[i]))
            {
                writeException(writer, e);
            }
            else if ("now".equals(ERROR_PARTS[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(ERROR_PARTS[i]))
            {
                if (faces.getViewRoot() != null)
                {
                    writeComponent(writer, faces.getViewRoot(), getErrorId(e));
                }
            }
            else if ("vars".equals(ERROR_PARTS[i]))
            {
                writeVariables(writer, faces);
            }
            else if ("cause".equals(ERROR_PARTS[i]))
            {
                writeCause(writer, e);
            }
            else
            {
                writer.write(ERROR_PARTS[i]);
            }
        }
    }

    public static void debugHtml(Writer writer, FacesContext faces, List<Exception> exceptionList) throws IOException
    {
        init(faces);
        Date now = new Date();
        for (int i = 0; i < ERROR_PARTS.length; i++)
        {
            if ("message".equals(ERROR_PARTS[i]))
            {
                for (int j = 0; j < exceptionList.size(); j++)
                {
                    Exception e = exceptionList.get(j);
                    String msg = e.getMessage();
                    if (msg != null)
                    {
                        writer.write(msg.replaceAll("<", TS));
                    }
                    else
                    {
                        writer.write(e.getClass().getName());
                    }
                    if (!(j + 1 == exceptionList.size()))
                    {
                        writer.write("<br>");
                    }
                }
            }
            else if ("trace".equals(ERROR_PARTS[i]))
            {
                for (int j = 0; j < exceptionList.size(); j++)
                {
                    Exception e = exceptionList.get(j);
                    writeException(writer, e);
                }
            }
            else if ("now".equals(ERROR_PARTS[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(ERROR_PARTS[i]))
            {
                if (faces.getViewRoot() != null)
                {
                    List<String> highlightId = null;
                    for (int j = 0; j < exceptionList.size(); j++)
                    {
                        Exception e = exceptionList.get(j);
                        if (highlightId == null)
                        {
                            highlightId = getErrorId(e);
                        }
                        else
                        {
                            highlightId.addAll(getErrorId(e));
                        }
                    }
                    writeComponent(writer, faces.getViewRoot(), highlightId);
                }
            }
            else if ("vars".equals(ERROR_PARTS[i]))
            {
                writeVariables(writer, faces);
            }
            else if ("cause".equals(ERROR_PARTS[i]))
            {
                for (int j = 0; j < exceptionList.size(); j++)
                {
                    Exception e = exceptionList.get(j);
                    writeCause(writer, e);
                    if (!(j + 1 == exceptionList.size()))
                    {
                        writer.write("<br>");
                    }
                }
            }
            else
            {
                writer.write(ERROR_PARTS[i]);
            }
        }
    }

    private static void writeException(Writer writer, Throwable e) throws IOException
    {
        StringWriter str = new StringWriter(256);
        PrintWriter pstr = new PrintWriter(str);
        e.printStackTrace(pstr);
        pstr.close();
        writer.write(str.toString().replaceAll("<", TS));
    }

    public static void debugHtml(Writer writer, FacesContext faces) throws IOException
    {
        init(faces);
        Date now = new Date();
        for (int i = 0; i < DEBUG_PARTS.length; i++)
        {
            if ("message".equals(DEBUG_PARTS[i]))
            {
                writer.write(faces.getViewRoot().getViewId());
            }
            else if ("now".equals(DEBUG_PARTS[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(DEBUG_PARTS[i]))
            {
                writeComponent(writer, faces.getViewRoot(), null);
            }
            else if ("vars".equals(DEBUG_PARTS[i]))
            {
                writeVariables(writer, faces);
            }
            else
            {
                writer.write(DEBUG_PARTS[i]);
            }
        }
    }

    private static void writeVariables(Writer writer, FacesContext faces) throws IOException
    {
        ExternalContext ctx = faces.getExternalContext();
        writeVariables(writer, ctx.getRequestParameterMap(), "Request Parameters");
        writeVariables(writer, ctx.getRequestMap(), "Request Attributes");
        if (ctx.getSession(false) != null)
        {
            writeVariables(writer, ctx.getSessionMap(), "Session Attributes");
        }
        writeVariables(writer, ctx.getApplicationMap(), "Application Attributes");
    }

    private static void writeVariables(Writer writer, Map<String, ? extends Object> vars, String caption) throws IOException
    {
        writer.write("<table><caption>");
        writer.write(caption);
        writer
              .write("</caption><thead><tr><th style=\"width: 10%; \">Name</th><th style=\"width: 90%; \">Value</th></tr></thead><tbody>");
        boolean written = false;
        if (!vars.isEmpty())
        {
            SortedMap<String, Object> sortedMap = new TreeMap<String, Object>(vars);
            for (Map.Entry<String, Object> entry : sortedMap.entrySet())
            {
                String key = entry.getKey().toString();
                if (key.indexOf('.') == -1)
                {
                    writer.write("<tr><td>");
                    writer.write(key.replaceAll("<", TS));
                    writer.write("</td><td>");
                    writer.write(entry.getValue().toString().replaceAll("<", TS));
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

    private static void writeComponent(Writer writer, UIComponent c, List<String> highlightId) throws IOException
    {
        writer.write("<dl><dt");
        if (isText(c))
        {
            writer.write(" class=\"uicText\"");
        }
        if (highlightId != null)
        {
            if ((highlightId.size() > 0) && (highlightId.get(0).equals(c.getId())))
            {
                highlightId.remove(0);
                if (highlightId.size() == 0)
                {
                    writer.write(" class=\"highlightComponent\"");
                }
            }
        }
        writer.write(">");

        boolean hasChildren = c.getChildCount() > 0 || c.getFacets().size() > 0;

        writeStart(writer, c, hasChildren);
        writer.write("</dt>");
        if (hasChildren)
        {
            if (c.getFacets().size() > 0)
            {
                for (Map.Entry<String, UIComponent> entry : c.getFacets().entrySet())
                {
                    writer.write("<dd class=\"uicFacet\">");
                    writer.write("<span>");
                    writer.write(entry.getKey());
                    writer.write("</span>");
                    writeComponent(writer, entry.getValue(), highlightId);
                    writer.write("</dd>");
                }
            }
            if (c.getChildCount() > 0)
            {
                for (UIComponent child : c.getChildren())
                {
                    writer.write("<dd>");
                    writeComponent(writer, child, highlightId);
                    writer.write("</dd>");
                }
            }
            writer.write("<dt>");
            writeEnd(writer, c);
            writer.write("</dt>");
        }
        writer.write("</dl>");
    }

    private static void writeEnd(Writer writer, UIComponent c) throws IOException
    {
        if (!isText(c))
        {
            writer.write(TS);
            writer.write('/');
            writer.write(getName(c));
            writer.write('>');
        }
    }

    private final static String[] IGNORE = new String[] { "parent", "rendererType" };

    private static void writeAttributes(Writer writer, UIComponent c)
    {
        try
        {
            BeanInfo info = Introspector.getBeanInfo(c.getClass());
            PropertyDescriptor[] pd = info.getPropertyDescriptors();
            Method m = null;
            Object v = null;
            String str = null;
            for (int i = 0; i < pd.length; i++)
            {
                if (pd[i].getWriteMethod() != null && Arrays.binarySearch(IGNORE, pd[i].getName()) < 0)
                {
                    m = pd[i].getReadMethod();
                    try
                    {
                        v = m.invoke(c, (Object[])null);
                        if (v != null)
                        {
                            if (v instanceof Collection || v instanceof Map || v instanceof Iterator)
                            {
                                continue;
                            }
                            writer.write(" ");
                            writer.write(pd[i].getName());
                            writer.write("=\"");
                            if (v instanceof Expression)
                            {
                                str = ((Expression)v).getExpressionString();
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

    private static void writeStart(Writer writer, UIComponent c, boolean children) throws IOException
    {
        if (isText(c))
        {
            String str = c.toString().trim();
            writer.write(str.replaceAll("<", TS));
        }
        else
        {
            writer.write(TS);
            writer.write(getName(c));
            writeAttributes(writer, c);
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

    private static String getName(UIComponent c)
    {
        String nm = c.getClass().getName();
        return nm.substring(nm.lastIndexOf('.') + 1);
    }

    private static boolean isText(UIComponent c)
    {
        return (c.getClass().getName().startsWith("com.sun.facelets.compiler"));
    }

    public static void handleException(FacesContext facesContext, Exception ex) throws ServletException, IOException
    {
        handleThrowable(facesContext, ex);
    }

    public static void handleThrowable(FacesContext facesContext, Throwable ex) throws ServletException, IOException
    {

        prepareExceptionStack(ex);

        Object response = facesContext.getExternalContext().getResponse();
        if (response instanceof HttpServletResponse)
        {
            HttpServletResponse httpResp = (HttpServletResponse)response;
            if (!httpResp.isCommitted())
            {
                httpResp.reset();
                httpResp.setContentType("text/html; charset=UTF-8");
                Writer writer = httpResp.getWriter();

                debugHtml(writer, facesContext, ex);

                log.log(Level.SEVERE, "An exception occurred", ex);
            }
            else
            {
                throwException(ex);
            }
        }
        else
        {
            throwException(ex);
        }
    }

    public static void handleExceptionList(FacesContext facesContext, List<Exception> exceptionList) throws ServletException,
        IOException
    {
        for (Exception exception : exceptionList)
        {
            prepareExceptionStack(exception);
        }

        Object response = facesContext.getExternalContext().getResponse();
        if (response instanceof HttpServletResponse)
        {
            HttpServletResponse httpResp = (HttpServletResponse)response;
            if (!httpResp.isCommitted())
            {
                httpResp.reset();
                httpResp.setContentType("text/html; charset=UTF-8");
                Writer writer = httpResp.getWriter();

                debugHtml(writer, facesContext, exceptionList);

                for (Exception exception : exceptionList)
                {
                    log.log(Level.SEVERE, "An exception occurred", exception);
                }
            }
            else
            {
                throwException(exceptionList.get(0));
            }
        }
        else
        {
            throwException(exceptionList.get(0));
        }
    }

    private static void prepareExceptionStack(Throwable ex)
    {

        if (ex == null)
            return;

        // check for getRootCause and getCause-methods
        if (!initCausePerReflection(ex, "getRootCause"))
        {
            initCausePerReflection(ex, "getCause");
        }

        prepareExceptionStack(ex.getCause());
    }

    private static boolean initCausePerReflection(Throwable ex, String methodName)
    {
        try
        {
            Method causeGetter = ex.getClass().getMethod(methodName, (Class[])null);
            Throwable rootCause = (Throwable)causeGetter.invoke(ex, (Object[])null);
            return initCauseIfAvailable(ex, rootCause);
        }
        catch (Exception e1)
        {
            return false;
        }
    }

    static void throwException(Throwable e) throws IOException, ServletException
    {

        prepareExceptionStack(e);

        if (e instanceof IOException)
        {
            throw (IOException)e;
        }
        else if (e instanceof ServletException)
        {
            throw (ServletException)e;
        }
        else
        {
            ServletException ex;

            if (e.getMessage() != null)
            {
                ex = new ServletException(e.getMessage(), e);
            }
            else
            {
                ex = new ServletException(e);
            }

            initCauseIfAvailable(ex, e);

            throw ex;
        }
    }

    private static boolean initCauseIfAvailable(Throwable th, Throwable cause)
    {

        if (cause == null)
            return false;

        try
        {
            Method m = Throwable.class.getMethod("initCause", new Class[] { Throwable.class });
            m.invoke(th, new Object[] { cause });
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
