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

import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.view.facelets.tag.AbstractTagLibrary;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeComponentResourceTagHandler;
import org.apache.myfaces.view.facelets.tag.composite.CompositeResouceWrapper;
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

    //protected final static Logger log = Logger.getLogger("facelets.compiler");
    protected final static Logger log = Logger.getLogger(TagLibraryConfig.class.getName());

    private static class TagLibraryImpl extends AbstractTagLibrary
    {
        private String _compositeLibraryName;
        
        public TagLibraryImpl(String namespace)
        {
            super(namespace);
            _compositeLibraryName = null;
        }
        
        @Override
        public boolean containsTagHandler(String ns, String localName)
        {
            boolean result = super.containsTagHandler(ns, localName);
            
            if (!result && _compositeLibraryName != null)
            {
                ResourceHandler resourceHandler = 
                    FacesContext.getCurrentInstance().getApplication().getResourceHandler();

                Resource compositeComponentResource = resourceHandler.createResource(
                        localName +".xhtml", _compositeLibraryName);
                
                if (compositeComponentResource != null)
                {
                    URL url = compositeComponentResource.getURL();
                    return (url != null);
                }
            }
            return result;
        }
        
        @Override
        public TagHandler createTagHandler(String ns, String localName,
                TagConfig tag) throws FacesException
        {
            TagHandler tagHandler = super.createTagHandler(ns, localName, tag);
            
            if (tagHandler == null && containsNamespace(ns) && _compositeLibraryName != null)
            {
                ResourceHandler resourceHandler = 
                    FacesContext.getCurrentInstance().getApplication().getResourceHandler();

                String resourceName = localName + ".xhtml";
                Resource compositeComponentResource = new CompositeResouceWrapper(
                    resourceHandler.createResource(resourceName, _compositeLibraryName));
                
                if (compositeComponentResource != null)
                {
                    ComponentConfig componentConfig = new ComponentConfigWrapper(tag,
                            "javax.faces.NamingContainer", null);
                    
                    return new CompositeComponentResourceTagHandler(componentConfig, compositeComponentResource);
                }
            }
            return tagHandler;
        }

        public void setCompositeLibrary(String compositeLibraryName)
        {
            _compositeLibraryName = compositeLibraryName;
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
        
        public void putBehavior(String name, String id)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            this.addBehavior(name, id);
        }
        
        public void putBehavior(String name, String id, Class<? extends TagHandler> handlerClass)
        {
            ParameterCheck.notNull("name", name);
            ParameterCheck.notNull("id", id);
            ParameterCheck.notNull("handlerClass", handlerClass);
            this.addBehavior(name, id, handlerClass);
        }
    }
    
    private static class ComponentConfigWrapper implements ComponentConfig {

        protected final TagConfig parent;

        protected final String componentType;

        protected final String rendererType;

        public ComponentConfigWrapper(TagConfig parent, String componentType,
                String rendererType) {
            this.parent = parent;
            this.componentType = componentType;
            this.rendererType = rendererType;
        }

        public String getComponentType() {
            return this.componentType;
        }

        public String getRendererType() {
            return this.rendererType;
        }

        public FaceletHandler getNextHandler() {
            return this.parent.getNextHandler();
        }

        public Tag getTag() {
            return this.parent.getTag();
        }

        public String getTagId() {
            return this.parent.getTagId();
        }
    }    
    
    /*
     * We need this class to do a quick check on a facelets taglib document to see if it's
     * a pre-2.0 document.  If it is, we really need to construct a DTD validating, non-namespace
     * aware parser. Otherwise, we have to construct a schema validating, namespace-aware parser.
     */
    /*private static class VersionCheckHandler extends DefaultHandler
    {
        private boolean version20OrLater;
        
        public boolean isVersion20OrLater ()
        {
            return this.version20OrLater;
        }
        
        @Override
        public void startElement (String uri, String localName, String name, Attributes attributes) throws SAXException
        {
            if (name.equals ("facelet-taglib"))
            {
                int length = attributes.getLength();
                
                for (int i = 0; i < length; ++i)
                {
                    if (attributes.getLocalName (i).equals ("version"))
                    {
                        // This document has a "version" attribute in the <facelet-taglib> element, so
                        // it must be a 2.0 or later document as this attribute was never required before.

                        this.version20OrLater = true;
                    }
                }
                
                // Throw a dummy parsing exception to terminate parsing as there really isn't any need to go any
                // further.
                // -= Leonardo Uribe =- THIS IS NOT GOOD PRACTICE! It is better to let the checker continue that                
                // throw an exception, and run this one only when project stage != production.
                //throw new SAXException();
            }
        }
    }*/
    
    private static class LibraryHandler extends DefaultHandler
    {
        private final URL source;

        private TagLibrary library;

        private final StringBuffer buffer;

        private Locator locator;

        private String tagName;

        private String converterId;

        private String validatorId;
        
        private String behaviorId;

        private String componentType;

        private String rendererType;

        private String functionName;

        private Class<? extends TagHandler> handlerClass;

        private Class<?> functionClass;

        private String functionSignature;
        
        private String compositeLibraryName;
        
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
                    if (this.compositeLibraryName != null)
                    {
                        ((TagLibraryImpl)this.library).setCompositeLibrary(compositeLibraryName);
                    }
                }
                else if ("composite-library-name".equals(qName))
                {
                    this.compositeLibraryName = this.captureBuffer();
                    if (this.library != null)
                    {
                        ((TagLibraryImpl)this.library).setCompositeLibrary(compositeLibraryName);
                    }
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
                    else if ("behavior-id".equals(qName))
                    {
                        this.behaviorId = this.captureBuffer();
                    }
                    else if ("behavior".equals(qName))
                    {
                        if (this.handlerClass != null)
                        {
                            impl.putBehavior(this.tagName, this.behaviorId, handlerClass);
                            this.handlerClass = null;
                        }
                        else
                        {
                            impl.putBehavior(this.tagName, this.behaviorId);
                        }
                        this.behaviorId = null;
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
            SAXParser parser = createSAXParser(handler, url);
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
                TagLibrary tl = create(urls[i]);
                if (tl != null)
                {
                    compiler.addTagLibrary(tl);
                }
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

    private static final SAXParser createSAXParser(LibraryHandler handler, URL url) throws SAXException,
            ParserConfigurationException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
       
        // -= Leonardo Uribe =- this code is commented because it is not mandatory to
        // validate facelets tag lib and this cause performance issues. Maybe we can 
        // refactor this part an use SchemaFactory for validation instead the proposed here
        // only on ProjectStage.Development time before parse it (on create(URL) method).
        //if (FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development))
        //{
            // We have to make two different parsers depending on whether or not the facelets taglib
            // document is version 2.0 or later.  If pre-version 2.0, then the parser must not be
            // namespace-aware and must be DTD validating.  If verison 2.0 or later, the parser must be
            // namespace-aware and must be schema validating.
            //if (!isTaglibDocument20OrLater (url))
            //{
            //    factory.setNamespaceAware(false);
            //    factory.setFeature("http://xml.org/sax/features/validation", true);
            //    factory.setValidating(true);
            //}
            //else
            //{
                // TODO: CJH: the Facelets project does not make the 2.0 schema available via their
                // CVS.  The schema is also not available at its URL.  For now, 2.0 documents will simply
                // have no validation.  We must get a copy of their schema once they release it and set up
                // the parser to use it.
                
                //factory.setNamespaceAware(true);
                //factory.setFeature("http://xml.org/sax/features/validation", false);
                //factory.setValidating(false);
            //}
        //}
        //else
        //{
            //Just parse it and do not validate, because it is not necessary.
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setValidating(false);
        //}

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }

    /*
    private static final boolean isTaglibDocument20OrLater (URL url)
    {
        InputStream input = null;
        boolean result = false;
        
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            VersionCheckHandler handler = new VersionCheckHandler();
            
            // We need to create a non-validating, non-namespace aware parser used to simply check
            // which version of the facelets taglib document we are dealing with.

            factory.setNamespaceAware(false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setValidating(false);
            
            parser = factory.newSAXParser();
            
            input = url.openStream();
            
            try
            {
                parser.parse (input, handler);
            }
            
            catch (SAXException e)
            {
                // This is as a result of our aborted parse, so ignore.
            }
            
            result = handler.isVersion20OrLater();
        }
        
        catch (Throwable e)
        {
            // Most likely a result of our aborted parse, so ignore.
        }
        
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                
                catch (Throwable e)
                {
                }
            }
        }
        
        return result;
    }*/
}
