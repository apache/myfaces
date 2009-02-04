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

package com.sun.facelets.el;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.facelets.util.FacesAPI;

/**
 * 
 * 
 * @author Jacob Hookom
 * @version $Id: ELAdaptor.java,v 1.9 2008/07/13 19:01:43 rlubke Exp $
 */
public final class ELAdaptor
{

    private static final boolean ELSUPPORT = (FacesAPI.getVersion() >= 12);

    private final static String LEGACY_ELCONTEXT_KEY = "com.sun.facelets.legacy.ELCONTEXT";

    public ELAdaptor()
    {
        super();
    }

    public final static ELContext getELContext(FacesContext faces)
    {
        if (ELSUPPORT)
        {
            return faces.getELContext();
        }
        else
        {
            Map request = faces.getExternalContext().getRequestMap();
            Object ctx = request.get(LEGACY_ELCONTEXT_KEY);
            if (!(ctx instanceof LegacyELContext) || (((LegacyELContext) ctx).getFacesContext() != faces))
            {
                ctx = new LegacyELContext(faces);
                request.put(LEGACY_ELCONTEXT_KEY, ctx);
            }
            return (ELContext) ctx;
        }
    }

    public final static void setExpression(UIComponent c, String name, ValueExpression ve)
    {
        if (FacesAPI.getComponentVersion(c) >= 12)
        {
            c.setValueExpression(name, ve);
        }
        else
        {
            c.setValueBinding(name, new LegacyValueBinding(ve));
        }
    }

}
