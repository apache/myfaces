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

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFConverter;
import org.apache.myfaces.core.api.shared.MessageUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
@JSFConverter
public class EnumConverter implements Converter<Enum>, PartialStateHolder
{
    public static final String CONVERTER_ID = "jakarta.faces.Enum";
    public static final String ENUM_ID = "jakarta.faces.converter.EnumConverter.ENUM";
    public static final String ENUM_NO_CLASS_ID = "jakarta.faces.converter.EnumConverter.ENUM_NO_CLASS";

    private Class<? extends Enum> targetClass;

    private boolean isTransient = false;

    public EnumConverter()
    {
    }

    public EnumConverter(Class<? extends Enum> targetClass)
    {
        if (!targetClass.isEnum())
        {
            throw new IllegalArgumentException("targetClass for EnumConverter must be an Enum");
        }
        this.targetClass = targetClass;
    }

    @Override
    public Enum<?> getAsObject(FacesContext facesContext, UIComponent component, String value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(component, "uiComponent");

        if (value == null)
        {
            return null;
        }
        value = value.trim();
        if (value.isEmpty())
        {
            return null;
        }

        if (targetClass == null)
        {
            targetClass = tryToExtractEnumClassFromValueBinding(facesContext, component);
        }

        checkTargetClass(facesContext, component, value);

        // we know targetClass and value can't be null, so we can use Enum.valueOf
        // instead of the hokey looping called for in the javadoc
        try
        {
            return Enum.valueOf(targetClass, value);
        }
        catch (IllegalArgumentException e)
        {
            Object[] params =
                    new Object[] { value, firstConstantOfEnum(), MessageUtils.getLabel(facesContext, component) };

            throw new ConverterException(MessageUtils.getErrorMessage(facesContext, ENUM_ID, params));
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Enum value)
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(component, "uiComponent");

        checkTargetClass(facesContext, component, value);

        if (value == null)
        {
            return "";
        }

        if (targetClass == null)
        {
            targetClass = value != null
                    ? value.getDeclaringClass()
                    : tryToExtractEnumClassFromValueBinding(facesContext, component);
        }

        // check if the value is an instance of the enum class
        if (targetClass.isInstance(value))
        {
            return value.name();
        }
        
        Object[] params =
            new Object[] { value, firstConstantOfEnum(), MessageUtils.getLabel(facesContext, component) };

        throw new ConverterException(MessageUtils.getErrorMessage(facesContext, ENUM_ID, params));
    }


    private void checkTargetClass(FacesContext facesContext, UIComponent component, Object value)
    {
        if (targetClass == null)
        {
            Object[] params = new Object[] { value, MessageUtils.getLabel(facesContext, component) };
            throw new ConverterException(MessageUtils.getErrorMessage(facesContext, ENUM_NO_CLASS_ID, params));
        }
    }

    // find the first constant value of the targetClass and return as a String
    private String firstConstantOfEnum()
    {
        Object[] enumConstants = targetClass.getEnumConstants();

        if (enumConstants.length != 0)
        {
            return enumConstants[0].toString();
        }

        return ""; // if empty Enum
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state != null)
        {
            targetClass = (Class<? extends Enum>)state;
        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        if (!initialStateMarked())
        {
            return targetClass;
        }
        return null;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        isTransient = newTransientValue;
    }

    @Override
    public boolean isTransient()
    {
        return isTransient;
    }
    
    private boolean _initialStateMarked = false;

    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
    }

    static <T extends Enum> Class<T> tryToExtractEnumClassFromValueBinding(FacesContext context, UIComponent component)
    {
        ValueExpression ve = component.getValueExpression("value");

        // no ValueExpression defined... skip
        if (ve == null)
        {
            return null;
        }

        // try getExpectedType first, likely returns Object.class
        Class<?> type = ve.getExpectedType();

        // fallback to getType
        if (type == null || type == Object.class)
        {
            try
            {
                type = ve.getType(context.getELContext());
            }
            catch (ELException e)
            {
                // fails if the ValueExpression is actually a MethodExpression
                type = null;
            }
        }

        return (Class<T>) type;
    }
}
