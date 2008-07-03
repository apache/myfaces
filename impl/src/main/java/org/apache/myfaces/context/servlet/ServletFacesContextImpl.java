package org.apache.myfaces.context.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Deprecated
public final class ServletFacesContextImpl extends FacesContextImpl
{
    public ServletFacesContextImpl(ServletContext servletContext, ServletRequest servletRequest, ServletResponse servletResponse)
    {
        super(servletContext, servletRequest, servletResponse);
    }
}
