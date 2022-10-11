/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package extras.apache.org.jsintegration.protocol.responseWriter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author werpu
 */
public class MockupResponseWriter extends ResponseWriter {

    StringBuilder target = new StringBuilder();
    boolean _openTag = false;

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public String getCharacterEncoding() {
        return "utf-8";
    }

    @Override
    public void flush() throws IOException {
    }

    

    @Override
    public void startDocument() throws IOException {
        target.append("<html><head></head><body>\n");
    }

    @Override
    public void endDocument() throws IOException {
        target.append("</body></html>");

    }

    @Override
    public void startElement(String arg0, UIComponent arg1) throws IOException {
        if (_openTag) {
            target.append(">");

        }

        target.append("<");
        target.append(arg0);
        _openTag = true;
    }

    @Override
    /*for testing purposes this is not 100% correct we have to use the nesting depth
     to check for
     but it is ok for testing*/
    public void endElement(String arg0) throws IOException {
        if(_openTag) {
            target.append("/>");
        } else {
            target.append("</");
            target.append(arg0);
            target.append("/>");
        }
    }

    @Override
    public void writeAttribute(String arg0, Object arg1, String arg2) throws IOException {
        if(arg1 instanceof String) {
            target.append(" ");
            target.append(arg0);
            target.append("=");
            target.append("'");
            target.append(arg1);
            target.append("'");
            target.append(" ");
        } else {
            target.append(" ");
            target.append(arg0);
            target.append("=");
            target.append(arg1.toString());
            target.append(" ");
        }
    }

    @Override
    public void writeURIAttribute(String arg0, Object arg1, String arg2) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeComment(Object arg0) throws IOException {
       if(_openTag) {
           _openTag = false;
           target.append(">");
       }
       target.append("<!--");
       target.append(arg0.toString());
       target.append("//-->");
    }

    @Override
    public void writeText(Object arg0, String arg1) throws IOException {
       if(_openTag) {
           _openTag = false;
           target.append(">");
       }

       target.append(arg1.toString());
     
    }

    @Override
    public void writeText(char[] arg0, int arg1, int arg2) throws IOException {
       if(_openTag) {
           _openTag = false;
           target.append(">");
       }
       target.append(arg0);

    }

    @Override
    public ResponseWriter cloneWithWriter(Writer arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        target.append(cbuf);
    }

    @Override
    public void close() throws IOException {
    }

    public StringBuilder getTarget() {
        return target;
    }
}
