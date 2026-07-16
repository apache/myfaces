package org.apache.myfaces.perf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/** CDI-managed converter that trims input (mirrors Mojarra test/perf's managed converters). */
@FacesConverter(value = "trimConverter", managed = true)
public class TrimConverter implements Converter<String>
{
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value)
    {
        return value == null ? null : value.trim();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value)
    {
        return value == null ? "" : value;
    }
}
