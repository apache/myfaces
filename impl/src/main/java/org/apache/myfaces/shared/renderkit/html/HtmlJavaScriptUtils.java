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

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.shared.renderkit.html.HtmlRendererUtils.ScriptContext;
import org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared.renderkit.html.util.HTML;

public final class HtmlJavaScriptUtils
{
    private static final Logger log = Logger.getLogger(HtmlJavaScriptUtils.class.getName());

    private static final String FIRST_SUBMIT_SCRIPT_ON_PAGE = "org.apache.MyFaces.FIRST_SUBMIT_SCRIPT_ON_PAGE";
    

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
            return '\'' + HtmlRendererUtils.CLEAR_HIDDEN_FIELD_FN_NAME
                    + "_'+formName.replace(/-/g, '\\$" + separatorChar
                    + "').replace(/" + separatorChar + "/g,'_')";
        }

        return JavascriptUtils
                .getValidJavascriptNameAsInRI(HtmlRendererUtils.CLEAR_HIDDEN_FIELD_FN_NAME + '_'
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
                script.append("\n    ").append(elemVarName).append(".value='';");
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
            script.append('\'');
            script.append(formTarget);
            script.append("';");
        }
        script.append("\n}");

        //Just to be sure we call this clear method on each load.
        //Otherwise in the case, that someone submits a form by pressing Enter
        //within a text input, the hidden inputs won't be cleared!
        script.append('\n');
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
