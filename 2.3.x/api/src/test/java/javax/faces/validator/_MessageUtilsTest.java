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

import static org.easymock.EasyMock.expect;

import java.util.Locale;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockFacesContext12;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Test;

public class _MessageUtilsTest
{

    /**
     * Test method for
     * {@link javax.faces.validator._MessageUtils#getErrorMessage(javax.faces.context.FacesContext, java.lang.String, java.lang.Object[])}.
     */
    @Test
    public void testErrorMessage()
    {
        UIViewRoot root = new UIViewRoot();
        MockFacesContext12 facesContext = new MockFacesContext12();
        IMocksControl mocksControl = EasyMock.createControl();
        Application application = mocksControl.createMock(Application.class);
        ViewHandler viewHandler = mocksControl.createMock(ViewHandler.class);
        ELContext elContext = mocksControl.createMock(ELContext.class);
        ExpressionFactory expressionFactory = mocksControl.createMock(ExpressionFactory.class);
        ValueExpression valueExpression = mocksControl.createMock(ValueExpression.class);
        facesContext.setApplication(application);
        facesContext.setViewRoot(root);
        facesContext.setELContext(elContext);
        
        expect(application.getViewHandler()).andReturn(viewHandler);
        expect(viewHandler.calculateLocale(facesContext)).andReturn(Locale.ENGLISH);
        expect(application.getMessageBundle()).andReturn("javax.faces.Messages");
        expect(application.getExpressionFactory()).andReturn(expressionFactory);
        String s = "xxx: Validation Error: Value is greater than allowable maximum of ''xyz''";
        expect(expressionFactory.createValueExpression(elContext,s,String.class)).andReturn(valueExpression);
        expect(valueExpression.getValue(elContext)).andReturn(s);
        mocksControl.replay();

        Assert.assertEquals(_MessageUtils.getErrorMessage(facesContext, "javax.faces.validator.DoubleRangeValidator.MAXIMUM",
                new Object[] { "xyz", "xxx" }).getDetail(),
                "xxx: Validation Error: Value is greater than allowable maximum of 'xyz'");
    }

}
