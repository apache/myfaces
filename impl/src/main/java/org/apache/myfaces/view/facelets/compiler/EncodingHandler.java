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
import java.util.List;

import javax.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

public class EncodingHandler implements FaceletHandler
{
    private final FaceletHandler next;
    private final String encoding;
    
    private volatile List<String> _uniqueIdList;

    public EncodingHandler(FaceletHandler next, String encoding)
    {
        this.next = next;
        this.encoding = encoding;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        this.next.apply(ctx, parent);
        if (this.encoding == null)
        {
            if (!ctx.getFacesContext().getAttributes().containsKey(FaceletViewDeclarationLanguage.PARAM_ENCODING))
            {
                ctx.getFacesContext().getAttributes().put(FaceletViewDeclarationLanguage.PARAM_ENCODING, "UTF-8");
            }
        }
        else
        {
            //Encoding of document takes precedence over f:view contentType
            ctx.getFacesContext().getAttributes().put(FaceletViewDeclarationLanguage.PARAM_ENCODING, this.encoding);
        }
    }

    public List<String> getUniqueIdList()
    {
        return _uniqueIdList;
    }

    public void setUniqueIdList(List<String> uniqueIdList)
    {
        this._uniqueIdList = uniqueIdList;
    }

}
