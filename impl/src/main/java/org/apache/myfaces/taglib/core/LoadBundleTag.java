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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Loads a resource bundle and saves it as a variable in the request scope.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFJspTag
 *   name="f:loadBundle"
 *   bodyContent="empty"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class LoadBundleTag
        extends TagSupport
{
 /*
 * TODO:
 * We should find a way to save loaded bundles in the state, because otherwise
 * on the next request the bundle map will not be present before the render phase
 * and value bindings that reference to the bundle will always log annoying
 * "Variable 'xxx' could not be resolved" error messages.
 */
    
    private static final long serialVersionUID = -8892145684062838928L;

    private static final Log log = LogFactory.getLog(LoadBundleTag.class);

    private String _basename;
    private String _var;

    /**
     * The base name of the resource bundle.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setBasename(String basename)
    {
        _basename = basename;
    }

    /**
     * The name of the variable in request scope that the resources
     * are saved to.  This must be a static value.
     *  
     * @JSFJspAttribute
     *   required="true"
     */
    public void setVar(String var)
    {
        _var = var;
    }

    public int doStartTag() throws JspException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            throw new JspException("No faces context?!");
        }

        try
        {
            resolveBundle(getBasename(facesContext));
        }
        catch(IllegalStateException ex)
        {
            throw new JspException(ex);
        }

        return Tag.SKIP_BODY;
    }

    private String getBasename(FacesContext facesContext) {
        String basename = null;

        if (_basename!=null) {
            if (UIComponentTag.isValueReference(_basename)) {
                basename = (String)facesContext.getApplication().createValueBinding(_basename).getValue(facesContext);
            } else {
                basename = _basename;
            }
        }
        return basename;
    }

    /**
     * This method is copied over to org.apache.myfaces.custom.loadbundle.LoadBundle.
     * If you change anything here, think about changing it there as well.
     *
     * @param resolvedBasename
     */
    private void resolveBundle(String resolvedBasename) {
        //ATTENTION: read comment above before changing this!
        FacesContext facesContext = FacesContext.getCurrentInstance();

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot == null)
        {
            throw new IllegalStateException("No view root! LoadBundle must be nested inside <f:view> action.");
        }

        Locale locale = viewRoot.getLocale();
        if (locale == null)
        {
            locale = facesContext.getApplication().getDefaultLocale();
        }

        final ResourceBundle bundle;
        try
        {
            bundle = ResourceBundle.getBundle(resolvedBasename,
                                              locale,
                                              Thread.currentThread().getContextClassLoader());

            facesContext.getExternalContext().getRequestMap().put(_var,
                                                                  new BundleMap(bundle));

        }
        catch (MissingResourceException e)
        {
            log.error("Resource bundle '" + resolvedBasename + "' could not be found.",e);
        }
        //ATTENTION: read comment above before changing this!
    }


    /**
     * This class is copied over to org.apache.myfaces.custom.loadbundle.LoadBundle.
     * If you change anything here, think about changing it there as well.
     *
     */
    private static class BundleMap implements Map
    {
        //ATTENTION: read javadoc
        private ResourceBundle _bundle;
        private List _values;

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
                return "MISSING: " + key + " :MISSING";
            }
        }

        public boolean isEmpty()
        {
            return !_bundle.getKeys().hasMoreElements();
        }

        public boolean containsKey(Object key)
        {
            try {
                return _bundle.getObject(key.toString()) != null;
            } catch (MissingResourceException e) {
                return false;
            }
        }


        //Unoptimized methods

        public Collection values()
        {
            if (_values == null)
            {
                _values = new ArrayList();
                for (Enumeration enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
                {
                    String v = _bundle.getString((String)enumer.nextElement());
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
            Set set = new HashSet();
            for (Enumeration enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
            {
                final String k = (String)enumer.nextElement();
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
            Set set = new HashSet();
            for (Enumeration enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
            {
                set.add(enumer.nextElement());
            }
            return set;
        }
        //ATTENTION: read javadoc


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
        //ATTENTION: read javadoc
    }

}
