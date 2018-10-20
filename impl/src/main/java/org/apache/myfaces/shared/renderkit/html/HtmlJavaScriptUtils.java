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
package org.apache.myfaces.shared.renderkit.html;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.shared.renderkit.html.HtmlRendererUtils.ScriptContext;
import org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared.util.renderkit.HTML;

public final class HtmlJavaScriptUtils
{
    private static final Logger log = Logger.getLogger(HtmlJavaScriptUtils.class
            .getName());

    private static final String AUTO_SCROLL_PARAM = "autoScroll";
    private static final String AUTO_SCROLL_FUNCTION = "getScrolling";

    private static final String SET_HIDDEN_INPUT_FN_NAME = "oamSetHiddenInput";
    private static final String SET_HIDDEN_INPUT_FN_NAME_JSF2 = "myfaces.oam.setHiddenInput";

    private static final String FIRST_SUBMIT_SCRIPT_ON_PAGE = "org.apache.MyFaces.FIRST_SUBMIT_SCRIPT_ON_PAGE";
    private static final String CLEAR_HIDDEN_INPUT_FN_NAME = "oamClearHiddenInput";
    

    @SuppressWarnings("unchecked")
    public static void renderFormSubmitScript(FacesContext facesContext)
            throws IOException
    {
        if (facesContext.getPartialViewContext() != null && 
                (facesContext.getPartialViewContext().isPartialRequest() ||
                 facesContext.getPartialViewContext().isAjaxRequest() )
            )
        {
            return;
        }

        Map map = facesContext.getExternalContext().getRequestMap();
        Boolean firstScript = (Boolean) map.get(FIRST_SUBMIT_SCRIPT_ON_PAGE);

        if (firstScript == null || firstScript.equals(Boolean.TRUE))
        {
            map.put(FIRST_SUBMIT_SCRIPT_ON_PAGE, Boolean.FALSE);

            //we have to render the config just in case
            renderConfigOptionsIfNecessary(facesContext);
        }
    }
    
