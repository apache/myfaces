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
package org.apache.myfaces.view.facelets;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.compiler.Compiler;

/**
 * Expose protected api of FaceletViewDeclarationLanguage to 
 * junit tests.
 * 
 * @author Leonardo
 *
 */
public class MockFaceletViewDeclarationLanguage extends
        FaceletViewDeclarationLanguage
{

    public MockFaceletViewDeclarationLanguage(FacesContext context)
    {
        super(context);
    }
    
    private String _renderedViewId;
    
    public void buildView(FacesContext context, UIViewRoot view, String xmlFile) throws IOException
    {
        _renderedViewId = xmlFile;
        buildView(context, view);
    }
    @Override
    public String getRenderedViewId(FacesContext context, String actionId)
    {
        return _renderedViewId;//super.getRenderedViewId(context, actionId);
    }    

    @Override
    public String calculateViewId(FacesContext context, String viewId)
    {
        String calculatedViewId = super.calculateViewId(context, viewId);
        if (calculatedViewId == null)
        {
            //can't calculate it, just passthrough the received one
            calculatedViewId = viewId;
        }
        return calculatedViewId;
    }

    @Override
    public Compiler createCompiler(FacesContext context)
    {
        return super.createCompiler(context);
    }

    @Override
    public FaceletFactory createFaceletFactory(FacesContext context,
            Compiler compiler)
    {
        return super.createFaceletFactory(context, compiler);
    }

    @Override
    public ResponseWriter createResponseWriter(FacesContext context)
            throws IOException, FacesException
    {
        return super.createResponseWriter(context);
    }

    @Override
    public String getDefaultSuffix(FacesContext context)
            throws FacesException
    {
        return super.getDefaultSuffix(context);
    }

    @Override
    public String getResponseContentType(FacesContext context, String orig)
    {
        return super.getResponseContentType(context, orig);
    }

    @Override
    public String getResponseEncoding(FacesContext context, String orig)
    {
        return super.getResponseEncoding(context, orig);
    }

    @Override
    public void handleFaceletNotFound(FacesContext context, String viewId)
            throws FacesException, IOException
    {
        super.handleFaceletNotFound(context, viewId);
    }

    @Override
    public void handleRenderException(FacesContext context, Exception e)
            throws IOException, ELException, FacesException
    {
        super.handleRenderException(context, e);
    }

    @Override
    public void initialize(FacesContext context)
    {
        super.initialize(context);
    }

    @Override
    public void loadDecorators(FacesContext context, Compiler compiler)
    {
        super.loadDecorators(context, compiler);
    }

    @Override
    public void loadLibraries(FacesContext context, Compiler compiler)
    {
        super.loadLibraries(context, compiler);
    }

    @Override
    public void loadOptions(FacesContext context, Compiler compiler)
    {
        super.loadOptions(context, compiler);
    }

    @Override
    public void sendSourceNotFound(FacesContext context, String message)
    {
        super.sendSourceNotFound(context, message);
    }

    //public Facelet getFacelet(String viewId) throws IOException
    //{
    //    return super._getFacelet(viewId);
    //}

}
