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
package org.apache.myfaces.renderkit;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
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
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletResponse;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

/**
 * This class provides utility methods to generate the
 * MyFaces error and debug pages. 
 * 
 * @author Jacob Hookom (ICLA with ASF filed)
 * @author Jakob Korherr (refactored and moved here from javax.faces.webapp._ErrorPageWriter)
 */
public final class ErrorPageWriter
{
    
    /**
     * This bean aims to generate the error page html for inclusion on a facelet error page via
     * <ui:include src="javax.faces.error.xhtml" />. When performing this include the facelet
     * "myfaces-dev-error-include.xhtml" will be included. This facelet references to the ErrorPageBean.
     * This also works for custom error page templates.
     * The bean is added to the ViewMap of the UIViewRoot, which is 
     * displaying the error page, in RestoreViewExecutor.execute().
     * @author Jakob Korherr
     */
    public static class ErrorPageBean implements Serializable
    {
        
        private static final long serialVersionUID = -79513324193326616L;

        public String getErrorPageHtml() throws IOException
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
            
            Throwable t = (Throwable) requestMap.get(EXCEPTION_KEY);
            if (t == null)
            {
                throw new IllegalStateException("No Exception to handle");
            }
            
            UIViewRoot view = (UIViewRoot) requestMap.get(VIEW_KEY);
            
            StringWriter writer = new StringWriter();
            ErrorPageWriter.debugHtml(writer, facesContext, t, view);
            String html = writer.toString();
            
            // change the HTML in the buffer to be included in an existing html page
            String body;
            try
            {
                body = html.substring(html.indexOf("<body>") + "<body>".length(), html.indexOf("</body>"));
            }
            catch (Exception e)
            {
                // no body found - return the entire html
                return html;
            }
            
            String head;
            try
            {
                head = html.substring(html.indexOf("<head>") + "<head>".length(), html.indexOf("</head>"));
            }
            catch (Exception e)
            {
                // no head found - return entire body
                return body;
            }
            
            // extract style and script information from head and add it to body
            StringBuilder builder = new StringBuilder(body);
            // extract <style>
            int startIndex = 0;
            while (true)
            {
                try
                {
                    int endIndex = head.indexOf("</style>", startIndex) + "</style>".length();
                    builder.append(head.substring(head.indexOf("<style", startIndex), endIndex));
                    startIndex = endIndex;
                }
                catch (Exception e)
                {
                    // no style found - break extraction
                    break;
                }
            }
            // extract <script>
            startIndex = 0;
            while (true)
            {
                try
                {
                    int endIndex = head.indexOf("</script>", startIndex) + "</script>".length();
                    builder.append(head.substring(head.indexOf("<script", startIndex), endIndex));
                    startIndex = endIndex;
                }
                catch (Exception e)
                {
                    // no script found - break extraction
                    break;
                }
            }
            
