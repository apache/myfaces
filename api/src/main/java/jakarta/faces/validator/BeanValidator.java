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
package jakarta.faces.validator;

import org.apache.myfaces.core.api.shared.MessageUtils;
import org.apache.myfaces.core.api.shared.ExternalSpecifications;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFValidator;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.core.api.shared.FacesMessageInterpolator;
import org.apache.myfaces.core.api.shared.ValueReferenceResolver;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.core.api.shared.lang.ClassUtils;

/**
 * <p>
 * <strong>BeanValidator</strong> is a {@link jakarta.faces.validator.Validator}
 * that doesn't do any validation itself, but delegates validation logic to
 * Bean Validation.
 * </p>
 *
 * @since 2.0
 */
@JSFValidator(
        name="f:validateBean",
        bodyContent="empty")
@JSFJspProperty(
        name = "binding",
        returnType = "jakarta.faces.validator.BeanValidator",
        longDesc = "A ValueExpression that evaluates to a BeanValidator.")
public class BeanValidator implements Validator, PartialStateHolder
{

    private static final Logger log = Logger.getLogger(BeanValidator.class.getName());

    /**
     * Converter ID, as defined by the Faces 2.0 specification.
     */
    public static final String VALIDATOR_ID = "jakarta.faces.Bean";

    /**
     * The message ID for this Validator in the message bundles.
     */
    public static final String MESSAGE_ID = "jakarta.faces.validator.BeanValidator.MESSAGE";

    /**
     * If this init parameter is present, no Bean Validators should be added to an UIInput by default.
     * Explicitly adding a BeanValidator to an UIInput is possible though.
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true, false", since="2.0", group="validation")
    public static final String DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME
            = "jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR";

    /**
     * The key in the ServletContext where the Bean Validation Factory can be found.
     * In a managed Java EE 6 environment, the container initializes the ValidatorFactory
     * and stores it in the ServletContext under this key.
     * If not present, the manually instantiated ValidatorFactory is stored in the ServletContext
     * under this key for caching purposes.
     */
    public static final String VALIDATOR_FACTORY_KEY = "jakarta.faces.validator.beanValidator.ValidatorFactory";

    /**
     * This is used as a separator so multiple validation groups can be specified in one String.
     */
    public static final String VALIDATION_GROUPS_DELIMITER = ",";

    /**
     * This regular expression is used to match for empty validation groups.
     * Currently, a string containing only whitespace is classified as empty.
     */
    public static final String EMPTY_VALIDATION_GROUPS_PATTERN = "^[\\W,]*$";
    
    /**
     * Enable f:validateWholeBean use.
     */
    @JSFWebConfigParam(since="2.3", defaultValue = "false", expectedValues = "true, false", group="validation")
    public static final String ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME = 
            "jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN";
    
    private static final Class<?>[] DEFAULT_VALIDATION_GROUPS_ARRAY = new Class<?>[] { Default.class };

    private static final String DEFAULT_VALIDATION_GROUP_NAME = "jakarta.validation.groups.Default";
    
    private static final String CANDIDATE_COMPONENT_VALUES_MAP = "oam.WBV.candidatesMap";
    
    private static final String BEAN_VALIDATION_FAILED = "oam.WBV.validationFailed";

    private String validationGroups;

    private Class<?>[] validationGroupsArray;

    private boolean isTransient = false;

