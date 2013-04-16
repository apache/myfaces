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

package org.apache.myfaces.view.facelets.mock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.myfaces.test.mock.MockHttpSession;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class MockHttpServletRequest extends org.apache.myfaces.test.mock.MockHttpServletRequest {

    private final ServletContext servletContext;

    private final URI uri;

    private final String method;

    private Cookie[] cookies = new Cookie[0];

    private String servletPath;

    private HttpSession session;

    private final Properties param = new Properties();

    private String characterEncoding = "ISO-8859-1";

    private String contentType = "text/html";

    private int contentLength = 0;

    private String protocol = "HTTP/1.1";

    private String localName = "localhost";

    private int localPort = 80;

    private String remoteAddr = "127.0.0.1";

    private String remoteHost = "localhost";

    private Locale locale = Locale.getDefault();

    private Vector locales = new Vector(Arrays.asList(Locale
            .getAvailableLocales()));
    
    private boolean secure = false;
    
    private int remotePort = 1024;
    
    private String localAddr = "127.0.0.1";

    private ServletInputStream inputStream = new MockServletInputStream();

    public MockHttpServletRequest(ServletContext servletContext, URI uri) {
        this(servletContext, "GET", uri);
    }

    public MockHttpServletRequest(ServletContext servletContext, String uri) {
        this(servletContext, "GET", uri);
    }

    public MockHttpServletRequest(ServletContext servletContext, String method,
            String uri) {
        this(servletContext, method, URI.create(uri));
    }

    public MockHttpServletRequest(ServletContext servletContext, String method,
            URI uri) {
        this.servletContext = servletContext;
        this.uri = uri;
        this.method = method;

        String q = this.uri.getRawQuery();
        if (q != null) {
            String[] p = q.split("(&|=)");
            for (int i = 0; i < p.length; i += 2) {
                this.param.put(p[i], p[i + 1]);
            }
        }
    }

    @Override
    public String getAuthType() {
        return BASIC_AUTH;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getPathInfo() {
        return this.uri.getPath();
    }

    @Override
    public String getPathTranslated() {
        return this.servletContext.getRealPath(this.uri.getPath());
    }

    @Override
    public String getContextPath() {
        return this.uri.getPath();
    }

    @Override
    public String getQueryString() {
        return this.uri.getQuery();
    }

    @Override
    public String getRequestedSessionId() {
        return this.getParameter("jsessionid");
    }

    @Override
    public String getRequestURI() {
        return this.uri.getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(this.uri.toString());
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (this.session == null && create) {
            this.session = new MockHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        return this.getSession(true);
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding){
        this.characterEncoding = characterEncoding;
    }
    
    @Override
    public int getContentLength() {
        return this.contentLength;
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream(){
        return this.inputStream;
    }
    
    public void setParameter(String name, String value) {
    	this.getParameterMap().put(name, value);
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public String getScheme() {
        return this.uri.getScheme();
    }

    @Override
    public String getServerName() {
        return this.localName;
    }

    @Override
    public int getServerPort() {
        return this.localPort;
    }

    @Override
    public BufferedReader getReader(){
        if (this.inputStream != null) {
            try{
                Reader sourceReader = (this.characterEncoding != null) ? new InputStreamReader(
                        this.inputStream, this.characterEncoding)
                        : new InputStreamReader(this.inputStream);
                return new BufferedReader(sourceReader);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public Enumeration getLocales() {
        return this.locales.elements();
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.servletContext.getRequestDispatcher(path);
    }

    @Override
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

}
