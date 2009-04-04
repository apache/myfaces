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
package org.apache.myfaces.view.facelets.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;

import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Default Facelet implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultFacelet.java,v 1.11 2008/07/13 19:01:52 rlubke Exp $
 */
final class DefaultFacelet extends Facelet
{

    private static final Logger log = Logger.getLogger("facelets.facelet");

    private final static String APPLIED_KEY = "org.apache.myfaces.view.facelets.APPLIED";

    private final String _alias;

    private final ExpressionFactory _elFactory;

    private final DefaultFaceletFactory _factory;

    private final long _createTime;

    private final long _refreshPeriod;

    private final Map<String, URL> _relativePaths;

    private final FaceletHandler _root;

    private final URL _src;

    public DefaultFacelet(DefaultFaceletFactory factory, ExpressionFactory el, URL src, String alias,
                          FaceletHandler root)
    {
        _factory = factory;
        _elFactory = el;
        _src = src;
        _root = root;
        _alias = alias;
        _createTime = System.currentTimeMillis();
        _refreshPeriod = _factory.getRefreshPeriod();
        _relativePaths = new WeakHashMap<String, URL>();
    }

    /**
     * @see org.apache.myfaces.view.facelets.Facelet#apply(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
     */
    public void apply(FacesContext facesContext, UIComponent parent) throws IOException, FacesException,
            FaceletException, ELException
    {
        DefaultFaceletContext ctx = new DefaultFaceletContext(facesContext, this);
        this.refresh(parent);
        ComponentSupport.markForDeletion(parent);
        _root.apply(ctx, parent);
        ComponentSupport.finalizeForDeletion(parent);
        this.markApplied(parent);
    }

    private final void refresh(UIComponent c)
    {
        if (_refreshPeriod > 0)
        {

            // finally remove any children marked as deleted
            int sz = c.getChildCount();
            if (sz > 0)
            {
                UIComponent cc = null;
                List<UIComponent> cl = c.getChildren();
                ApplyToken token;
                while (--sz >= 0)
                {
                    cc = cl.get(sz);
                    if (!cc.isTransient())
                    {
                        token = (ApplyToken) cc.getAttributes().get(APPLIED_KEY);
                        if (token != null && token._time < _createTime && token._alias.equals(_alias))
                        {
                            if (log.isLoggable(Level.INFO))
                            {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + _alias + "] was modified @ "
                                        + df.format(new Date(_createTime)) + ", flushing component applied @ "
                                        + df.format(new Date(token._time)));
                            }
                            cl.remove(sz);
                        }
                    }
                }
            }

            // remove any facets marked as deleted
            if (c.getFacets().size() > 0)
            {
                Collection<UIComponent> col = c.getFacets().values();
                UIComponent fc;
                ApplyToken token;
                for (Iterator<UIComponent> itr = col.iterator(); itr.hasNext();)
                {
                    fc = itr.next();
                    if (!fc.isTransient())
                    {
                        token = (ApplyToken) fc.getAttributes().get(APPLIED_KEY);
                        if (token != null && token._time < _createTime && token._alias.equals(_alias))
                        {
                            if (log.isLoggable(Level.INFO))
                            {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + _alias + "] was modified @ "
                                        + df.format(new Date(_createTime)) + ", flushing component applied @ "
                                        + df.format(new Date(token._time)));
                            }
                            itr.remove();
                        }
                    }
                }
            }
        }
    }

    private final void markApplied(UIComponent parent)
    {
        if (this._refreshPeriod > 0)
        {
            Iterator<UIComponent> itr = parent.getFacetsAndChildren();
            ApplyToken token = new ApplyToken(_alias, System.currentTimeMillis() + _refreshPeriod);
            while (itr.hasNext())
            {
                UIComponent c = itr.next();
                if (!c.isTransient())
                {
                    Map<String, Object> attr = c.getAttributes();
                    if (!attr.containsKey(APPLIED_KEY))
                    {
                        attr.put(APPLIED_KEY, token);
                    }
                }
            }
        }
    }

    /**
     * Return the alias name for error messages and logging
     * 
     * @return alias name
     */
    public String getAlias()
    {
        return _alias;
    }

    /**
     * Return this Facelet's ExpressionFactory instance
     * 
     * @return internal ExpressionFactory instance
     */
    public ExpressionFactory getExpressionFactory()
    {
        return _elFactory;
    }

    /**
     * The time when this Facelet was created, NOT the URL source code
     * 
     * @return final timestamp of when this Facelet was created
     */
    public long getCreateTime()
    {
        return _createTime;
    }

    /**
     * Delegates resolution to DefaultFaceletFactory reference. Also, caches URLs for relative paths.
     * 
     * @param path
     *            a relative url path
     * @return URL pointing to destination
     * @throws IOException
     *             if there is a problem creating the URL for the path specified
     */
    private URL getRelativePath(String path) throws IOException
    {
        URL url = (URL) _relativePaths.get(path);
        if (url == null)
        {
            url = _factory.resolveURL(_src, path);
            _relativePaths.put(path, url);
        }
        return url;
    }

    /**
     * The URL this Facelet was created from.
     * 
     * @return the URL this Facelet was created from
     */
    public URL getSource()
    {
        return _src;
    }

    /**
     * Given the passed FaceletContext, apply our child FaceletHandlers to the passed parent
     * 
     * @see FaceletHandler#apply(FaceletContext, UIComponent)
     * @param ctx
     *            the FaceletContext to use for applying our FaceletHandlers
     * @param parent
     *            the parent component to apply changes to
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    private void include(DefaultFaceletContext ctx, UIComponent parent) throws IOException, FacesException,
            FaceletException, ELException
    {
        this.refresh(parent);
        _root.apply(new DefaultFaceletContext(ctx, this), parent);
        this.markApplied(parent);
    }

    /**
     * Used for delegation by the DefaultFaceletContext. First pulls the URL from {@link #getRelativePath(String)
     * getRelativePath(String)}, then calls {@link #include(FaceletContext, UIComponent, URL) include(FaceletContext,
     * UIComponent, URL)}.
     * 
     * @see FaceletContext#includeFacelet(UIComponent, String)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param path
     *            relative path to the desired Facelet from the FaceletContext
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, String path) throws IOException, FacesException,
            FaceletException, ELException
    {
        URL url = this.getRelativePath(path);
        this.include(ctx, parent, url);
    }

    /**
     * Grabs a DefaultFacelet from referenced DefaultFaceletFacotry
     * 
     * @see DefaultFaceletFactory#getFacelet(URL)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param url
     *            URL source to include Facelet from
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, URL url) throws IOException, FacesException,
            FaceletException, ELException
    {
        DefaultFacelet f = (DefaultFacelet) _factory.getFacelet(url);
        f.include(ctx, parent);
    }

    private static class ApplyToken implements Externalizable
    {
        public String _alias;

        public long _time;

        public ApplyToken()
        {
        }

        public ApplyToken(String alias, long time)
        {
            _alias = alias;
            _time = time;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
        {
            _alias = in.readUTF();
            _time = in.readLong();
        }

        public void writeExternal(ObjectOutput out) throws IOException
        {
            out.writeUTF(_alias);
            out.writeLong(_time);
        }
    }

    public String toString()
    {
        return _alias;
    }
}
