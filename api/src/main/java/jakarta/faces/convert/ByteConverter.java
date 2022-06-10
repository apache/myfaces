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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFConverter;
import org.apache.myfaces.core.api.shared.MessageUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
@JSFConverter
public class ByteConverter implements Converter
{
    public static final String CONVERTER_ID = "jakarta.faces.Byte";
    public static final String STRING_ID = "jakarta.faces.converter.STRING";
    public static final String BYTE_ID = "jakarta.faces.converter.ByteConverter.BYTE";

    public ByteConverter()
    {
    }

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(uiComponent, "uiComponent");

        if (value == null)
        {
            return null;
        }
        
        value = value.trim();
        if (value.length() < 1)
        {
            return null;
        }
        
        try
        {
            return Byte.valueOf(value);
        }
        catch (NumberFormatException e)
        {
            throw new ConverterException(MessageUtils.getErrorMessage(facesContext,
                           BYTE_ID,
                           new Object[]{value,"156",MessageUtils.getLabel(facesContext, uiComponent)}), e);
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(uiComponent, "uiComponent");

        if (value == null)
        {
            return "";
        }

        if (value instanceof String)
        {
            return (String) value;
        }
        
        try
        {
            return Byte.toString(((Number)value).byteValue());
        }
        catch (Exception e)
        {
            throw new ConverterException(MessageUtils.getErrorMessage(facesContext, STRING_ID,
                    new Object[]{value,MessageUtils.getLabel(facesContext, uiComponent)}),e);
        }

    }
}
