package org.apache.myfaces.application.jsp;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter _writer;
    private CharArrayWriter _charArrayWriter;
    private int _status = HttpServletResponse.SC_OK;

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
    {
        _charArrayWriter.writeTo(getResponse().getWriter());
        _charArrayWriter.reset();
        _writer.flush();
        
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (_writer == null)
        {
            _charArrayWriter = new CharArrayWriter(4096);
            _writer = new PrintWriter(_charArrayWriter);
        }
        return _writer;
    }

    @Override
    public String toString()
    {
        return _charArrayWriter.toString();
    }
}
