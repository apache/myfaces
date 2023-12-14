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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.el.Expression;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;
import jakarta.faces.view.Location;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.lifecycle.ViewNotFoundException;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.component.visit.MyFacesVisitHints;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.context.ExceptionHandlerUtils;
import org.apache.myfaces.view.facelets.component.UIRepeat;

/**
 * This class provides utility methods to generate the
 * MyFaces error and debug pages. 
 *
 * @author Jacob Hookom (ICLA with ASF filed)
 * @author Jakob Korherr (refactored and moved here from jakarta.faces.webapp._ErrorPageWriter)
 */
public final class ErrorPageWriter
{

    /**
     * This bean aims to generate the error page html for inclusion on a facelet error page via
     * &lt;ui:include src="jakarta.faces.error.xhtml" /&gt;. When performing this include the facelet
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
            ErrorPageWriter.debugHtml(writer, facesContext, view, null, t);
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
                    builder.append(head, head.indexOf("<style", startIndex), endIndex);
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
                    builder.append(head, head.indexOf("<script", startIndex), endIndex);
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

    private static final String EXCEPTION_KEY = "jakarta.servlet.error.exception";
    public static final String VIEW_KEY = "org.apache.myfaces.error.UIViewRoot";

    private static final Logger log = Logger.getLogger(ErrorPageWriter.class.getName());

    private final static String TS = "&lt;";

    private static final String ERROR_TEMPLATE = "META-INF/rsc/myfaces-dev-error.xml";

    /**
     * Indicate the template name used to render the default error page used by MyFaces specific 
     * error handler implementation. 
     *
     * <p>See org.apache.myfaces.ERROR_HANDLING for details about
     * how to enable/disable it.</p>
     */
    @JSFWebConfigParam(defaultValue="META-INF/rsc/myfaces-dev-error.xml", since="1.2.4")
    private static final String ERROR_TEMPLATE_RESOURCE = "org.apache.myfaces.ERROR_TEMPLATE_RESOURCE";

    private static String[] errorParts;

    private static final String DEBUG_TEMPLATE = "META-INF/rsc/myfaces-dev-debug.xml";

    /**
     * Indicate the template name used to render the default debug page (see ui:debug tag).
     */
    @JSFWebConfigParam(defaultValue="META-INF/rsc/myfaces-dev-debug.xml", since="1.2.4")
    private static final String DEBUG_TEMPLATE_RESOURCE = "org.apache.myfaces.DEBUG_TEMPLATE_RESOURCE";

    private static String[] debugParts;

    private static final Pattern REGEX_PATTERN = Pattern.compile(".*?\\Q,Id:\\E\\s*(\\S+)\\s*\\].*?");

    private final static String[] IGNORE = new String[] { "parent", "rendererType" };

    private final static String[] ALWAYS_WRITE = new String[] { "class", "clientId" };

    /**
     * Extended debug info is stored under this key in the request
     * map for every UIInput component when in Development mode.
     * ATTENTION: this constant is duplicate in jakarta.faces.component.UIInput
     */
    public static final String DEBUG_INFO_KEY = "org.apache.myfaces.debug.DEBUG_INFO";

    /**
     * The number of facets of this component which have already been visited while
     * creating the extended component tree is saved under this key in the component's
     * attribute map.
     */
    private static final String VISITED_FACET_COUNT_KEY = "org.apache.myfaces.debug.VISITED_FACET_COUNT";

