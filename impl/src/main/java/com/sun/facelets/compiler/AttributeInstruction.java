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
import java.util.List;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELAdaptor;
import com.sun.facelets.el.ELText;

final class AttributeInstruction implements Instruction
{
    private final String alias;

    private final String attr;

    private final ELText txt;

    public AttributeInstruction(String alias, String attr, ELText txt)
    {
        this.alias = alias;
        this.attr = attr;
        this.txt = txt;
    }

    public void write(FacesContext context) throws IOException
    {
        ResponseWriter out = context.getResponseWriter();
        try
        {
            ELContext elContext = ELAdaptor.getELContext(context);
            String val = txt.toString(elContext);

            out.writeAttribute(attr, val, null);
        }
        catch (ELException e)
        {
            throw new ELException(this.alias + ": " + e.getMessage(), e.getCause());
        }
        catch (Exception e)
        {
            throw new ELException(this.alias + ": " + e.getMessage(), e);
        }
    }

    public Instruction apply(ExpressionFactory factory, ELContext ctx)
    {
        ELText nt = this.txt.apply(factory, ctx);
        if (nt == this.txt)
        {
            return this;
        }

        return new AttributeInstruction(alias, attr, nt);
    }

    public boolean isLiteral()
    {
        return txt.isLiteral();
    }
}
