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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.faces.FacesException;
import jakarta.faces.component.Doctype;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.TagDecorator;

import org.apache.myfaces.config.element.FaceletsProcessing;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.facelets.tag.BaseMultipleTagDecorator;
import org.apache.myfaces.view.facelets.tag.BaseTagDecorator;
import org.apache.myfaces.view.facelets.tag.CompositeTagDecorator;
import org.apache.myfaces.view.facelets.tag.CompositeTagLibrary;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.apache.myfaces.view.facelets.tag.faces.html.DefaultTagDecorator;

/**
 * A Compiler instance may handle compiling multiple sources
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public abstract class Compiler
{

    protected final static Logger log = Logger.getLogger(Compiler.class.getName());

    public final static String EXPRESSION_FACTORY = "compiler.ExpressionFactory";

    private static final TagLibrary EMPTY_LIBRARY = new CompositeTagLibrary(new TagLibrary[0]);

    private boolean validating = false;
    private boolean trimmingWhitespace = false;
    private boolean trimmingComments = false;
    private final List<TagLibrary> libraries = new ArrayList<>();
    private final List<TagDecorator> decorators = new ArrayList<>();
    private final Map<String, String> features = new HashMap<>();
    private boolean developmentProjectStage = false;
    private Collection<FaceletsProcessing> faceletsProcessingConfigurations;

    public Compiler()
    {

    }

    public final CompilerResult compile(URL src, String alias) throws IOException, FaceletException, ELException,
            FacesException
    {
        return this.doCompile(src, alias);
    }
    
    public final CompilerResult compileViewMetadata(URL src, String alias)
            throws IOException, FaceletException, ELException, FacesException
    {
        return this.doCompileViewMetadata(src, alias);
    }
    
    public final CompilerResult compileCompositeComponentMetadata(URL src, String alias)
            throws IOException, FaceletException, ELException, FacesException
    {
        return this.doCompileCompositeComponentMetadata(src, alias);
    }
    
    public final CompilerResult compileComponent(
        String taglibURI, String tagName, Map<String,Object> attributes)
    {
        return this.doCompileComponent(taglibURI, tagName, attributes);
    }

    protected abstract CompilerResult doCompile(URL src, String alias)
            throws IOException, FaceletException, ELException, FacesException;

    protected abstract CompilerResult doCompileViewMetadata(URL src, String alias)
            throws IOException, FaceletException, ELException, FacesException;
    
    protected abstract CompilerResult doCompileCompositeComponentMetadata(URL src, String alias)
            throws IOException, FaceletException, ELException, FacesException;
    
    protected abstract CompilerResult doCompileComponent(
        String taglibURI, String tagName, Map<String,Object> attributes);

    public final TagDecorator createTagDecorator()
    {
        if (!this.decorators.isEmpty())
        {
            return new BaseMultipleTagDecorator(new DefaultTagDecorator(), 
                new CompositeTagDecorator(this.decorators.toArray(
                    new TagDecorator[this.decorators.size()])));
        }

        // Faces 2.2 has always enabled the default tag decorator.
        return new BaseTagDecorator(new DefaultTagDecorator());
    }

    public final void addTagDecorator(TagDecorator decorator)
    {
        Assert.notNull(decorator, "decorator");
        if (!this.decorators.contains(decorator))
        {
            this.decorators.add(decorator);
        }
    }

    public final ExpressionFactory createExpressionFactory()
    {
        ExpressionFactory el = (ExpressionFactory) this.featureInstance(EXPRESSION_FACTORY);
        if (el == null)
        {
            try
            {
                el = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
                if (el == null)
                {
                    log.warning("No default ExpressionFactory from Faces Implementation, "
                                + "attempting to load from Feature["
                                + EXPRESSION_FACTORY + ']');
                }
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
        
        return el;
    }

    private Object featureInstance(String name)
    {
        String type = this.features.get(name);
        if (type != null)
        {
            try
            {
                return ClassUtils.forName(type).newInstance();
            }
            catch (Throwable t)
            {
                throw new FaceletException("Could not instantiate feature[" + name + "]: " + type);
            }
        }
        return null;
    }

    public final TagLibrary createTagLibrary()
    {
        if (!this.libraries.isEmpty())
        {
            return new CompositeTagLibrary(this.libraries.toArray(new TagLibrary[this.libraries.size()]));
        }
        return EMPTY_LIBRARY;
    }

    public final void addTagLibrary(TagLibrary library)
    {
        Assert.notNull(library, "library");
        if (!this.libraries.contains(library))
        {
            this.libraries.add(library);
        }
    }

    public final void setFeature(String name, String value)
    {
        this.features.put(name, value);
    }

    public final String getFeature(String name)
    {
        return this.features.get(name);
    }

    public final boolean isTrimmingComments()
    {
        return this.trimmingComments;
    }

    public final void setTrimmingComments(boolean trimmingComments)
    {
        this.trimmingComments = trimmingComments;
    }

    public final boolean isTrimmingWhitespace()
    {
        return this.trimmingWhitespace;
    }

    public final void setTrimmingWhitespace(boolean trimmingWhitespace)
    {
        this.trimmingWhitespace = trimmingWhitespace;
    }

    public final boolean isValidating()
    {
        return this.validating;
    }

    public final void setValidating(boolean validating)
    {
        this.validating = validating;
    }
    
    public final boolean isDevelopmentProjectStage()
    {
        return this.developmentProjectStage;
    }
    
    public final void setDevelopmentProjectStage(boolean developmentProjectStage)
    {
        this.developmentProjectStage = developmentProjectStage;
    }

    /**
     * 
     * @since 2.1.0
     * @return
     */
    public Collection<FaceletsProcessing> getFaceletsProcessingConfigurations()
    {
        return faceletsProcessingConfigurations;
    }

    /**
     * 
     * @since 2.1.0
     * @param faceletsProcessingConfigurations 
     */
    public void setFaceletsProcessingConfigurations(
            Collection<FaceletsProcessing> faceletsProcessingConfigurations)
    {
        this.faceletsProcessingConfigurations = faceletsProcessingConfigurations;
    }
    
    public static class CompilerResult
    {
        private FaceletHandler faceletHandler;
        private Doctype doctype;

        CompilerResult(FaceletHandler faceletHandler, Doctype doctype)
        {
            this.faceletHandler = faceletHandler;
            this.doctype = doctype;
        }

        public FaceletHandler getFaceletHandler()
        {
            return faceletHandler;
        }

        public void setFaceletHandler(FaceletHandler faceletHandler)
        {
            this.faceletHandler = faceletHandler;
        }

        public Doctype getDoctype()
        {
            return doctype;
        }

        public void setDoctypeUnit(Doctype doctype)
        {
            this.doctype = doctype;
        }
    }
}