            return builder.toString();
        }
        
    }
    
    /**
     * The key which is used to store the ErrorPageBean in the view map of a facelet error page.
     */
    public static final String ERROR_PAGE_BEAN_KEY = "__myFacesErrorPageBean";
    
    private static final String EXCEPTION_KEY = "javax.servlet.error.exception";
    private static final String VIEW_KEY = "org.apache.myfaces.error.UIViewRoot";

    private static final Logger log = Logger.getLogger(ErrorPageWriter.class.getName());

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
    
    private final static String[] IGNORE = new String[] { "parent", "rendererType" };
    
    /**
     * Indicate if myfaces is responsible to handle errors. 
     * See http://wiki.apache.org/myfaces/Handling_Server_Errors for details. 
     */
    @JSFWebConfigParam(defaultValue="true",expectedValues="true,false", since="1.2.4")
    public static final String ERROR_HANDLING_PARAMETER = "org.apache.myfaces.ERROR_HANDLING";

    public ErrorPageWriter()
    {
        super();
    }
    
    /**
     * Generates the HTML error page for the given Throwable 
     * and writes it to the given writer.
     * @param writer
     * @param faces
     * @param e
     * @throws IOException
     */
    public static void debugHtml(Writer writer, FacesContext faces, Throwable e) throws IOException
    {
        debugHtml(writer, faces, e, faces.getViewRoot());
    }
    
    private static void debugHtml(Writer writer, FacesContext faces, Throwable e, UIViewRoot view) throws IOException
    {
        _init(faces);
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
                _writeException(writer, e);
            }
            else if ("now".equals(ERROR_PARTS[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(ERROR_PARTS[i]))
            {
                if (view != null)
                {
                    _writeComponent(writer, view, _getErrorId(e));
                }
            }
            else if ("vars".equals(ERROR_PARTS[i]))
            {
                _writeVariables(writer, faces, view);
            }
            else if ("cause".equals(ERROR_PARTS[i]))
            {
                _writeCause(writer, e);
            }
            else
            {
                writer.write(ERROR_PARTS[i]);
            }
        }
    }

    /**
     * Generates the HTML debug page for the current view
     * and writes it to the given writer.
     * @param writer
     * @param faces
     * @throws IOException
     */
    public static void debugHtml(Writer writer, FacesContext faces) throws IOException
    {
        _init(faces);
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
                _writeComponent(writer, faces.getViewRoot(), null);
            }
            else if ("vars".equals(DEBUG_PARTS[i]))
            {
                _writeVariables(writer, faces, faces.getViewRoot());
            }
            else
            {
                writer.write(DEBUG_PARTS[i]);
            }
        }
    }

    /**
     * Handles the given Throwbale in the following way:
     * If there is no <error-page> entry in web.xml, try to reset the current HttpServletResponse,
     * generate the error page and call responseComplete(). If this fails, rethrow the Exception.
     * If there is an <error-page> entry in web.xml, save the current UIViewRoot in the RequestMap
     * with the key "org.apache.myfaces.error.UIViewRoot" to access it on the error page and
     * rethrow the Exception to let it flow up to FacesServlet.service() and thus be handled by the container.
     * @param facesContext
     * @param ex
     * @throws FacesException
     */
    public static void handleThrowable(FacesContext facesContext, Throwable ex) throws FacesException
    {
        _prepareExceptionStack(ex);
        
        boolean errorPageWritten = false;
        
        // check if an error page is present in web.xml
        // if so, do not generate an error page
        WebXml webXml = WebXml.getWebXml(facesContext.getExternalContext());
        if (webXml.isErrorPagePresent())
        {
            // save current view in the request map to access it on the error page
            facesContext.getExternalContext().getRequestMap().put(VIEW_KEY, facesContext.getViewRoot());
        }
        else
        {
            // check for org.apache.myfaces.ERROR_HANDLING
            // do not generate an error page if it is false
            String errorHandling = facesContext.getExternalContext().getInitParameter(ERROR_HANDLING_PARAMETER);
            boolean errorHandlingDisabled = (errorHandling != null && errorHandling.equalsIgnoreCase("false"));
            if (!errorHandlingDisabled)
            {
                // write the error page
                Object response = facesContext.getExternalContext().getResponse();
                if (response instanceof HttpServletResponse)
                {
                    HttpServletResponse httpResp = (HttpServletResponse) response;
                    if (!httpResp.isCommitted())
                    {
                        httpResp.reset();
                        if (facesContext.getPartialViewContext().isAjaxRequest())
                        {    
                            // ajax request --> xml error page 
                            httpResp.setContentType("text/xml; charset=UTF-8");
                            try
                            {
                                Writer writer = httpResp.getWriter();
                                // can't use facesContext.getResponseWriter(), because it might not have been set
                                ResponseWriter responseWriter = new HtmlResponseWriterImpl(writer, "text/xml", "utf-8");
                                PartialResponseWriter partialWriter = new PartialResponseWriter(responseWriter);
                                partialWriter.startDocument();
                                partialWriter.startError(ex.getClass().getName());
                                if (ex.getCause() != null)
                                {
                                    partialWriter.write(ex.getCause().toString());
                                }
                                else
                                {
                                    partialWriter.write(ex.getMessage());
                                }
                                partialWriter.endError();
                                partialWriter.endDocument();
                            }
                            catch(IOException ioe)
                            {
                                throw new FacesException("Could not write the error page", ioe);
                            }
                        }
                        else
                        {    
                            // normal request --> html error page
                            httpResp.setContentType("text/html; charset=UTF-8");
                            try
                            {
                                Writer writer = httpResp.getWriter();
                                debugHtml(writer, facesContext, ex);
                            }
                            catch(IOException ioe)
                            {
                                throw new FacesException("Could not write the error page", ioe);
                            }
                        }
                        log.log(Level.SEVERE, "An exception occurred", ex);
                        
                        // mark the response as complete
                        facesContext.responseComplete();
                        
                        errorPageWritten = true;
                    }
                }
            }
        }
        
        // rethrow the throwable, if we did not write the error page
        if (!errorPageWritten)
        {
            if (ex instanceof FacesException)
            {
                throw (FacesException) ex;
            }
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            throw new FacesException(ex);
        }

    }

    private static String _getErrorTemplate(FacesContext context)
    {
        String errorTemplate = context.getExternalContext().getInitParameter(ERROR_TEMPLATE_RESOURCE);
        if (errorTemplate != null)
        {
            return errorTemplate;
        }
        return ERROR_TEMPLATE;
    }

    private static String _getDebugTemplate(FacesContext context)
    {
        String debugTemplate = context.getExternalContext().getInitParameter(DEBUG_TEMPLATE_RESOURCE);
        if (debugTemplate != null)
        {
            return debugTemplate;
        }
        return DEBUG_TEMPLATE;
    }

    private static void _init(FacesContext context) throws IOException
    {
        if (ERROR_PARTS == null)
        {
            ERROR_PARTS = _splitTemplate(_getErrorTemplate(context));
        }

        if (DEBUG_PARTS == null)
        {
            DEBUG_PARTS = _splitTemplate(_getDebugTemplate(context));
        }
    }

    private static String[] _splitTemplate(String rsc) throws IOException
    {
        InputStream is = ClassUtils.getContextClassLoader().getResourceAsStream(rsc);            
        if (is == null)
        {
            // try to get the resource from ExternalContext
            is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(rsc);
            if (is == null)
            {
                // fallback
                is = ErrorPageWriter.class.getClassLoader().getResourceAsStream(rsc);
            }
        }        

        if (is == null)
        {
            // throw an IllegalArgumentException instead of a FileNotFoundException,
            // because when using <ui:debug /> this error is hard to trace,
            // because the Exception is thrown in the Renderer and so it seems like
            // the facelet (or jsp) does not exist.
            throw new IllegalArgumentException("Could not find resource " + rsc);
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
    
    private static List<String> _getErrorId(Throwable e)
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

    private static void _writeException(Writer writer, Throwable e) throws IOException
    {
        StringWriter str = new StringWriter(256);
        PrintWriter pstr = new PrintWriter(str);
        e.printStackTrace(pstr);
        pstr.close();
        writer.write(str.toString().replaceAll("<", TS));
    }
    
    private static void _writeCause(Writer writer, Throwable ex) throws IOException
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

    private static void _writeVariables(Writer writer, FacesContext faces, UIViewRoot view) throws IOException
    {
        ExternalContext ctx = faces.getExternalContext();
        _writeVariables(writer, ctx.getRequestParameterMap(), "Request Parameters");
        _writeVariables(writer, ctx.getRequestMap(), "Request Attributes");
        if (view != null)
        {
          _writeVariables(writer, view.getViewMap(), "View Attributes");
        }
        if (ctx.getSession(false) != null)
        {
            _writeVariables(writer, ctx.getSessionMap(), "Session Attributes");
        }
        _writeVariables(writer, ctx.getFlash(), "Flash Attributes");
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

    private static void _writeComponent(Writer writer, UIComponent c, List<String> highlightId) throws IOException
    {
        writer.write("<dl><dt");
        if (_isText(c))
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

        _writeStart(writer, c, hasChildren);
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
                    _writeComponent(writer, entry.getValue(), highlightId);
                    writer.write("</dd>");
                }
            }
            if (c.getChildCount() > 0)
            {
                for (UIComponent child : c.getChildren())
                {
                    writer.write("<dd>");
                    _writeComponent(writer, child, highlightId);
                    writer.write("</dd>");
                }
            }
            writer.write("<dt>");
            _writeEnd(writer, c);
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

    private static void _writeAttributes(Writer writer, UIComponent c)
    {
        try
        {
            BeanInfo info = Introspector.getBeanInfo(c.getClass());
            PropertyDescriptor[] pd = info.getPropertyDescriptors();
            Method m = null;
            Object v = null;
            ValueExpression valueExpression = null;
            String str = null;
            for (int i = 0; i < pd.length; i++)
            {
                if (pd[i].getWriteMethod() != null && Arrays.binarySearch(IGNORE, pd[i].getName()) < 0)
                {
                    m = pd[i].getReadMethod();
                    try
                    {
                        // first check if the property is a ValueExpression
                        valueExpression = c.getValueExpression(pd[i].getName());
                        if (valueExpression != null)
                        {
                            _writeAttribute(writer, pd[i].getName(), valueExpression.getExpressionString());
                        }
                        else
                        {
                            v = m.invoke(c, null);
                            if (v != null)
                            {
                                if (v instanceof Collection || v instanceof Map || v instanceof Iterator)
                                {
                                    continue;
                                }
                                if (v instanceof Expression)
                                {
                                    str = ((Expression)v).getExpressionString();
                                }
                                else if (v instanceof ValueBinding)
                                {
                                    str = ((ValueBinding) v).getExpressionString();
                                }
                                else if (v instanceof MethodBinding)
                                {
                                    str = ((MethodBinding) v).getExpressionString();
                                }
                                else
                                {
                                    str = v.toString();
                                }
                                _writeAttribute(writer, pd[i].getName(), str);
                            }
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
                _writeAttribute(writer, "binding", binding.getExpressionString());
            }
        }
        catch (Exception e)
        {
            // do nothing
        }
    }
    
    private static void _writeAttribute(Writer writer, String name, String value) throws IOException
    {
        writer.write(" ");
        writer.write(name);
        writer.write("=\"");
        writer.write(value.replaceAll("<", TS));
        writer.write("\"");
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

    private static String _getName(UIComponent c)
    {
        String nm = c.getClass().getName();
        return nm.substring(nm.lastIndexOf('.') + 1);
    }

    private static boolean _isText(UIComponent c)
    {
        return (c.getClass().getName().startsWith("org.apache.myfaces.view.facelets.compiler"));
    }

    private static void _prepareExceptionStack(Throwable ex)
    {

        if (ex == null)
            return;

        // check for getRootCause and getCause-methods
        if (!_initCausePerReflection(ex, "getRootCause"))
        {
            _initCausePerReflection(ex, "getCause");
        }

        _prepareExceptionStack(ex.getCause());
    }

    private static boolean _initCausePerReflection(Throwable ex, String methodName)
    {
        try
        {
            Method causeGetter = ex.getClass().getMethod(methodName, (Class[])null);
            Throwable rootCause = (Throwable)causeGetter.invoke(ex, (Object[])null);
            return _initCauseIfAvailable(ex, rootCause);
        }
        catch (Exception e1)
        {
            return false;
        }
    }

    private static boolean _initCauseIfAvailable(Throwable th, Throwable cause)
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
