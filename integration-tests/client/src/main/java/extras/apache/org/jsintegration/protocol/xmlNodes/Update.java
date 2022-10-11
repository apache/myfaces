/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol.xmlNodes;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.2 $ $Date: 2009/04/17 10:53:30 $
 */
public class Update implements Change{
    String id = "";
    String data = "";

    public Update(Changes parent, String id, String data) {
        this.id = id;
        this.data = data;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<update id='");
        builder.append(id);
        builder.append("'><![CDATA[");
        builder.append(data);
        builder.append("]]></update>");
        return builder.toString();
    }



}
