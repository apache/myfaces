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

package com.sun.facelets.tag.jstl.core;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

/**
 * @author Jacob Hookom
 * @version $Id: CatchHandler.java,v 1.5 2008/07/13 19:01:43 rlubke Exp $
 */
public final class CatchHandler extends TagHandler
{

    private final TagAttribute var;

    /**
     * @param config
     */
    public CatchHandler(TagConfig config)
    {
        super(config);
        this.var = this.getAttribute("var");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        try
        {
            this.nextHandler.apply(ctx, parent);
        }
        catch (Exception e)
        {
            if (this.var != null)
            {
                ctx.setAttribute(this.var.getValue(ctx), e);
            }
        }
    }

}
