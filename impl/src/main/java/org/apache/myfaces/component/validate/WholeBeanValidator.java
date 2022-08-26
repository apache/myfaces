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

package org.apache.myfaces.component.validate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import static jakarta.faces.validator.BeanValidator.EMPTY_VALIDATION_GROUPS_PATTERN;
import static jakarta.faces.validator.BeanValidator.MESSAGE_ID;
import static jakarta.faces.validator.BeanValidator.VALIDATION_GROUPS_DELIMITER;
import static jakarta.faces.validator.BeanValidator.VALIDATOR_FACTORY_KEY;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.core.api.shared.ELContextDecorator;
import org.apache.myfaces.core.api.shared.FacesMessageInterpolator;
import org.apache.myfaces.core.api.shared.ValueReferenceResolver;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.util.MessageUtils;
import org.apache.myfaces.util.MyFacesObjectInputStream;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.util.lang.FastByteArrayOutputStream;

public class WholeBeanValidator implements Validator
{
    private static final Logger log = Logger.getLogger(WholeBeanValidator.class.getName());
    
    private static final Class<?>[] DEFAULT_VALIDATION_GROUPS_ARRAY = new Class<?>[] { Default.class };
    
    private static final String CANDIDATE_COMPONENT_VALUES_MAP = "oam.WBV.candidatesMap";
    
    private static final String BEAN_VALIDATION_FAILED = "oam.WBV.validationFailed";

    private Class<?>[] validationGroupsArray;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        Assert.notNull(context, "context");
        Assert.notNull(component, "component");

        ValueExpression valueExpression = component.getValueExpression("value");
        if (valueExpression == null)
        {
            log.warning("cannot validate component with empty value: " 
                    + component.getClientId(context));
            return;
        }

        Object base = valueExpression.getValue(context.getELContext());
                
        Class<?> valueBaseClass = base.getClass();
        if (valueBaseClass == null)
        {
            return;
        }

        // Initialize Bean Validation.
        ValidatorFactory validatorFactory = createValidatorFactory(context);
        jakarta.validation.Validator validator = createValidator(validatorFactory, context, 
                (ValidateWholeBeanComponent)component);
        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(valueBaseClass);
        if (!beanDescriptor.isBeanConstrained())
        {
            return;
        }
        
        // Note that validationGroupsArray was initialized when createValidator was called
        Class[] validationGroupsArray = this.validationGroupsArray;

        // Delegate to Bean Validation.
        
        // TODO: Use validator.validate(...) over the copy instance.
        
        Boolean beanValidationFailed = (Boolean) context.getViewRoot().getTransientStateHelper()
                .getTransient(BEAN_VALIDATION_FAILED);
        
        if (Boolean.TRUE.equals(beanValidationFailed))
        {
            // Faces 2.3 Skip class level validation
            return;
        }
        
