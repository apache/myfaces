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

package com.sun.facelets.tag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.el.ELException;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import com.sun.facelets.TemplateClient;
import com.sun.facelets.el.VariableMapperWrapper;
import com.sun.facelets.tag.ui.DefineHandler;

/**
 * A Tag that is specified in a FaceletFile. Takes all attributes specified and sets them on the FaceletContext before
 * including the targeted Facelet file.
 * 
 * @author Jacob Hookom
 * @version $Id: UserTagHandler.java,v 1.12 2008/07/13 19:01:35 rlubke Exp $
 */
final class UserTagHandler extends TagHandler implements TemplateClient
{

    protected final TagAttribute[] vars;

    protected final URL location;

    protected final Map handlers;

    /**
     * @param config
     */
    public UserTagHandler(TagConfig config, URL location)
    {
        super(config);
        this.vars = this.tag.getAttributes().getAll();
        this.location = location;
        Iterator itr = this.findNextByType(DefineHandler.class);
        if (itr.hasNext())
        {
            handlers = new HashMap();

            DefineHandler d = null;
            while (itr.hasNext())
            {
                d = (DefineHandler) itr.next();
                this.handlers.put(d.getName(), d);
            }
        }
        else
        {
            handlers = null;
        }
    }

    /**
     * Iterate over all TagAttributes and set them on the FaceletContext's VariableMapper, then include the target
     * Facelet. Finally, replace the old VariableMapper.
     * 
     * @see TagAttribute#getValueExpression(FaceletContext, Class)
     * @see VariableMapper
     * @see javax.faces.webapp.pdl.facelets.FaceletHandler#apply(javax.faces.webapp.pdl.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        VariableMapper orig = ctx.getVariableMapper();

        // setup a variable map
        if (this.vars.length > 0)
        {
            VariableMapper varMapper = new VariableMapperWrapper(orig);
            for (int i = 0; i < this.vars.length; i++)
            {
                varMapper.setVariable(this.vars[i].getLocalName(), this.vars[i].getValueExpression(ctx, Object.class));
            }
            ctx.setVariableMapper(varMapper);
        }

        // eval include
        try
        {
            ctx.pushClient(this);
            ctx.includeFacelet(parent, this.location);
        }
        catch (FileNotFoundException e)
        {
            throw new TagException(this.tag, e.getMessage());
        }
        finally
        {

            // make sure we undo our changes
            ctx.popClient(this);
            ctx.setVariableMapper(orig);
        }
    }

    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException,
            FaceletException, ELException
    {
        if (name != null)
        {
            if (this.handlers == null)
            {
                return false;
            }
            DefineHandler handler = (DefineHandler) this.handlers.get(name);
            if (handler != null)
            {
                handler.applyDefinition(ctx, parent);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            this.nextHandler.apply(ctx, parent);
            return true;
        }
    }

}
