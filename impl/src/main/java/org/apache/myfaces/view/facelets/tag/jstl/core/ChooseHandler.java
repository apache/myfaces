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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;

/**
 * @author Jacob Hookom
 * @version $Id: ChooseHandler.java,v 1.3 2008/07/13 19:01:43 rlubke Exp $
 */
public final class ChooseHandler extends TagHandler
{

    private final ChooseOtherwiseHandler otherwise;
    private final ChooseWhenHandler[] when;

    public ChooseHandler(TagConfig config)
    {
        super(config);

        List<ChooseWhenHandler> whenList = new ArrayList<ChooseWhenHandler>();
        for (ChooseWhenHandler handler : TagHandlerUtils.findNextByType(nextHandler, ChooseWhenHandler.class))
        {
            whenList.add(handler);
        }
        if (whenList.isEmpty())
        {
            throw new TagException(this.tag, "Choose Tag must have one or more When Tags");
        }

        this.when = (ChooseWhenHandler[]) whenList.toArray(new ChooseWhenHandler[whenList.size()]);

        Iterator<ChooseOtherwiseHandler> itrOtherwise = 
            TagHandlerUtils.findNextByType(nextHandler, ChooseOtherwiseHandler.class).iterator();
        if (itrOtherwise.hasNext())
        {
            this.otherwise = itrOtherwise.next();
        }
        else
        {
            this.otherwise = null;
        }
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        for (int i = 0; i < this.when.length; i++)
        {
            if (this.when[i].isTestTrue(ctx))
            {
                this.when[i].apply(ctx, parent);
                return;
            }
        }
        if (this.otherwise != null)
        {
            this.otherwise.apply(ctx, parent);
        }
    }
}
