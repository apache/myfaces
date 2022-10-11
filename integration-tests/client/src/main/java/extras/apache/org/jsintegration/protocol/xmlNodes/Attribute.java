/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol.xmlNodes;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/16 15:45:19 $
 */
public class Attribute {
    String name = "";
    String value = "";

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<attribute name='");
        retVal.append(name);
        retVal.append("' value='");
        retVal.append(value.replaceAll("'","\""));
        retVal.append("' />");

        return retVal.toString();
    }

}
