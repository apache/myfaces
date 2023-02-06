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
package jakarta.faces.component;

import jakarta.faces.component.UIViewParameter;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for UIViewParameter.
 * 
 * @since 2.0
 */
public class UIViewParameterTest extends AbstractJsfTestCase
{
    
    private UIViewParameter viewParameter = null;
    
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        viewParameter = new UIViewParameter();
        viewParameter.setName("param");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        viewParameter = null;
        
        super.tearDown();
    }

    /**
     * Tests if UIViewParameter.processValidators() correctly calls FacesContext.validationFailed()
     * if the submitted value is null, but required is set to true.
     * This is a special validation case only for UIViewParameter, so this has to be tested here.
     */
    @Test
    public void testValidationErrorTriggersFacesContextValidationFailed()
    {
        viewParameter.setRequired(true);
        viewParameter.setSubmittedValue(null);
        
        Assertions.assertFalse(facesContext.isValidationFailed());
        viewParameter.processValidators(facesContext);
        Assertions.assertTrue(facesContext.isValidationFailed());
    }
    
    /**
     * Tests if UIViewParameter.decode() sets the submitted value only if it is not null.
     */
    @Test
    public void testDecodeSetOnlyNonNullSubmittedValue()
    {
        String notNull = "not null";
        viewParameter.setSubmittedValue(notNull);
        
        // explicitly set the value in the request parameter map to null
        externalContext.addRequestParameterMap(viewParameter.getName(), null);
        
        viewParameter.decode(facesContext);
        
        Assertions.assertEquals(viewParameter.getSubmittedValue(), notNull);
    }

}