    /**
     * Indicate if myfaces is responsible to handle errors. 
     * See https://cwiki.apache.org/confluence/display/MYFACES/Handling+Server+Errors for details.
     */
    @JSFWebConfigParam(defaultValue="false, on Development Project stage: true",
                       expectedValues="true,false", since="1.2.4")
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
        debugHtml(writer, faces, faces.getViewRoot(), null,  e);
    }

    private static void debugHtml(Writer writer, FacesContext faces, UIViewRoot view,
                                  Collection<UIComponent> components, Throwable... exs) throws IOException
    {
        init(faces);
        Date now = new Date();

        for (int i = 0; i < errorParts.length; i++)
        {
            if ("view".equals((errorParts[i])))
            {
                if (faces.getViewRoot() != null)
                {
                    String viewId = faces.getViewRoot().getViewId();
                    writer.write("viewId=" + viewId);
                    writer.write("<br/>");

                    try
                    {
                        //Could not work on tomcat 7 running by cargo
                        String realPath = faces.getExternalContext().getRealPath(viewId);
                        writer.write("location=" + realPath);
                        writer.write("<br/>");
                    }
                    catch (Throwable e)
                    {
                        //swallow it
                    }

                    writer.write("phaseId=" + faces.getCurrentPhaseId());
                    writer.write("<br/>");
                    writer.write("<br/>");
                }
            }
            else if ("message".equals(errorParts[i]))
            {
                boolean printed = false;

                for (Throwable e : exs)
                {
                    String msg = e.getMessage();
                    if (printed)
                    {
                        writer.write("<br/>");
                    }
                    if (msg != null)
                    {
                        writer.write(msg.replaceAll("<", TS));
                    }
                    else
                    {
                        writer.write(e.getClass().getName());
                    }
                    printed = true;
                }
            }
            else if ("trace".equals(errorParts[i]))
            {
                boolean printed = false;
                for (Throwable e : exs)
                {
                    if (printed)
                    {
                        writer.write("\n");
                    }
                    writeException(writer, e);
                    printed = true;
                }
            }
            else if ("now".equals(errorParts[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(errorParts[i]))
            {
                if (view != null)
                {
                    List<String> errorIds = getErrorId(components, exs);
                    writeComponent(faces, writer, view, errorIds, true);
                }
            }
            else if ("vars".equals(errorParts[i]))
            {
                writeVariables(writer, faces, view);
            }
            else if ("cause".equals(errorParts[i]))
            {
                boolean printed = false;
                Iterator<UIComponent> iterator = null;
                if (components != null)
                {
                    iterator = components.iterator();
                }
                for (Throwable e : exs)
                {
                    if (printed)
                    {
                        writer.write("<br/>");
                    }
                    writeCause(writer, e);
                    if (iterator != null)
                    {
                        UIComponent uiComponent = iterator.next();
                        if (uiComponent != null)
                        {
                            writeComponent(faces, writer, uiComponent, null, /* writeChildren */false);
                        }
                    }
                    printed = true;
                }
            }
            else
            {
                writer.write(errorParts[i]);
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
        init(faces);
        Date now = new Date();

        for (int i = 0; i < debugParts.length; i++)
        {
            if ("message".equals(debugParts[i]))
            {
                writer.write(faces.getViewRoot().getViewId());
            }
            else if ("now".equals(debugParts[i]))
            {
                writer.write(DateFormat.getDateTimeInstance().format(now));
            }
            else if ("tree".equals(debugParts[i]))
            {
                writeComponent(faces, writer, faces.getViewRoot(), null, true);
            }
            else if ("extendedtree".equals(debugParts[i]))
            {
                writeExtendedComponentTree(writer, faces);
            }
            else if ("vars".equals(debugParts[i]))
            {
                writeVariables(writer, faces, faces.getViewRoot());
            }
            else
            {
                writer.write(debugParts[i]);
            }
        }
    }

    public static void handle(FacesContext facesContext, Collection<UIComponent> components,
                              Throwable... exs) throws FacesException
    {
        for (Throwable ex : exs)
        {
            prepareExceptionStack(ex);
        }

        if (!facesContext.getExternalContext().isResponseCommitted())
        {
            facesContext.getExternalContext().responseReset();
        }

        int responseStatus = -1;
        for (Throwable ex : exs)
        {
            if (ex instanceof ViewNotFoundException)
            {
                responseStatus = HttpServletResponse.SC_NOT_FOUND;
                break;
            }
            else
            {
                responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }
        if (responseStatus != -1)
        {
            facesContext.getExternalContext().setResponseStatus(responseStatus);
        }

        // normal request --> html error page
        facesContext.getExternalContext().setResponseContentType("text/html");
        facesContext.getExternalContext().setResponseCharacterEncoding("UTF-8");
        try
        {
            // We need the real one, because the one returned from FacesContext.getResponseWriter()
            // is configured with the encoding of the view.
            Writer writer = facesContext.getExternalContext().getResponseOutputWriter();
            debugHtml(writer, facesContext, facesContext.getViewRoot(), components, exs);
        }
        catch(IOException ioe)
        {
            throw new FacesException("Could not write the error page", ioe);
        }

        // mark the response as complete
        facesContext.responseComplete();
    }

    private static String getErrorTemplate(FacesContext context)
    {
        String errorTemplate = context.getExternalContext().getInitParameter(ERROR_TEMPLATE_RESOURCE);
        return errorTemplate == null ? ERROR_TEMPLATE : errorTemplate;
    }

    private static String getDebugTemplate(FacesContext context)
    {
        String debugTemplate = context.getExternalContext().getInitParameter(DEBUG_TEMPLATE_RESOURCE);
        return debugTemplate == null ? DEBUG_TEMPLATE : debugTemplate;
    }

    private static void init(FacesContext context) throws IOException
    {
        if (errorParts == null)
        {
            errorParts = splitTemplate(getErrorTemplate(context));
        }

        if (debugParts == null)
        {
            debugParts = splitTemplate(getDebugTemplate(context));
        }
    }

    private static String[] splitTemplate(String rsc) throws IOException
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
            // the facelet does not exist.
            throw new IllegalArgumentException("Could not find resource " + rsc);
        }

        try
        {
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
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    private static List<String> getErrorId(Collection<UIComponent> components, Throwable... exs)
    {
        List<String> list = null;
        for (Throwable e : exs)
        {
            String message = e.getMessage();

            if (message == null)
            {
                continue;
            }

            Matcher matcher = REGEX_PATTERN.matcher(message);

            while (matcher.find())
            {
                if (list == null)
                {
                    list = new ArrayList<>();
                }
                list.add(matcher.group(1));
            }
        }

        if (list != null && !list.isEmpty())
        {
            return list;
        }
        else if (components != null)
        {
            list = new ArrayList<>();
            for (UIComponent uiComponent : components)
            {
                if (uiComponent  != null)
                {
                    list.add(uiComponent.getId());
                }
            }
            return list;
        }

        return null;
    }

    private static void writeException(Writer writer, Throwable e) throws IOException
    {
        StringWriter str = new StringWriter(256);
        try (PrintWriter pstr = new PrintWriter(str))
        {
            e.printStackTrace(pstr);
        }
        writer.write(str.toString().replaceAll("<", TS));
    }

    private static void writeCause(Writer writer, Throwable ex) throws IOException
    {
        String msg = ex.getMessage();
        String location = ExceptionHandlerUtils.buildLocation(ex, null);

        while (ex.getCause() != null)
        {
            ex = ex.getCause();
            if (ex.getMessage() != null)
            {
                msg = ex.getMessage();
            }
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
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        writer.write("<br/> at " + stackTraceElement.toString());

        if (location != null)
        {
            writer.write("<br/> <br/>");
            writer.write("Facelet: <br/>");
            writer.write(location);
            writer.write("<br/>");
        }
    }

    private static void writeVariables(Writer writer, FacesContext faces, UIViewRoot view) throws IOException
    {
        ExternalContext ctx = faces.getExternalContext();
        writeVariables(writer, ctx.getRequestParameterMap(), "Request Parameters");
        writeVariables(writer, ctx.getRequestMap(), "Request Attributes");
        if (view != null)
        {
          writeVariables(writer, view.getViewMap(), "View Attributes");
        }
        if (ctx.getSession(false) != null)
        {
            writeVariables(writer, ctx.getSessionMap(), "Session Attributes");
        }
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(faces);
        if(config!=null && !config.isFlashScopeDisabled() && ctx.getFlash() != null)
        {
            writeVariables(writer, ctx.getFlash(), "Flash Attributes");
        }
        writeVariables(writer, ctx.getApplicationMap(), "Application Attributes");
    }

    private static void writeVariables(Writer writer, Map<String, ? extends Object> vars, String caption)
            throws IOException
    {
        writer.write("<table><caption>");
        writer.write(caption);
        writer.write("</caption><thead><tr><th style=\"width: 10%; \">Name</th>"
                     + "<th style=\"width: 90%; \">Value</th></tr></thead><tbody>");
        boolean written = false;
        if (!vars.isEmpty())
        {
            SortedMap<String, Object> sortedMap = new TreeMap<>(vars);
            for (Map.Entry<String, Object> entry : sortedMap.entrySet())
            {
                String key = entry.getKey();
                if (key.indexOf('.') == -1)
                {
                    writer.write("<tr><td>");
                    writer.write(key.replaceAll("<", TS));
                    writer.write("</td><td>");
                    Object value = entry.getValue();
                    // in some (very rare) situations value can be null or not null
                    // but with null toString() representation
                    if (value != null && value.toString() != null)
                    {
                        writer.write(value.toString().replaceAll("<", TS));
                    }
                    else
                    {
                        writer.write("null");
                    }
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

    private static void writeComponent(FacesContext faces, Writer writer, UIComponent c, List<String> highlightId,
                                        boolean writeChildren) throws IOException
    {
        writer.write("<dl><dt");
        if (isText(c))
        {
            writer.write(" class=\"uicText\"");
        }
        if (highlightId != null)
        {
            if (highlightId.isEmpty())
            {
                String id = c.getId();
                if (highlightId.contains(id))
                {
                    writer.write(" class=\"highlightComponent\"");
                }
            }
        }
        writer.write(">");

        boolean hasChildren = (c.getChildCount() > 0 || c.getFacetCount() > 0) && writeChildren;

        int stateSize = 0;

        Object state = c.saveState(faces);
        if (state != null)
        {
            try
            {
                byte[] stateBytes = StateUtils.getAsByteArray(state, faces.getExternalContext());
                stateSize = stateBytes.length;
            }
            catch (Exception e)
            {
                stateSize = -1;
                if (log.isLoggable(Level.FINEST))
                {
                    log.fine("Could not determine state size: " + e.getMessage());
                }
            }
        }
        writeStart(writer, c, hasChildren, true);
        writer.write(" - State size:" + stateSize + " bytes");
        writer.write("</dt>");
        if (hasChildren)
        {
            if (c.getFacetCount() > 0)
            {
                for (Map.Entry<String, UIComponent> entry : c.getFacets().entrySet())
                {
                    writer.write("<dd class=\"uicFacet\">");
                    writer.write("<span>");
                    writer.write(entry.getKey());
                    writer.write("</span>");
                    writeComponent(faces, writer, entry.getValue(), highlightId, true);
                    writer.write("</dd>");
                }
            }
            if (c.getChildCount() > 0)
            {
                for (int i = 0, childCount = c.getChildCount(); i < childCount; i++)
                {
                    UIComponent child = c.getChildren().get(i);
                    writer.write("<dd>");
                    writeComponent(faces, writer, child, highlightId, writeChildren);
                    writer.write("</dd>");
                }
            }
            writer.write("<dt>");
            writeEnd(writer, c);
            writer.write("</dt>");
        }
        writer.write("</dl>");
    }

    /**
     * Creates the Extended Component Tree via UIViewRoot.visitTree()
     * and ExtendedComponentTreeVisitCallback as VisitCallback.
     *
     * @param writer
     * @param facesContext
     * @throws IOException
     */
    private static void writeExtendedComponentTree(Writer writer, FacesContext facesContext) throws IOException
    {
        VisitContext visitContext = VisitContext.createVisitContext(facesContext, null,
                MyFacesVisitHints.SET_SKIP_UNRENDERED);
        facesContext.getViewRoot().visitTree(visitContext, new ExtendedComponentTreeVisitCallback(writer));
        clearVisitedFacetCountMap(facesContext);
    }

    /**
     * The VisitCallback that is used to create the Extended Component Tree.
     *
     * @author Jakob Korherr
     */
    private static class ExtendedComponentTreeVisitCallback implements VisitCallback
    {
        private Writer writer;

        public ExtendedComponentTreeVisitCallback(Writer writer)
        {
            this.writer = writer;
        }

        @SuppressWarnings("unchecked")
        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            Map<String, Object> requestMap = context.getFacesContext().getExternalContext().getRequestMap();

            try
            {
                if (!(target instanceof UIViewRoot))
                {
                    writer.write("<dd>");
                }

                UIComponent parent = target.getParent();
                boolean hasChildren = (target.getChildCount() > 0 || target.getFacetCount() > 0);
                String facetName = getFacetName(target);

                if (!(target instanceof UIColumn))
                {
                    if (parent instanceof UIColumn column
                            && ((parent.getChildCount() > 0 && parent.getChildren().get(0) == target)
                                    ||  (facetName != null &&
                                            getVisitedFacetCount(context.getFacesContext(), parent) == 0)))
                    {
                        if (parent.getParent() instanceof UIData
                                && isFirstUIColumn(parent.getParent(), column))
                        {
                            writer.write("<span>Row: ");
                            int rowIndex = ((UIData) parent.getParent()).getRowIndex();
                            writer.write("" + rowIndex);
                            if (rowIndex == -1)
                            {
                                // tell the user that rowIndex == -1 stands for visiting column-facets
                                writer.write(" (all column facets)");
                            }
                            writer.write("</span>");
                        }
                        writer.write("<dl><dt>");
                        writeStart(writer, parent, true, false);
                        writer.write("</dt><dd>");
                    }

                    if (facetName != null)
                    {
                        writer.write("<span>" + facetName + "</span>");
                        incrementVisitedFacetCount(context.getFacesContext(), parent);
                    }
                    writer.write("<dl><dt");
                    if (isText(target))
                    {
                        writer.write(" class=\"uicText\"");
                    }
                    writer.write(">");

                    Map<String, List<Object[]>> debugInfos = null;
                    // is the target a EditableValueHolder component?
                    // If so, debug infos from DebugPhaseListener should be available
                    if (target instanceof EditableValueHolder)
                    {
                        // get the debug info
                        debugInfos = (Map<String, List<Object[]>>) requestMap
                                .get(DEBUG_INFO_KEY + target.getClientId());
                    }

                    // Get the component's renderer.
                    // Note that getRenderer(FacesContext context) is definded in UIComponent,
                    // but it is protected, so we have to use reflection!
                    Renderer renderer = null;
                    try
                    {
                        Method getRenderer = UIComponent.class.getDeclaredMethod(
                                "getRenderer", FacesContext.class);
                        // make it accessible for us!
                        getRenderer.setAccessible(true);
                        renderer = (Renderer) getRenderer.invoke(target, context.getFacesContext());
                    }
                    catch (Exception e)
                    {
                        // nothing - do not output renderer information
                    }

                    // write the component start
                    writeStart(writer, target, (hasChildren || debugInfos != null || renderer != null), false);
                    writer.write("</dt>");

                    if (renderer != null)
                    {
                        // write renderer info
                        writer.write("<div class=\"renderer\">Rendered by ");
                        writer.write(renderer.getClass().getCanonicalName());
                        writer.write("</div>");

                        if (!hasChildren && debugInfos == null)
                        {
                            // close the component
                            writer.write("<dt>");
                            writeEnd(writer, target);
                            writer.write("</dt>");
                        }
                    }

                    if (debugInfos != null)
                    {
                        final String fieldid = target.getClientId() + "_lifecycle";
                        writer.write("<div class=\"lifecycle_values_wrapper\">");
                        writer.write("<a href=\"#\" onclick=\"toggle('");
                        writer.write(fieldid);
                        writer.write("'); return false;\"><span id=\"");
                        writer.write(fieldid);
                        writer.write("Off\">+</span><span id=\"");
                        writer.write(fieldid);
                        writer.write("On\" style=\"display: none;\">-</span> Value Lifecycle</a>");
                        writer.write("<div id=\"");
                        writer.write(fieldid);
                        writer.write("\" class=\"lifecycle_values\">");

                        // process any available debug info
                        for (Map.Entry<String, List<Object[]>> entry : debugInfos.entrySet())
                        {
                            writer.write("<span>");
                            writer.write(entry.getKey());
                            writer.write("</span><ol>");
                            int i = 0;
                            for (Object[] debugInfo : entry.getValue())
                            {
                                // structure of the debug-info array:
                                //     - 0: phase
                                //     - 1: old value
                                //     - 2: new value
                                //     - 3: StackTraceElement List

                                // oldValue and newValue could be null
                                String oldValue = debugInfo[1] == null ? "null" : debugInfo[1].toString();
                                String newValue = debugInfo[2] == null ? "null" : debugInfo[2].toString();
                                writer.write("<li><b>");
                                writer.write(entry.getKey());
                                writer.write("</b> set from <b>");
                                writer.write(oldValue);
                                writer.write("</b> to <b>");
                                writer.write(newValue);
                                writer.write("</b> in Phase ");
                                writer.write(debugInfo[0].toString());

                                // check if a call stack is available
                                if (debugInfo[3] != null)
                                {
                                    final String stackTraceId = fieldid + '_' + entry.getKey() + '_' + i;
                                    writer.write("<div class=\"stacktrace_wrapper\">");
                                    writer.write("<a href=\"#\" onclick=\"toggle('");
                                    writer.write(stackTraceId);
                                    writer.write("'); return false;\"><span id=\"");
                                    writer.write(stackTraceId);
                                    writer.write("Off\">+</span><span id=\"");
                                    writer.write(stackTraceId);
                                    writer.write("On\" style=\"display: none;\">-</span> Call Stack</a>");
                                    writer.write("<div id=\"");
                                    writer.write(stackTraceId);
                                    writer.write("\" class=\"stacktrace_values\">");
                                    writer.write("<ul>");
                                    for (StackTraceElement stackTraceElement
                                            : (List<StackTraceElement>) debugInfo[3])
                                    {
                                        writer.write("<li>");
                                        writer.write(stackTraceElement.toString());
                                        writer.write("</li>");
                                    }
                                    writer.write("</ul></div></div>");
                                }

                                writer.write("</li>");

                                i++;
                            }
                            writer.write("</ol>");
                        }

                        writer.write("</div></div>");

                        // now remove the debug info from the request map, 
                        // so that it does not appear in the scope values of the debug page 
                        requestMap.remove(DEBUG_INFO_KEY + target.getClientId());

                        if (!hasChildren)
                        {
                            // close the component
                            writer.write("<dt>");
                            writeEnd(writer, target);
                            writer.write("</dt>");
                        }
                    }
                }

                if (!hasChildren)
                {
                    writer.write("</dl>");

                    while (parent != null &&
                           ((parent.getChildCount()>0 && parent.getChildren().get(parent.getChildCount()-1) == target)
                                    || (parent.getFacetCount() != 0
                                            && getVisitedFacetCount(context.getFacesContext(), parent) == 
                                                    parent.getFacetCount())))
                    {
                        // target is last child of parent or the "last" facet

                        // remove the visited facet count from the attribute map
                        removeVisitedFacetCount(context.getFacesContext(), parent);

                        // check for componentes that visit their children multiple times
                        if (parent instanceof UIData uidata)
                        {
                            if (uidata.getRowIndex() != uidata.getRowCount() - 1)
                            {
                                // only continue if we're in the last row
                                break;
                            }
                        }
                        else if (parent instanceof UIRepeat uirepeat)
                        {
                            if (uirepeat.getIndex() + uirepeat.getStep() < uirepeat.getRowCount())
                            {
                                // only continue if we're in the last row
                                break;
                            }
                        }

                        writer.write("</dd><dt>");
                        writeEnd(writer, parent);
                        writer.write("</dt></dl>");

                        if (!(parent instanceof UIViewRoot))
                        {
                            writer.write("</dd>");
                        }

                        target = parent;
                        parent = target.getParent();
                    }
                }
            }
            catch (IOException ioe)
            {
                throw new FacesException(ioe);
            }

            return VisitResult.ACCEPT;
        }

    }

    private static boolean isFirstUIColumn(UIComponent uidata, UIColumn uicolumn)
    {
        for (int i = 0, childCount = uidata.getChildCount(); i < childCount; i++)
        {
            UIComponent child = uidata.getChildren().get(i);
            if (child instanceof UIColumn)
            {
                return (child == uicolumn);
            }
        }
        return false;
    }

    private static String getFacetName(UIComponent component)
    {
        UIComponent parent = component.getParent();
        if (parent != null)
        {
            if (parent.getFacetCount() > 0)
            {
                for (Map.Entry<String, UIComponent> entry : parent.getFacets().entrySet())
                {
                    if (entry.getValue() == component)
                    {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    private static int getVisitedFacetCount(FacesContext facesContext, UIComponent component)
    {
        Map<UIComponent, Integer> visitedFacetCount = (Map<UIComponent, Integer>)
            facesContext.getAttributes().get(VISITED_FACET_COUNT_KEY);
        if (visitedFacetCount == null)
        {
            return 0;
        }

        Integer count = visitedFacetCount.get(component);
        return count == null ? 0 : count;
    }

    private static void incrementVisitedFacetCount(FacesContext facesContext, UIComponent component)
    {
        Map<UIComponent, Integer> visitedFacetCount = (Map<UIComponent, Integer>)
            facesContext.getAttributes().computeIfAbsent(VISITED_FACET_COUNT_KEY, k -> new HashMap<>());
        visitedFacetCount.put(component, getVisitedFacetCount(facesContext, component) + 1);
    }

    private static void removeVisitedFacetCount(FacesContext facesContext, UIComponent component)
    {
        Map<UIComponent, Integer> visitedFacetCount = (Map<UIComponent, Integer>)
            facesContext.getAttributes().get(VISITED_FACET_COUNT_KEY);
        if (visitedFacetCount == null)
        {
            return;
        }
        visitedFacetCount.remove(component);
    }
    
    private static void clearVisitedFacetCountMap(FacesContext facesContext)
    {
        Map<UIComponent, Integer> visitedFacetCount = (Map<UIComponent, Integer>)
            facesContext.getAttributes().get(VISITED_FACET_COUNT_KEY);
        if (visitedFacetCount != null)
        {
            visitedFacetCount.clear();
            facesContext.getAttributes().remove(VISITED_FACET_COUNT_KEY);
        }
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

    private static void writeAttributes(Writer writer, UIComponent c, boolean valueExpressionValues)
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
                if ((pd[i].getWriteMethod() != null || Arrays.binarySearch(ALWAYS_WRITE, pd[i].getName()) > -1)
                    && Arrays.binarySearch(IGNORE, pd[i].getName()) < 0)
                {
                    m = pd[i].getReadMethod();
                    if (m != null)
                    {
                        try
                        {
                            // first check if the property is a ValueExpression
                            valueExpression = c.getValueExpression(pd[i].getName());
                            if (valueExpressionValues && valueExpression != null)
                            {
                                String expressionString = valueExpression.getExpressionString();
                                if (null == expressionString)
                                {
                                    expressionString = "";
                                }
                                writeAttribute(writer, pd[i].getName(), expressionString);
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
                                    if (v instanceof Expression expression)
                                    {
                                        str = expression.getExpressionString();
                                    }
                                    else
                                    {
                                        str = v.toString();
                                    }

                                    writeAttribute(writer, pd[i].getName(), str);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            // do nothing
                        }
                    }
                }
            }

            ValueExpression binding = c.getValueExpression("binding");
            if (binding != null)
            {
                writeAttribute(writer, "binding", binding.getExpressionString());
            }

            // write the location
            String location = getComponentLocation(c);
            if (location != null)
            {
                writeAttribute(writer, "location", location);
            }
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    private static void writeAttribute(Writer writer, String name, String value) throws IOException
    {
        writer.write(" ");
        writer.write(name);
        writer.write("=\"");
        writer.write(value.replaceAll("<", TS));
        writer.write("\"");
    }

    private static void writeStart(Writer writer, UIComponent c, boolean children, boolean valueExpressionValues)
            throws IOException
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
            writeAttributes(writer, c, valueExpressionValues);
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
        return (c.getClass().getName().startsWith("org.apache.myfaces.view.facelets.compiler"));
    }

    private static void prepareExceptionStack(Throwable ex)
    {

        if (ex == null)
        {
            return;
        }

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
            Method causeGetter = ex.getClass().getMethod(methodName, (Class[]) null);
            Throwable rootCause = (Throwable) causeGetter.invoke(ex, (Object[]) null);
            return initCauseIfAvailable(ex, rootCause);
        }
        catch (Exception e1)
        {
            return false;
        }
    }

    private static boolean initCauseIfAvailable(Throwable th, Throwable cause)
    {
        if (cause == null)
        {
            return false;
        }

        try
        {
            Method m = Throwable.class.getMethod("initCause", Throwable.class);
            m.invoke(th, cause);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Gets the Location of the given UIComponent from its attribute map.
     * @param component
     * @return
     */
    private static String getComponentLocation(UIComponent component)
    {
        Location location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
        if (location != null)
        {
            return location.toString();
        }
        return null;
    }
}
