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
package org.apache.myfaces.core.api.shared;

import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialResponseWriter;

import java.util.Collection;
import java.util.Set;

public class ResetValuesUtils
{
    private ResetValuesUtils()
    {

    }

    public static void resetValues(FacesContext context, UIViewRoot viewRoot,
                                   Collection<String> clientIds)
    {
        resetValues(context, viewRoot, clientIds, null);
    }

    public static void resetValues(FacesContext context, UIViewRoot viewRoot,
                                   Collection<String> clientIds, Set<VisitHint> visitHints)
    {
        if (clientIds == null || clientIds.isEmpty())
        {
            return;
        }

        VisitContext visitContext = null;
        ResetInputContextCallback contextCallback = null;

        for (String clientId : clientIds)
        {
            if (clientId == null || clientId.isBlank() || "@none".equals(clientId))
            {
                continue;
            }

            // lazy init
            if (visitContext == null)
            {
                visitContext = VisitContext.createVisitContext(context, null, visitHints);
            }

            if ("@all".equals(clientId) || PartialResponseWriter.RENDER_ALL_MARKER.equals(clientId))
            {
                viewRoot.visitTree(visitContext, ResetInputVisitCallback.INSTANCE);
            }
            else
            {
                // lazy init
                if (contextCallback == null)
                {
                    contextCallback = new ResetInputContextCallback(visitContext);
                }

                viewRoot.invokeOnComponent(context, clientId, contextCallback);
            }
        }
    }

    public static class ResetInputContextCallback implements ContextCallback
    {
        private VisitContext visitContext;

        /**
         * Constructs a new ResetInputContextCallback with the given {@link VisitContext}.
         *
         * @param visitContext the visit context to be used for visiting component trees
         */
        public ResetInputContextCallback(VisitContext visitContext)
        {
            this.visitContext = visitContext;
        }

        /**
         * Invokes the context callback on the given component. If the component is an instance
         * of {@link EditableValueHolder}, its value is reset. Otherwise, the component's tree
         * is visited using the {@link ResetInputVisitCallback} instance.
         *
         * @param fc the current {@link FacesContext}
         * @param component the component on which to invoke the context callback
         */
        @Override
        public void invokeContextCallback(FacesContext fc, UIComponent component)
        {
            if (component instanceof EditableValueHolder)
            {
                ((EditableValueHolder) component).resetValue();
            }
            else
            {
                component.visitTree(visitContext, ResetInputVisitCallback.INSTANCE);
            }
        }
    }

    public static class ResetInputVisitCallback implements VisitCallback
    {
        public static final ResetInputVisitCallback INSTANCE = new ResetInputVisitCallback();

        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            if (target instanceof EditableValueHolder)
            {
                EditableValueHolder input = (EditableValueHolder) target;
                input.resetValue();
            }

            return VisitResult.ACCEPT;
        }
    }
}
