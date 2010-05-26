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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.AbstractFacelet;
import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.TemplateContext;
import org.apache.myfaces.view.facelets.TemplateManager;

/**
 * 
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 947351 $ $Date: 2010-05-22 19:19:48 -0500 (Sáb, 22 May 2010) $
 * @since 2.0.1
 */
public class TemplateContextImpl extends TemplateContext
{
    private final LinkedList<TemplateManager> _clients;
    
    private TemplateManager _compositeComponentClient;

    public TemplateContextImpl()
    {
        super();
        _clients = new LinkedList<TemplateManager>();
    }

    @Override
    public TemplateManager popClient()
    {
        return _clients.removeFirst();
    }

    @Override
    public void pushClient(final AbstractFacelet owner, final TemplateClient client)
    {
        _clients.addFirst(new TemplateManagerImpl(owner, client, true));
    }

    public TemplateManager popExtendedClient()
    {
        return _clients.removeLast();
    }
    
    @Override
    public void extendClient(final AbstractFacelet owner, final TemplateClient client)
    {
        _clients.addLast(new TemplateManagerImpl(owner, client, false));
    }

    @Override
    public boolean includeDefinition(FaceletContext ctx, Facelet owner, UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException
    {
        boolean found = false;
        TemplateManager client;
        Iterator<TemplateManager> itr = _clients.iterator();
        while (itr.hasNext() && !found)
        {
            client = itr.next();
            if (client.equals(owner))
                continue;
            found = ((TemplateManagerImpl)client).apply(ctx, parent, name);
        }
        return found;
    }
    
    private final static class TemplateManagerImpl extends TemplateManager implements TemplateClient
    {
        private final AbstractFacelet _owner;

        private final TemplateClient _target;

        private final boolean _root;

        private final Set<String> _names = new HashSet<String>();

        public TemplateManagerImpl(AbstractFacelet owner, TemplateClient target,
                boolean root)
        {
            this._owner = owner;
            this._target = target;
            this._root = root;
        }

        public boolean apply(FaceletContext ctx, UIComponent parent, String name)
                throws IOException, FacesException, FaceletException,
                ELException
        {
            String testName = (name != null) ? name : "facelets._NULL_DEF_";
            if (this._names.contains(testName))
            {
                return false;
            }
            else
            {
                this._names.add(testName);
                boolean found = false;
                found = this._target
                        .apply(new DefaultFaceletContext(
                                (DefaultFaceletContext) ctx, this._owner, false),
                                parent, name);
                this._names.remove(testName);
                return found;
            }
        }

        public boolean equals(Object o)
        {
            // System.out.println(this.owner.getAlias() + " == " +
            // ((DefaultFacelet) o).getAlias());
            return this._owner == o || this._target == o;
        }

        public boolean isRoot()
        {
            return this._root;
        }
    }
    
    public TemplateManager getCompositeComponentClient()
    {
        return _compositeComponentClient;
    }


    public void setCompositeComponentClient(
            TemplateManager compositeComponentClient)
    {
        _compositeComponentClient = compositeComponentClient;
    }
}
