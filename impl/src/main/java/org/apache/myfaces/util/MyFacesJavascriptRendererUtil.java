/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.util;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.renderkit.html.HTML;
import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.renderkit.html.util.DummyFormUtils;
import org.apache.myfaces.renderkit.html.util.HtmlBufferResponseWriterWrapper;
import org.apache.myfaces.renderkit.html.util.JavascriptUtils;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;


/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyFacesJavascriptRendererUtil
{

    /**
     * Renders stuff such as the dummy form and the autoscroll javascript, which goes before the closing &lt;/body&gt;
     * @throws IOException
     */
    public static void renderCodeBeforeBodyEnd(FacesContext facesContext) throws IOException
    {
        Object myFacesJavascript = facesContext.getExternalContext().getRequestMap().get("org.apache.myfaces.myFacesJavascript");

        if (myFacesJavascript != null)
        {
            return;
        }

        ResponseWriter responseWriter = facesContext.getResponseWriter();
        HtmlBufferResponseWriterWrapper writerWrapper = HtmlBufferResponseWriterWrapper
                    .getInstance(responseWriter);

        if (DummyFormUtils.isWriteDummyForm(facesContext))
        {
            DummyFormUtils.writeDummyForm(writerWrapper, DummyFormUtils.getDummyFormParameters(facesContext));
        }

        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(facesContext.getExternalContext());
        if (myfacesConfig.isDetectJavascript())
        {
            if (! JavascriptUtils.isJavascriptDetected(facesContext.getExternalContext()))
            {

                writerWrapper.startElement(HTML.SCRIPT_ELEM,null);
                writerWrapper.writeAttribute(HTML.SCRIPT_TYPE_ATTR,HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT,null);
                StringBuffer script = new StringBuffer();
                script.append("document.location.replace('").
                        append(facesContext.getApplication().getViewHandler().getResourceURL(facesContext, "/_javascriptDetector_")).append("?goto=").append(facesContext.getApplication().getViewHandler().getActionURL(facesContext, facesContext.getViewRoot().getViewId())).append("');");
                writerWrapper.writeText(script.toString(),null);
                writerWrapper.endElement(HTML.SCRIPT_ELEM);
            }
        }

        if (myfacesConfig.isAutoScroll())
        {
            JavascriptUtils.renderAutoScrollFunction(facesContext, writerWrapper);
        }

        facesContext.getExternalContext().getRequestMap().put("org.apache.myfaces.myFacesJavascript", "<!-- MYFACES JAVASCRIPT -->\n"+writerWrapper.toString()+"\n");
    }
}
