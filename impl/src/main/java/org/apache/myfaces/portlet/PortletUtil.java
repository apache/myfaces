/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.portlet;

import javax.faces.context.FacesContext;
import javax.portlet.RenderResponse;

/**
 * Static utility class for portlet-related operations.
 *
 * @author  Stan Silvert
 */
public class PortletUtil {
    
        /** This flag is imbedded in the request.
         *  It signifies to MyFaces that the request is coming from a portlet.
         */
        public static final String PORTLET_REQUEST_FLAG = 
           PortletUtil.class.getName() + ".PORTLET_REQUEST_FLAG";
    
        /** Don't allow a new instance of PortletUtil */
        private PortletUtil() {
        }
        
        /**
         * Determine if we are processing a portlet RenderResponse.
         *
         * @param facesContext The current FacesContext.
         * @return <code>true</code> if we are processing a RenderResponse,
         *         <code>false</code> otherwise.
         */
        public static boolean isRenderResponse(FacesContext facesContext) {
            if (!isPortletRequest(facesContext)) return false;
            
            return facesContext.getExternalContext().getResponse() instanceof RenderResponse;
        }
        
        /**
         * Determine if we are running as a portlet.
         *
         * @param facesContext The current FacesContext.
         * @return <code>true</code> if we are running as a portlet,
         *         <code>false</code> otherwise.
         */
        public static boolean isPortletRequest(FacesContext facesContext) {
            return facesContext.getExternalContext()
                               .getSessionMap()
                               .get(PORTLET_REQUEST_FLAG) != null;
        }
    }