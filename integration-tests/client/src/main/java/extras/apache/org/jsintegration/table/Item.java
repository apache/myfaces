/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package extras.apache.org.jsintegration.table;

/**
 *
 * @author werpu2
 */
public class Item {

    Item(String a, String b, String c) {
        this.a = a;
        this.b = b;
        this.c = c;
        
    }
    String a = "";
    String b = "";
    String c = "";

    public String getA() {
        return a;
    }
    
    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }



    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }
}
