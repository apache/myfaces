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
package javax.faces.view.facelets;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-15 17:11:43 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class DelegatingMetaTagHandler extends MetaTagHandler
{
    protected TagHandlerDelegateFactory helperFactory;

    public DelegatingMetaTagHandler(TagConfig config)
    {
        super(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {
        getTagHandlerHelper().apply(ctx, parent);
    }

    public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public TagAttribute getBinding()
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    public Tag getTag()
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    public TagAttribute getTagAttribute(String localName)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    public String getTagId()
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    public boolean isDisabled(FaceletContext ctx)
    {
        // TODO: IMPLEMENT HERE
        return false;
    }

    public void setAttributes(FaceletContext ctx, Object instance)
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class<?> type)
    {
        return getTagHandlerHelper().createMetaRuleset(type);
    }

    protected abstract TagHandlerDelegate getTagHandlerHelper();

}
