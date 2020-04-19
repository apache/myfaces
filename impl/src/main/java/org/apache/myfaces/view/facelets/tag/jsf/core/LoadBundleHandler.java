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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Load a resource bundle localized for the Locale of the current view, and expose it (as a Map) in the request
 * attributes of the current request. 
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(
        name = "f:loadBundle",
        bodyContent = "empty", 
        tagClass="org.apache.myfaces.taglib.core.LoadBundleTag")
public final class LoadBundleHandler extends TagHandler
{

    private final static class ResourceBundleMap implements Map<String, String>
    {
        private final static class ResourceEntry implements Map.Entry<String, String>
        {
            protected final String key;
            protected final String value;

            public ResourceEntry(String key, String value)
            {
                this.key = key;
                this.value = value;
            }

            @Override
            public String getKey()
            {
                return this.key;
            }

            @Override
            public String getValue()
            {
                return this.value;
            }

            @Override
            public String setValue(String value)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode()
            {
                return this.key.hashCode();
            }

            @Override
            public boolean equals(Object obj)
            {
                return (obj instanceof ResourceEntry && this.hashCode() == obj.hashCode());
            }
        }

        protected final ResourceBundle bundle;

        public ResourceBundleMap(ResourceBundle bundle)
        {
            this.bundle = bundle;
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key)
        {
            return bundle.containsKey(key.toString());
        }

        @Override
        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Map.Entry<String, String>> entrySet()
        {
            Enumeration<String> e = this.bundle.getKeys();
            Set<Map.Entry<String, String>> s = new HashSet<>();
            String k;
            while (e.hasMoreElements())
            {
                k = e.nextElement();
                s.add(new ResourceEntry(k, this.bundle.getString(k)));
            }
            return s;
        }

        @Override
        public String get(Object key)
        {
            try
            {
                if (this.bundle.containsKey((String) key))
                {
                    return this.bundle.getString((String) key);
                }
            }
            catch (java.util.MissingResourceException mre)
            {
                // NOOP
            }

            return "???" + key + "???";
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public Set<String> keySet()
        {
            Enumeration<String> e = this.bundle.getKeys();
            Set<String> s = new HashSet<String>();
            while (e.hasMoreElements())
            {
                s.add(e.nextElement());
            }
            return s;
        }

        @Override
        public String put(String key, String value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> t)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size()
        {
            return this.keySet().size();
        }

        @Override
        public Collection<String> values()
        {
            Enumeration<String> e = this.bundle.getKeys();
            Set<String> s = new HashSet<String>();
            while (e.hasMoreElements())
            {
                s.add(this.bundle.getString(e.nextElement()));
            }
            return s;
        }
    }

    private final TagAttribute basename;

    private final TagAttribute var;

    /**
     * @param config
     */
    public LoadBundleHandler(TagConfig config)
    {
        super(config);
        this.basename = this.getRequiredAttribute("basename");
        this.var = this.getRequiredAttribute("var");
    }

    /**
     * See taglib documentation.
     * 
     * See javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, 
     * javax.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        ResourceBundle bundle = null;
        try
        {
            String name = this.basename.getValue(ctx);
            ClassLoader cl = ClassUtils.getContextClassLoader();
            if (root != null && root.getLocale() != null)
            {
                bundle = ResourceBundle.getBundle(name, root.getLocale(), cl);
            }
            else
            {
                bundle = ResourceBundle.getBundle(name, Locale.getDefault(), cl);
            }
        }
        catch (Exception e)
        {
            throw new TagAttributeException(this.tag, this.basename, e);
        }
        ResourceBundleMap map = new ResourceBundleMap(bundle);
        FacesContext faces = ctx.getFacesContext();
        faces.getExternalContext().getRequestMap().put(this.var.getValue(ctx), map);
    }
}
