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
package org.apache.myfaces.view.facelets.compiler;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextWrapper;
import jakarta.faces.view.facelets.TagDecorator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.ComponentTagDeclaration;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.util.WebConfigParamUtils;
import org.apache.myfaces.view.facelets.tag.ComponentTagDeclarationLibrary;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeResourceLibrary;
import org.apache.myfaces.view.facelets.tag.faces.JsfLibrary;
import org.apache.myfaces.view.facelets.tag.faces.PassThroughLibrary;
import org.apache.myfaces.view.facelets.tag.faces.core.CoreLibrary;
import org.apache.myfaces.view.facelets.tag.faces.html.HtmlLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.core.JstlCoreLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.core.LegacyJstlCoreLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.fn.JstlFnLibrary;
import org.apache.myfaces.view.facelets.tag.ui.LegacyUILibrary;
import org.apache.myfaces.view.facelets.tag.ui.UILibrary;

/**
 * Perform initialization steps for facelets compiler
 *
 * @since 2.2
 */
public class FaceletsCompilerSupport
{
    private static final Logger log = Logger.getLogger(FaceletsCompilerSupport.class.getName());

    /**
     * Load the various tag libraries for Facelets.
     *
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the page compiler
     */
    public void loadLibraries(FacesContext context, Compiler compiler)
    {
        ExternalContext eContext = context.getExternalContext();
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(context);

        // Initialize Runtime Libraries
        compiler.addTagLibrary(new CoreLibrary());
        compiler.addTagLibrary(new HtmlLibrary());
        if (config.isStrictJsf2FaceletsCompatibility())
        {
            compiler.addTagLibrary(new LegacyUILibrary());
            compiler.addTagLibrary(new LegacyJstlCoreLibrary());
            compiler.addTagLibrary(new LegacyJstlCoreLibrary(JstlCoreLibrary.ALTERNATIVE_NAMESPACE));
        }
        else
        {
            compiler.addTagLibrary(new UILibrary());
            compiler.addTagLibrary(new JstlCoreLibrary());
            compiler.addTagLibrary(new JstlCoreLibrary(JstlCoreLibrary.ALTERNATIVE_NAMESPACE));            
        }
        compiler.addTagLibrary(new JstlFnLibrary());
        compiler.addTagLibrary(new CompositeLibrary());
        compiler.addTagLibrary(new CompositeResourceLibrary(context,
            CompositeResourceLibrary.NAMESPACE_PREFIX));
        compiler.addTagLibrary(new CompositeResourceLibrary(context,
            CompositeResourceLibrary.JCP_NAMESPACE_PREFIX));
        compiler.addTagLibrary(new CompositeResourceLibrary(context,
            CompositeResourceLibrary.SUN_NAMESPACE_PREFIX));
        compiler.addTagLibrary(new JsfLibrary());
        compiler.addTagLibrary(new PassThroughLibrary());
        
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(eContext);
        if (!runtimeConfig.getComponentTagDeclarations().isEmpty())
        {
            // Set a dummy view root, to avoid Application.createComponent() to fail
            ComponentTagDeclarationLibrary componentTagDeclarationLibrary = new ComponentTagDeclarationLibrary();
            
            LoadComponentTagDeclarationFacesContextWrapper wrappedFacesContext = 
                new LoadComponentTagDeclarationFacesContextWrapper(context);
            try
            {
                wrappedFacesContext.setWrapperAsCurrentFacesContext();
                UIViewRoot root = new UIViewRoot();
                root.setRenderKitId("HTML_BASIC");
                wrappedFacesContext.setViewRoot(new UIViewRoot());
                
                for (ComponentTagDeclaration declaration : runtimeConfig.getComponentTagDeclarations())
                {
                    // We have here probably an inconsistency, because the annotation does not
                    // have a default renderer type. Let the renderer type be null will cause problems 
                    // later, because application.createComponent() may not scan the renderer class if
                    // a rendererType is not provided. The easy way to overcome this situation is create
                    // a dummy instance and check its rendererType. If is set the renderer if any will be
                    // scanned for annotations, if not it just do things as usual. It is unlikely to create
                    // a component and does not set a default renderer type if is required.
                    UIComponent component = context.getApplication().createComponent(declaration.getComponentType());
                    componentTagDeclarationLibrary.addComponent(declaration.getNamespace(), 
                            declaration.getTagName(), declaration.getComponentType(), component.getRendererType());
                }
            }
            finally
            {
                wrappedFacesContext.restoreCurrentFacesContext();
            }
            compiler.addTagLibrary(componentTagDeclarationLibrary);
        }
        
        List<FaceletTagLibrary> faceletTagLibraries = runtimeConfig.getFaceletTagLibraries();
        for (FaceletTagLibrary faceletTagLibrary : faceletTagLibraries)
        {
            //Create TagLibrary here, populate and add it to the compiler.
            TagLibrary tl = TagLibraryConfig.create(context, faceletTagLibrary);
            if (tl != null)
            {
                compiler.addTagLibrary(tl);
            }
        }
    }
    
    /**
     * Load the various decorators for Facelets.
     *
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the page compiler
     */
    public void loadDecorators(FacesContext context, Compiler compiler)
    {
        String param = WebConfigParamUtils.getStringInitParameter(context.getExternalContext(),
                ViewHandler.FACELETS_DECORATORS_PARAM_NAME);
        if (param != null)
        {
            for (String decorator : param.split(";"))
            {
                try
                {
                    compiler.addTagDecorator((TagDecorator) ClassUtils.forName(decorator).newInstance());
                    if (log.isLoggable(Level.FINE))
                    {
                        log.fine("Successfully loaded decorator: " + decorator);
                    }
                }
                catch (Exception e)
                {
                    log.log(Level.SEVERE, "Error loading decorator: " + decorator, e);
                }
            }
        }
    }
    
    public void loadOptions(FacesContext context, Compiler compiler)
    {
        ExternalContext eContext = context.getExternalContext();

        // skip comments?
        compiler.setTrimmingComments(WebConfigParamUtils.getBooleanInitParameter(
                eContext, ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, false));
        
        compiler.setFaceletsProcessingConfigurations(
                RuntimeConfig.getCurrentInstance(
                        context.getExternalContext()).getFaceletProcessingConfigurations());
    }
    
    private static class LoadComponentTagDeclarationFacesContextWrapper extends FacesContextWrapper
    {
        private FacesContext delegate;
        private UIViewRoot root;

        public LoadComponentTagDeclarationFacesContextWrapper(FacesContext delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public UIViewRoot getViewRoot()
        {
            return root;
        }

        @Override
        public void setViewRoot(UIViewRoot root)
        {
            this.root = root;
        }
        
        @Override
        public FacesContext getWrapped()
        {
            return delegate;
        }
        
        void setWrapperAsCurrentFacesContext()
        {
            setCurrentInstance(this);
        }
        
        void restoreCurrentFacesContext()
        {
            setCurrentInstance(delegate);
        }
    }

}
