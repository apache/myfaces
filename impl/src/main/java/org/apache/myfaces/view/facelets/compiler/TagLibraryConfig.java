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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.view.facelets.TagHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.view.facelets.tag.AbstractTagLibrary;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.apache.myfaces.view.facelets.util.Classpath;
import org.apache.myfaces.view.facelets.util.ParameterCheck;
import org.apache.myfaces.view.facelets.util.ReflectionUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles creating a {@link org.apache.myfaces.view.facelets.tag.TagLibrary TagLibrary} from a {@link java.net.URL URL} source.
 * 
 * @author Jacob Hookom
 * @version $Id: TagLibraryConfig.java,v 1.14 2008/07/13 19:01:34 rlubke Exp $
 */
public final class TagLibraryConfig
{

    private final static String SUFFIX = ".taglib.xml";

    protected final static Logger log = Logger.getLogger("facelets.compiler");

    private static class TagLibraryImpl extends AbstractTagLibrary
    {
        public TagLibraryImpl(String namespace)
        {
            super(namespace);
        }

        public void putConverter(String name, String id)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            this.addConverter(name, id);
        }

        public void putConverter(String name, String id, Class<? extends TagHandler> handlerClass)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            ParameterCheck.notNull("handlerClass", handlerClass);
            this.addConverter(name, id, handlerClass);
        }

        public void putValidator(String name, String id)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            this.addValidator(name, id);
        }

        public void putValidator(String name, String id, Class<? extends TagHandler> handlerClass)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            ParameterCheck.notNull("handlerClass", handlerClass);
            this.addValidator(name, id, handlerClass);
        }

        public void putTagHandler(String name, Class<? extends TagHandler> type)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("type", type);
            this.addTagHandler(name, type);
        }

        public void putComponent(String name, String componentType, String rendererType)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("componentType", componentType);
            this.addComponent(name, componentType, rendererType);
        }

        public void putComponent(String name, String componentType, String rendererType, 
                                 Class<? extends TagHandler> handlerClass)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("componentType", componentType);
            ParameterCheck.notNull("handlerClass", handlerClass);
            this.addComponent(name, componentType, rendererType, handlerClass);
        }

        public void putUserTag(String name, URL source)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("source", source);
            this.addUserTag(name, source);
        }

        public void putFunction(String name, Method method)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("method", method);
            this.addFunction(name, method);
        }
    }

    private static class LibraryHandler extends DefaultHandler
    {
        private final URL source;

        private TagLibrary library;

        private final StringBuffer buffer;

        private Locator locator;

        private String tagName;

        private String converterId;

        private String validatorId;

        private String componentType;

        private String rendererType;

        private String functionName;

        private Class<? extends TagHandler> handlerClass;

        private Class<?> functionClass;

        private String functionSignature;

        public LibraryHandler(URL source)
        {
            this.source = source;
            this.buffer = new StringBuffer(64);
        }

        public TagLibrary getLibrary()
        {
            return this.library;
        }

        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            try
            {
                if ("facelet-taglib".equals(qName))
                {
                    ; // Nothing to do
                }
                else if ("library-class".equals(qName))
                {
                    this.processLibraryClass();
                }
                else if ("namespace".equals(qName))
                {
                    this.library = new TagLibraryImpl(this.captureBuffer());
                }
                else if ("component-type".equals(qName))
                {
                    this.componentType = this.captureBuffer();
                }
                else if ("renderer-type".equals(qName))
                {
                    this.rendererType = this.captureBuffer();
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
                    String className = this.captureBuffer();
                    this.functionClass = createClass(Object.class, className);
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

                    TagLibraryImpl impl = (TagLibraryImpl) this.library;

                    if ("tag".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            impl.putTagHandler(this.tagName, this.handlerClass);
                        }
                    }
                    else if ("handler-class".equals(qName))
                    {
                        String cName = this.captureBuffer();
                        this.handlerClass = createClass(TagHandler.class, cName);
                    }
                    else if ("component".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            impl.putComponent(this.tagName, this.componentType, this.rendererType, this.handlerClass);
                            this.handlerClass = null;
                        }
                        else
                        {
                            impl.putComponent(this.tagName, this.componentType, this.rendererType);
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
                            impl.putConverter(this.tagName, this.converterId, handlerClass);
                            this.handlerClass = null;
                        }
                        else
                        {
                            impl.putConverter(this.tagName, this.converterId);
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
                            impl.putValidator(this.tagName, this.validatorId, handlerClass);
                            this.handlerClass = null;
                        }
                        else
                        {
                            impl.putValidator(this.tagName, this.validatorId);
                        }
                        this.validatorId = null;
                    }
                    else if ("source".equals(qName))
                    {
                        String path = this.captureBuffer();
                        URL url = new URL(this.source, path);
                        impl.putUserTag(this.tagName, url);
                    }
                    else if ("function-signature".equals(qName))
                    {
                        this.functionSignature = this.captureBuffer();
                        Method m = createMethod(this.functionClass, this.functionSignature);
                        impl.putFunction(this.functionName, m);
                    }
                }
            }
            catch (Exception e)
            {
                SAXException saxe = new SAXException("Error Handling [" + this.source + "@"
                        + this.locator.getLineNumber() + "," + this.locator.getColumnNumber() + "] <" + qName + ">");
                saxe.initCause(e);
                throw saxe;
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

        @SuppressWarnings("unchecked")
        private static <T> Class<? extends T> createClass(Class<T> type, String name) throws Exception
        {
            Class<? extends T> factory = (Class<? extends T>)ReflectionUtil.forName(name);
            if (!type.isAssignableFrom(factory))
            {
                throw new Exception(name + " must be an instance of " + type.getName());
            }
            return factory;
        }

        private static Method createMethod(Class<?> type, String s) throws Exception
        {
            int pos = s.indexOf(' ');
            if (pos == -1)
            {
                throw new Exception("Must Provide Return Type: " + s);
            }
            else
            {
                int pos2 = s.indexOf('(', pos + 1);
                if (pos2 == -1)
                {
                    throw new Exception("Must provide a method name, followed by '(': " + s);
                }
                else
                {
                    String mn = s.substring(pos + 1, pos2).trim();
                    pos = s.indexOf(')', pos2 + 1);
                    if (pos == -1)
                    {
                        throw new Exception("Must close parentheses, ')' missing: " + s);
                    }
                    else
                    {
                        String[] ps = s.substring(pos2 + 1, pos).trim().split(",");
                        Class<?>[] pc;
                        if (ps.length == 1 && "".equals(ps[0]))
                        {
                            pc = new Class[0];
                        }
                        else
                        {
                            pc = new Class[ps.length];
                            for (int i = 0; i < pc.length; i++)
                            {
                                pc[i] = ReflectionUtil.forName(ps[i].trim());
                            }
                        }
                        try
                        {
                            return type.getMethod(mn, pc);
                        }
                        catch (NoSuchMethodException e)
                        {
                            throw new Exception("No Function Found on type: " + type.getName() + " with signature: "
                                    + s);
                        }

                    }

                }
            }
        }

        private void processLibraryClass() throws Exception
        {
            String name = this.captureBuffer();
            Class<?> type = createClass(TagLibrary.class, name);
            this.library = (TagLibrary) type.newInstance();
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException
        {
            if ("-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN".equals(publicId))
            {
                URL url = ClassUtils.getContextClassLoader().getResource("org/apache/myfaces/resource/facelet-taglib_1_0.dtd");
                return new InputSource(url.toExternalForm());
            }
            return null;
        }

        public void characters(char[] ch, int start, int length) throws SAXException
        {
            this.buffer.append(ch, start, length);
        }

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

        public void error(SAXParseException e) throws SAXException
        {
            SAXException saxe = new SAXException("Error Handling [" + this.source + "@" + e.getLineNumber() + ","
                    + e.getColumnNumber() + "]");
            saxe.initCause(e);
            throw saxe;
        }

        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }

        public void fatalError(SAXParseException e) throws SAXException
        {
            throw e;
        }

        public void warning(SAXParseException e) throws SAXException
        {
            throw e;
        }
    }

    public TagLibraryConfig()
    {
        super();
    }

    public static TagLibrary create(URL url) throws IOException
    {
        InputStream is = null;
        TagLibrary t = null;
        try
        {
            is = url.openStream();
            LibraryHandler handler = new LibraryHandler(url);
            SAXParser parser = createSAXParser(handler);
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
                is.close();
        }
        return t;
    }

    public void loadImplicit(Compiler compiler) throws IOException
    {
        ClassLoader cl = ClassUtils.getContextClassLoader();
        URL[] urls = Classpath.search(cl, "META-INF/", SUFFIX);
        for (int i = 0; i < urls.length; i++)
        {
            try
            {
                compiler.addTagLibrary(create(urls[i]));
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("Added Library from: " + urls[i]);
                }
            }
            catch (Exception e)
            {
                log.log(Level.SEVERE, "Error Loading Library: " + urls[i], e);
            }
        }
    }

    private static final SAXParser createSAXParser(LibraryHandler handler) throws SAXException,
            ParserConfigurationException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://xml.org/sax/features/validation", true);
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }

}
