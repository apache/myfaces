/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol;

/**
 * 
 *
 * @author Werner Punz(latest modification by $Author: werpu $)
 * @version $Revision: 1.1 $ $Date: 2009/04/17 10:27:53 $
 */
public class StringUtils
{

    public static Boolean isBlank(String in) {

        return in == null || in.trim().equals("");

    }

}
