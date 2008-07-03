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

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Martin Haimberger
 */

class TempServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream tempOS = null;


    public TempServletOutputStream() {
        tempOS = new ByteArrayOutputStream();
    }

    public void write(int n) {
        tempOS.write(n);
    }

    public void resetByteArray() {
        tempOS.reset();
    }

    public byte[] toByteArray() {
        return tempOS.toByteArray();
    }

    public String toString() {
        return tempOS.toString();
    }

    public String toString(String enc) {
        String result = null;
        try {
        result = tempOS.toString(enc);
        }
        catch (UnsupportedEncodingException usee) {
        }
        return result;
    }
    }

