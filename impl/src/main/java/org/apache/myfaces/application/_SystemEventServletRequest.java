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
package org.apache.myfaces.application;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
import javax.naming.OperationNotSupportedException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * Dummy request for various system event listeners
 *
 * the problem with the system event listeners is that they
 * are triggered often outside of an existing request
 * hence we have to provide dummy objects
 */


public class _SystemEventServletRequest extends ServletRequestWrapper{
    public _SystemEventServletRequest(ServletRequest request)
    {
        super(request);
    }

    private static final String ERR_OP = "This request class is an empty placeholder";

    Map<String, Object> _attributesMap = new HashMap<String, Object>();

    public Object getAttribute(String s) {
       return  _attributesMap.get(s);
    }

    public Enumeration getAttributeNames() {
        throw new RuntimeException(ERR_OP);
    }

    public String getCharacterEncoding() {
        throw new RuntimeException(ERR_OP);
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new RuntimeException(ERR_OP);
    }

    public int getContentLength() {
        throw new RuntimeException(ERR_OP);
    }

    public String getContentType() {
        throw new RuntimeException(ERR_OP);
    }

    public ServletInputStream getInputStream() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    public String getParameter(String s) {
        throw new RuntimeException(ERR_OP);
    }

    public Enumeration getParameterNames() {
        throw new RuntimeException(ERR_OP);
    }

    public String[] getParameterValues(String s) {
        throw new RuntimeException(ERR_OP);
    }

    public Map getParameterMap() {
        throw new RuntimeException(ERR_OP);
    }

    public String getProtocol() {
        throw new RuntimeException(ERR_OP);
    }

    public String getScheme() {
        throw new RuntimeException(ERR_OP);
    }

    public String getServerName() {
        throw new RuntimeException(ERR_OP);
      }

    public int getServerPort() {
        throw new RuntimeException(ERR_OP);
    }

    public BufferedReader getReader() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    public String getRemoteAddr() {
        throw new RuntimeException(ERR_OP);
    }

    public String getRemoteHost() {
        throw new RuntimeException(ERR_OP);
      }

    public void setAttribute(String s, Object o) {
        _attributesMap.put(s, o);
    }

    public void removeAttribute(String s) {
        _attributesMap.remove(s);
    }

    public Locale getLocale() {
        throw new RuntimeException(ERR_OP);
      }

    public Enumeration getLocales() {
        throw new RuntimeException(ERR_OP);
      }

    public boolean isSecure() {
        throw new RuntimeException(ERR_OP);
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        throw new RuntimeException(ERR_OP);
      }

    public String getRealPath(String s) {
        throw new RuntimeException(ERR_OP);
      }

    public int getRemotePort() {
        throw new RuntimeException(ERR_OP);
    }

    public String getLocalName() {
        throw new RuntimeException(ERR_OP);
      }

    public String getLocalAddr() {
        throw new RuntimeException(ERR_OP);
    }

    public int getLocalPort() {
        throw new RuntimeException(ERR_OP);
    }
}
