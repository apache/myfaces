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
package org.apache.myfaces.renderkit;

import jakarta.faces.context.FacesContext;

import org.apache.myfaces.util.lang.StringUtils;

public class ContentTypeUtils
{
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final String TEXT_ANY_CONTENT_TYPE = "text/*";
    public static final String ANY_CONTENT_TYPE = "*/*";

    public static final String[] HTML_ALLOWED_CONTENT_TYPES = {HTML_CONTENT_TYPE, 
        ANY_CONTENT_TYPE, TEXT_ANY_CONTENT_TYPE};
    
    public static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    public static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    
    public static final String[] XHTML_ALLOWED_CONTENT_TYPES = {XHTML_CONTENT_TYPE, 
        APPLICATION_XML_CONTENT_TYPE, TEXT_XML_CONTENT_TYPE};
    
    public static final String[] AJAX_XHTML_ALLOWED_CONTENT_TYPES = {XHTML_CONTENT_TYPE};

    public static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";

    // The order is important in this case.
    private static final String[] SUPPORTED_CONTENT_TYPES = {
            HTML_CONTENT_TYPE, //Prefer this over any other, because IE does not support XHTML content type
            XHTML_CONTENT_TYPE, APPLICATION_XML_CONTENT_TYPE,
            TEXT_XML_CONTENT_TYPE, TEXT_ANY_CONTENT_TYPE, ANY_CONTENT_TYPE };

    /**
     * Indicate if the passes content type match one of the options passed. 
     */
    public static boolean containsContentType(String contentType, String[] allowedContentTypes)
    {
        if (allowedContentTypes == null)
        {
            return false;
        }
        for (int i = 0; i < allowedContentTypes.length; i++)
        {
            if (allowedContentTypes[i].contains(contentType))
            {
                return true;
            }
        }
        return false;
    }

    public static String chooseWriterContentType(String contentTypeListString, 
            String[] htmlContentTypes, String[] xhtmlContentTypes)
    {
        String[] contentTypeList = splitContentTypeListString(contentTypeListString);
        String[] supportedContentTypeArray = getSupportedContentTypes();
        String selectedContentType = null;
        for (int i = 0; i < supportedContentTypeArray.length; i++)
        {
            String supportedContentType = supportedContentTypeArray[i].trim();

            for (int j = 0; j < contentTypeList.length; j++)
            {
                String contentType = contentTypeList[j];
                if (contentType.contains(supportedContentType))
                {
                    if (containsContentType(contentType, htmlContentTypes))
                    {
                        selectedContentType = HTML_CONTENT_TYPE;
                    }
                    else if (containsContentType(contentType, xhtmlContentTypes))
                    {
                        selectedContentType = XHTML_CONTENT_TYPE;
                    }
                    break;
                }
            }
            if (selectedContentType != null)
            {
                break;
            }
        }
        return selectedContentType;
    }
    
    public static String[] splitContentTypeListString(String contentTypeListString)
    {
        String[] splittedArray = StringUtils.splitShortString(contentTypeListString, ',');
        for (int i = 0; i < splittedArray.length; i++)
        {
            int semicolonIndex = splittedArray[i].indexOf(';');
            if (semicolonIndex != -1)
            {
                splittedArray[i] = splittedArray[i].substring(0,semicolonIndex);
            }
        }
        return splittedArray;
    }
    
    public static String getContentTypeFromAcceptHeader(FacesContext context)
    {
        String contentTypeListString = context.getExternalContext()
            .getRequestHeaderMap().get("Accept");
        // There is a windows mobile IE client (6.12) sending
        // "application/vnd.wap.mms-message;*/*"
        // Note that the Accept header should be written as 
        // "application/vnd.wap.mms-message,*/*" ,
        // so this is bug of the client. Anyway, this is a workaround ...
        if (contentTypeListString != null
                && contentTypeListString.startsWith("application/vnd.wap.mms-message;*/*"))
        {
            contentTypeListString = "*/*";
        }
        return contentTypeListString;
    }

    public static String[] getSupportedContentTypes()
    {
        return SUPPORTED_CONTENT_TYPES;
    }

    public static boolean isXHTMLContentType(String contentType)
    {
        return contentType.contains(XHTML_CONTENT_TYPE)
                || contentType.contains(APPLICATION_XML_CONTENT_TYPE)
                || contentType.contains(TEXT_XML_CONTENT_TYPE);
    }

}
