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
public class Insert implements Change {

    String id = "";
    String before = "";
    String after = "";
    String data = "";

    public Insert(Changes parent, String id, String data, String before, String after) {
        super();
        this.id = id;
        this.before = before;
        this.after = after;
        this.data = data;
    }

    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<insert id='");
        builder.append(id);
        builder.append("' ");
        if (before != null && !before.trim().equals("")) {
            builder.append(" before='");
            builder.append(before);
            builder.append("' >");
        } else {
            builder.append(" after='");
            builder.append(after);
            builder.append("' >");
        }
        builder.append("<![CDATA[");
        builder.append(data);
        builder.append("]]>");
        builder.append("</insert>");

        return builder.toString();
    }
}
