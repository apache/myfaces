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
package javax.faces.view.facelets;

import javax.faces.view.EditableValueHolderAttachedObjectHandler;
import javax.faces.view.facelets.FaceletContext;

/**
 * Handles setting a Validator instance on a EditableValueHolder. Will wire all attributes set to the Validator instance
 * created/fetched. Uses the "binding" attribute for grabbing instances to apply attributes to. <p/> Will only
 * set/create Validator is the passed UIComponent's parent is null, signifying that it wasn't restored from an existing
 * tree.
 * 
 * @author Jacob Hookom
 * @version $Id: ValidateHandler.java,v 1.4 2008/07/13 19:01:46 rlubke Exp $
 */
public class ValidatorHandler extends FaceletsAttachedObjectHandler implements EditableValueHolderAttachedObjectHandler
{
    public ValidatorHandler(ValidatorConfig config)
    {
        super(config);

        // TODO: IMPLEMENT HERE
    }

    public String getValidatorId(FaceletContext ctx)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    protected TagHandlerDelegate getTagHandlerHelper()
    {
        // TODO: IMPLEMENT HERE
        return null;
    }
}
