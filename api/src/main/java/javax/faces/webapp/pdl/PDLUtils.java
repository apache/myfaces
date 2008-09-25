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
package javax.faces.webapp.pdl;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 19:55:27 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class PDLUtils
{
    public PDLUtils()
    {

    }

    public static void retargetAttachedObjects(FacesContext context, UIComponent topLevelComponent,
                                               List<AttachedObjectHandler> handlers)
    {
        /*
         * 1. Obtain the metadata for the composite component. Currently this entails getting the value of 
         *    the UIComponent.BEANINFO_KEY component attribute, which will be an instance of BeanInfo. If 
         *    the metadata cannot be found, log an error message and return.
         * 
         * 2. Get the BeanDescriptor from the BeanInfo.
         * 
         * 3. Get the value of the AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY from the BeanDescriptor's 
         *    getValue() method. This will be a List<AttachedObjectTarget>. Let this be targetList.
         * 
         * 4. For each curHandler entry in the argument handlers:
         *      - Let forAttributeValue be the return from AttachedObjectHandler.getFor().
         *      - For each curTarget entry in targetList, the first of the following items that causes a match 
         *        will take this action:
         *            + For each UIComponent in the list returned from curTarget.getTargets(), call 
         *              curHandler.applyAttachedObject(), passing the FacesContext and the UIComponent and cause 
         *              this inner loop to terminate.
         *            + If curHandler is an instance of ActionSource2AttachedObjectHandler and curTarget is an instance 
         *              of ActionSource2AttachedObjectTarget, consider it a match.
         *            + If curHandler is an instance of EditableValueHolderAttachedObjectHandler and curTarget is an 
         *              instance of EditableValueHolderAttachedObjectTarget, consider it a match.
         *            + If curHandler is an instance of ValueHolderAttachedObjectHandler and curTarget is an instance 
         *              of ValueHolderAttachedObjectTarget, consider it a match.
         */
        
        // TODO: JSF 2.0 #41
    }
}
