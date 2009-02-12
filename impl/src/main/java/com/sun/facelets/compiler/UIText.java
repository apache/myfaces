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
package com.sun.facelets.compiler;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELText;

/**
 * @author Jacob Hookom
 * @version $Id: UIText.java,v 1.7 2008/07/13 19:01:33 rlubke Exp $
 */
final class UIText extends UILeaf
{
    private final String _alias;

    private final ELText _txt;

    public UIText(String alias, ELText txt)
    {
        _txt = txt;
        _alias = alias;
    }

    @Override
    public String getFamily()
    {
        return null;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException
    {
        ResponseWriter out = context.getResponseWriter();
        try
        {
            _txt.write(out, context.getELContext());
        }
        catch (ELException e)
        {
            throw new ELException(_alias + ": " + e.getMessage(), e.getCause());
        }
        catch (Exception e)
        {
            throw new ELException(_alias + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getRendererType()
    {
        return null;
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return _txt.toString();
    }
}
