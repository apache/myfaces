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

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.CompositeComponentExpressionHolder;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFValidator;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

/**
 * <p>
 * <strong>BeanValidator</strong> is a {@link javax.faces.validator.Validator}
 * that doesn't do any validation itself, but delegates validation logic to
 * Bean Validation.
 * </p>
 *
 * @author Jan-Kees van Andel (latest modification by $Author$)
 * @version $Revision$ $Date$
 * 
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

//    private static final Logger log = Logger.getLogger(BeanValidator.class.getName());

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
    @JSFWebConfigParam(defaultValue="true", expectedValues="true, false", since="2.0")
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
    public void validate(final FacesContext context, final UIComponent component, final Object value) throws ValidatorException
    {
        if (context == null) throw new NullPointerException("context");
        if (component == null) throw new NullPointerException("component");

        // Obtain a reference to the to-be-validated object and the property name.
        final ValueReferenceWrapper reference = getValueReference(component, context);
        if (reference == null)
        {
            return;
        }
        final Object base = reference.getBase();
        if (base == null)
        {
            return;
        }
        final Class<?> valueBaseClass = base.getClass();
        final String valueProperty = (String) reference.getProperty();
        if (valueBaseClass == null || valueProperty == null)
        {
            return;
        }

        // Initialize Bean Validation.
        final ValidatorFactory validatorFactory = createValidatorFactory(context);
        final javax.validation.Validator validator = createValidator(validatorFactory);
        final BeanDescriptor beanDescriptor = validator.getConstraintsForClass(valueBaseClass);
        if (!beanDescriptor.isBeanConstrained())
        {
            return;
        }
        
        // Note that validationGroupsArray was initialized when createValidator was called
        final Class[] validationGroupsArray = this.validationGroupsArray;

        // Delegate to Bean Validation.
        final Set constraintViolations = validator.validateValue(valueBaseClass, valueProperty, value, validationGroupsArray);
        if (!constraintViolations.isEmpty())
        {
            final Set<FacesMessage> messages = new LinkedHashSet<FacesMessage>(constraintViolations.size());
            for (Object violation: constraintViolations)
            {
                final ConstraintViolation constraintViolation = (ConstraintViolation) violation;
                final String message = constraintViolation.getMessage();
                final String[] args = new String[]{ message, _MessageUtils.getLabel(context, component) };
                final FacesMessage msg = _MessageUtils.getErrorMessage(context, MESSAGE_ID, args);
                messages.add(msg);
            }
            throw new ValidatorException(messages);
        }
    }

    private javax.validation.Validator createValidator(final ValidatorFactory validatorFactory)
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

    private ValueReferenceWrapper getValueReference(final UIComponent component, final FacesContext context)
    {
        final ValueExpression valueExpression = component.getValueExpression("value");
        final ELContext elCtx = context.getELContext();
        if (_ExternalSpecifications.isUnifiedELAvailable())
        {
            // unified el 2.2 is available --> we can use ValueExpression.getValueReference()
            
            // TODO handle wrapped ValueExpressions
            
            // we can't access ValueExpression.getValueReference() directly here, because
            // Class loading would fail in applications with el-api versions prior to 2.2
            ValueReferenceWrapper valueReference 
                    = _BeanValidatorUELUtils.getUELValueReferenceWrapper(valueExpression, elCtx);
            if (valueReference != null)
            {
                return valueReference;
            }
        }
        
        // get base object and property name the "old-fashioned" way
        return ValueReferenceResolver.resolve(valueExpression, elCtx);
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
        final Object ctx = context.getExternalContext().getContext();
        if (ctx instanceof ServletContext)
        {
            final ServletContext servletCtx = (ServletContext) ctx;
            final Object attr = servletCtx.getAttribute(VALIDATOR_FACTORY_KEY);
            if (attr != null)
            {
                return (ValidatorFactory) attr;
            }
            else
            {
                if (_ExternalSpecifications.isBeanValidationAvailable())
                {
                    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
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
            final String[] classes = this.validationGroups.split(VALIDATION_GROUPS_DELIMITER);
            final List<Class<?>> validationGroupsList = new ArrayList<Class<?>>(classes.length);

            for (String clazz : classes)
            {
                clazz = clazz.trim();
                if (!clazz.equals(""))
                {
                    try
                    {
                        final Class<?> theClass = Class.forName(clazz);
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
    public Object saveState(final FacesContext context)
    {
        return this.validationGroups;
    }

    /** {@inheritDoc} */
    public void restoreState(final FacesContext context, final Object state)
    {
        this.validationGroups = (String) state;

        // Only the String is saved, recalculate the Class[] on state restoration.
        //postSetValidationGroups();
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
        //postSetValidationGroups();
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
    public void setTransient(final boolean isTransient)
    {
        this.isTransient = isTransient;
    }

    /** {@inheritDoc} */
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    /** {@inheritDoc} */
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    /** {@inheritDoc} */
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
final class FacesMessageInterpolatorHolder
{
    // Needs to be volatile.
    private static volatile FacesMessageInterpolator instance;

    /**
     * Helper method for initializing the FacesMessageInterpolator.
     *
     * It uses the "Single Check Idiom" as described in Joshua Bloch's Effective Java 2nd Edition.
     *
     * @param validatorFactory Used to obtain the MessageInterpolator.
     * @return The instantiated MessageInterpolator for BeanValidator.
     */
    static MessageInterpolator get(final ValidatorFactory validatorFactory)
    {
        FacesMessageInterpolatorHolder.FacesMessageInterpolator ret = instance;
        if (ret == null)
        {
            final MessageInterpolator interpolator = validatorFactory.getMessageInterpolator();
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

        FacesMessageInterpolator(final MessageInterpolator interpolator)
        {
            this.interpolator = interpolator;
        }

        public String interpolate(final String s, final Context context)
        {
            final Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            return interpolator.interpolate(s, context, locale);
        }

        public String interpolate(final String s, final Context context, final Locale locale)
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
final class ValueReferenceWrapper
{
    private final Object base, property;

    /**
     * Full constructor.
     *
     * @param base The object the reference points to.
     * @param property The property the reference points to.
     */
    public ValueReferenceWrapper(final Object base, final Object property)
    {
        this.base = base;
        this.property = property;
    }

    /**
     * The object the reference points to.
     * @return base.
     */
    public final Object getBase()
    {
        return base;
    }

    /**
     * The property the reference points to.
     * @return property.
     */
    public final Object getProperty()
    {
        return property;
    }
}

/**
 * This class inspects the EL expression and returns a ValueReferenceWrapper
 * when Unified EL is not available.
 */
final class ValueReferenceResolver extends ELResolver
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
     * @param elResolver An ELResolver from the current ELContext.
     */
    ValueReferenceResolver(final ELResolver elResolver)
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
    public static ValueReferenceWrapper resolve(ValueExpression valueExpression, final ELContext elCtx)
    {
        final ValueReferenceResolver resolver = new ValueReferenceResolver(elCtx.getELResolver());
        final ELContext elCtxDecorator = new ELContextDecorator(elCtx, resolver);
        
        valueExpression.getValue(elCtxDecorator);
        
        while (resolver.lastObject.getBase() instanceof CompositeComponentExpressionHolder)
        {
            valueExpression = ((CompositeComponentExpressionHolder) resolver.lastObject.getBase())
                                  .getExpression((String) resolver.lastObject.getProperty());
            valueExpression.getValue(elCtxDecorator);
        }

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
    public Object getValue(final ELContext context, final Object base, final Object property)
    {
        lastObject = new ValueReferenceWrapper(base, property);
        return resolver.getValue(context, base, property);
    }

    // ############################ Standard delegating implementations ############################
    public final Class<?> getType(final ELContext ctx, final Object base, final Object property){return resolver.getType(ctx, base, property);}
    public final void setValue(final ELContext ctx, final Object base, final Object property, final Object value){resolver.setValue(ctx, base, property, value);}
    public final boolean isReadOnly(final ELContext ctx, final Object base, final Object property){return resolver.isReadOnly(ctx, base, property);}
    public final Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext ctx, final Object base){return resolver.getFeatureDescriptors(ctx, base);}
    public final Class<?> getCommonPropertyType(final ELContext ctx, final Object base){return resolver.getCommonPropertyType(ctx, base);}

}

/**
 * This ELContext is used to hook into the EL handling, by decorating the
 * ELResolver chain with a custom ELResolver.
 */
final class ELContextDecorator extends ELContext
{
    private final ELContext ctx;
    private final ELResolver interceptingResolver;

    /**
     * Only used by ValueExpressionResolver.
     *
     * @param elContext The standard ELContext. All method calls, except getELResolver, are delegated to it.
     * @param interceptingResolver The ELResolver to be returned by getELResolver.
     */
    ELContextDecorator(final ELContext elContext, final ELResolver interceptingResolver)
    {
        this.ctx = elContext;
        this.interceptingResolver = interceptingResolver;
    }

    /**
     * This is the important one, it returns the passed ELResolver.
     * @return The ELResolver passed into the constructor.
     */
    @Override
    public final ELResolver getELResolver()
    {
        return interceptingResolver;
    }

    // ############################ Standard delegating implementations ############################
    public final FunctionMapper getFunctionMapper(){return ctx.getFunctionMapper();}
    public final VariableMapper getVariableMapper(){return ctx.getVariableMapper();}
    public final void setPropertyResolved(final boolean resolved){ctx.setPropertyResolved(resolved);}
    public final boolean isPropertyResolved(){return ctx.isPropertyResolved();}
    public final void putContext(final Class key, Object contextObject){ctx.putContext(key, contextObject);}
    public final Object getContext(final Class key){return ctx.getContext(key);}
    public final Locale getLocale(){return ctx.getLocale();}
    public final void setLocale(final Locale locale){ctx.setLocale(locale);}
}
