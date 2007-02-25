package org.apache.myfaces.application.pss;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Martin Haimberger
 */
public class EncodeAllComponentUtil {
    /**
     * ensure that this util class can not be instanciated
     */
    private EncodeAllComponentUtil(){}

    /**
     * Encodes a whole UI-Component Tree or a part of the tree.
     * @param context The facescontext
     * @param component The base of the tree or the part or the tree
     * @throws IOException thrown Exception
     */

    public static void encodeAll(FacesContext context, UIComponent component)
    throws IOException
    {
        if (!component.isRendered()) {
            return;
        }

        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        }
        else if (component.getChildCount() > 0) {
                Iterator kids = component.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    encodeAll(context,kid);
                }
            }

        component.encodeEnd(context);
    }
}
