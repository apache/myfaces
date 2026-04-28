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
package org.apache.myfaces.test.facelets.taghandler;

import java.io.IOException;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * Facelets tag handler mirroring OmniFaces {@code o:tagAttribute} for MYFACES-4585/4589 integration tests.
 * <p>
 * Bindings are applied with {@link org.apache.myfaces.view.facelets.TemplateContext#setParameter} (same mechanism
 * as {@code UserTagHandler}), not by replacing {@link FaceletContext#getVariableMapper()}. Installing a delegating
 * mapper for taglib attribute names leaks across {@code cc:insertChildren} and shadows consumer EL (MYFACES-4589).
 * </p>
 * <p>
 * Derived from OmniFaces {@code org.omnifaces.taghandler.TagAttribute} (Apache License 2.0, Copyright OmniFaces).
 * </p>
 *
 * @author Arjan Tijms (OmniFaces)
 */
public class TagAttributeHandler extends TagHandler
{

    private final String name;
    private final TagAttribute defaultValue;

    public TagAttributeHandler(TagConfig config)
    {
        super(config);
        name = getRequiredAttribute("name").getValue();
        defaultValue = getAttribute("default");
    }

    @Override
    public void apply(FaceletContext context, UIComponent parent) throws IOException
    {
        AbstractFaceletContext actx = (AbstractFaceletContext) context;
        VariableMapper currentMapper = context.getVariableMapper();
        DelegatingVariableMapper probe = new DelegatingVariableMapper(currentMapper);
        ValueExpression valueExpression = probe.resolveWrappedVariable(name, context);

        if (valueExpression == null)
        {
            if (defaultValue != null)
            {
                valueExpression = defaultValue.getValueExpression(context, Object.class);
            }
            else if ("id".equals(name))
            {
                valueExpression = createValueExpression(context,
                        "#{'j_ido" + context.generateUniqueId(this.tagId) + "'}", String.class);
                actx.getTemplateContext().setParameter(name, valueExpression);
                return;
            }
        }

        if (valueExpression != null)
        {
            actx.getTemplateContext().setParameter(name, valueExpression);
        }
    }

    private static ValueExpression createValueExpression(FaceletContext context, String expression, Class<?> type)
    {
        FacesContext facesContext = context.getFacesContext();
        return facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), expression, type);
    }
}
