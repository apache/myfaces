/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.ExternalContext;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.myfaces.util.lang.ClassUtils;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigFilesXmlValidationUtils
{
    public final static LSResourceResolver LS_RESOURCE_RESOLVER = new ValidatorLSResourceResolver();
    public final static ErrorHandler VALIDATION_ERROR_HANDLER = new ValidationErrorHandler();

    private final static Logger log = Logger.getLogger(ConfigFilesXmlValidationUtils.class.getName());

    private final static String FACES_CONFIG_SCHEMA_PATH_12 = "org/apache/myfaces/resource/web-facesconfig_1_2.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_20 = "org/apache/myfaces/resource/web-facesconfig_2_0.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_21 = "org/apache/myfaces/resource/web-facesconfig_2_1.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_22 = "org/apache/myfaces/resource/web-facesconfig_2_2.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_23 = "org/apache/myfaces/resource/web-facesconfig_2_3.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_30 = "org/apache/myfaces/resource/web-facesconfig_3_0.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_40 = "org/apache/myfaces/resource/web-facesconfig_4_0.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_41 = "org/apache/myfaces/resource/web-facesconfig_4_1.xsd";
    private final static String FACES_CONFIG_SCHEMA_PATH_50 = "org/apache/myfaces/resource/web-facesconfig_5_0.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH = "org/apache/myfaces/resource/web-facelettaglibrary_2_0.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_20 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_2_0.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_22 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_2_2.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_23 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_2_3.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_30 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_3_0.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_40 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_4_0.xsd";
    private final static String FACES_TAGLIB_SCHEMA_PATH_41 = 
                                                        "org/apache/myfaces/resource/web-facelettaglibrary_4_1.xsd";

    public static class LSInputImpl implements LSInput
    {
        private final String _publicId;
        private final String _systemId;
        private final String _baseURI;
        private final InputStream _input;

        public LSInputImpl(String publicId,String systemId, String baseURI, InputStream input)
        {
            super();
            _publicId = publicId;
            _systemId = systemId;
            _baseURI = baseURI;
            _input = input;
        }

        @Override
        public String getBaseURI()
        {
            return _baseURI;
        }

        @Override
        public InputStream getByteStream()
        {
            return _input;
        }

        @Override
        public boolean getCertifiedText()
        {
            return false;
        }

        @Override
        public Reader getCharacterStream()
        {
            return null;
        }

        @Override
        public String getEncoding()
        {
            return null;
        }

        @Override
        public String getPublicId()
        {
            return _publicId;
        }

        @Override
        public String getStringData()
        {
            return null;
        }

        @Override
        public String getSystemId()
        {
            return _systemId;
        }

        @Override
        public void setBaseURI(String baseURI)
        {
        }

        @Override
        public void setByteStream(InputStream byteStream)
        {
        }

        @Override
        public void setCertifiedText(boolean certifiedText)
        {
        }

        @Override
        public void setCharacterStream(Reader characterStream)
        {
        }

        @Override
        public void setEncoding(String encoding)
        {
        }

        @Override
        public void setPublicId(String publicId)
        {
        }

        @Override
        public void setStringData(String stringData)
        {
        }

        @Override
        public void setSystemId(String systemId)
        {
        }
    }

    public static class ValidatorLSResourceResolver implements LSResourceResolver
    {
        @Override
        public LSInput resolveResource(String type, String namespaceURI,
                String publicId, String systemId, String baseURI)
        {
            if ("http://www.w3.org/TR/REC-xml".equals(type) && "datatypes.dtd".equals(systemId))
            {
                return new LSInputImpl(publicId, systemId, baseURI,
                        ClassUtils.getResourceAsStream("org/apache/myfaces/resource/datatypes.dtd"));
            }
            if ("-//W3C//DTD XMLSCHEMA 200102//EN".equals(publicId) && "XMLSchema.dtd".equals(systemId))
            {
                return new LSInputImpl(publicId, systemId, baseURI,
                        ClassUtils.getResourceAsStream("org/apache/myfaces/resource/XMLSchema.dtd"));
            }
            if ("http://java.sun.com/xml/ns/javaee".equals(namespaceURI))
            {
                if ("javaee_5.xsd".equals(systemId))
                {
                    return new LSInputImpl(publicId, systemId, baseURI,
                            ClassUtils.getResourceAsStream("org/apache/myfaces/resource/javaee_5.xsd"));
                }
            }
            if ("http://xmlns.jcp.org/xml/ns/javaee".equals(namespaceURI))
            {
                 if ("javaee_7.xsd".equals(systemId))
                 {
                     return new LSInputImpl(publicId, systemId, baseURI,
                             ClassUtils.getResourceAsStream("org/apache/myfaces/resource/javaee_7.xsd"));
                 }
                 if ("javaee_8.xsd".equals(systemId))
                 {
                     return new LSInputImpl(publicId, systemId, baseURI,
                             ClassUtils.getResourceAsStream("org/apache/myfaces/resource/javaee_8.xsd"));
                 }
                 if ("javaee_web_services_client_1_4.xsd".equals(systemId))
                 {
                    String location = "org/apache/myfaces/resource/javaee_web_services_client_1_4.xsd";
                    return new LSInputImpl(publicId, systemId, baseURI,
                            ClassUtils.getResourceAsStream(location));
                 }

             }
             if ("https://jakarta.ee/xml/ns/jakartaee".equals(namespaceURI))
             {
                 if ("jakartaee_9.xsd".equals(systemId))
                 {
                     return new LSInputImpl(publicId, systemId, baseURI,
                             ClassUtils.getResourceAsStream("org/apache/myfaces/resource/jakartaee_9.xsd"));
                 }
                 if ("jakartaee_10.xsd".equals(systemId))
                 {
                     return new LSInputImpl(publicId, systemId, baseURI,
                             ClassUtils.getResourceAsStream("org/apache/myfaces/resource/jakartaee_10.xsd"));
                 }
                 if ("jakartaee_11.xsd".equals(systemId))
                 {
                     return new LSInputImpl(publicId, systemId, baseURI,
                             ClassUtils.getResourceAsStream("org/apache/myfaces/resource/jakartaee_11.xsd"));
                 }
                 if ("jakartaee_web_services_client_2_0.xsd".equals(systemId))
                 {
                    String location = "org/apache/myfaces/resource/jakartaee_web_services_client_2_0.xsd";
                    return new LSInputImpl(publicId, systemId, baseURI,
                            ClassUtils.getResourceAsStream(location));
                 }
             }
            if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI))
            {
                return new LSInputImpl(publicId, systemId, baseURI,
                        ClassUtils.getResourceAsStream("org/apache/myfaces/resource/xml.xsd"));
            }
            
            return null;
        }

    }

    public static class ValidationErrorHandler implements ErrorHandler
    {
        @Override
        public void fatalError(SAXParseException exception) throws SAXException
        {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException
        {
            Logger log = Logger.getLogger(ConfigFilesXmlValidationUtils.class.getName());
            log.log(Level.SEVERE, exception.getMessage(), exception);
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException
        {
            Logger log = Logger.getLogger(ConfigFilesXmlValidationUtils.class.getName());
            log.log(Level.WARNING, exception.getMessage(), exception);
        }
    }

    public static void validateFacesConfigFile(URL xmlFile,ExternalContext externalContext, String version)
            throws SAXException, IOException
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source schemaFile = getFacesConfigSchemaFileAsSource(externalContext, version);
        if (schemaFile == null)
        {
            throw new IOException("Could not find schema file for validation.");
        }

        schemaFactory.setResourceResolver(LS_RESOURCE_RESOLVER);
        Schema schema = schemaFactory.newSchema(schemaFile);

        Validator validator = schema.newValidator();
        URLConnection conn = xmlFile.openConnection();
        conn.setUseCaches(false);
        try (InputStream is = conn.getInputStream())
        {
            Source source = new StreamSource(is);
            validator.setErrorHandler(VALIDATION_ERROR_HANDLER);
            validator.validate(source);
        }
    }

    private static Source getFacesConfigSchemaFileAsSource(ExternalContext externalContext, String version)
    {
        String xmlSchema = "1.2".equals(version)
                            ? FACES_CONFIG_SCHEMA_PATH_12
                            : ("2.0".equals(version) ? FACES_CONFIG_SCHEMA_PATH_20
                            : ("2.1".equals(version) ? FACES_CONFIG_SCHEMA_PATH_21
                            : ("2.2".equals(version) ? FACES_CONFIG_SCHEMA_PATH_22
                            : ("2.3".equals(version) ? FACES_CONFIG_SCHEMA_PATH_23
                            : ("3.0".equals(version) ? FACES_CONFIG_SCHEMA_PATH_30
                            : ("4.0".equals(version) ? FACES_CONFIG_SCHEMA_PATH_40
                            : ("4.1".equals(version) ? FACES_CONFIG_SCHEMA_PATH_41 : FACES_CONFIG_SCHEMA_PATH_50)))))));

        InputStream stream = ClassUtils.getResourceAsStream(xmlSchema);

        if (stream == null)
        {
           stream = externalContext.getResourceAsStream(xmlSchema);
        }

        if (stream == null)
        {
            return null;
        }

        return new StreamSource(stream);
    }

    public static final String getFacesConfigVersion(URL url)
    {
        String result = "5.0";

        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            FacesConfigVersionCheckHandler handler = new FacesConfigVersionCheckHandler();

            // We need to create a non-validating, non-namespace aware parser used to simply check
            // which version of the facelets taglib document we are dealing with.

            factory.setNamespaceAware(false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setValidating(false);
            try
            {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                factory.setXIncludeAware(false);
            }
            catch (Throwable e)
            {
                log.log(Level.WARNING, "SAXParserFactory#setFeature not implemented. Skipping...", e);
            }

            parser = factory.newSAXParser();

            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            try (InputStream input = conn.getInputStream())
            {
                try
                {
                    parser.parse(input, handler);
                }
                catch (SAXException e)
                {
                    // This is as a result of our aborted parse, so ignore.
                }
            }

            if (handler.isVersion11())
            {
                return "1.1";
            }
            else if (handler.isVersion12())
            {
                return "1.2";
            }
            else if (handler.isVersion20())
            {
                return "2.0";
            }
            else if (handler.isVersion21())
            {
                return "2.1";
            }
            else if (handler.isVersion22())
            {
                return "2.2";
            }
            else if (handler.isVersion23())
            {
                return "2.3";
            }
            else if (handler.isVersion30())
            {
                return "3.0";
            }
            else if (handler.isVersion40())
            {
                return "4.0";
            }
            else if (handler.isVersion41())
            {
                return "4.1";
            }
            else if (handler.isVersion50OrLater())
            {
                return "5.0";
            }
        }
        catch (Throwable e)
        {
            // Most likely a result of our aborted parse, so ignore.
        }

        return result;
    }

    private static class FacesConfigVersionCheckHandler extends DefaultHandler
    {
        private boolean version11;
        private boolean version12;
        private boolean version20;
        private boolean version21;
        private boolean version22;
        private boolean version23;
        private boolean version30;
        private boolean version40;
        private boolean version41;
        private boolean version50OrLater;

        public boolean isVersion11()
        {
            return this.version11;
        }

        public boolean isVersion12()
        {
            return this.version12;
        }

        public boolean isVersion20()
        {
            return this.version20;
        }

        public boolean isVersion21()
        {
            return this.version21;
        }

        public boolean isVersion22()
        {
            return this.version22;
        }

        public boolean isVersion23()
        {
            return this.version23;
        }

        public boolean isVersion30()
        {
            return this.version30;
        }

        public boolean isVersion40()
        {
            return this.version40;
        }

        public boolean isVersion41()
        {
            return this.version41;
        }

        public boolean isVersion50OrLater()
        {
            return this.version50OrLater;
        }

        protected void reset()
        {
            this.version11 = false;
            this.version12 = false;
            this.version20 = false;
            this.version21 = false;
            this.version22 = false;
            this.version23 = false;
            this.version30 = false;
            this.version40 = false;
            this.version41 = false;
            this.version50OrLater = false;
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes attributes) throws SAXException
        {
            if (name.equals("faces-config"))
            {
                int length = attributes.getLength();

                for (int i = 0; i < length; i++)
                {
                    String attrName = attributes.getLocalName(i);
                    attrName = (attrName != null) ? ((attrName.length() > 0) ? attrName
                            : attributes.getQName(i))
                            : attributes.getQName(i);
                    if (attrName.equals("version"))
                    {
                        if (attributes.getValue(i).equals("1.1"))
                        {
                            reset();
                            this.version11 = true;
                        }
                        else if (attributes.getValue(i).equals("1.2"))
                        {
                            reset();
                            this.version12 = true;
                        }
                        else if (attributes.getValue(i).equals("2.0"))
                        {
                            reset();
                            this.version20 = true;
                        }
                        else if (attributes.getValue(i).equals("2.1"))
                        {
                            reset();
                            this.version21 = true;
                        }
                        else if (attributes.getValue(i).equals("2.2"))
                        {
                            reset();
                            this.version22 = true;
                        }
                        else if (attributes.getValue(i).equals("2.3"))
                        {
                            reset();
                            this.version23 = true;
                        }
                        else if (attributes.getValue(i).equals("3.0"))
                        {
                            reset();
                            this.version30 = true;
                        }
                        else if (attributes.getValue(i).equals("4.0"))
                        {
                            reset();
                            this.version40 = true;
                        }
                        else if (attributes.getValue(i).equals("4.1"))
                        {
                            reset();
                            this.version41 = true;
                        }
                        else
                        {
                            reset();
                            this.version50OrLater = true;
                        }
                    }
                }
            }
        }
    }

    public static void validateFaceletTagLibFile(URL xmlFile, ExternalContext externalContext, Double version)
        throws SAXException, IOException, ParserConfigurationException
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source schemaFile = getFaceletSchemaFileAsSource(externalContext, version);
        if (schemaFile == null)
        {
            throw new IOException("Could not find schema file for validation.");
        }
        schemaFactory.setResourceResolver(ConfigFilesXmlValidationUtils.LS_RESOURCE_RESOLVER);
        Schema schema = schemaFactory.newSchema(schemaFile);

        Validator validator = schema.newValidator();
        URLConnection conn = xmlFile.openConnection();
        conn.setUseCaches(false);
        try (InputStream is = conn.getInputStream())
        {
            Source source = new StreamSource(is);
            validator.setErrorHandler(VALIDATION_ERROR_HANDLER);
            validator.validate(source);
        }
    }

    private static Source getFaceletSchemaFileAsSource(ExternalContext externalContext, Double version)
    {
        String tagLibraryPath = "";

        if(version == 4.1)
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_41;
        } 
        else if(version == 4.0)
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_40;
        }
        else if(version == 3.0)
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_30;
        }
        else if(version == 2.3)
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_23;
        }
        else if(version == 2.2)
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_22;
        }
        else
        {
            tagLibraryPath = FACES_TAGLIB_SCHEMA_PATH_20;
        }

        InputStream stream = ClassUtils.getResourceAsStream(tagLibraryPath);

        if (stream == null)
        {
           stream = externalContext.getResourceAsStream(tagLibraryPath);
        }

        if (stream == null)
        {
            return null;
        }

        return new StreamSource(stream);
    }

    public static Double getTagLibVersion(URL url)
    {
        TagLibVersionHandler handler = new TagLibVersionHandler();
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            // We need to create a non-validating, non-namespace aware parser used to simply
            // check
            // which version of the facelets taglib document we are dealing with.
            factory.setNamespaceAware(false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setValidating(false);
            try
            {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                factory.setXIncludeAware(false);
            } 
            catch (Throwable e)
            {
                log.log(Level.WARNING, "SAXParserFactory#setFeature not implemented. Skipping...", e);
            }
            parser = factory.newSAXParser();

            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            try (InputStream input = conn.getInputStream())
            {
                try 
                {
                    parser.parse(input, handler);
                }
                catch (SAXException e)
                {
                    // This is as a result of our aborted parse, so ignore.
                }
            }

            return handler.getVersion();
        } 
        catch (Throwable e)
        {
            // Most likely a result of our aborted parse, so ignore.
        }

        return handler.getDefaultVersion();
    }

    /*
     * We need this class to do a quick check on a facelets taglib document to see
     * if it's
     * a pre-2.0 document. If it is, we really need to construct a DTD validating,
     * non-namespace
     * aware parser. Otherwise, we have to construct a schema validating,
     * namespace-aware parser.
     */
    private static class TagLibVersionHandler extends DefaultHandler
    {
        private double version;
        private double defaultVersion = 1.0;
        private ArrayList<Double> acceptedVersions;

        public TagLibVersionHandler()
        {
            acceptedVersions = new ArrayList<Double>();
            // 1.0/1.1 isn't necessary as the version attribute wasn't required prior to 2.0
            acceptedVersions.add(2.0);
            acceptedVersions.add(2.2);
            acceptedVersions.add(2.3);
            acceptedVersions.add(3.0);
            acceptedVersions.add(4.0);
            acceptedVersions.add(4.1);
        }

        public Double getVersion()
        {
            if (acceptedVersions.contains(this.version))
            {
                return this.version;
            }
            else
            {
                return getDefaultVersion();
            }
        }

        public Double getDefaultVersion()
        {
            return defaultVersion;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
        {
            if (name.equals("facelet-taglib"))
            {
                int length = attributes.getLength();

                for (int i = 0; i < length; i++)
                {
                    String attrName = attributes.getLocalName(i);
                    attrName = attrName != null
                            ? ((attrName.length() > 0) ? attrName : attributes.getQName(i))
                            : attributes.getQName(i);
                    if (attrName.equals("version"))
                    {
                        try
                        {
                            this.version = Double.parseDouble(attributes.getValue(i));
                        }
                        catch (Exception e)
                        {
                           this.version = getDefaultVersion();
                        }
                    }
                }

                // Throw a dummy parsing exception to terminate parsing as there really isn't
                // any need to go any
                // further.
                // -= Leonardo Uribe =- THIS IS NOT GOOD PRACTICE! It is better to let the
                // checker continue that
                // throw an exception, and run this one only when project stage != production.
                // throw new SAXException();
            }
        }
    }
}
