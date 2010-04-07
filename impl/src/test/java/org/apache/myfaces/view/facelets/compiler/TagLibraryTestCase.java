package org.apache.myfaces.view.facelets.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.myfaces.shared_impl.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.xml.sax.SAXException;


public class TagLibraryTestCase extends FaceletTestCase
{
    public final static String TAGLIB_SCHEMA_PATH = "/org/apache/myfaces/resource/web-facelettaglibrary_2_0.xsd";
    
    private URL _validLibUrl = null;
    private URL _invalidLibUrl = null;
    private URL _invalidOldLibUrl = null;

    public void setUp() throws Exception {
        super.setUp();
        _validLibUrl = resolveUrl("/testlib.taglib.xml");
        _invalidLibUrl = resolveUrl("/testlib_invalid.taglib.xml");
        _invalidOldLibUrl = resolveUrl("/testlib_old_invalid.taglib.xml");        

        // set document root for loading schema file as resource
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = cl.getResource(TAGLIB_SCHEMA_PATH.substring(1)).getPath();
        File documentRoot = new File(path.substring(0, path.indexOf(TAGLIB_SCHEMA_PATH)));
        servletContext.setDocumentRoot(documentRoot);
    }

    public void testLoadValidLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "true");

        TagLibrary lib = TagLibraryConfig.create(_validLibUrl);
        assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib"));
    }

    public void testLoadValidLibraryWithoutValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "false");

        TagLibrary lib = TagLibraryConfig.create(_validLibUrl);
        assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib"));
    }
    /*
    public void testLoadInvalidLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "true");

        try {
            TagLibraryConfig.create(_invalidLibUrl);
            fail("IOException expected");
        } catch (IOException ioe) {
            assertTrue(ioe.getCause() instanceof SAXException);
        }

    }

    public void testLoadInvalidLibraryWithoutValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "false");

        TagLibrary lib = TagLibraryConfig.create(_invalidLibUrl);
        assertTrue(lib.containsNamespace("http://myfaces.apache.org/testlib_invalid"));
    }

    public void testLoadInvalidOldLibraryWithValidation() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_VALIDATE_XML, "true");

        try {
            TagLibraryConfig.create(_invalidOldLibUrl);
            fail("IOException expected");
        } catch (IOException ioe) {
            assertTrue(ioe.getCause() instanceof SAXException);
        }
    }
    */
}
