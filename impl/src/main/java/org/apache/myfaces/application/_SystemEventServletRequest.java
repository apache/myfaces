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


public class _SystemEventServletRequest implements ServletRequest{
    private static final String ERR_OP = "This request class is an empty placeholder";

    Map<String, Object> _attributesMap = new HashMap<String, Object>();

    @Override
    public Object getAttribute(String s) {
       return  _attributesMap.get(s);
    }

    @Override
    public Enumeration getAttributeNames() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getCharacterEncoding() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public int getContentLength() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getContentType() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getParameter(String s) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public Enumeration getParameterNames() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String[] getParameterValues(String s) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public Map getParameterMap() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getProtocol() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getScheme() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getServerName() {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public int getServerPort() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getRemoteAddr() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getRemoteHost() {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public void setAttribute(String s, Object o) {
        _attributesMap.put(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        _attributesMap.remove(s);
    }

    @Override
    public Locale getLocale() {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public Enumeration getLocales() {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public boolean isSecure() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public String getRealPath(String s) {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public int getRemotePort() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getLocalName() {
        throw new RuntimeException(ERR_OP);
      }

    @Override
    public String getLocalAddr() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public int getLocalPort() {
        throw new RuntimeException(ERR_OP);
    }
}
