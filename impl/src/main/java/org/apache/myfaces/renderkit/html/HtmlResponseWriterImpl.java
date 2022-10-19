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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.el.ValueExpression;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.core.api.shared.ComponentUtils;
import org.apache.myfaces.renderkit.ContentTypeUtils;
import org.apache.myfaces.renderkit.html.util.UnicodeEncoder;
import org.apache.myfaces.util.CommentUtils;
import org.apache.myfaces.util.lang.StreamCharBuffer;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.HTMLEncoder;
import org.apache.myfaces.core.api.shared.lang.Assert;

public class HtmlResponseWriterImpl extends ResponseWriter
{
    private static final Logger log = Logger.getLogger(HtmlResponseWriterImpl.class.getName());

    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    private static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
    private static final String UTF8 = "UTF-8";

    private static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static final String TEXT_XML_CONTENT_TYPE = "text/xml";

    private static final String CDATA_START = "<![CDATA[ \n";
    private static final String CDATA_START_NO_LINE_RETURN = "<![CDATA[";
    private static final String COMMENT_START = "<!--\n";
    private static final String CDATA_COMMENT_START = "//<![CDATA[ \n";
    private static final String CDATA_COMMENT_END = "\n//]]>";
    private static final String CDATA_END = "\n]]>";
    private static final String CDATA_END_NO_LINE_RETURN = "]]>";
    private static final String COMMENT_COMMENT_END = "\n//-->";
    private static final String COMMENT_END = "\n-->";

    static private final String[][] EMPTY_ELEMENT_ARR = new String[256][];

    static private final String[] A_NAMES = new String[]
    {
      "area",
    };

    static private final String[] B_NAMES = new String[]
    {
      "br",
      "base",
      "basefont",
    };

    static private final String[] C_NAMES = new String[]
    {
      "col",
    };

    static private final String[] E_NAMES = new String[]
    {
      "embed",
    };

    static private final String[] F_NAMES = new String[]
    {
      "frame",
    };

    static private final String[] H_NAMES = new String[]
    {
      "hr",
    };

    static private final String[] I_NAMES = new String[]
    {
      "img",
      "input",
      "isindex",
    };

    static private final String[] L_NAMES = new String[]
    {
      "link",
    };

    static private final String[] M_NAMES = new String[]
    {
      "meta",
    };

    static private final String[] P_NAMES = new String[]
    {
      "param",
    };

    static
    {
      EMPTY_ELEMENT_ARR['a'] = A_NAMES;
      EMPTY_ELEMENT_ARR['A'] = A_NAMES;
      EMPTY_ELEMENT_ARR['b'] = B_NAMES;
      EMPTY_ELEMENT_ARR['B'] = B_NAMES;
      EMPTY_ELEMENT_ARR['c'] = C_NAMES;
      EMPTY_ELEMENT_ARR['C'] = C_NAMES;
      EMPTY_ELEMENT_ARR['e'] = E_NAMES;
      EMPTY_ELEMENT_ARR['E'] = E_NAMES;
      EMPTY_ELEMENT_ARR['f'] = F_NAMES;
      EMPTY_ELEMENT_ARR['F'] = F_NAMES;
      EMPTY_ELEMENT_ARR['h'] = H_NAMES;
      EMPTY_ELEMENT_ARR['H'] = H_NAMES;
      EMPTY_ELEMENT_ARR['i'] = I_NAMES;
      EMPTY_ELEMENT_ARR['I'] = I_NAMES;
      EMPTY_ELEMENT_ARR['l'] = L_NAMES;
      EMPTY_ELEMENT_ARR['L'] = L_NAMES;
      EMPTY_ELEMENT_ARR['m'] = M_NAMES;
      EMPTY_ELEMENT_ARR['M'] = M_NAMES;
      EMPTY_ELEMENT_ARR['p'] = P_NAMES;
      EMPTY_ELEMENT_ARR['P'] = P_NAMES;
    }    
    

    /**
     * The writer used as output, or in other words, the one passed on the constructor
     */
    private Writer _outputWriter;
    
    /**
     * The writer we are using to store data.
     */
    private Writer _currentWriter;
    
    /**
     * The writer used to buffer script and style content
     */
    private StreamCharBuffer _buffer;
    
    private String _contentType;
    
    private String _writerContentTypeMode;
    
