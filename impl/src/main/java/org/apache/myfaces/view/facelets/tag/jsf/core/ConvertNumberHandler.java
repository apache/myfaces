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

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.view.facelets.ConverterConfig;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Register a NumberConverter instance on the UIComponent associated with the closest parent UIComponent custom action.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(
        name = "f:convertNumber",
        bodyContent = "empty", 
        converterClass="jakarta.faces.convert.NumberConverter")
public final class ConvertNumberHandler extends ConverterHandler
{
    private final TagAttribute locale;

    public ConvertNumberHandler(ConverterConfig config)
    {
        super(config);
        this.locale = this.getAttribute("locale");
    }

    /**
     * Returns a new NumberConverter
     * 
     * See NumberConverter
     * See org.apache.myfaces.view.facelets.tag.jsf.ConverterHandler#createConverter(
     * jakarta.faces.view.facelets.FaceletContext)
     */
    protected Converter createConverter(FaceletContext ctx) throws FacesException, ELException, FaceletException
    {
        return ctx.getFacesContext().getApplication().createConverter(NumberConverter.CONVERTER_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * See org.apache.myfaces.view.facelets.tag.ObjectHandler#setAttributes(jakarta.faces.view.facelets.FaceletContext,
     * java.lang.Object)
     */
    @Override
    public void setAttributes(FaceletContext ctx, Object obj)
    {
        super.setAttributes(ctx, obj);
        NumberConverter c = (NumberConverter) obj;
        if (this.locale != null)
        {
            c.setLocale(ComponentSupport.getLocale(ctx, this.locale));
        }
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type)
    {
        return super.createMetaRuleset(type).ignore("locale");
    }

}
