package org.apache.myfaces.renderkits;

import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author martin.haimberger
 */
public class OwnRenderKitImpl
        extends RenderKit {

    RenderKit renderKit = new HtmlRenderKitImpl();

    public Renderer getRenderer(String componentFamily, String rendererType) {
        OwnRenderkitTest.SetIsOwnRenderKit();
        return renderKit.getRenderer(componentFamily, rendererType);
    }

    public void addRenderer(String componentFamily, String rendererType, Renderer renderer) {
        renderKit.addRenderer(componentFamily, rendererType, renderer);
    }

    public ResponseStateManager getResponseStateManager() {
        return renderKit.getResponseStateManager();
    }

    public ResponseWriter createResponseWriter(Writer writer,
                                               String contentTypeListString,
                                               String characterEncoding) {


        return renderKit.createResponseWriter(writer, contentTypeListString, characterEncoding);
    }

    public ResponseStream createResponseStream(OutputStream outputStream) {
        final OutputStream output = outputStream;

        return new ResponseStream() {
            public void write(int b) throws IOException {
                output.write(b);
            }


            public void write(byte b[]) throws IOException {
                output.write(b);
            }


            public void write(byte b[], int off, int len) throws IOException {
                output.write(b, off, len);
            }


            public void flush() throws IOException {
                output.flush();
            }


            public void close() throws IOException {
                output.close();
            }
        };
    }


}
