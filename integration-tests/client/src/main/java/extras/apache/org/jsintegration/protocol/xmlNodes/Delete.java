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
public class Delete implements Change {

    private String id = "";

    public Delete(Changes parent, String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<delete id='");
        retVal.append(id);
        retVal.append("'/>");
        return retVal.toString();
    }
}
