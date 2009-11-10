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
package javax.faces.validator;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFValidator;

import javax.el.*;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 * <strong>BeanValidator</strong> is a {@link javax.faces.validator.Validator}
 * that doesn't do any validation itself, but delegates validation logic to
 * Bean Validation.
 * </p>
 *
 * @author Jan-Kees van Andel
 * @since 2.0
 */
@JSFValidator(
        name="f:validateBean",
        bodyContent="empty")
@JSFJspProperty(
        name = "binding",
        returnType = "javax.faces.validator.BeanValidator",
        longDesc = "A ValueExpression that evaluates to a BeanValidator.")
@FacesValidator(value = BeanValidator.VALIDATOR_ID, isDefault = true)
public class BeanValidator implements Validator, PartialStateHolder
{

    //private static final Log log = LogFactory.getLog(BeanValidator.class);
    private static final Logger log = Logger.getLogger(BeanValidator.class.getName());

    /**
     * Converter ID, as defined by the JSF 2.0 specification.
     */
    public static final String VALIDATOR_ID = "javax.faces.Bean";

    /**
     * The message ID for this Validator in the message bundles.
     */
    public static final String MESSAGE_ID = "javax.faces.validator.BeanValidator.MESSAGE";

    /**
     * If this init parameter is present, no Bean Validators should be added to an UIInput by default.
     * Explicitly adding a BeanValidator to an UIInput is possible though.
     */
    public static final String DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME = "javax.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR";

    /**
     * The key in the ServletContext where the Bean Validation Factory can be found.
     * In a managed Java EE 6 environment, the container initializes the ValidatorFactory
     * and stores it in the ServletContext under this key.
     * If not present, the manually instantiated ValidatorFactory is stored in the ServletContext
     * under this key for caching purposes.
     */
    public static final String VALIDATOR_FACTORY_KEY = "javax.faces.validator.beanValidator.ValidatorFactory";

    /**
     * This is used as a separator so multiple validation groups can be specified in one String.
     */
    public static final String VALIDATION_GROUPS_DELIMITER = ",";

    /**
     * This regular expression is used to match for empty validation groups.
     * Currently, a string containing only whitespace is classified as empty.
     */
    public static final String EMPTY_VALIDATION_GROUPS_PATTERN = "^[\\W,]*$";

    private String validationGroups;

    private Class<?>[] validationGroupsArray;

    private boolean isTransient = false;

    private boolean _initialStateMarked = false;

    /**
     * {@inheritDoc}
     */
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (context == null) throw new NullPointerException("context");
        if (component == null) throw new NullPointerException("component");

        if (value == null)
        {
            return;
        }

        // Obtain a reference to the to-be-validated object and the property name.
        ValueReferenceWrapper valueReferenceWrapper = getValueReference(component, context);
        if (valueReferenceWrapper == null)
        {
            return;
        }
        Class<?> valueBaseClass = valueReferenceWrapper.getBase().getClass();
        String valueProperty = (String) valueReferenceWrapper.getProp();
        if (valueBaseClass == null || valueProperty == null)
        {
            return;
        }

        // Initialize Bean Validation.
        ValidatorFactory validatorFactory = createValidatorFactory(context);
        javax.validation.Validator validator = createValidator(validatorFactory);
        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(valueBaseClass);
        if (!beanDescriptor.isBeanConstrained())
        {
            return;
        }
        Class[] validationGroupsArray = this.validationGroupsArray;

