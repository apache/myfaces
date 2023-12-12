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
package jakarta.faces.webapp;

import jakarta.faces.FactoryFinder;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

import java.io.IOException;
/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
@MultipartConfig
public final class FacesServlet implements Servlet
{
    /**
     * Comma separated list of URIs of (additional) faces config files.
     * (e.g. /WEB-INF/my-config.xml)See Faces 1.0 PRD2, 10.3.2
     * Attention: You do not need to put /WEB-INF/faces-config.xml in here.
     */
    @JSFWebConfigParam(since="1.1")
    public static final String CONFIG_FILES_ATTR = "jakarta.faces.CONFIG_FILES";

    /**
     * Identify the Lifecycle instance to be used.
     */
    @JSFWebConfigParam(since="1.1")
    public static final String LIFECYCLE_ID_ATTR = "jakarta.faces.LIFECYCLE_ID";
    
    /**
     * Disable automatic FacesServlet xhtml mapping.
     */
    @JSFWebConfigParam(since="2.3")
    public static final String DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME = "jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML";
    
    /**
     * <p class="changed_added_4_0">
     * The <code>ServletContext</code> init parameter consulted by the runtime to tell if the automatic mapping of
     * the {@code FacesServlet} to the extensionless variant (without {@code *.xhtml}) should be enabled.
     * The implementation must enable this automatic mapping if and only if the value of this parameter is equal,
     * ignoring case, to {@code true}.
     * </p>
     *
     * <p>
     * If this parameter is not specified, this automatic mapping is not enabled.
     * </p>
     */
    @JSFWebConfigParam(since="4.0")
    public static final String AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME
            = "jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING";

    private ServletConfig servletConfig;
    private Servlet facesServlet;

    public FacesServlet()
    {
        super();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        this.servletConfig = servletConfig;

        FacesServletFactory factory = (FacesServletFactory)
                FactoryFinder.getFactory(FactoryFinder.FACES_SERVLET_FACTORY);
        this.facesServlet = factory.getFacesServlet(servletConfig);
        this.facesServlet.init(servletConfig);
    }

    @Override
    public void destroy()
    {
        facesServlet.destroy();
        facesServlet = null;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException
    {
        facesServlet.service(request, response);
    }

    @Override
    public String getServletInfo()
    {
        return "FacesServlet of the MyFaces API";
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }
}
