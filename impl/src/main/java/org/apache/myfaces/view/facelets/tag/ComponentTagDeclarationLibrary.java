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
package org.apache.myfaces.view.facelets.tag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 *
 * @author lu4242
 */
public class ComponentTagDeclarationLibrary implements TagLibrary
{
    private final Map<String, Map<String, TagHandlerFactory>> _factories;

    public ComponentTagDeclarationLibrary()
    {
        _factories = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.TagLibrary#containsNamespace(java.lang.String)
     */
    @Override
    public boolean containsNamespace(String ns)
    {
        return _factories.containsKey(ns);
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.TagLibrary#containsTagHandler(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsTagHandler(String ns, String localName)
    {
        if (containsNamespace(ns))
        {
            Map<String, TagHandlerFactory> map = _factories.get(ns);
            if (map == null)
            {
                return false;
            }
            return map.containsKey(localName);
        }
        else
        {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.TagLibrary#createTagHandler(java.lang.String, java.lang.String,
     * org.apache.myfaces.view.facelets.tag.TagConfig)
     */
    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException
    {
        if (containsNamespace(ns))
        {
            Map<String, TagHandlerFactory> map = _factories.computeIfAbsent(ns, k -> new HashMap<>());
            TagHandlerFactory f = map.get(localName);
            if (f != null)
            {
                return f.createHandler(tag);
            }
        }
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.TagLibrary#containsFunction(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsFunction(String ns, String name)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.TagLibrary#createFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method createFunction(String ns, String name)
    {
        return null;
    }

    /**
     * Add a ComponentHandler with the specified componentType and rendererType, aliased by the tag name.
     * 
     * See ComponentHandler
     * See jakarta.faces.application.Application#createComponent(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo /&gt;
     * @param componentType
     *            componentType to use
     * @param rendererType
     *            rendererType to use
     */
    public final void addComponent(String namespace, String name, String componentType, String rendererType)
    {
        Map<String, TagHandlerFactory> map = _factories.computeIfAbsent(namespace, k -> new HashMap<>());
        map.put(name, new ComponentHandlerFactory(componentType, rendererType));
    }

    /**
     * Add a ComponentHandler with the specified componentType and rendererType, aliased by the tag name. The Facelet
     * will be compiled with the specified HandlerType (which must extend AbstractComponentHandler).
     * 
     * See AbstractComponentHandler
     * @param name
     *            name to use, "foo" would be &lt;my:foo /&gt;
     * @param componentType
     *            componentType to use
     * @param rendererType
     *            rendererType to use
     * @param handlerType
     *            a Class that extends AbstractComponentHandler
     */
    public final void addComponent(String namespace, String name, String componentType, String rendererType, 
                                      Class<? extends TagHandler> handlerType)
    {
        Map<String, TagHandlerFactory> map = _factories.computeIfAbsent(namespace, k -> new HashMap<>());
        map.put(name, new UserComponentHandlerFactory(componentType, rendererType, handlerType));
    }

    private static class ComponentConfigWrapper implements ComponentConfig
    {
        protected final TagConfig parent;
        protected final String componentType;
        protected final String rendererType;

        public ComponentConfigWrapper(TagConfig parent, String componentType, String rendererType)
        {
            this.parent = parent;
            this.componentType = componentType;
            this.rendererType = rendererType;
        }

        @Override
        public String getComponentType()
        {
            return this.componentType;
        }

        @Override
        public String getRendererType()
        {
            return this.rendererType;
        }

        @Override
        public FaceletHandler getNextHandler()
        {
            return this.parent.getNextHandler();
        }

        @Override
        public Tag getTag()
        {
            return this.parent.getTag();
        }

        @Override
        public String getTagId()
        {
            return this.parent.getTagId();
        }
    }

    private static class ComponentHandlerFactory implements TagHandlerFactory
    {
        protected final String componentType;
        protected final String renderType;

        public ComponentHandlerFactory(String componentType, String renderType)
        {
            this.componentType = componentType;
            this.renderType = renderType;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            ComponentConfig ccfg = new ComponentConfigWrapper(cfg, this.componentType, this.renderType);
            return new jakarta.faces.view.facelets.ComponentHandler(ccfg);
        }
    }

    private static class UserComponentHandlerFactory implements TagHandlerFactory
    {
        private final static Class<?>[] CONS_SIG = new Class[] { ComponentConfig.class };

        protected final String componentType;
        protected final String renderType;
        protected final Class<? extends TagHandler> type;
        protected final Constructor<? extends TagHandler> constructor;

        public UserComponentHandlerFactory(String componentType, String renderType, Class<? extends TagHandler> type)
        {
            this.componentType = componentType;
            this.renderType = renderType;
            this.type = type;
            try
            {
                this.constructor = this.type.getConstructor(CONS_SIG);
            }
            catch (Exception e)
            {
                throw new FaceletException("Must have a Constructor that takes in a ComponentConfig", e);
            }
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            try
            {
                ComponentConfig ccfg = new ComponentConfigWrapper(cfg, componentType, renderType);
                return constructor.newInstance(new Object[] { ccfg });
            }
            catch (InvocationTargetException e)
            {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            }
            catch (Exception e)
            {
                throw new FaceletException("Error Instantiating ComponentHandler: " + this.type.getName(), e);
            }
        }
    }
}
