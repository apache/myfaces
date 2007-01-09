package org.apache.myfaces.application.jsp;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter _writer;
    private StringWriter _stringWriter;
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
        getResponse().getWriter().write(_stringWriter.toString());
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (_writer == null)
        {
            _stringWriter = new StringWriter(4096);
            _writer = new PrintWriter(_stringWriter);
        }
        return _writer;
    }

    @Override
    public String toString()
    {
        return _stringWriter.toString();
    }
}
