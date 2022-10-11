/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol.xmlNodes;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/17 10:27:53 $
 */
public class ErrorResponse {

    String errorMessage = "";
    String errorName = "";

    public ErrorResponse(PartialResponse parent, String errorMessage, String errorName) {
        this.errorMessage = errorMessage;
        this.errorName = errorName;
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<error>");
        retVal.append("<error-name>");
        retVal.append(errorName);
        retVal.append("</error-name>");

        retVal.append("<error-message><![CDATA[");
        retVal.append(errorMessage);
        retVal.append("]]></error-message>");
        retVal.append("</error>");

        return retVal.toString();
    }



}
