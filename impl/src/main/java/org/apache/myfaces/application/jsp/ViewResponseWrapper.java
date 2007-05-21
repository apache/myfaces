package org.apache.myfaces.application.jsp;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter _writer;
    private CharArrayWriter _charArrayWriter;
    private int _status = HttpServletResponse.SC_OK;
    private WrappedServletOutputStream _byteArrayWriter;

    public ViewResponseWrapper(HttpServletResponse httpServletResponse)
    {
        super(httpServletResponse);
    }

    @Override
    public void sendError(int status) throws IOException
    {
        super.sendError(status);
        _status = status;
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException
    {
        super.sendError(status, errorMessage);
        _status = status;
    }

    @Override
    public void setStatus(int status)
    {
        super.setStatus(status);
        _status = status;
    }

    @Override
    public void setStatus(int status, String errorMessage)
    {
        super.setStatus(status, errorMessage);
        _status = status;
    }

    public int getStatus()
    {
        return _status;
    }

    public void flushToWrappedResponse() throws IOException
    {   if (_charArrayWriter != null) {
            _charArrayWriter.writeTo(getResponse().getWriter());
            _charArrayWriter.reset();
            _writer.flush();
        } else if (_byteArrayWriter != null) {
        System.out.println("BYTE ARRAY WRITER");
            getResponse().getOutputStream().write(_byteArrayWriter.toByteArray());
            _byteArrayWriter.reset();
            _byteArrayWriter.flush();
        }
    }

    public void flushToWriter(Writer writer) throws IOException {
        if (_charArrayWriter != null) {
            _charArrayWriter.writeTo(getResponse().getWriter());
            _charArrayWriter.reset();
            _writer.flush();
        } else if (_byteArrayWriter != null) {
            getResponse().getOutputStream().write(_byteArrayWriter.toByteArray());
            _byteArrayWriter.reset();
            _byteArrayWriter.flush();
        }
        writer.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (_charArrayWriter != null ) throw new IllegalStateException();
        if (_byteArrayWriter == null ) {
            _byteArrayWriter = new WrappedServletOutputStream();
        }
        return _byteArrayWriter;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (_byteArrayWriter != null ) throw new IllegalStateException();
        if (_writer == null)
        {
            _charArrayWriter = new CharArrayWriter(4096);
            _writer = new PrintWriter(_charArrayWriter);
        }
        return _writer;
    }

    @Override
    public void reset()
    {
        if (_charArrayWriter != null)
        {
            _charArrayWriter.reset();
        }
    }

    @Override
    public String toString()
    {
        if (_charArrayWriter != null)
        {
            return _charArrayWriter.toString();
        }
        return null;
    }

    static class WrappedServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream _byteArrayOutputStream;


        public WrappedServletOutputStream() {
            _byteArrayOutputStream = new ByteArrayOutputStream(1024);
        }

        public void write(int i) throws IOException {
           _byteArrayOutputStream.write(i);
        }

        public byte[] toByteArray() {
            return _byteArrayOutputStream.toByteArray();
        }

        public void reset() {
            _byteArrayOutputStream.reset();
        }
        
    }
}
