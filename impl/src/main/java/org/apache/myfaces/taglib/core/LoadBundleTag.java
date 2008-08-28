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
package org.apache.myfaces.taglib.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * TODO:
 * We should find a way to save loaded bundles in the state, because otherwise
 * on the next request the bundle map will not be present before the render phase
 * and value bindings that reference to the bundle will always log annoying
 * "Variable 'xxx' could not be resolved" error messages.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class LoadBundleTag
        extends TagSupport
{
    private static final long serialVersionUID = -8892145684062838928L;

    private ValueExpression _basename;
    private String _var;

    public void setBasename(ValueExpression basename)
    {
        _basename = basename;
    }

    public void setVar(String var)
    {
        _var = var;
    }

    public int doStartTag() throws JspException
    {
        if (null == _var)
        {
            throw new NullPointerException("LoadBundle: 'var' must not be null");
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            throw new JspException("No faces context?!");
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot == null)
        {
            throw new JspException("No view root! LoadBundle must be nested inside <f:view> action.");
        }

        Locale locale = viewRoot.getLocale();
        if (locale == null)
        {
            locale = facesContext.getApplication().getDefaultLocale();
        }

        String basename = null;
        if (_basename!=null) {
            if (_basename.isLiteralText()) {
                basename = _basename.getExpressionString();
            } else {
                basename = (String) _basename.getValue(facesContext.getELContext());
            }
        }

        if (null == basename)
        {
            throw new NullPointerException("LoadBundle: 'basename' must not be null");
        }

        final ResourceBundle bundle;
        try
        {
            bundle = ResourceBundle.getBundle(basename,
                                              locale,
                                              Thread.currentThread().getContextClassLoader());
        }
        catch (MissingResourceException e)
        {
            throw new JspException("Resource bundle '" + basename + "' could not be found.", e);
        }

        facesContext.getExternalContext().getRequestMap().put(_var,
                                                              new BundleMap(bundle));
        return Tag.SKIP_BODY;
    }


    private static class BundleMap implements Map
    {
        private ResourceBundle _bundle;
        private List<String> _values;

        public BundleMap(ResourceBundle bundle)
        {
            _bundle = bundle;
        }

        //Optimized methods

        public Object get(Object key)
        {
            try {
                return _bundle.getObject(key.toString());
            } catch (Exception e) {
                return "???" + key + "???";
            }
        }

        public boolean isEmpty()
        {
            return !_bundle.getKeys().hasMoreElements();
        }

        public boolean containsKey(Object key)
        {
            try
            {
                return _bundle.getObject(key.toString()) != null;
            }
            catch (MissingResourceException e)
            {
                return false;
            }
        }


        //Unoptimized methods

        public Collection values()
        {
            if (_values == null)
            {
                _values = new ArrayList<String>();
                for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();)
                {
                    String v = _bundle.getString(enumer.nextElement());
                    _values.add(v);
                }
            }
            return _values;
        }

        public int size()
        {
            return values().size();
        }

        public boolean containsValue(Object value)
        {
            return values().contains(value);
        }

        public Set entrySet()
        {
            Set<Entry> set = new HashSet<Entry>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
            {
                final String k = enumer.nextElement();
                set.add(new Map.Entry() {
                    public Object getKey()
                    {
                        return k;
                    }

                    public Object getValue()
                    {
                        return _bundle.getObject(k);
                    }

                    public Object setValue(Object value)
                    {
                        throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
                    }
                });
            }
            return set;
        }

        public Set keySet()
        {
            Set<String> set = new HashSet<String>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
            {
                set.add(enumer.nextElement());
            }
            return set;
        }


        //Unsupported methods

        public Object remove(Object key)
        {
            throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
        }

        public void putAll(Map t)
        {
            throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
        }

        public Object put(Object key, Object value)
        {
            throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
        }

        public void clear()
        {
            throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
        }

    }

}
