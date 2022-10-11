/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package extras.apache.org.jsintegration.protocol.xmlNodes;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.2 $ $Date: 2009/04/17 10:27:53 $
 */
public class PartialResponse {

    List<Object> elements = new LinkedList<Object>();

    public void addElement(Object element) {
        elements.add(element);
    }


    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        retVal.append("<partial-response>");

        for (Object element : elements) {
            retVal.append(element.toString());
        }

        retVal.append("</partial-response>");

        return retVal.toString();
    }
}