    /**
     * This var prevents check if the contentType is for xhtml multiple times.
     */
    private boolean _isXhtmlContentType;
    
    /**
     * Indicate the current response writer should not close automatically html elements
     * and let the writer close them.
     */
    private boolean _useStraightXml;
    
    private String _characterEncoding;
    private boolean _wrapScriptContentWithXmlCommentTag;
    private boolean _isUTF8;
    
    private String _startElementName;
    private boolean _isInsideScript = false;
    private boolean _isStyle = false;
    private Boolean _isTextArea;
    private UIComponent _startElementUIComponent;
    private boolean _startTagOpen;
    private Map<String, Object> _passThroughAttributesMap;
    private FacesContext _facesContext;

    private boolean _cdataOpen;
    
    private List<String> _startedChangedElements;
    private List<Integer> _startedElementsCount;
    
    public HtmlResponseWriterImpl(Writer writer, String contentType, String characterEncoding)
    {
        this(writer,contentType,characterEncoding,true);
    }

    public HtmlResponseWriterImpl(Writer writer, String contentType, String characterEncoding,
            boolean wrapScriptContentWithXmlCommentTag)
    {
        this(writer,contentType, characterEncoding, wrapScriptContentWithXmlCommentTag, 
                contentType != null && ContentTypeUtils.isXHTMLContentType(contentType) ? 
                    ContentTypeUtils.XHTML_CONTENT_TYPE : ContentTypeUtils.HTML_CONTENT_TYPE);
    }
    
