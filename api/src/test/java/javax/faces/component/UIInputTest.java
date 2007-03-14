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
package javax.faces.component;

import static org.easymock.EasyMock.*;

import javax.faces.application.FacesMessage;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import junit.framework.Test;

import org.apache.shale.test.base.AbstractJsfTestCase;

public class UIInputTest extends AbstractJsfTestCase{
	
	private Converter mockConverter;
	private Validator mockValidator;

	public UIInputTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mockConverter = createMock(Converter.class);
		mockValidator = createMock(Validator.class);
	}
	
    protected void tearDown() throws Exception {
        super.tearDown();
        mockConverter = null;
        mockValidator = null;
    }

	public static Test suite() {
		return null;
	}
	
	public void testWhenSpecifiedConverterMessageIsUsedInCaseConverterExceptionOccurs() {
		UIInput input = new UIInput();
		input.setId("testId");
		input.setConverterMessage("Cannot convert");
		
		input.setConverter(mockConverter);
		expect(mockConverter.getAsObject(facesContext, input, "xxx")).andThrow(new ConverterException());
		replay(mockConverter);
		
		input.getConvertedValue(facesContext, "xxx");
		verify(mockConverter);
		
		assertFalse(input.isValid());
		assertNotNull(facesContext.getMessages("testId"));
		
		FacesMessage message = (FacesMessage) facesContext.getMessages("testId").next();
		assertEquals(message.getDetail(), "Cannot convert");
		assertEquals(message.getSummary(), "Cannot convert");
	}
	
	public void testWhenSpecifiedValidatorMessageIsUsedInCaseValidatorExceptionOccurs() {
		UIInput input = new UIInput();
		input.setId("testId");
		input.setValidatorMessage("Cannot validate");
		
		input.addValidator(mockValidator);
		mockValidator.validate(facesContext, input, "xxx");
		expectLastCall().andThrow(new ValidatorException(new FacesMessage()));
		replay(mockValidator);
		
		input.validateValue(facesContext, "xxx");
		verify(mockValidator);
		
		assertFalse(input.isValid());
		assertNotNull(facesContext.getMessages("testId"));
		
		FacesMessage message = (FacesMessage) facesContext.getMessages("testId").next();
		assertEquals(message.getDetail(), "Cannot validate");
		assertEquals(message.getSummary(), "Cannot validate");
	}
}
