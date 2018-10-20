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

import java.io.StringWriter;

import org.junit.Assert;

import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.shared.renderkit.ContentTypeUtils;
import org.apache.myfaces.shared.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Test;

public class HtmlRenderKitImplTest extends AbstractJsfTestCase
{

    @Test
    public void testCreateResponseWriterContentType1()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test
    public void testCreateResponseWriterContentType2()
    {
        MyfacesConfig config = new MyfacesConfig();
        config.setDefaultResponseWriterContentTypeMode(ContentTypeUtils.XHTML_CONTENT_TYPE);
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.XHTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.XHTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test
    public void testCreateResponseWriterContentType3()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); //Firefox
        request.addHeader("Faces-Request", "partial/ajax");
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test
    public void testCreateResponseWriterContentType4()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "application/xml, text/xml, */*; q=0.01"); //Firefox
        request.addHeader("Faces-Request", "partial/ajax");
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test
    public void testCreateResponseWriterContentType5()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"); //Webkit
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateResponseWriterContentType6()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "image/png"); //Webkit
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
    }
    
    @Test
    public void testCreateResponseWriterContentType7()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, " +
        		"image/pjpeg, application/x-ms-xbap, application/x-shockwave-flash, application/msword, */*"); //IE8
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
    
    @Test
    public void testCreateResponseWriterContentType8()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "text/html, application/xml;q=0.9, application/xhtml+xml;q=0.9, image/png, " +
        		"image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1"); //Opera
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }

    @Test
    public void testCreateResponseWriterContentType9()
    {
        MyfacesConfig config = new MyfacesConfig();
        facesContext.getExternalContext().getApplicationMap().put(MyfacesConfig.class.getName(), config);
        request.addHeader("Accept", "image/gif, image/jpeg, image/pjpeg, application/x-ms-application,"+
                "application/vnd.ms-xpsdocument, application/xaml+xml,"+
                "application/x-ms-xbap, application/x-shockwave-flash,"+
                "application/x-silverlight-2-b2, application/x-silverlight,"+
                "application/vnd.ms-excel, application/vnd.ms-powerpoint,"+
                "application/msword, */*"); //IE
        HtmlRenderKitImpl renderKit = new HtmlRenderKitImpl();
        StringWriter writer = new StringWriter();
        HtmlResponseWriterImpl responseWriter = (HtmlResponseWriterImpl) renderKit.createResponseWriter(writer, null, null);
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getContentType());
        Assert.assertEquals(ContentTypeUtils.HTML_CONTENT_TYPE, responseWriter.getWriterContentTypeMode());
    }
}
