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

import java.util.Locale;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class MockHttpServletResponse extends org.apache.myfaces.test.mock.MockHttpServletResponse{
    
    private boolean committed = false;
    private int status;
    private String message;
    private long contentLength = 0;
    private int bufferSize = 0;
    private Locale locale = Locale.getDefault();
    
    public MockHttpServletResponse() {
        super();
        setCharacterEncoding("ISO-8859-1");
        setContentType("text/html");
    }

    public void sendError(int status, String message){
        if (this.committed) {
            throw new IllegalStateException("Response is already committed");
        }
        this.status = status;
        this.message = message;
        this.committed = true;
    }

    public void sendError(int status){
        if (this.committed) {
            throw new IllegalStateException("Response is already committed");
        }
        this.status = status;
        this.committed = true;
    }

    public void sendRedirect(String path){
        if (this.committed) {
            throw new IllegalStateException("Response is already committed");
        }
        this.committed = true;
    }

    public void setStatus(int sc) {
        this.status = sc;
    }

    public void setStatus(int sc, String message) {
        this.status = sc;
        this.message = message;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setBufferSize(int sz) {
        this.bufferSize = sz;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void flushBuffer(){

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return this.committed;
    }

    public void reset() {

    }

}
