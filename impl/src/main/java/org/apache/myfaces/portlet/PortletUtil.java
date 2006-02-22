package org.apache.myfaces.portlet;


import javax.faces.context.FacesContext;
import javax.portlet.RenderResponse;

/**
 * Static utility class for portlet-related operations.
 *
 * @author  Stan Silvert
 * @deprecated These utils work only with the MyFaces implementation and therefore should be moved to myfaces-impl in the future
 */
public final class PortletUtil {
    
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