    public HtmlResponseWriterImpl(Writer writer, String contentType, String characterEncoding,
             boolean wrapScriptContentWithXmlCommentTag, String writerContentTypeMode) throws FacesException
    {
        _outputWriter = writer;
        //The current writer to be used is the one used as output 
        _currentWriter = _outputWriter;
        _wrapScriptContentWithXmlCommentTag = wrapScriptContentWithXmlCommentTag;
        
        _contentType = contentType;
        if (_contentType == null)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("No content type given, using default content type " + DEFAULT_CONTENT_TYPE);
            }
            _contentType = DEFAULT_CONTENT_TYPE;
        }
        _writerContentTypeMode = writerContentTypeMode;
        _isXhtmlContentType = writerContentTypeMode.contains(ContentTypeUtils.XHTML_CONTENT_TYPE);
        
        _useStraightXml = _isXhtmlContentType
                && (_contentType.contains(APPLICATION_XML_CONTENT_TYPE)
                    || _contentType.contains(TEXT_XML_CONTENT_TYPE));

        if (characterEncoding == null)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("No character encoding given, using default character encoding "
                        + DEFAULT_CHARACTER_ENCODING);
            }
            _characterEncoding = DEFAULT_CHARACTER_ENCODING;
        }
        else
        {
            // canonize to uppercase, that's the standard format
            _characterEncoding = characterEncoding.toUpperCase();
            
            // Check if encoding is valid by javadoc of RenderKit.createResponseWriter()
            if (!Charset.isSupported(_characterEncoding))
            {
                throw new IllegalArgumentException("Encoding "+_characterEncoding
                        +" not supported by HtmlResponseWriterImpl");
            }
        }
        _isUTF8 = UTF8.equals(_characterEncoding);
        _startedChangedElements = new ArrayList<String>();
        _startedElementsCount = new ArrayList<Integer>();
    }

    public static boolean supportsContentType(String contentType)
    {
        String[] supportedContentTypes = ContentTypeUtils.getSupportedContentTypes();
        for (String supportedContentType : supportedContentTypes)
        {
            if (supportedContentType.contains(contentType))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getContentType()
    {
        return _contentType;
    }
    
    public String getWriterContentTypeMode()
    {
        return _writerContentTypeMode;
    }

    @Override
    public String getCharacterEncoding()
    {
        return _characterEncoding;
    }

    @Override
    public void flush() throws IOException
    {
        // API doc says we should not flush the underlying writer
        //_writer.flush();
        // but rather clear any values buffered by this ResponseWriter:
        closeStartTagIfNecessary();
    }

    @Override
    public void startDocument()
    {
        // do nothing
    }

    @Override
    public void endDocument() throws IOException
    {
        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(FacesContext.getCurrentInstance());
        if (myfacesConfig.isEarlyFlushEnabled())
        {
            _currentWriter.flush();
        }
        _facesContext = null;
    }

    @Override
    public void startElement(String name, UIComponent uiComponent) throws IOException
    {
        Assert.notNull(name, "name");

        closeStartTagIfNecessary();
        _currentWriter.write('<');

        resetStartedElement();

        _startElementName = name;
        _startElementUIComponent = uiComponent;
        _startTagOpen = true;
        _passThroughAttributesMap = _startElementUIComponent != null
                ? _startElementUIComponent.getPassThroughAttributes(false)
                : null;

        if (_passThroughAttributesMap != null)
        {
            Object value = _passThroughAttributesMap.get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
            if (value != null)
            {
                if (value instanceof ValueExpression)
                {
                    value = ((ValueExpression)value).getValue(getFacesContext().getELContext());
                }
                String elementName = value.toString().trim();
                
                if (!name.equals(elementName))
                {
                    _startElementName = elementName;
                    _startedChangedElements.add(elementName);
                    _startedElementsCount.add(0);
                }
                _currentWriter.write((String) elementName);
            }
            else
            {
                _currentWriter.write(name);
            }
        }
        else
        {
            _currentWriter.write(name);
        }

        if (!_startedElementsCount.isEmpty())
        {
            int i = _startedElementsCount.size()-1;
            _startedElementsCount.set(i, _startedElementsCount.get(i)+1);
        }
        
        // Each time we start a element, it is necessary to check <script> or <style>,
        // because we need to buffer all content to post process it later when it reach its end
        // according to the initialization properties used.
        if(isScript(_startElementName))
        {
            // handle a <script> start
            _isInsideScript = true;
            _isStyle = false;
            _isTextArea = Boolean.FALSE;
        }
        else if (isStyle(_startElementName))
        {
            _isInsideScript = false;
            _isStyle = true;
            _isTextArea = Boolean.FALSE;
        }
    }

    @Override
    public void startCDATA() throws IOException
    {
        if (!_cdataOpen)
        {
            write(CDATA_START_NO_LINE_RETURN);
            _cdataOpen = true;
        }
    }

    @Override
    public void endCDATA() throws IOException
    {
        if (_cdataOpen)
        {
            write(CDATA_END_NO_LINE_RETURN);
            _cdataOpen = false;
        }
    }

    private void closeStartTagIfNecessary() throws IOException
    {
        if (_startTagOpen)
        {
            if (_passThroughAttributesMap != null)
            {
                for (Map.Entry<String, Object> entry : _passThroughAttributesMap.entrySet())
                {
                    String key = entry.getKey();
                    if (Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY.equals(key))
                    {
                        // Special attribute stored in passthrough attribute map,
                        // skip rendering
                        continue;
                    }
                    
                    Object value = entry.getValue();
                    if (value instanceof ValueExpression)
                    {
                        value = ((ValueExpression)value).getValue(getFacesContext().getELContext());
                    }
                    // encodeAndWriteURIAttribute(key, value, key);
                    // Faces 2.2 In the renderkit javadoc of jsf 2.2 spec says this 
                    // (Rendering Pass Through Attributes):
                    // "... The ResponseWriter must ensure that any pass through attributes are 
                    // rendered on the outer-most markup element for the component. If there is 
                    // a pass through attribute with the same name as a renderer specific 
                    // attribute, the pass through attribute takes precedence. Pass through 
                    // attributes are rendered as if they were passed to 
                    // ResponseWriter.writeURIAttribute(). ..."
                    // Note here it says "as if they were passed", instead say "... attributes are
                    // encoded and rendered as if ...". Black box testing against RI shows that there
                    // is no URI encoding at all in this part, so in this case the best is do the
                    // same here. After all, it is resposibility of the one who set the passthrough
                    // attribute to do the proper encoding in cases when a URI is provided. However,
                    // that does not means the attribute should not be encoded as other attributes.
                    encodeAndWriteAttribute(key, value);
                }
            }

            if (!_useStraightXml && isEmptyElement(_startElementName))
            {
                _currentWriter.write(" />");
                // make null, this will cause NullPointer in some invalid element nestings
                // (better than doing nothing)
                resetStartedElement();
            }
            else
            {
                _currentWriter.write('>');
                if (isScript(_startElementName) && (_isXhtmlContentType || _wrapScriptContentWithXmlCommentTag))
                {
                    _currentWriter = getInternalBuffer(true).getWriter();
                }                
                if (isStyle(_startElementName) && _isXhtmlContentType)
                {
                    _currentWriter = getInternalBuffer(true).getWriter();
                }
            }
            _startTagOpen = false;
        }
    }
    
    private boolean isEmptyElement(String elem)
    {
        // =-=AEW Performance?  Certainly slower to use a hashtable,
        // at least if we can't assume the input name is lowercased.
        // -= Leonardo Uribe =- elem.toLowerCase() internally creates an array,
        // and the contains() force a call to hashCode(). The array uses simple
        // char comparison, which at the end is faster and use less memory.
        // Note this call is very frequent, so at the end it is worth to do it.
        String[] array = EMPTY_ELEMENT_ARR[elem.charAt(0)];
        if (array != null)
        {
            for (int i = array.length - 1; i >= 0; i--)
            {
                if (elem.equalsIgnoreCase(array[i]))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetStartedElement()
    {
        _startElementName = null;
        _startElementUIComponent = null;
        _passThroughAttributesMap = null;
        _isStyle = false;
        _isTextArea = null;
    }

    @Override
    public void endElement(String name) throws IOException
    {
        Assert.notNull(name, "name");

        String elementName = name;

        if (!_startedElementsCount.isEmpty())
        {
            int i = _startedElementsCount.size()-1;
            _startedElementsCount.set(i, _startedElementsCount.get(i)-1);
            if (_startedElementsCount.get(i) == 0)
            {
                elementName = _startedChangedElements.get(i);
                _startedChangedElements.remove(i);
                _startedElementsCount.remove(i);
            }
        }

        if (log.isLoggable(Level.WARNING))
        {
            if (_startElementName != null &&
                !elementName.equals(_startElementName))
            {
                log.warning("HTML nesting warning on closing " + elementName + ": element " + _startElementName +
                        (_startElementUIComponent==null ? "" : (" rendered by component : " +
                        ComponentUtils.getPathToComponent(_startElementUIComponent))) + " not explicitly closed");
            }
        }

        if(_startTagOpen)
        {

            // we will get here only if no text or attribute was written after the start element was opened
            // now we close out the started tag - if it is an empty tag, this is then fully closed
            closeStartTagIfNecessary();

            //tag was no empty tag - it has no accompanying end tag now.
            if(_startElementName!=null)
            {                
                if (isScript() && (_isXhtmlContentType || _wrapScriptContentWithXmlCommentTag))
                {
                    writeScriptContent();
                    _currentWriter = _outputWriter;
                }
                else if (isStyle() && _isXhtmlContentType)
                {
                    writeStyleContent();
                    _currentWriter = _outputWriter;
                }

                //write closing tag
                writeEndTag(elementName);
            }
        }
        else
        {
            if (!_useStraightXml && isEmptyElement(elementName))
            {

            }
            else
            {
                if (isScript() && (_isXhtmlContentType || _wrapScriptContentWithXmlCommentTag))
                {
                    writeScriptContent();
                    _currentWriter = _outputWriter;
                }
                else if (isStyle() && _isXhtmlContentType)
                {
                    writeStyleContent();
                    _currentWriter = _outputWriter;
                }
                writeEndTag(elementName);
            }
        }

        resetStartedElement();
    }


    
    private void writeStyleContent() throws IOException
    {
        String content = getInternalBuffer().toString();
        
        if(_isXhtmlContentType)
        {
            // In xhtml, the content inside <style> tag is PCDATA, but
            // in html the content is CDATA, so in order to preserve 
            // compatibility we need to wrap the content inside proper
            // CDATA tags.
            // Since the response content type is xhtml, we can use
            // simple CDATA without comments, but note we need to check
            // when we are using any valid notation (simple CDATA, commented CDATA, xml comment) 
            String trimmedContent = content.trim();
            if (trimmedContent.startsWith(CommentUtils.CDATA_SIMPLE_START) && trimmedContent.endsWith(
                    CommentUtils.CDATA_SIMPLE_END))
            {
                _outputWriter.write(content);
                return;
            }
            else if (CommentUtils.isStartMatchWithCommentedCDATA(trimmedContent) && 
                    CommentUtils.isEndMatchWithCommentedCDATA(trimmedContent))
            {
                _outputWriter.write(content);
                return;
            }
            else if (trimmedContent.startsWith(CommentUtils.COMMENT_SIMPLE_START) && 
                    trimmedContent.endsWith(CommentUtils.COMMENT_SIMPLE_END))
            {
                //Use comment wrap is valid, but for xhtml it is preferred to use CDATA
                _outputWriter.write(CDATA_START);
                _outputWriter.write(trimmedContent.substring(4,trimmedContent.length()-3));
                _outputWriter.write(CDATA_END);
                return;
            }
            else
            {
                _outputWriter.write(CDATA_START);
                _outputWriter.write(content);
                _outputWriter.write(CDATA_END);
                return;                
            }
        }
        // If the response is handled as text/html, 
        // it is not necessary to wrap with xml comment tag,
        // so we can just write the content as is.
        _outputWriter.write(content);
    }
    
    private void writeScriptContent() throws IOException
    {
        String content = getInternalBuffer().toString();
        String trimmedContent = null;
        
        if(_isXhtmlContentType)
        {
            trimmedContent = content.trim();
            
            if ( trimmedContent.startsWith(CommentUtils.COMMENT_SIMPLE_START) && 
                    CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag(trimmedContent))
            {
                // In xhtml use xml comment to wrap is invalid, so it is only required to remove the <!--
                // the ending //--> will be parsed as a comment, so it will not be a problem. Let it on the content.
                if (_cdataOpen)
                {
                    _outputWriter.write("//\n");
                }
                else
                {
                   _outputWriter.write(CDATA_COMMENT_START);
                }

                _outputWriter.write(trimmedContent.substring(4));

                if (_cdataOpen)
                {
                    _outputWriter.write('\n');
                }
                else
                {
                    _outputWriter.write(CDATA_COMMENT_END);
                }
                
                return;
            }
            else if (CommentUtils.isStartMatchWithCommentedCDATA(trimmedContent) && 
                    CommentUtils.isEndMatchWithCommentedCDATA(trimmedContent))
            {
                _outputWriter.write(content);
                return;
            }
            else if (CommentUtils.isStartMatchWithInlineCommentedCDATA(trimmedContent) && 
                    CommentUtils.isEndMatchWithInlineCommentedCDATA(trimmedContent))
            {
                _outputWriter.write(content);
                return;
            }
            else
            {
                // <script> in xhtml has as content type PCDATA, but in html it is CDATA,
                // so we need to wrap here to prevent problems.
                if (_cdataOpen)
                {
                    _outputWriter.write("//\n");
                }
                else
                {
                   _outputWriter.write(CDATA_COMMENT_START);
                }

                _outputWriter.write(content);

                if (_cdataOpen)
                {
                    _outputWriter.write('\n');
                }
                else
                {
                    _outputWriter.write(CDATA_COMMENT_END);
                }

                return;
            }
        }
        else
        {
            if (_wrapScriptContentWithXmlCommentTag)
            {
                trimmedContent = content.trim();
                
                if ( trimmedContent.startsWith(CommentUtils.COMMENT_SIMPLE_START) && 
                        CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag(trimmedContent))
                {
                    _outputWriter.write(content);
                    return;
                }
                else if (CommentUtils.isStartMatchWithCommentedCDATA(trimmedContent) && 
                        CommentUtils.isEndMatchWithCommentedCDATA(trimmedContent))
                {
                    _outputWriter.write(content);
                    return;
                }
                else if (CommentUtils.isStartMatchWithInlineCommentedCDATA(trimmedContent) && 
                        CommentUtils.isEndMatchWithInlineCommentedCDATA(trimmedContent))
                {
                    _outputWriter.write(content);
                    return;
                }
                else
                {
                    _outputWriter.write(COMMENT_START);
                    _outputWriter.write(content);
                    _outputWriter.write(COMMENT_COMMENT_END);
                    return;
                }
            }
        }
        
        //If no return, just write everything
        _outputWriter.write(content);
    }
    

    private void writeEndTag(String name) throws IOException
    {
        if (isScript(name))
        {
            // reset _isInsideScript
            _isInsideScript = false;
        }
        else if (isStyle(name))
        {
            _isStyle = false;
        }

        _currentWriter.write("</");
        _currentWriter.write(name);
        _currentWriter.write('>');
    }

    @Override
    public void writeAttribute(String name, Object value, String componentPropertyName) throws IOException
    {
        Assert.notNull(name, "name");

        if (!_startTagOpen)
        {
            throw new IllegalStateException("Must be called before the start element is closed (attribute '"
                    + name + "')");
        }
        // From Faces 2.2 RenderKit javadoc: "... If there is a pass through attribute with the same 
        // name as a renderer specific attribute, the pass through attribute takes precedence. ..."
        if (_passThroughAttributesMap != null && _passThroughAttributesMap.containsKey(name))
        {
            return;
        }

        if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
            {
                // name as value for XHTML compatibility
                _currentWriter.write(' ');
                _currentWriter.write(name);
                _currentWriter.write("=\"");
                _currentWriter.write(name);
                _currentWriter.write('"');
            }
        }
        else
        {
            _currentWriter.write(' ');
            _currentWriter.write(name);
            _currentWriter.write("=\"");
            if (value != null)
            {
                HTMLEncoder.encode(_currentWriter, value.toString(), false, false, !_isUTF8);
            }
            _currentWriter.write('"');
        }
    }
    
    private void encodeAndWriteAttribute(String name, Object value) throws IOException
    {
        _currentWriter.write(' ');
        _currentWriter.write(name);
        _currentWriter.write("=\"");
        if (value != null)
        {
            HTMLEncoder.encode(_currentWriter, value.toString(), false, false, !_isUTF8);
        }
        _currentWriter.write('"');
    }

    @Override
    public void writeURIAttribute(String name, Object value, String componentPropertyName) throws IOException
    {
        Assert.notNull(name, "name");

        if (!_startTagOpen)
        {
            throw new IllegalStateException("Must be called before the start element is closed (attribute '"
                    + name + "')");
        }
        // From Faces 2.2 RenderKit javadoc: "... If there is a pass through attribute with the same 
        // name as a renderer specific attribute, the pass through attribute takes precedence. ..."
        if (_passThroughAttributesMap != null && _passThroughAttributesMap.containsKey(name))
        {
            return;
        }
        
        encodeAndWriteURIAttribute(name, value);
    }
    
    private void encodeAndWriteURIAttribute(String name, Object value) throws IOException
    {
        String strValue = value.toString();
        _currentWriter.write(' ');
        _currentWriter.write(name);
        _currentWriter.write("=\"");
        if (strValue.toLowerCase().startsWith("javascript:"))
        {
            HTMLEncoder.encode(_currentWriter, strValue, false, false, !_isUTF8);
        }
        else
        {
            HTMLEncoder.encodeURIAttribute(_currentWriter, strValue, _characterEncoding);
        }
        _currentWriter.write('"');
    }

    @Override
    public void writeComment(Object value) throws IOException
    {
        Assert.notNull(value, "value");

        closeStartTagIfNecessary();
        _currentWriter.write("<!--");
        _currentWriter.write(value.toString());    //TODO: Escaping: must not have "-->" inside!
        _currentWriter.write("-->");
    }

    @Override
    public void writeText(Object value, String componentPropertyName) throws IOException
    {
        Assert.notNull(value, "value");

        closeStartTagIfNecessary();

        String strValue = value.toString();

        if (isScriptOrStyle())
        {
            // Don't bother encoding anything if chosen character encoding is UTF-8
            if (_isUTF8)
            {
                _currentWriter.write(strValue);
            }
            else
            {
                UnicodeEncoder.encode(_currentWriter, strValue);
            }
        }
        else
        {
            HTMLEncoder.encode(_currentWriter, strValue, false, false, !_isUTF8);
        }
    }

    @Override
    public void writeText(char[] cbuf, int off, int len) throws IOException
    {
        Assert.notNull(cbuf, "cbuf");

        if (cbuf.length < off + len)
        {
            throw new IndexOutOfBoundsException((off + len) + " > " + cbuf.length);
        }

        closeStartTagIfNecessary();

        if (isScriptOrStyle())
        {
            String strValue = new String(cbuf, off, len);
            // Don't bother encoding anything if chosen character encoding is UTF-8
            if (_isUTF8)
            {
                _currentWriter.write(strValue);
            }
            else
            {
                UnicodeEncoder.encode(_currentWriter, strValue);
            }
        }
        else if (isTextarea())
        {
            // For textareas we must *not* map successive spaces to &nbsp or Newlines to <br/>
            HTMLEncoder.encode(cbuf, off, len, false, false, !_isUTF8, _currentWriter);
        }
        else
        {
            // We map successive spaces to &nbsp; and Newlines to <br/>
            HTMLEncoder.encode(cbuf, off, len, true, true, !_isUTF8, _currentWriter);
        }
    }

    private boolean isScriptOrStyle()
    {
        return _isStyle || _isInsideScript;
    }
    
    /**
     * Is the given element a script tag?
     * @param element
     * @return
     */
    private boolean isScript(String element)
    {
        return (HTML.SCRIPT_ELEM.equalsIgnoreCase(element));
    }
    
    private boolean isScript()
    {
        return _isInsideScript;
    }
    
    private boolean isStyle(String element)
    {
        return (HTML.STYLE_ELEM.equalsIgnoreCase(element));
    }
    
    private boolean isStyle()
    {
        return _isStyle;
    }

    private boolean isTextarea()
    {
        initializeStartedTagInfo();

        return _isTextArea != null && _isTextArea;
    }

    private void initializeStartedTagInfo()
    {
        if (_startElementName != null)
        {
            if (_isTextArea == null)
            {
                if (_startElementName.equalsIgnoreCase(HTML.TEXTAREA_ELEM))
                {
                    _isTextArea = Boolean.TRUE;
                }
                else
                {
                    _isTextArea = Boolean.FALSE;
                }
            }
        }
    }

    @Override
    public ResponseWriter cloneWithWriter(Writer writer)
    {
        HtmlResponseWriterImpl newWriter = new HtmlResponseWriterImpl(writer, getContentType(),
                getCharacterEncoding(), _wrapScriptContentWithXmlCommentTag, _writerContentTypeMode);
        return newWriter;
    }


    // Writer methods

    @Override
    public void close() throws IOException
    {
        closeStartTagIfNecessary();
        _currentWriter.close();
        _facesContext = null;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        closeStartTagIfNecessary();
        // Don't bother encoding anything if chosen character encoding is UTF-8
        if (_isUTF8)
        {
            _currentWriter.write(cbuf, off, len);
        }
        else
        {
            UnicodeEncoder.encode(_currentWriter, cbuf, off, len);
        }
    }

    @Override
    public void write(int c) throws IOException
    {
        closeStartTagIfNecessary();
        _currentWriter.write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException
    {
        closeStartTagIfNecessary();
        // Don't bother encoding anything if chosen character encoding is UTF-8
        if (_isUTF8)
        {
            _currentWriter.write(cbuf);
        }
        else
        {
            UnicodeEncoder.encode(_currentWriter, cbuf, 0, cbuf.length);
        }
    }

    @Override
    public void write(String str) throws IOException
    {
        closeStartTagIfNecessary();
        // empty string commonly used to force the start tag to be closed.
        // in such case, do not call down the writer chain
        if (str != null && str.length() > 0)
        {
            // Don't bother encoding anything if chosen character encoding is UTF-8
            if (_isUTF8)
            {
                _currentWriter.write(str);
            }
            else
            {
                UnicodeEncoder.encode(_currentWriter, str);
            }
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException
    {
        closeStartTagIfNecessary();
        // Don't bother encoding anything if chosen character encoding is UTF-8
        if (_isUTF8)
        {
            _currentWriter.write(str, off, len);
        }
        else
        {
            UnicodeEncoder.encode(_currentWriter, str, off, len);
        }
    }
    
    /**
     * This method ignores the <code>UIComponent</code> provided and simply calls
     * <code>writeText(Object,String)</code>
     * @since 1.2
     */
    @Override
    public void writeText(Object object, UIComponent component, String string) throws IOException
    {
        writeText(object,string);
    }
    
    protected StreamCharBuffer getInternalBuffer()
    {
        return getInternalBuffer(false);
    }
    
    protected StreamCharBuffer getInternalBuffer(boolean reset)
    {
        if (_buffer == null)
        {
            _buffer = new StreamCharBuffer(256, 100);
        }
        else if (reset)
        {
            _buffer.reset();
        }
        return _buffer;
    }
    
    protected FacesContext getFacesContext()
    {
        if (_facesContext == null)
        {
            _facesContext = FacesContext.getCurrentInstance();
        }
        return _facesContext;
    }
    
    protected boolean getWrapScriptContentWithXmlCommentTag()
    {
        return _wrapScriptContentWithXmlCommentTag;
    }
    
    protected void forceFlush() throws IOException
    {
        _currentWriter.flush();
    }

}
