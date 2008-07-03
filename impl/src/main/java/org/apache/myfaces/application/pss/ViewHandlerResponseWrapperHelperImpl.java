/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.application.pss;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.CharArrayWriter;

/**
 * @author Martin Haimberger
 */
public class ViewHandlerResponseWrapperHelperImpl extends HttpServletResponseWrapper {
    private TempServletOutputStream tempOS = null;
    private PrintWriter pw = null;
    private CharArrayWriter caw = null;
    private int status = HttpServletResponse.SC_OK;

    public PrintWriter getPw() {
        if (pw == null) {
            caw = new CharArrayWriter();
            pw = new PrintWriter(caw);
        }
        return pw;
    }

    public ServletOutputStream getTempOS() {
        if (tempOS == null)
        {
            tempOS = new TempServletOutputStream();
        }
        return tempOS;
    }

    public ViewHandlerResponseWrapperHelperImpl(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
    }


    // from HttpServletResponseWrapper
    public void sendError(int i, String string) throws IOException {
        super.sendError(i, string);
        status = i;
    }

    public void sendError(int i) throws IOException {
        super.sendError(i);
        status = i;
    }

    public void setStatus(int i) {
        super.setStatus(i);
        status = i;
    }

    public void setStatus(int i, String string) {
        super.setStatus(i, string);
        status = i;
    }


    public String toString() {
        String result = null;
    if (caw != null) {
        result = caw.toString();
    }
    else if (tempOS != null) {
        result = tempOS.toString();
    }

    return result;
    }

    public PrintWriter getWriter() throws IOException {
        return getPw();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return getTempOS();
    }

    public void resetWriter() {
        if (caw != null)
        {
            pw.flush();
            caw.flush();
            caw.reset();
        }
    }


}