        // Delegate to Bean Validation.
        Set constraintViolations = validator.validateValue(valueBaseClass, valueProperty, value, validationGroupsArray);
        if (!constraintViolations.isEmpty())
        {
            Set<FacesMessage> messages = new LinkedHashSet<FacesMessage>(constraintViolations.size());
            for (Object violation: constraintViolations)
            {
                ConstraintViolation constraintViolation = (ConstraintViolation) violation;
                String message = constraintViolation.getMessage();
                String[] args = new String[]{ message, _MessageUtils.getLabel(context, component) };
                FacesMessage msg = _MessageUtils.getErrorMessage(context, MESSAGE_ID, args);
                messages.add(msg);
            }
            throw new ValidatorException(messages);
        }
    }

    private javax.validation.Validator createValidator(ValidatorFactory validatorFactory)
    {
        // Set default validation group when setValidationGroups has not been called.
        // The null check is there to prevent it from happening twice.
        if (validationGroupsArray == null)
        {
            postSetValidationGroups();
        }

        return validatorFactory //
                .usingContext() //
                .messageInterpolator(FacesMessageInterpolatorHolder.get(validatorFactory)) //
                .getValidator();

    }

    /**
     * Get the ValueReference from the ValueExpression.
     *
     * @param component The component.
     * @param context The FacesContext.
     * @return A ValueReferenceWrapper with the necessary information about the ValueReference.
     */
    private ValueReferenceWrapper getValueReference(UIComponent component, FacesContext context)
    {
        if (_ExternalSpecifications.isUnifiedELAvailable)
        {
            //TODO: Implement when Unified EL for Java EE6 is available.
            throw new FacesException("Unified EL for Java EE6 support is not yet implemented");
        }
        else
        {
            ValueExpression valueExpression = component.getValueExpression("value");
            ELContext elCtx = context.getELContext();
            return ValueReferenceResolver.resolve(valueExpression, elCtx);
        }
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
    private synchronized ValidatorFactory createValidatorFactory(FacesContext context)
    {
        Object ctx = context.getExternalContext().getContext();
        if (ctx instanceof ServletContext)
        {
            ServletContext servletCtx = (ServletContext) ctx;
            Object attr = servletCtx.getAttribute(VALIDATOR_FACTORY_KEY);
            if (attr != null)
            {
                return (ValidatorFactory) attr;
            }
            else
            {
                if (_ExternalSpecifications.isBeanValidationAvailable)
                {
                    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                    servletCtx.setAttribute(VALIDATOR_FACTORY_KEY, attr);
                    return factory;
                }
                else
                {
                    throw new FacesException("Bean Validation is not present");
                }
            }
        }
        else
        {
            throw new FacesException("Only Servlet environments are supported for Bean Validation");
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
            this.validationGroupsArray = new Class<?>[] { Default.class };
        }
        else
        {
            String[] classes = this.validationGroups.split(VALIDATION_GROUPS_DELIMITER);
            List<Class<?>> validationGroupsList = new ArrayList<Class<?>>(classes.length);

            for (String clazz : classes)
            {
                clazz = clazz.trim();
                if (!clazz.equals(""))
                {
                    try
                    {
                        Class<?> theClass = Class.forName(clazz);
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
    public Object saveState(FacesContext context)
    {
        return this.validationGroups;
    }

    /** {@inheritDoc} */
    public void restoreState(FacesContext context, Object state)
    {
        this.validationGroups = (String) state;

        // Only the String is saved, recalculate the Class[] on state restoration.
        postSetValidationGroups();
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
    public void setValidationGroups(String validationGroups)
    {
        this.validationGroups = validationGroups;
        postSetValidationGroups();
    }

    @JSFProperty
    private Boolean isDisabled()
    {
        return null;
    }
    
    @JSFProperty
    private String getFor()
    {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isTransient()
    {
        return isTransient;
    }

    /** {@inheritDoc} */
    public void setTransient(boolean isTransient)
    {
        this.isTransient = isTransient;
    }

    /** {@inheritDoc} */
    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    /** {@inheritDoc} */
    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
    }
}

/**
 * Holder class to prevent NoClassDefFoundError in environments without Bean Validation.
 *
 * This is needed, because holder classes are loaded lazily. This means that when it's not
 * used, it will not be loaded, parsed and initialized. The BeanValidator class is used always,
 * so the MessageInterpolator references need to be in this separate class.
 */
class FacesMessageInterpolatorHolder
{
    // Needs to be volatile.
    private static volatile FacesMessageInterpolator instance;

    /**
     * Helper method for initializing the FacesMessageInterpolator.
     *
     * It uses the "Single Check Idiom" as described in Joshua Bloch's Effective Java 2nd Edition.
     *
     * @param validatorFactory Used to obtain the MessageInterpolator.
     */
    static MessageInterpolator get(ValidatorFactory validatorFactory)
    {
        FacesMessageInterpolatorHolder.FacesMessageInterpolator ret = instance;
        if (ret == null)
        {
            MessageInterpolator interpolator = validatorFactory.getMessageInterpolator();
            instance = ret = new FacesMessageInterpolator(interpolator);
        }
        return ret;
    }

    /**
     * Standard MessageInterpolator, as described in the JSR-314 spec.
     */
    private static class FacesMessageInterpolator implements MessageInterpolator
    {
        private final MessageInterpolator interpolator;

        FacesMessageInterpolator(MessageInterpolator interpolator)
        {
            this.interpolator = interpolator;
        }

        @Override
        public String interpolate(String s, Context context)
        {
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            return interpolator.interpolate(s, context, locale);
        }

        @Override
        public String interpolate(String s, Context context, Locale locale)
        {
            return interpolator.interpolate(s, context, locale);
        }
    }
}

/**
 * This class provides access to the object pointed to by the EL expression.
 *
 * It makes the BeanValidator work when Unified EL is not available.
 */
class ValueReferenceWrapper
{
    private final Object base, prop;

    public ValueReferenceWrapper(Object base, Object prop)
    {
        this.base = base;
        this.prop = prop;
    }

    public Object getBase()
    {
        return base;
    }

    public Object getProp()
    {
        return prop;
    }
}

/**
 * This class inspects the EL expression and returns a ValueReferenceWrapper
 * when Unified EL is not available.
 */
class ValueReferenceResolver extends ELResolver
{
    private final ELResolver resolver;

    /**
     * This is a simple solution to keep track of the resolved objects,
     * since ELResolver provides no way to know if the current ELResolver
     * is the last one in the chain. By assigning (and effectively overwriting)
     * this field, we know that the value after invoking the chain is always
     * the last one.
     *
     * This solution also deals with nested objects (like: #{myBean.prop.prop.prop}.
     */
    private ValueReferenceWrapper lastObject;

    /**
     * Constructor is only used internally.
     * @param elResolver
     */
    ValueReferenceResolver(ELResolver elResolver)
    {
        this.resolver = elResolver;
    }

    /**
     * This method can be used to extract the ValueReferenceWrapper from the given ValueExpression.
     *
     * @param valueExpression The ValueExpression to resolve.
     * @param elCtx The ELContext, needed to parse and execute the expression.
     * @return The ValueReferenceWrapper.
     */
    public static ValueReferenceWrapper resolve(ValueExpression valueExpression, ELContext elCtx)
    {
        ValueReferenceResolver resolver = new ValueReferenceResolver(elCtx.getELResolver());
        valueExpression.getValue(new MyELContext(elCtx, resolver));
        return resolver.lastObject;
    }

    /**
     * This method is the only one that matters. It keeps track of the objects in the EL expression.
     *
     * It creates a new ValueReferenceWrapper and assigns it to lastObject.
     *
     * @param context The ELContext.
     * @param base The base object, may be null.
     * @param property The property, may be null.
     * @return The resolved value
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        lastObject = new ValueReferenceWrapper(base, property);
        return resolver.getValue(context, base, property);
    }

    // ############################ Standard delegating implementations ############################
    public Class<?> getType(ELContext ctx, Object base, Object property){return resolver.getType(ctx, base, property);}
    public void setValue(ELContext ctx, Object base, Object property, Object value){resolver.setValue(ctx, base, property, value);}
    public boolean isReadOnly(ELContext ctx, Object base, Object property){return resolver.isReadOnly(ctx, base, property);}
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext ctx, Object base){return resolver.getFeatureDescriptors(ctx, base);}
    public Class<?> getCommonPropertyType(ELContext ctx, Object base){return resolver.getCommonPropertyType(ctx, base);}

}

/**
 * This ELContext is used to hook into the EL handling, by decorating the
 * ELResolver chain with a custom ELResolver.
 */
class MyELContext extends ELContext
{
    private final ELContext ctx;
    private final ELResolver interceptingResolver;

    /**
     * Only used by ValueExpressionResolver.
     *
     * @param elContext The standard ELContext. All method calls, except getELResolver, are delegated to it.
     * @param interceptingResolver The ELResolver to be returned by getELResolver.
     */
    MyELContext(ELContext elContext, ELResolver interceptingResolver)
    {
        this.ctx = elContext;
        this.interceptingResolver = interceptingResolver;
    }

    /**
     * This is the important one, it returns the passed ELResolver.
     * @return The ELResolver passed into the constructor.
     */
    @Override
    public ELResolver getELResolver()
    {
        return interceptingResolver;
    }

    // ############################ Standard delegating implementations ############################
    public FunctionMapper getFunctionMapper(){return ctx.getFunctionMapper();}
    public VariableMapper getVariableMapper(){return ctx.getVariableMapper();}
    public void setPropertyResolved(boolean resolved){ctx.setPropertyResolved(resolved);}
    public boolean isPropertyResolved(){return ctx.isPropertyResolved();}
    public void putContext(Class key, Object contextObject){ctx.putContext(key, contextObject);}
    public Object getContext(Class key){return ctx.getContext(key);}
    public Locale getLocale(){return ctx.getLocale();}
    public void setLocale(Locale locale){ctx.setLocale(locale);}
}
