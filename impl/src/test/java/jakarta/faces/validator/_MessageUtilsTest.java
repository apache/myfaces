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
import java.util.HashMap;
import static org.easymock.EasyMock.expect;

import java.util.Locale;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class _MessageUtilsTest
{

    /**
     * Test method for
     * {@link jakarta.faces.validator._MessageUtils#getErrorMessage(jakarta.faces.context.FacesContext, java.lang.String, java.lang.Object[])}.
     */
    @Test
    public void testErrorMessage()
    {
        UIViewRoot root = new UIViewRoot();
        MockFacesContext facesContext = new MockFacesContext();
        IMocksControl mocksControl = EasyMock.createControl();
        Application application = mocksControl.createMock(Application.class);
        ViewHandler viewHandler = mocksControl.createMock(ViewHandler.class);
        ELContext elContext = mocksControl.createMock(ELContext.class);
        ExternalContext externalContext = mocksControl.createMock(ExternalContext.class);
        ExpressionFactory expressionFactory = mocksControl.createMock(ExpressionFactory.class);
        ValueExpression valueExpression = mocksControl.createMock(ValueExpression.class);
        facesContext.setApplication(application);
        facesContext.setViewRoot(root);
        facesContext.setELContext(elContext);
        facesContext.setExternalContext(externalContext);
        
        expect(application.getViewHandler()).andReturn(viewHandler);
        expect(viewHandler.calculateLocale(facesContext)).andReturn(Locale.ENGLISH);
        expect(application.getMessageBundle()).andReturn("jakarta.faces.Messages");
        expect(application.getExpressionFactory()).andReturn(expressionFactory);
        expect(externalContext.getApplicationMap()).andReturn(new HashMap<>());
        String s = "xxx: Validation Error: Value is greater than allowable maximum of ''xyz''";
        expect(expressionFactory.createValueExpression(elContext,s,String.class)).andReturn(valueExpression);
        expect(valueExpression.getValue(elContext)).andReturn(s);
        mocksControl.replay();

        Assertions.assertEquals(MessageUtils.getErrorMessage(facesContext, "jakarta.faces.validator.DoubleRangeValidator.MAXIMUM",
                new Object[] { "xyz", "xxx" }).getDetail(),
                "xxx: Validation Error: Value is greater than allowable maximum of 'xyz'");
    }

}
