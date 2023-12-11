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
package org.apache.myfaces.view.facelets.tag.faces.core;

import java.io.IOException;
import java.lang.reflect.Field;

import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.Validator;

import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.util.lang.Lazy;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for <f:validateBean />.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ValidateBeanTestCase extends FaceletTestCase
{
    
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        // configure bean validation to be available
        _setBeanValidationAvailable(true);
    }

    /**
     * Sets the cached field in ExternalSpecifications to enable or 
     * disable bean validation for testing purposes.
     * 
     * @param available
     */
    private void _setBeanValidationAvailable(boolean available)
    {
        try
        {
            Field field = ExternalSpecifications.class.getDeclaredField("beanValidationAvailable");
            field.setAccessible(true);
            field.set(ExternalSpecifications.class, new Lazy<>(() ->
            {
                return available;
            }));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not configure BeanValidation for the test case.", e);
        }
    }
    
    /**
     * Gets the validator from the given UIInput with the given validatorClass
     * or null if the UIInput does not have such a validator installed.
     * @param <T>
     * @param input
     * @param validatorClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T _getValidator(UIInput input, Class<T> validatorClass)
    {
        Validator[] validators = input.getValidators();
        if (validators != null)
        {
            for (Validator validator : validators)
            {
                if (validatorClass.isAssignableFrom(validator.getClass()))
                {
                    return (T) validator;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Tests if the given UIInput has a validator of the given validatorClass installed.
     * @param input
     * @param validatorClass
     * @return
     */
    private boolean _hasValidator(UIInput input, Class<?> validatorClass)
    {
        return (_getValidator(input, validatorClass) != null);
    }
    
    /**
     * Gets the attached BeanValidator from the given UIInput and returns its
     * validationGroups. Returns null if no BeanValidator is attached.
     * @param input
     * @return
     */
    private String _getValidationGroups(UIInput input)
    {
        BeanValidator validator = _getValidator(input, BeanValidator.class);
        if (validator != null)
        {
            return validator.getValidationGroups();
        }
        return null;
    }

    /**
     * Tests the case that the BeanValidator is not a default validator,
     * but the UIInput has a <f:validateBean /> child tag on the facelet.
     * In this case the BeanValidator has to be installed.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testBeanValidatorInstalledManually() throws IOException
    {
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.FALSE);
        
        // build testValidateBean.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBean.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the UIInput has to have the BeanValidator installed
        Assertions.assertTrue(_hasValidator(input, BeanValidator.class));
    }
    
    /**
     * Tests the case that the UIInput has no nested <f:validateBean>
     * on the facelet, but the BeanValidator is a default validator.
     * In this case the BeanValidator has to be installed.
     * @throws IOException
     */
    @Test
    public void testBeanValidatorInstalledAutomatically() throws IOException
    {
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // build testValidateBeanEmptyInput.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanEmptyInput.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the UIInput has to have the BeanValidator installed
        Assertions.assertTrue(_hasValidator(input, BeanValidator.class));
    }
    
    /**
     * Tests the case that the BeanValidator is a default validator, but the
     * UIInput has a nested <f:validateBean disabled="true" /> on the facelet.
     * In this case the BeanValidator must not be installed.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDisabledBeanValidatorNotInstalled() throws IOException
    {
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.TRUE);
        
        // build testValidateBean.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBean.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the UIInput must not have the BeanValidator installed
        Assertions.assertFalse(_hasValidator(input, BeanValidator.class));
    }
    
    /**
     * Tests the case when <f:validateBean> is used in wrapping mode
     * and the BeanValidator is not a default-validator.
     * In this case the BeanValidator has to be installed on all
     * EditableValueHolders which he is nesting and must not be installed
     * on any other EditableValueHolder on the facelet.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testNestingValidateBean() throws IOException
    {
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.FALSE);
        
        // build testValidateBeanNesting.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanNesting.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput doublenestedinput = (UIInput) root.findComponent("form:doublenestedinput");
        UIInput nestedinput2 = (UIInput) root.findComponent("form:nestedinput2");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // all wrapped UIInputs have to have the BeanValidator installed
        Assertions.assertTrue(_hasValidator(nestedinput, BeanValidator.class));
        Assertions.assertTrue(_hasValidator(doublenestedinput, BeanValidator.class));
        Assertions.assertTrue(_hasValidator(nestedinput2, BeanValidator.class));
        Assertions.assertFalse(_hasValidator(nonnestedinput, BeanValidator.class));
    }
    
    /**
     * Tests the case when <f:validateBean> is used in wrapping mode,
     * its disabled attribute is true and the BeanValidator is a default-validator.
     * In this case the BeanValidator has to be installed on all
     * EditableValueHolders which are not nested and must not be installed
     * on all nested EditableValueHolders on the facelet.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testNestingValidateBeanDisabled() throws IOException
    {
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.TRUE);
        
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // build testValidateBeanNesting.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanNesting.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput doublenestedinput = (UIInput) root.findComponent("form:doublenestedinput");
        UIInput nestedinput2 = (UIInput) root.findComponent("form:nestedinput2");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // all wrapped UIInputs have to have the BeanValidator installed
        Assertions.assertFalse(_hasValidator(nestedinput, BeanValidator.class));
        Assertions.assertFalse(_hasValidator(doublenestedinput, BeanValidator.class));
        Assertions.assertFalse(_hasValidator(nestedinput2, BeanValidator.class));
        Assertions.assertTrue(_hasValidator(nonnestedinput, BeanValidator.class));
    }
    
    /**
     * Tests the case that there are two <f:validateBean> tags, one disabled and
     * one not. The disabled one should add the validator id of the BeanValidator
     * to an default-validator exclusion list on the parent (<h:inputText>) 
     * whereas the enabled one should manually add the BeanValidator no matter what.
     * In this case the BeanValidator has to be installed (see also MYFACES-2731).
     * @throws IOException
     */
    @Test
    public void testValidateBeanDisabledAndEnabled() throws IOException
    {
        // build testValidateBeanDisabledAndEnabled.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanDisabledAndEnabled.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the UIInput has to have the BeanValidator installed
        Assertions.assertTrue(_hasValidator(input, BeanValidator.class));
    }
    
    /**
     * Tests the case that there are a wrapping f:validateBean with disabled
     * set to true and one nested f:validateBean. Also the BeanValidator is
     * a default-validator.
     * In this case all EditableValueHolders outside the wrapping f:validateBean
     * and the one EditableValueHolder which is wrapped, but has a f:validateBean
     * itself should have the BeanValidator installed.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanNestingAndNested() throws IOException
    {
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.TRUE);
        
        // build testValidateBeanNestingAndNested.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanNestingAndNested.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput nestedinputWithValidator = (UIInput) root.findComponent("form:nestedinputWithValidator");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // no wrapped UIInput has to have the BeanValidator installed,
        // except the one nesting <f:validateBean /> itself.
        Assertions.assertFalse(_hasValidator(nestedinput, BeanValidator.class));
        Assertions.assertTrue(_hasValidator(nestedinputWithValidator, BeanValidator.class));
        Assertions.assertTrue(_hasValidator(nonnestedinput, BeanValidator.class));
    }
    
    /**
     * Tests if the validationGroups are set correctly.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanValidationGroups() throws IOException
    {
        final String validationGroups = """
                org.apache.myfaces.beanvalidation.Group1,\
                org.apache.myfaces.beanvalidation.Group2\
                """;
        
        // put the validationGroups on the request scope
        externalContext.getRequestMap().put("validationGroups", validationGroups);
        
        // build testValidateBean.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBean.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the validationGroups have to match
        Assertions.assertEquals(validationGroups, _getValidationGroups(input));
    }
    
    /**
     * Tests the case that the BeanValidator is a default validator and the
     * UIInput on the facelet does not have a custom <f:validateBean> tag.
     * In this case the Default validation group has to be set on the BeanValidator.
     * @throws IOException
     */
    @Test
    public void testValidateBeanDefaultValidationGroup() throws IOException
    {
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // build testValidateBeanEmptyInput.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanEmptyInput.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the validationGroups have to match the Default ones
        Assertions.assertEquals(jakarta.validation.groups.Default.class.getName(),
                _getValidationGroups(input));
    }
    
    /**
     * Tests the case that there is a wrapping <f:validateBean> on the facelet
     * with some validationGroups. These validationGroups should be applied to
     * all automatically added BeanValidators of all nested UIInput components.
     * The UIInput outside of the wrapping <f:validateBean> should get the 
     * Default validation group.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanValidationGroupsNested() throws IOException
    {
        final String validationGroups = "org.apache.myfaces.beanvalidation.Group1";
        
        // put the validationGroups on the request scope
        externalContext.getRequestMap().put("validationGroups", validationGroups);
        
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // build testValidateBeanNesting.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanNesting.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput doublenestedinput = (UIInput) root.findComponent("form:doublenestedinput");
        UIInput nestedinput2 = (UIInput) root.findComponent("form:nestedinput2");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // the validationGroups in the wrapped components have to match
        // org.apache.myfaces.beanvalidation.Group1 and the non-nested ones
        // have to match the Default group.
        Assertions.assertEquals(validationGroups, _getValidationGroups(nestedinput));
        Assertions.assertEquals(validationGroups, _getValidationGroups(doublenestedinput));
        Assertions.assertEquals(validationGroups, _getValidationGroups(nestedinput2));
        Assertions.assertEquals(jakarta.validation.groups.Default.class.getName(),
                _getValidationGroups(nonnestedinput));
    }
    
    /**
     * Tests the case that there is a wrapping <f:validateBean> with some validationGroups
     * and one nested component has its own <f:validateBean> with different validationGroups.
     * In this case the nested component without a validator must get the wrapping validationGroups,
     * the nested component with the validator must get the validationGroups from the 
     * validator and the non-nested component must get the Default validationGroups.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanValidationGroupsNestingAndNested() throws IOException
    {
        final String wrappingValidationGroups = "org.apache.myfaces.beanvalidation.Group1";
        final String componentValidationGroups = "org.apache.myfaces.beanvalidation.ACompletelyOtherGroup";
        
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // put the disabled value on the request scope
        externalContext.getRequestMap().put("validateBeanDisabled", Boolean.FALSE);
        
        // put the validationGroups on the request scope
        externalContext.getRequestMap().put("validationGroups", wrappingValidationGroups);
        externalContext.getRequestMap().put("validationGroupsComponent", componentValidationGroups);
        
        // build testValidateBeanNestingAndNested.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanNestingAndNested.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput nestedinputWithValidator = (UIInput) root.findComponent("form:nestedinputWithValidator");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // the nested component without a validator must get the wrapping validationGroups,
        // the nested component with the validator must get the validationGroups from the 
        // validator and the non-nested component must get the Default validationGroups.
        Assertions.assertEquals(wrappingValidationGroups, _getValidationGroups(nestedinput));
        Assertions.assertEquals(componentValidationGroups, _getValidationGroups(nestedinputWithValidator));
        Assertions.assertEquals(jakarta.validation.groups.Default.class.getName(),
                _getValidationGroups(nonnestedinput));
    }
    
    /**
     * Tests the case that there are two wrapping <f:validateBean> with different
     * validationGroups.
     * In this case the nested component must get the outer wrapping validationGroups,
     * the double-nested component must get the inner wrapping validationGroups
     * and the non-nested component must get the Default validationGroups.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanValidationGroupsDoubleNesting() throws IOException
    {
        final String validationGroupsOuter = "org.apache.myfaces.beanvalidation.OuterGroup";
        final String validationGroupsInner = "org.apache.myfaces.beanvalidation.InnerGroup";
        
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // put the validationGroups on the request scope
        externalContext.getRequestMap().put("validationGroupsOuter", validationGroupsOuter);
        externalContext.getRequestMap().put("validationGroupsInner", validationGroupsInner);
        
        // build testValidateBeanDoubleNesting.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanDoubleNesting.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput doublenestedinput = (UIInput) root.findComponent("form:doublenestedinput");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        
        // the nested component must get the outer wrapping validationGroups,
        // the double-nested component must get the inner wrapping validationGroups
        // and the non-nested component must get the Default validationGroups.
        Assertions.assertEquals(validationGroupsOuter, _getValidationGroups(nestedinput));
        Assertions.assertEquals(validationGroupsInner, _getValidationGroups(doublenestedinput));
        Assertions.assertEquals(jakarta.validation.groups.Default.class.getName(),
                _getValidationGroups(nonnestedinput));
    }
    
    /**
     * Tests the case that the BeanValidator is installed as a default-validator,
     * but bean validation is not available in the classpath.
     * In this case the BeanValidator must not be installed. However MyFaces
     * provides a log message for this scenario.
     * @throws IOException
     */
    @Test
    public void testValidateBeanWithBeanValidationNotAvailable() throws IOException
    {
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // configure bean validation to be not available
        _setBeanValidationAvailable(false);
        
        // build testValidateBeanEmptyInput.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanEmptyInput.xhtml");
        
        // get the component instances
        UIInput input = (UIInput) root.findComponent("form:input");
        
        // the UIInput must not have the BeanValidator installed,
        // because bean validation is not available
        Assertions.assertFalse(_hasValidator(input, BeanValidator.class));
    }

    /**
     * 
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateBeanValidationDisabledDoubleNesting() throws IOException
    {
        final String validationGroupsOuter = "org.apache.myfaces.beanvalidation.OuterGroup";
        final String validationGroupsInner = "org.apache.myfaces.beanvalidation.InnerGroup";
        
        // add the BeanValidator as default-validator
        application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
        
        // put the validationGroups on the request scope
        externalContext.getRequestMap().put("validationGroupsOuter", validationGroupsOuter);
        externalContext.getRequestMap().put("validationGroupsInner", validationGroupsInner);
        
        // build testValidateBeanDoubleNesting.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testValidateBeanDisableDoubleNesting.xhtml");
        
        // get the component instances
        UIInput nestedinput = (UIInput) root.findComponent("form:nestedinput");
        UIInput doublenestedinput = (UIInput) root.findComponent("form:doublenestedinput");
        UIInput nonnestedinput = (UIInput) root.findComponent("form:nonnestedinput");
        UIInput nesteouterdisabledinput = (UIInput) root.findComponent("form:nesteouterdisabledinput");
        
        // the nested component must get the outer wrapping validationGroups,
        // the double-nested component must get the inner wrapping validationGroups
        // and the non-nested component must get the Default validationGroups.
        Assertions.assertEquals(validationGroupsOuter, _getValidationGroups(nestedinput));
        Assertions.assertFalse(_hasValidator(doublenestedinput, BeanValidator.class));
        Assertions.assertEquals(validationGroupsInner, _getValidationGroups(nesteouterdisabledinput));
        Assertions.assertEquals(jakarta.validation.groups.Default.class.getName(),
                _getValidationGroups(nonnestedinput));
    }

}
