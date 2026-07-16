package org.apache.myfaces.perf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/** CDI-managed validator (mirrors Mojarra test/perf's managed validators). */
@FacesValidator(value = "lengthRangeValidator", managed = true)
public class LengthRangeValidator implements Validator<String>
{
    @Override
    public void validate(FacesContext context, UIComponent component, String value)
    {
        if (value == null)
        {
            return;
        }
        int len = value.length();
        if (len > 100)
        {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "too long", "value must be at most 100 chars"));
        }
    }
}
