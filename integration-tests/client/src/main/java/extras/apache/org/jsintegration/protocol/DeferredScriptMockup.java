/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extras.apache.org.jsintegration.protocol;

import extras.apache.org.jsintegration.protocol.responseWriter.PartialResponseWriterImpl;

import java.io.IOException;

/**
 *
 * @author werpu
 *
 * code to test script deferring if a script
 * and script src tag is discovered
 */
public class DeferredScriptMockup
{

    public String testScriptMockup() throws IOException {
        PartialResponseWriterImpl writer = new PartialResponseWriterImpl();
        writer.startDocument();
        writer.startUpdate(PartialResponseWriterImpl.RENDER_ALL_MARKER);
        writer.startElement("h1", null);
        writer.write("Dies ist ein text");
        writer.endElement("h1");
        
        writer.startElement("script",null);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("alert('hello world');");
        writer.endElement("script");

        writer.startElement("h1", null);
        writer.write("Dies ist ein text");
        writer.endElement("h1");


        writer.startEval();
        writer.write("alert('hello world from eval');");
        writer.endEval();

        writer.endUpdate();

        writer.endDocument();

        return writer.toString();

    }

}