    private static void renderConfigOptionsIfNecessary(FacesContext facesContext)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext
                .getExternalContext());
        ScriptContext script = new ScriptContext();
        boolean autoSave = JavascriptUtils.isSaveFormSubmitLinkIE(facesContext
                .getExternalContext());

        if (autoSave)
        {
            script.prettyLine();
            script.increaseIndent();
            script.append("(!window.myfaces) ? window.myfaces = {} : null;");
            script.append("(!myfaces.core) ? myfaces.core = {} : null;");
            script.append("(!myfaces.core.config) ? myfaces.core.config = {} : null;");
            script.append("myfaces.core.config.ieAutoSave = true;");
            writer.startElement(HTML.SCRIPT_ELEM, null);
            writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
            writer.writeText(script.toString(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }
    
    public static void appendAutoScrollAssignment(StringBuilder onClickValue,
            String formName)
    {
        appendAutoScrollAssignment(FacesContext.getCurrentInstance(),
                new ScriptContext(onClickValue, false), formName);
    }

    /**
     * Adds the hidden form input value assignment that is necessary for the autoscroll
     * feature to an html link or button onclick attribute.
     */
    public static void appendAutoScrollAssignment(FacesContext context,
            StringBuilder onClickValue, String formName)
    {
        appendAutoScrollAssignment(context, new ScriptContext(onClickValue,
                false), formName);
    }
    
    private static void appendAutoScrollAssignment(FacesContext context,
            ScriptContext scriptContext, String formName)
    {
        String formNameStr = formName == null ? "formName" : (new StringBuilder(
                "'").append(formName).append("'").toString());
        String paramName = new StringBuilder().append("'")
                .append(AUTO_SCROLL_PARAM).append("'").toString();
        String value = new StringBuilder().append(AUTO_SCROLL_FUNCTION)
                .append("()").toString();

        scriptContext.prettyLine();
        scriptContext.append("if(typeof window." + AUTO_SCROLL_FUNCTION
                + "!='undefined')");
        scriptContext.append("{");
        scriptContext.append(SET_HIDDEN_INPUT_FN_NAME_JSF2);
        scriptContext.append("(").append(formNameStr).append(",")
                .append(paramName).append(",").append(value).append(");");
        scriptContext.append("}");

    }
    
    public static String getAutoScrollFunction(FacesContext facesContext)
    {
        ScriptContext script = new ScriptContext();

        script.prettyLineIncreaseIndent();

        script.append("function ");
        script.append(AUTO_SCROLL_FUNCTION);
        script.append("()");
        script.append("{");
        script.append("var x = 0; var y = 0;");
        script.append("if (self.pageXOffset || self.pageYOffset)");
        script.append("{");
        script.append("x = self.pageXOffset;");
        script.prettyLine();
        script.append("y = self.pageYOffset;");
        script.append("}");
        script.append(" else if ((document.documentElement && document.documentElement.scrollLeft)||"+
                "(document.documentElement && document.documentElement.scrollTop))");
        script.append("{");
        script.append("x = document.documentElement.scrollLeft;");
        script.prettyLine();
        script.append("y = document.documentElement.scrollTop;");
        script.append("}");
        script.append(" else if (document.body) ");
        script.append("{");
        script.append("x = document.body.scrollLeft;");
        script.prettyLine();
        script.append("y = document.body.scrollTop;");
        script.append("}");
        script.append("return x + \",\" + y;");
        script.append("}");

        ExternalContext externalContext = facesContext.getExternalContext();
        String oldViewId = JavascriptUtils.getOldViewId(externalContext);
        if (oldViewId != null
                && oldViewId.equals(facesContext.getViewRoot().getViewId()))
        {
            //ok, we stayed on the same page, so let's scroll it to the former place
            String scrolling = (String) externalContext
                    .getRequestParameterMap().get(AUTO_SCROLL_PARAM);
            if (scrolling != null && scrolling.length() > 0)
            {
                double x = 0;
                double y = 0;
                int comma = scrolling.indexOf(',');
                if (comma == -1)
                {
                    log.warning("Illegal autoscroll request parameter: "
                            + scrolling);
                }
                else
                {
                    try
                    {
                        //we convert to double against XSS vulnerability
                        x = Double.parseDouble(scrolling.substring(0, comma));
                    }
                    catch (NumberFormatException e)
                    {
                        log.warning("Error getting x offset for autoscroll feature. Bad param value: "
                                + scrolling);
                        x = 0; //ignore false numbers
                    }

                    try
                    {
                        //we convert to int against XSS vulnerability
                        y = Integer.parseInt(scrolling.substring(comma + 1));
                    }
                    catch (NumberFormatException e)
                    {
                        log.warning("Error getting y offset for autoscroll feature. Bad param value: "
                                + scrolling);
                        y = 0; //ignore false numbers
                    }
                }
                script.append("window.scrollTo(").append(String.valueOf(x)).append(",")
                        .append(String.valueOf(y)).append(");\n");
            }
        }

        return script.toString();
    }
    
    /**
     * Renders the hidden form input that is necessary for the autoscroll feature.
     */
    public static void renderAutoScrollHiddenInput(FacesContext facesContext,
            ResponseWriter writer) throws IOException
    {
        writer.startElement(HTML.INPUT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, AUTO_SCROLL_PARAM, null);
        writer.endElement(HTML.INPUT_ELEM);
    }

    /**
     * Renders the autoscroll javascript function.
     */
    public static void renderAutoScrollFunction(FacesContext facesContext,
            ResponseWriter writer) throws IOException
    {
        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR,
                HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
        writer.writeText(getAutoScrollFunction(facesContext), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }
    
    public static void appendClearHiddenCommandFormParamsFunctionCall(
            StringBuilder buf, String formName)
    {
        appendClearHiddenCommandFormParamsFunctionCall(new ScriptContext(buf,
                false), formName);
    }
    
    private static void appendClearHiddenCommandFormParamsFunctionCall(
            ScriptContext context, String formName)
    {
        String functionName = HtmlRendererUtils
                .getClearHiddenCommandFormParamsFunctionName(formName);
        if (formName == null)
        {
            context.prettyLine();
            context.append("var clearFn = ");
            context.append(functionName);
            context.append(";");
            context.prettyLine();
            context.append("if(typeof window[clearFn] =='function')");
            context.append("{");
            context.append("window[clearFn](formName);");
            context.append("}");
        }
        else
        {
            context.prettyLine();
            context.append("if(typeof window.");
            context.append(functionName);
            context.append("=='function')");
            context.append("{");
            context.append(functionName).append("('").append(formName)
                    .append("');");
            context.append("}");
        }
    }
    
    /**
     * Prefixes the given String with "clear_" and removes special characters
     *
     * @param formName
     * @return String
     */
    public static String getClearHiddenCommandFormParamsFunctionName(
            String formName)
    {
        final char separatorChar = FacesContext.getCurrentInstance().getNamingContainerSeparatorChar();
        if (formName == null)
        {
            return "'" + HtmlRendererUtils.CLEAR_HIDDEN_FIELD_FN_NAME
                    + "_'+formName.replace(/-/g, '\\$" + separatorChar
                    + "').replace(/" + separatorChar + "/g,'_')";
        }

        return JavascriptUtils
                .getValidJavascriptNameAsInRI(HtmlRendererUtils.CLEAR_HIDDEN_FIELD_FN_NAME + "_"
                        + formName.replace(separatorChar, '_'));
    }

    public static String getClearHiddenCommandFormParamsFunctionNameMyfacesLegacy(
            String formName)
    {
        return "clear_"
                + JavascriptUtils.getValidJavascriptName(formName, false);
    }
    
    /**
     * Render the javascript function that is called on a click on a commandLink
     * to clear the hidden inputs. This is necessary because on a browser back,
     * each hidden input still has it's old value (browser cache!) and therefore
     * a new submit would cause the according action once more!
     *
     * @param writer
     * @param formName
     * @param dummyFormParams
     * @param formTarget
     * @throws IOException
     */
    public static void renderClearHiddenCommandFormParamsFunction(
            ResponseWriter writer, String formName, Set dummyFormParams,
            String formTarget) throws IOException
    {
        //render the clear hidden inputs javascript function
        String functionName = getClearHiddenCommandFormParamsFunctionName(formName);
        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);

        // Using writeComment instead of write with <!-- tag
        StringBuilder script = new StringBuilder();
        script.append("function ");
        script.append(functionName);
        script.append("() {");
        if (dummyFormParams != null)
        {
            script.append("\n  var f = document.forms['");
            script.append(formName);
            script.append("'];");
            int i = 0;
            for (Iterator it = dummyFormParams.iterator(); it.hasNext();)
            {
                String elemVarName = "elem" + i;
                script.append("\n  var ").append(elemVarName).append(" = ");
                script.append("f.elements['").append((String) it.next())
                        .append("'];");
                script.append("\n  if(typeof ").append(elemVarName)
                        .append(" !='undefined' && ");
                script.append(elemVarName).append(".nodeName=='INPUT'){");
                script.append("\n   if (").append(elemVarName)
                        .append(".value != '') {");
                script.append("\n    " + elemVarName + ".value='';");
                script.append("\n   }");
                script.append("\n  }");
                i++;
            }
        }
        // clear form target
        script.append("\n  f.target=");
        if (formTarget == null || formTarget.length() == 0)
        {
            //Normally one would think that setting target to null has the
            //desired effect, but once again IE is different...
            //Setting target to null causes IE to open a new window!
            script.append("'';");
        }
        else
        {
            script.append("'");
            script.append(formTarget);
            script.append("';");
        }
        script.append("\n}");

        //Just to be sure we call this clear method on each load.
        //Otherwise in the case, that someone submits a form by pressing Enter
        //within a text input, the hidden inputs won't be cleared!
        script.append("\n");
        script.append(functionName);
        script.append("();");

        writer.writeText(script.toString(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }
    
    /**
     * This function correctly escapes the given JavaScript code
     * for the use in the jsf.util.chain() JavaScript function.
     * It also handles double-escaping correclty.
     *
     * @param javaScript
     * @return
     */
    public static String escapeJavaScriptForChain(String javaScript)
    {
        // first replace \' with \\'
        //String escaped = StringUtils.replace(javaScript, "\\'", "\\\\'");

        // then replace ' with \'
        // (this will replace every \' in the original to \\\')
        //escaped = StringUtils.replace(escaped, '\'', "\\'");

        //return escaped;

        StringBuilder out = null;
        for (int pos = 0; pos < javaScript.length(); pos++)
        {
            char c = javaScript.charAt(pos);

            if (c == '\\' || c == '\'')
            {
                if (out == null)
                {
                    out = new StringBuilder(javaScript.length() + 8);
                    if (pos > 0)
                    {
                        out.append(javaScript, 0, pos);
                    }
                }
                out.append('\\');
            }
            if (out != null)
            {
                out.append(c);
            }
        }

        if (out == null)
        {
            return javaScript;
        }
        else
        {
            return out.toString();
        }
    }
}
