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
 * @version $Revision: 1.1 $ $Date: 2009/04/16 15:45:19 $
 */
public class Changes {

    PartialResponse parent;
    List<Change> childs = new LinkedList<Change>();

    public Changes(PartialResponse parent) {
        this.parent = parent;
    }

    public void addChild(Change child) {
        childs.add(child);
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<changes>");
        for (Change child : childs) {
            retVal.append(child.toString());
        }
        retVal.append("</changes>");
        return retVal.toString();
    }
}
