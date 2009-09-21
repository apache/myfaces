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

import javax.servlet.ServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * the problem with the system event listeners is that they
 * are triggered often outside of an existing request
 * hence we have to provide dummy objects
 */

public class _SystemEventServletResponse implements ServletResponse {
    private static final String ERR_OP = "This response class is an empty placeholder";


    @Override
    public String getCharacterEncoding() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public String getContentType() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setCharacterEncoding(String s) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setContentLength(int i) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setContentType(String s) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setBufferSize(int i) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public int getBufferSize() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void resetBuffer() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public boolean isCommitted() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void reset() {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public void setLocale(Locale locale) {
        throw new RuntimeException(ERR_OP);
    }

    @Override
    public Locale getLocale() {
        throw new RuntimeException(ERR_OP);
    }
}
