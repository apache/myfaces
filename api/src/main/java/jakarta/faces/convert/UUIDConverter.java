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
package jakarta.faces.convert;

import java.util.UUID;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFConverter;
import org.apache.myfaces.core.api.shared.MessageUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * @since 4.1
 */
@JSFConverter
public class UUIDConverter implements Converter<UUID>
{
    public static final String CONVERTER_ID = "jakarta.faces.UUID";
    public static final String STRING_ID = "jakarta.faces.converter.STRING";
    public static final String UUID_ID = "jakarta.faces.converter.UUIDConverter.UUID";

    @Override
    public UUID getAsObject(FacesContext facesContext, UIComponent uiComponent, String value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(uiComponent, "uiComponent");

        if (value == null || value.isBlank())
        {
            return null;
        }

        try
        {
            return UUID.fromString(value);
        }
        catch (IllegalArgumentException e)
        {
            throw new ConverterException(
                    MessageUtils.getErrorMessage(facesContext,
                            UUID_ID,
                            new Object[] { value,
                                "29573f55-4254-4afa-9ca6-6b5ae6c7ab6e",
                                MessageUtils.getLabel(facesContext, uiComponent) }),
                    e);
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, UUID value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(uiComponent, "uiComponent");

        if (value == null)
        {
            return "";
        }

        try
        {
            return value.toString();
        }
        catch (Exception e)
        {
            throw new ConverterException(MessageUtils.getErrorMessage(facesContext, STRING_ID,
                    new Object[]{value,MessageUtils.getLabel(facesContext, uiComponent)}), e);
        }
    }
}