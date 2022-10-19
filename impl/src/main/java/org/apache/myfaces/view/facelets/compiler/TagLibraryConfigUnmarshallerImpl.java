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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.ExternalContext;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.myfaces.config.ConfigFilesXmlValidationUtils;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;
import org.apache.myfaces.config.impl.element.facelets.FaceletBehaviorTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletComponentTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletConverterTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletFunctionImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletHandlerTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletSourceTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletTagImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletTagLibraryImpl;
import org.apache.myfaces.config.impl.element.facelets.FaceletValidatorTagImpl;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class TagLibraryConfigUnmarshallerImpl
{
    private static final Logger log = Logger.getLogger(TagLibraryConfigUnmarshallerImpl.class.getName());

    public static FaceletTagLibrary create(ExternalContext externalContext, URL url) throws IOException
    {
        InputStream is = null;
        FaceletTagLibrary t = null;
        URLConnection conn = null;
        try
        {
            boolean schemaValidating = false;

            // validate XML
            if (MyfacesConfig.getCurrentInstance(externalContext).isValidateXML())
            {
                String version = ConfigFilesXmlValidationUtils.getFaceletTagLibVersion(url);
                schemaValidating = "2.0".equals(version);
                if (schemaValidating)
                {
                    ConfigFilesXmlValidationUtils.validateFaceletTagLibFile(url, externalContext, version);
                }
            }
            
            // parse file
            LibraryHandler handler = new LibraryHandler(url);
            SAXParser parser = createSAXParser(handler, externalContext, schemaValidating);
            conn = url.openConnection();
            conn.setUseCaches(false);
            is = conn.getInputStream();
            parser.parse(is, handler);
            t = handler.getLibrary();
        }
        catch (SAXException e)
        {
            IOException ioe = new IOException("Error parsing [" + url + "]: ");
            ioe.initCause(e);
            throw ioe;
        }
        catch (ParserConfigurationException e)
        {
            IOException ioe = new IOException("Error parsing [" + url + "]: ");
            ioe.initCause(e);
            throw ioe;
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        return t;
    }    
    
    private static final SAXParser createSAXParser(LibraryHandler handler, ExternalContext externalContext,
                                                   boolean schemaValidating)
            throws SAXException, ParserConfigurationException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
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
        if (MyfacesConfig.getCurrentInstance(externalContext).isValidateXML() && !schemaValidating)
        {
            // DTD validating
            factory.setNamespaceAware(false);
            factory.setFeature("http://xml.org/sax/features/validation", true);
            factory.setValidating(true);
        }
        else
        {
            //Just parse it and do not validate, because it is not necessary.
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setValidating(false);
        }

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }

    private static class LibraryHandler extends DefaultHandler
    {
        private final URL source;
        private FaceletTagLibraryImpl library;
        private final StringBuffer buffer;
        private Locator locator;
        private String tagName;
        private String converterId;
        private String validatorId;
        private String behaviorId;
        private String componentType;
        private String rendererType;
        private String functionName;
        private String handlerClass;
        private String functionClass;
        private String functionSignature;
        private String resourceId;
       
        public LibraryHandler(URL source)
        {
            this.source = source;
            this.buffer = new StringBuffer(64);
        }

        public FaceletTagLibrary getLibrary()
        {
            return this.library;
        }
        
        private FaceletTagLibraryImpl getLibraryImpl()
        {
            if (this.library == null)
            {
                this.library = new FaceletTagLibraryImpl();
            }
            return this.library;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            try
            {
                if ("facelet-taglib".equals(qName))
                {
                    // Nothing to do
                }                
                else if ("library-class".equals(qName))
                {
                    getLibraryImpl().setLibraryClass(this.captureBuffer());
                }
                else if ("short-name".equals(qName))
                {
                    getLibraryImpl().setShortName(this.captureBuffer());
                }
                else if ("namespace".equals(qName))
                {
                    getLibraryImpl().setNamespace(this.captureBuffer());
                }
                else if ("composite-library-name".equals(qName))
                {
                    getLibraryImpl().setCompositeLibraryName(this.captureBuffer());
                }
                else if ("component-type".equals(qName))
                {
                    this.componentType = this.captureBuffer();
                }
                else if ("renderer-type".equals(qName))
                {
                    this.rendererType = this.captureBufferEmptyNull();
                }
                else if ("tag-name".equals(qName))
                {
                    this.tagName = this.captureBuffer();
                }
                else if ("function-name".equals(qName))
                {
                    this.functionName = this.captureBuffer();
                }
                else if ("function-class".equals(qName))
                {
                    //String className = this.captureBuffer();
                    //this.functionClass = createClass(Object.class, className);
                    this.functionClass = this.captureBuffer();
                }
                else if ("description".equals(qName))
                {
                    //Not used
                }
                else if ("display-name".equals(qName))
                {
                    //Not used
                }
                else if ("icon".equals(qName))
                {
                    //Not used
                }                
                else if ("resource-id".equals(qName))
                {
                    this.resourceId = this.captureBuffer();
                }
                else
                {
                    // Make sure there we've seen a namespace element
                    // before trying any of the following elements to avoid
                    // obscure NPEs
                    if (this.library == null)
                    {
                        throw new IllegalStateException("No <namespace> element");
                    }
                    else if (this.library.getNamespace() == null)
                    {
                        throw new IllegalStateException("No <namespace> element");
                    }

                    if ("tag".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            getLibraryImpl().addTag(
                                new FaceletTagImpl(this.tagName, 
                                    new FaceletHandlerTagImpl(this.handlerClass)) );
                            this.handlerClass = null;
                        }
                    }
                    else if ("handler-class".equals(qName))
                    {
                        this.handlerClass = this.captureBufferEmptyNull();
                    }
                    else if ("component".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletComponentTagImpl(this.componentType, this.rendererType, 
                                    this.handlerClass, null)));
                            this.handlerClass = null;
                        }
                        else if (this.resourceId != null)
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletComponentTagImpl(null, null, null, this.resourceId)));
                            this.resourceId = null;
                            this.handlerClass = null;
                        }
                        else
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletComponentTagImpl(this.componentType, this.rendererType, null, null)));
                            this.handlerClass = null;
                        }
                    }
                    else if ("converter-id".equals(qName))
                    {
                        this.converterId = this.captureBuffer();
                    }
                    else if ("converter".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletConverterTagImpl(this.converterId, this.handlerClass)));
                            this.handlerClass = null;
                        }
                        else
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletConverterTagImpl(this.converterId)));
                        }
                        this.converterId = null;
                    }
                    else if ("validator-id".equals(qName))
                    {
                        this.validatorId = this.captureBuffer();
                    }
                    else if ("validator".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletValidatorTagImpl(this.validatorId, this.handlerClass)));
                            this.handlerClass = null;
                        }
                        else
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletValidatorTagImpl(this.validatorId)));
                        }
                        this.validatorId = null;
                    }
                    else if ("behavior-id".equals(qName))
                    {
                        this.behaviorId = this.captureBuffer();
                    }
                    else if ("behavior".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletBehaviorTagImpl(this.behaviorId, this.handlerClass)));
                            this.handlerClass = null;
                        }
                        else
                        {
                            getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                                new FaceletBehaviorTagImpl(this.behaviorId)));
                        }
                        this.behaviorId = null;
                    }
                    else if ("source".equals(qName))
                    {
                        String path = this.captureBuffer();
                        URL url = new URL(this.source, path);
                        getLibraryImpl().addTag(new FaceletTagImpl(this.tagName,
                            new FaceletSourceTagImpl(url.toString())));
                    }
                    else if ("function-signature".equals(qName))
                    {
                        this.functionSignature = this.captureBuffer();
                        getLibraryImpl().addFunction(
                            new FaceletFunctionImpl(this.functionName, this.functionClass, functionSignature));
                    }
                }
            }
            catch (Exception e)
            {
                throw new SAXParseException("Error Handling [" + this.source + '@' + this.locator.getLineNumber()
                        + ',' + this.locator.getColumnNumber() + "] <" + qName + '>', locator, e);
            }
        }

        private String captureBuffer() throws Exception
        {
            String s = this.buffer.toString().trim();
            if (s.length() == 0)
            {
                throw new Exception("Value Cannot be Empty");
            }
            this.buffer.setLength(0);
            return s;
        }
        
        private String captureBufferEmptyNull() throws Exception
        {
            String s = this.buffer.toString().trim();
            if (s.length() == 0)
            {
                //if is "" just set null instead
                s = null;
            }
            this.buffer.setLength(0);
            return s;
        }        

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException
        {
            if ("-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN".equals(publicId))
            {
                URL url = ClassUtils.getResource("org/apache/myfaces/resource/facelet-taglib_1_0.dtd");
                return new InputSource(url.toExternalForm());
            }
            return null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            this.buffer.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            this.buffer.setLength(0);
            if ("tag".equals(qName))
            {
                this.handlerClass = null;
                this.componentType = null;
                this.rendererType = null;
                this.tagName = null;
            }
            else if ("function".equals(qName))
            {
                this.functionName = null;
                this.functionClass = null;
                this.functionSignature = null;
            }
        }

        @Override
        public void error(SAXParseException e) throws SAXException
        {
            throw new SAXException(
                    "Error Handling [" + this.source + '@' + e.getLineNumber() + ',' + e.getColumnNumber() + ']', e);
        }

        @Override
        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException
        {
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException
        {
            throw e;
        }
    }

}
