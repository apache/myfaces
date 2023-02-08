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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.view.facelets.FaceletMultipleRequestsTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SkipNamespaceUnitTest extends FaceletMultipleRequestsTestCase 
{
    private Boolean isWellFormed = Boolean.FALSE;

    @Override
    @BeforeEach
    public void setUp() throws Exception 
    {
        super.setUp();
        setupRequest();
    }

    @Test
    public void test() 
    {
        try 
        {
            StringWriter writer = new StringWriter();
            HtmlResponseWriterImpl htmlResponseWriter = new HtmlResponseWriterImpl(writer, "application/xml", "UTF-8");
            facesContext.setResponseWriter(htmlResponseWriter);
            String testData = "/testSkipNamespaceUnit.xhtml";
            UIViewRoot view = facesContext.getViewRoot();
            view.setViewId(testData);
            vdl.buildView(facesContext, view, testData);
            facesContext.renderResponse();
            List<UIComponent> children = view.getChildren();
            for (UIComponent child : children) 
            {
                if (child.isRendered()) 
                {
                    child.encodeAll(facesContext);
                }
            }
            parse(writer);
            Assertions.assertTrue(isWellFormed);
        } catch (IOException e) 
        {
            e.printStackTrace();
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private void parse(StringWriter sw) 
    {
        try 
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SimpleErrorHandler());
            builder.parse(new InputSource(new StringReader(sw.toString())));
            isWellFormed = Boolean.TRUE;
        }
        catch (ParserConfigurationException e) 
        {
            e.printStackTrace();
        } 
        catch (SAXException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return;
    }

    public class SimpleErrorHandler implements ErrorHandler 
    {
        public void warning(SAXParseException e) throws SAXException 
        {
            System.out.println(e.getMessage());
        }

        public void error(SAXParseException e) throws SAXException 
        {
            System.out.println(e.getMessage());
        }

        public void fatalError(SAXParseException e) throws SAXException 
        {
            System.out.println(e.getMessage());
        }
    }
}
