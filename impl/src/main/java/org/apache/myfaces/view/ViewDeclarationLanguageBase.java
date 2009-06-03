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
package org.apache.myfaces.view;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.List;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.MethodExpressionValidator;
import javax.faces.view.ActionSource2AttachedObjectHandler;
import javax.faces.view.ActionSource2AttachedObjectTarget;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.AttachedObjectTarget;
import javax.faces.view.EditableValueHolderAttachedObjectHandler;
import javax.faces.view.EditableValueHolderAttachedObjectTarget;
import javax.faces.view.ValueHolderAttachedObjectHandler;
import javax.faces.view.ValueHolderAttachedObjectTarget;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.InvalidViewIdException;
import org.apache.myfaces.application.ViewHandlerImpl;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-22 15:03:20 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class ViewDeclarationLanguageBase extends ViewDeclarationLanguage
{
    
    /**
     * Process the specification required algorithm that is generic to all PDL.
     * 
     * @param context
     * @param viewId
     */
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        checkNull(context, "context");
        checkNull(viewId, "viewId");

        try
        {
            viewId = calculateViewId(context, viewId);
            Application application = context.getApplication();

            // Create a new UIViewRoot object instance using Application.createComponent(UIViewRoot.COMPONENT_TYPE).
            UIViewRoot newViewRoot = (UIViewRoot) application.createComponent(UIViewRoot.COMPONENT_TYPE);
            UIViewRoot oldViewRoot = context.getViewRoot();
            if (oldViewRoot == null)
            {
                // If not, this method must call calculateLocale() and calculateRenderKitId(), and store the results
                // as the values of the locale and renderKitId, proeprties, respectively, of the newly created
                // UIViewRoot.
                ViewHandler handler = application.getViewHandler();
                newViewRoot.setLocale(handler.calculateLocale(context));
                newViewRoot.setRenderKitId(handler.calculateRenderKitId(context));
            }
            else
            {
                // If there is an existing UIViewRoot available on the FacesContext, this method must copy its locale
                // and renderKitId to this new view root
                newViewRoot.setLocale(oldViewRoot.getLocale());
                newViewRoot.setRenderKitId(oldViewRoot.getRenderKitId());
            }
            
            // TODO: VALIDATE - The spec is silent on the following line, but I feel bad if I don't set it
            newViewRoot.setViewId(viewId);

            return newViewRoot;
        }
        catch (InvalidViewIdException e)
        {
            // If no viewId could be identified, or the viewId is exactly equal to the servlet mapping, 
            // send the response error code SC_NOT_FOUND with a suitable message to the client.
            sendSourceNotFound(context, e.getMessage());
            
            // TODO: VALIDATE - Spec is silent on the return value when an error was sent
            return null;
        }
    }

    protected abstract String calculateViewId(FacesContext context, String viewId);
    
    protected abstract void sendSourceNotFound(FacesContext context, String message);
        
    protected void checkNull(final Object o, final String param)
    {
        if (o == null)
        {
            throw new NullPointerException(param + " can not be null.");
        }
    }
}
