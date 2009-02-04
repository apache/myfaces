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

package com.sun.facelets.compiler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import javax.faces.webapp.pdl.facelets.FaceletHandler;
import com.sun.facelets.el.ELText;
import com.sun.facelets.tag.TextHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;
import com.sun.facelets.util.FastWriter;

/**
 * @author Jacob Hookom
 * @version $Id: UITextHandler.java,v 1.10 2008/07/13 19:01:33 rlubke Exp $
 */
final class UITextHandler extends AbstractUIHandler
{

    private final ELText txt;

    private final String alias;

    private final int length;

    public UITextHandler(String alias, ELText txt)
    {
        this.alias = alias;
        this.txt = txt;
        this.length = txt.toString().length();
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent != null)
        {
            try
            {
                ELText nt = this.txt.apply(ctx.getExpressionFactory(), ctx);
                UIComponent c = new UIText(this.alias, nt);
                c.setId(ComponentSupport.getViewRoot(ctx, parent).createUniqueId());
                this.addComponent(ctx, parent, c);
            }
            catch (Exception e)
            {
                throw new ELException(this.alias + ": " + e.getMessage(), e.getCause());
            }
        }
    }

    public String toString()
    {
        return this.txt.toString();
    }

    public String getText()
    {
        return this.txt.toString();
    }

    public String getText(FaceletContext ctx)
    {
        Writer writer = new FastWriter(this.length);
        try
        {
            this.txt.apply(ctx.getExpressionFactory(), ctx).write(writer, ctx);
        }
        catch (IOException e)
        {
            throw new ELException(this.alias + ": " + e.getMessage(), e.getCause());
        }
        return writer.toString();
    }
}
