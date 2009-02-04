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

package com.sun.facelets.tag.jsf.core;

import javax.faces.validator.Validator;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.MetaRuleset;
import com.sun.facelets.tag.jsf.ValidateHandler;
import com.sun.facelets.tag.jsf.ValidatorConfig;

/**
 * Register a named Validator instance on the UIComponent associated with the closest parent UIComponent custom
 * action.<p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/validator.html">tag documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: ValidateDelegateHandler.java,v 1.4 2008/07/13 19:01:44 rlubke Exp $
 */
public final class ValidateDelegateHandler extends ValidateHandler
{

    private final TagAttribute validatorId;

    public ValidateDelegateHandler(ValidatorConfig config)
    {
        super(config);
        this.validatorId = this.getRequiredAttribute("validatorId");
    }

    /**
     * Uses the specified "validatorId" to get a new Validator instance from the Application.
     * 
     * @see javax.faces.application.Application#createValidator(java.lang.String)
     * @see com.sun.facelets.tag.jsf.ValidateHandler#createValidator(javax.faces.webapp.pdl.facelets.FaceletContext)
     */
    protected Validator createValidator(FaceletContext ctx)
    {
        return ctx.getFacesContext().getApplication().createValidator(this.validatorId.getValue(ctx));
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {
        return super.createMetaRuleset(type).ignoreAll();
    }

}