    private boolean _initialStateMarked = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final FacesContext context, final UIComponent component, final Object value)
    {
        Assert.notNull(context, "context");
        Assert.notNull(component, "component");

        ValueExpression valueExpression = component.getValueExpression("value");
        if (valueExpression == null)
        {
            if (context.isProjectStage(ProjectStage.Development))
            {
                log.fine("cannot validate component with empty value: " 
                    + component.getClientId(context));
            }
            return;
        }

        // Obtain a reference to the to-be-validated object and the property name.
        ValueReference reference = ValueReferenceResolver.resolve(valueExpression, context);
        if (reference == null)
        {
            return;
        }
        Object base = reference.getBase();
        if (base == null)
        {
            return;
        }
        
        Class<?> valueBaseClass = base.getClass();
        if (valueBaseClass == null)
        {
            return;
        }
        
        Object referenceProperty = reference.getProperty();
        if (!(referenceProperty instanceof String))
        {
            // if the property is not a String, the ValueReference does not
            // point to a bean method, but e.g. to a value in a Map, thus we 
            // can exit bean validation here
            return;
        }
        String valueProperty = (String) referenceProperty;

        // Initialize Bean Validation.
        ValidatorFactory validatorFactory = createValidatorFactory(context);
        jakarta.validation.Validator validator = createValidator(validatorFactory, context);
        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(valueBaseClass);
        if (!beanDescriptor.isBeanConstrained())
        {
            return;
        }
        
        // Note that validationGroupsArray was initialized when createValidator was called
        Class[] validationGroupsArray = this.validationGroupsArray;

        // Faces 2.3: If the ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME application parameter is enabled and this Validator 
        // instance has validation groups other than or in addition to the Default group
        boolean containsOtherValidationGroup = false;
        if (validationGroupsArray != null && validationGroupsArray.length > 0)
        {
            for (Class<?> clazz : validationGroupsArray)
            {
                if (!Default.class.equals(clazz))
                {
                    containsOtherValidationGroup = true;
                    break;
                }
            }
        }
        
        // Delegate to Bean Validation.
        Set<?> constraintViolations
                = validator.validateValue(valueBaseClass, valueProperty, value, validationGroupsArray);
        if (!constraintViolations.isEmpty())
        {
            Set<FacesMessage> messages = new LinkedHashSet<>(constraintViolations.size());
            for (Object violation : constraintViolations)
            {
                ConstraintViolation constraintViolation = (ConstraintViolation) violation;
                String message = constraintViolation.getMessage();
                Object[] args = new Object[]{ message, MessageUtils.getLabel(context, component) };
                FacesMessage msg = MessageUtils.getErrorMessage(context, MESSAGE_ID, args);
                messages.add(msg);
            }
            
            if (isValidateWholeBeanEnabled(context) && containsOtherValidationGroup)
            {
                // Faces 2.3: record the fact that this field failed validation so that any <f:validateWholeBean /> 
                // component later in the tree is able to skip class-level validation for the bean for which this 
                // particular field is a property. Regardless of whether or not 
                // ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME is set, throw the new exception.            
                context.getViewRoot().getTransientStateHelper().putTransient(BEAN_VALIDATION_FAILED, Boolean.TRUE);
            }
            
            throw new ValidatorException(messages);
        }
        else
        {
            
            // Faces 2.3: If the returned Set is empty, the ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME application parameter
            // is enabled and this Validator instance has validation groups other than or in addition to the 
            // Default group
            if (isValidateWholeBeanEnabled(context) && containsOtherValidationGroup)
            {
                // record the fact that this field passed validation so that any <f:validateWholeBean /> component 
                // later in the tree is able to allow class-level validation for the bean for which this particular 
                // field is a property.
                
                Map<String, Object> candidatesMap = (Map<String, Object>) context.getViewRoot()
                        .getTransientStateHelper().getTransient(CANDIDATE_COMPONENT_VALUES_MAP);
                if (candidatesMap == null)
                {
                    candidatesMap = new LinkedHashMap<>();
                    context.getViewRoot().getTransientStateHelper().putTransient(
                            CANDIDATE_COMPONENT_VALUES_MAP, candidatesMap);
                }
                candidatesMap.put(component.getClientId(context), value);
            }
        }
    }
    
    private boolean isValidateWholeBeanEnabled(FacesContext facesContext)
    {
        Boolean value = (Boolean) facesContext.getAttributes().get(ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME);
        if (value == null)
        {
            String enabled = facesContext.getExternalContext().getInitParameter(ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME);
            if (enabled == null)
            {
                value = Boolean.FALSE;
            }
            else
            {
                value = Boolean.valueOf(enabled);
            }
        }
        return Boolean.TRUE.equals(value);
    }

    private jakarta.validation.Validator createValidator(final ValidatorFactory validatorFactory, FacesContext context)
    {
        // Set default validation group when setValidationGroups has not been called.
        // The null check is there to prevent it from happening twice.
        if (validationGroupsArray == null)
        {
            postSetValidationGroups();
        }

        return validatorFactory //
                .usingContext() //
                .messageInterpolator(new FacesMessageInterpolator(
                        validatorFactory.getMessageInterpolator(), context)) //
                .getValidator();

    }

    /**
     * This method creates ValidatorFactory instances or retrieves them from the container.
     *
     * Once created, ValidatorFactory instances are stored in the container under the key
     * VALIDATOR_FACTORY_KEY for performance.
     *
     * @param context The FacesContext.
     * @return The ValidatorFactory instance.
     * @throws FacesException if no ValidatorFactory can be obtained because: a) the
     * container is not a Servlet container or b) because Bean Validation is not available.
     */
    private ValidatorFactory createValidatorFactory(FacesContext context)
    {
        Map<String, Object> applicationMap = context.getExternalContext().getApplicationMap();
        Object attr = applicationMap.get(VALIDATOR_FACTORY_KEY);
        if (attr instanceof ValidatorFactory factory)
        {
            return factory;
        }
        else
        {
            synchronized (this)
            {
                if (ExternalSpecifications.isBeanValidationAvailable())
                {
                    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                    applicationMap.put(VALIDATOR_FACTORY_KEY, factory);
                    return factory;
                }
                else
                {
                    throw new FacesException("Bean Validation is not present");
                }
            }
        }
    }

    /**
     * Fully initialize the validation groups if needed.
     * If no validation groups are specified, the Default validation group is used.
     */
    private void postSetValidationGroups()
    {
        if (this.validationGroups == null || this.validationGroups.matches(EMPTY_VALIDATION_GROUPS_PATTERN))
        {
            this.validationGroupsArray = DEFAULT_VALIDATION_GROUPS_ARRAY;
        }
        else
        {
            String[] classes = this.validationGroups.split(VALIDATION_GROUPS_DELIMITER);
            List<Class<?>> validationGroupsList = new ArrayList<>(classes.length);

            for (String clazz : classes)
            {
                clazz = clazz.trim();
                if (!clazz.isEmpty())
                {
                    try
                    {
                        Class<?> theClass = ClassUtils.classForName(clazz);

                        // the class was found
                        validationGroupsList.add(theClass);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new RuntimeException("Could not load validation group", e);                     
                    }
                }
            }
                    
            this.validationGroupsArray = validationGroupsList.toArray(new Class[validationGroupsList.size()]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object saveState(final FacesContext context)
    {
        Assert.notNull(context, "context");

        if (!initialStateMarked())
        {
           //Full state saving.
           return this.validationGroups;
        }
        else if (DEFAULT_VALIDATION_GROUP_NAME.equals(this.validationGroups))
        {
            // default validation groups can be saved as null.
            return null;
        }
        else
        {
            // Save it fully. Remember that by MYFACES-2528
            // validationGroups needs to be stored into the state
            // because this value is often susceptible to use in "combo"
            return this.validationGroups;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void restoreState(final FacesContext context, final Object state)
    {
        Assert.notNull(context, "context");

        if (state != null)
        {
            this.validationGroups = (String) state;
        }
        else
        {
            // When the value is being validated, postSetValidationGroups() sets
            // validationGroups to jakarta.validation.groups.Default. 
            this.validationGroups = null;
        }
    }

    /**
     * Get the Bean Validation validation groups.
     * @return The validation groups String.
     */
    @JSFProperty
    public String getValidationGroups()
    {
        return validationGroups;
    }

    /**
     * Set the Bean Validation validation groups.
     * @param validationGroups The validation groups String, separated by
     *                         {@link BeanValidator#VALIDATION_GROUPS_DELIMITER}.
     */
    public void setValidationGroups(final String validationGroups)
    {
        this.validationGroups = validationGroups;
        this.clearInitialState();
    }

    @JSFProperty
    @SuppressWarnings("unused")
    private Boolean isDisabled()
    {
        return null;
    }
    
    @JSFProperty
    @SuppressWarnings("unused")
    private String getFor()
    {
        return null;
    }

    @Override
    public boolean isTransient()
    {
        return isTransient;
    }

    @Override
    public void setTransient(final boolean isTransient)
    {
        this.isTransient = isTransient;
    }

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
}

