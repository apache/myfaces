/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package extras.apache.org.jsintegration.protocol.xmlNodes;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/16 15:45:19 $
 */
public class Attributes implements Change {

    String id = "";
    List<Attribute> attributes = new LinkedList<Attribute>();

    public Attributes(Changes parent, String id) {
        this.id = id;
    }

    public void addAttribute(Attribute attr) {
        attributes.add(attr);
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<attributes id='");
        retVal.append(id);
        retVal.append("'>");
        for (Attribute attribute : attributes) {
            retVal.append(attribute.toString());
        }
        retVal.append("</attributes>");

        return retVal.toString();
    }
}
