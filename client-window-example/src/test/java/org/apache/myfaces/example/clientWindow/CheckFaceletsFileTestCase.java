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
package org.apache.myfaces.example.clientWindow;

import java.util.List;
import java.util.Set;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared.config.MyfacesConfig;

import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.util.FastWriter;
import org.junit.Test;

public class CheckFaceletsFileTestCase extends FaceletTestCase
{

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.example");

    }
    /**
     * Check all .xhtml can be parsed by facelets compiler
     *
     * @throws Exception
     */
    @Test
    public void testCanCompileFaceletFiles() throws Exception
    {
        Compiler compiler = vdl.createCompiler(facesContext);
        FaceletFactory factory = vdl.createFaceletFactory(facesContext, compiler);

        checkFaceletsFiles(factory, "/");
    }

    private void checkFaceletsFiles(FaceletFactory factory, String pathBase) throws Exception
    {
        Set<String> paths = externalContext.getResourcePaths(pathBase);
        for (String path : paths)
        {
            if (path.endsWith(".xhtml"))
            {
                factory.getFacelet(getLocalFile(path.substring(1)));
            }
            else if (path.endsWith("/"))
            {
                checkFaceletsFiles(factory, path);
            }
        }
    }


    /**
     * Check a if a single view can be built.
     *
     * @throws Exception
     */
    @Test
    public void testSingleFaceletsFile() throws Exception
    {
        //Step 1: initialize beans

        //Step 2: build view
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "helloWorld.xhtml");

        //Step 3: render view
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }

    @Test
    public void testCanBuildViews() throws Exception
    {
        //Step 1: initialize beans
        /*
        List<String> viewPaths = new ArrayList<String>();
        
        Set<String> paths = externalContext.getResourcePaths("/");
        
        for (String path : paths)
        {
            // Those places are used to hold templates and composite components,
            // ignore them.
            if (path.startsWith("/META-INF") ||
                path.startsWith("/WEB-INF") ||
                path.startsWith("/resources"))
            {
                continue;
            }
            else if (path.endsWith(".xhtml")) 
            {
                viewPaths.add(path);
            }
            else if (path.endsWith("/"))
            {
                addViewFacelets(viewPaths, path);
            }
        }
        
        //Step 2: build view
        for (String viewPath : viewPaths)
        {
            UIViewRoot root = application.getViewHandler().createView(facesContext, viewPath);
            facesContext.setViewRoot(root);
            vdl.buildView(facesContext, root, viewPath);
        }*/
    }

    private void addViewFacelets(List<String> viewPaths, String pathBase)
    {
        Set<String> paths = externalContext.getResourcePaths(pathBase);
        for (String path : paths)
        {
            if (path.endsWith(".xhtml"))
            {
                viewPaths.add(path);
            }
            else if (path.endsWith("/"))
            {
                addViewFacelets(viewPaths, path);
            }
        }
    }


    @Override
    protected String getDirectory()
    {
        return "webapp";
    }

}
