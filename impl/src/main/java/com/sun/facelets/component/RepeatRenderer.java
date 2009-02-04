/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.sun.facelets.tag.jsf.ComponentSupport;
import com.sun.facelets.util.FacesAPI;

public class RepeatRenderer extends Renderer
{

    public RepeatRenderer()
    {
        super();
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {

    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        if (component.getChildCount() > 0)
        {
            Map a = component.getAttributes();
            String tag = (String) a.get("alias.element");
            if (tag != null)
            {
                ResponseWriter out = context.getResponseWriter();
                out.startElement(tag, component);
                String[] attrs = (String[]) a.get("alias.attributes");
                String attr;
                if (attrs != null)
                {
                    for (int i = 0; i < attrs.length; i++)
                    {
                        attr = attrs[i];
                        if ("styleClass".equals(attr))
                        {
                            attr = "class";
                        }
                        out.writeAttribute(attr, a.get(attrs[i]), attrs[i]);
                    }
                }
            }

            Iterator itr = component.getChildren().iterator();
            UIComponent c;
            while (itr.hasNext())
            {
                c = (UIComponent) itr.next();
                if (FacesAPI.getVersion() >= 12)
                {
                    c.encodeAll(context);
                }
                else
                {
                    ComponentSupport.encodeRecursive(context, c);
                }
            }

            if (tag != null)
            {
                context.getResponseWriter().endElement(tag);
            }
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {

    }

    public boolean getRendersChildren()
    {
        return true;
    }

}
