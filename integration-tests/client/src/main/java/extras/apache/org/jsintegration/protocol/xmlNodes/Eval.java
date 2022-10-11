/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol.xmlNodes;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.3 $ $Date: 2009/04/17 10:53:30 $
 */
public class Eval implements Change
{

    String javascriptText = "";

    public Eval(Changes parent, String javascript) {
        javascriptText = javascript;
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<eval><![CDATA[");
        retVal.append(javascriptText);
        retVal.append("]]></eval>");
        
        return retVal.toString();
    }


}
