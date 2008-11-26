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
package org.apache.myfaces.context.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * implementation of a switching response
 * wrapper to turn output on and off according to
 * the JSF spec 2.p.
 * <p/>
 * The Response has to be switchable between on and off according
 * to the JSF spec 2.0!
 * <p/>
 * we use an internal delegate to enable the switching
 * between on and off states!
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class ResponseSwitch extends ServletResponseWrapper {

    boolean _enabled = true;
    /**
     * one writer and
     * one stream per response
     */
    Writer _switchableWriter;
    OutputStream _switchableOutputStream;

    /**
     * Constructor, implemented as servlet response
     * wrapper so that the switching can be covered
     * at the lowest possible level
     * 
     * @param response
     * @throws java.io.IOException
     */
    public ResponseSwitch(ServletResponse response) throws IOException {
        super(response);
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public int getBufferSize() {
        if (_enabled) {
            return super.getBufferSize();
        }
        return 0;
    }

    @Override
    public boolean isCommitted() {
        if (_enabled) {
            return super.isCommitted();
        }
        return false;
    }

    @Override
    public void reset() {
        if (_enabled) {
            super.reset();
        }
    }

    @Override
    public void resetBuffer() {
        if (_enabled) {
            super.resetBuffer();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (_switchableOutputStream == null) {
            _switchableOutputStream = new SwitchableOutputStream(super.getOutputStream());
        }
        return (SwitchableOutputStream) _switchableOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (_switchableWriter == null) {
            _switchableWriter = new PrintWriter(new SwitchableWriter(super.getWriter()));
        }
        return (PrintWriter) _switchableWriter;
    }

    class SwitchableOutputStream extends ServletOutputStream {

        OutputStream _delegate = null;

        public SwitchableOutputStream(ServletOutputStream delegate) {
            _delegate = delegate;
        }

        public void write(int i) throws IOException {
            if (_enabled) {
                _delegate.write(i);
            }
        }

        public void write(byte[] bytes) throws IOException {
            if (_enabled) {
                _delegate.write(bytes);
            }
        }

        public void write(byte[] bytes, int i, int i1) throws IOException {
            if (_enabled) {
                _delegate.write(bytes, i, i1);
            }
        }

        public void flush() throws IOException {
            if (_enabled) {
                _delegate.flush();
            }
        }

        public void close() throws IOException {
            if (_enabled) {
                _delegate.close();
            }
        }
    }

    class SwitchableWriter extends Writer {

        Writer _delegate = null;

        public SwitchableWriter(Writer delegate) {
            _delegate = delegate;
        }

        public void write(String arg0, int arg1, int arg2) throws IOException {
            if (_enabled) {
                _delegate.write(arg0, arg1, arg2);
            }
        }

        public void write(String arg0) throws IOException {
            if (_enabled) {
                _delegate.write(arg0);
            }
        }

        public void write(char[] arg0, int arg1, int arg2) throws IOException {
            if (_enabled) {
                _delegate.write(arg0, arg1, arg2);
            }
        }

        public void write(char[] arg0) throws IOException {
            if (_enabled) {
                _delegate.write(arg0);

            }
        }

        public void write(int arg0) throws IOException {
            if (_enabled) {
                _delegate.write(arg0);
            }
        }

        public void flush() throws IOException {
            if (_enabled) {
                _delegate.flush();
            }
        }

        public void close() throws IOException {
            if (_enabled) {
                _delegate.close();
            }
        }

        public Writer append(char arg0) throws IOException {
            if (_enabled) {
                return _delegate.append(arg0);
            }
            return this;
        }

        public Writer append(CharSequence arg0, int arg1, int arg2) throws IOException {
            if (_enabled) {
                return _delegate.append(arg0, arg1, arg2);
            }
            return this;
        }

        public Writer append(CharSequence arg0) throws IOException {
            if (_enabled) {
                return _delegate.append(arg0);
            }
            return this;
        }
    }
}