        Map<String, Object> candidatesMap = (Map<String, Object>) context.getViewRoot()
                .getTransientStateHelper().getTransient(CANDIDATE_COMPONENT_VALUES_MAP);
        if (candidatesMap != null)
        {
            Object copy = createBeanCopy(base);
            
            UpdateBeanCopyCallback callback = new UpdateBeanCopyCallback(this, base, copy, candidatesMap);
            context.getViewRoot().visitTree(
                    VisitContext.createVisitContext(context, candidatesMap.keySet(), null), 
                    callback);
            
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(copy, validationGroupsArray);
            if (!constraintViolations.isEmpty())
            {
                Set<FacesMessage> messages = new LinkedHashSet<>(constraintViolations.size());
                for (ConstraintViolation constraintViolation : constraintViolations)
                {
                    String message = constraintViolation.getMessage();
                    Object[] args = new Object[]{ message, MessageUtils.getLabel(context, component) };
                    FacesMessage msg = MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR, MESSAGE_ID, args, context);
                    messages.add(msg);
                }
                throw new ValidatorException(messages);
            }
        }
    }
    
    private Object createBeanCopy(Object base)
    {
        Object copy = null;
        try
        {
            copy = base.getClass().newInstance();
        }
        catch (Exception ex)
        {
            log.log(Level.FINEST, null, ex);
        }
        
        if (base instanceof Serializable)
        {
            copy = copySerializableObject(base);
        }
        else if(base instanceof Cloneable)
        { 
            Method cloneMethod;
            try
            {
                cloneMethod = base.getClass().getMethod("clone");
                copy = cloneMethod.invoke(base);
            }
            catch (Exception ex) 
            {
                log.log(Level.FINEST, null, ex);
            }
        }
        else
        {
            Class<?> clazz = base.getClass();
            try
            {
                Constructor<?> copyConstructor = clazz.getConstructor(clazz);
                if (copyConstructor != null)
                {
                    copy = copyConstructor.newInstance(base);
                }
            }
            catch (Exception ex)
            {
                log.log(Level.FINEST, null, ex);
            }
        }
        
        if (copy == null)
        {
            throw new FacesException("Cannot create copy for wholeBeanValidator: "+base.getClass().getName());
        }
        
        return copy;
    }
    
    private Object copySerializableObject(Object base)
    {
        try 
        {
            FastByteArrayOutputStream baos = new FastByteArrayOutputStream(256);
            try (ObjectOutputStream oos = new ObjectOutputStream(baos))
            {
                oos.writeObject(base);
                oos.flush();
            }

            ObjectInputStream ois = new MyFacesObjectInputStream(baos.getInputStream());
            try 
            {
                return ois.readObject();
            }
            catch (ClassNotFoundException e)
            {
                //e.printStackTrace();
            }
        }
        catch (IOException e) 
        {
            //e.printStackTrace();
        }

        return null;
    }    
    
    private jakarta.validation.Validator createValidator(final ValidatorFactory validatorFactory, 
            FacesContext context, ValidateWholeBeanComponent component)
    {
        // Set default validation group when setValidationGroups has not been called.
        // The null check is there to prevent it from happening twice.
        if (validationGroupsArray == null)
        {
            postSetValidationGroups(component);
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
        if (attr instanceof ValidatorFactory)
        {
            return (ValidatorFactory) attr;
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
    private void postSetValidationGroups(ValidateWholeBeanComponent component)
    {
        String validationGroups = getValidationGroups(component);
        if (validationGroups == null || validationGroups.matches(EMPTY_VALIDATION_GROUPS_PATTERN))
        {
            this.validationGroupsArray = DEFAULT_VALIDATION_GROUPS_ARRAY;
        }
        else
        {
            String[] classes = validationGroups.split(VALIDATION_GROUPS_DELIMITER);
            List<Class<?>> validationGroupsList = new ArrayList<Class<?>>(classes.length);

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

    /**
     * Get the Bean Validation validation groups.
     * @return The validation groups String.
     */
    @JSFProperty
    public String getValidationGroups(ValidateWholeBeanComponent component)
    {
        return component.getValidationGroups();
    }

    /**
     * Set the Bean Validation validation groups.
     * @param validationGroups The validation groups String, separated by
     *                         {@link jakarta.faces.validator.BeanValidator#VALIDATION_GROUPS_DELIMITER}.
     */
    public void setValidationGroups(ValidateWholeBeanComponent component, final String validationGroups)
    {
        component.setValidationGroups(validationGroups);
    }

    private static class UpdateBeanCopyCallback implements VisitCallback
    {
        private WholeBeanValidator validator;
        private Object wholeBeanBase;
        private Object wholeBeanBaseCopy;
        private Map<String, Object> candidateValuesMap;

        public UpdateBeanCopyCallback(WholeBeanValidator validator, Object wholeBeanBase, Object wholeBeanBaseCopy,
                Map<String, Object> candidateValuesMap)
        {
            this.validator = validator;
            this.wholeBeanBase = wholeBeanBase;
            this.wholeBeanBaseCopy = wholeBeanBaseCopy;
            this.candidateValuesMap = candidateValuesMap;
        }

        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            // The idea is follow almost the same algorithm used by Bean Validation. This 
            // algorithm calculates the base of the ValueExpression used by the component.
            // Then a simple equals() check will do the trick to decide when to call
            // setValue and affect the model. If the base is the same than the value returned by
            // f:validateWholeBean, you are affecting to same instance.
            
            ValueExpression valueExpression = target.getValueExpression("value");
            if (valueExpression == null)
            {
                log.warning("cannot validate component with empty value: " 
                        + target.getClientId(context.getFacesContext()));
                return VisitResult.ACCEPT;
            }

            // Obtain a reference to the to-be-validated object and the property name.
            ValueReference reference = ValueReferenceResolver.resolve(
                    valueExpression, context.getFacesContext());
            if (reference == null)
            {
                return VisitResult.ACCEPT;
            }
            
            Object base = reference.getBase();
            if (base == null)
            {
                return VisitResult.ACCEPT;
            }

            Object referenceProperty = reference.getProperty();
            if (!(referenceProperty instanceof String))
            {
                // if the property is not a String, the ValueReference does not
                // point to a bean method, but e.g. to a value in a Map, thus we 
                // can exit bean validation here
                return VisitResult.ACCEPT;
            }
                        
            // If the base of the EL expression is the same to the base of the one in f:validateWholeBean
            if (base == this.wholeBeanBase || base.equals(this.wholeBeanBase))
            {
                // Do the trick over ELResolver and apply it to the copy.
                ELContext elCtxDecorator = new ELContextDecorator(context.getFacesContext().getELContext(),
                        new CopyBeanInterceptorELResolver(context.getFacesContext().getApplication().getELResolver(),
                            this.wholeBeanBase, this.wholeBeanBaseCopy));
                
                valueExpression.setValue(elCtxDecorator, candidateValuesMap.get(
                        target.getClientId(context.getFacesContext())));
            }
            
            return VisitResult.ACCEPT;
        }
    }
}