/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.renderkit;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContentTypeUtilsTest extends AbstractFacesTestCase
{

    @Test
    void containsContentType() throws Exception
    {
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.HTML_CONTENT_TYPE, ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES));
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.ANY_CONTENT_TYPE, ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES));
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.TEXT_ANY_CONTENT_TYPE, ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES));
        
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.XHTML_CONTENT_TYPE, ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES));
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.APPLICATION_XML_CONTENT_TYPE, ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES));
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.TEXT_XML_CONTENT_TYPE, ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES));
    }

    @Test
    void splitContentTypeListString() throws Exception
    {
        String [] splittedArray = ContentTypeUtils.splitContentTypeListString("text/*, text/html ");
        
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.HTML_CONTENT_TYPE, splittedArray));
        assertTrue(ContentTypeUtils.containsContentType(
                ContentTypeUtils.TEXT_ANY_CONTENT_TYPE, splittedArray));
        
        splittedArray = ContentTypeUtils.splitContentTypeListString(" text/x-dvi; q=.8; mxb=100000; mxt=5.0, text/x-c");
        assertTrue(ContentTypeUtils.containsContentType(
                "text/x-dvi", splittedArray));
        assertTrue(ContentTypeUtils.containsContentType(
                "text/x-c", splittedArray));
    }
    
    public void testChooseWriterContentType() throws Exception
    {
        assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, 
                ContentTypeUtils.chooseWriterContentType(
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", 
                        ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES,
                        ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES));

        assertEquals(ContentTypeUtils.XHTML_CONTENT_TYPE, 
                ContentTypeUtils.chooseWriterContentType(
                        "application/xml, text/xml , */*; q=0.01", 
                        ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES,
                        ContentTypeUtils.XHTML_ALLOWED_CONTENT_TYPES));

        //In ajax request application/xml and text/xml does not match.
        assertNull(ContentTypeUtils.chooseWriterContentType(
                        "application/xml, text/xml, */*; q=0.01", 
                        ContentTypeUtils.HTML_ALLOWED_CONTENT_TYPES,
                        ContentTypeUtils.AJAX_XHTML_ALLOWED_CONTENT_TYPES));
    }
}
