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

/**
 * @author Martin Haimberger
 */
import java.io.IOException;
import java.io.Writer;

public class FastStringWriter extends Writer {

    protected StringBuffer _buffer;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Constructs a new <code>FastStringWriter</code> instance
     * using the default capacity of <code>16</code>.</p>
     */
    public FastStringWriter() {
        _buffer = new StringBuffer();
    }

    /**
     * <p>Constructs a new <code>FastStringWriter</code> instance
     * using the specified <code>initialCapacity</code>.</p>
     *
     * @param initialCapacity specifies the initial capacity of the buffer
     *
     * @throws IllegalArgumentException if initialCapacity is less than zero
     */
    public FastStringWriter(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        _buffer = new StringBuffer(initialCapacity);
    }

    // ----------------------------------------------------- Methods from Writer

    public void write(char cbuf[], int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        _buffer.append(cbuf, off, len);
    }

     /**
      * noop
      * @throws IOException
      */
    public void flush() throws IOException {
    }

    /**
     * noop
     * @throws IOException
     */
    public void close() throws IOException {
    }

    // ---------------------------------------------------------- Public Methods

    public void write(String str) {
        write(str, 0, str.length());
    }

    public void write(String str, int off, int len) {
        _buffer.append(str.substring(off, off + len));
    }

    public StringBuffer getBuffer() {
        return _buffer;
    }

    public String toString() {
        return _buffer.toString();
    }

}