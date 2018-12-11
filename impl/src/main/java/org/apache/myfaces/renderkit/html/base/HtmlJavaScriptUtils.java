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

import org.apache.myfaces.renderkit.html.util.JavascriptContext;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.apache.myfaces.renderkit.html.util.JavascriptUtils;

public final class HtmlJavaScriptUtils
{
    private static final Logger log = Logger.getLogger(HtmlJavaScriptUtils.class.getName());
    
    public static void appendClearHiddenCommandFormParamsFunctionCall(
            StringBuilder buf, String formName)
    {
        appendClearHiddenCommandFormParamsFunctionCall(new JavascriptContext(buf, false), formName);
    }
    
    private static void appendClearHiddenCommandFormParamsFunctionCall(JavascriptContext context, String formName)
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
    public static String getClearHiddenCommandFormParamsFunctionName(String formName)
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

    public static String getClearHiddenCommandFormParamsFunctionNameMyfacesLegacy(String formName)
    {
        return "clear_" + JavascriptUtils.getValidJavascriptName(formName, false);
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
