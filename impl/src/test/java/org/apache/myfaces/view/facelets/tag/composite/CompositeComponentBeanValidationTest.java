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
package org.apache.myfaces.view.facelets.tag.composite;

import java.io.IOException;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.el.CompositeComponentExpressionHolder;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.el.resolver.CompositeComponentELResolver;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for using BeanValidation in conjunction with Composite Components.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeComponentBeanValidationTest extends AbstractFaceletTestCase
{

    /**
     * Tests the case that a composite component includes an editableValueHolder
     * attribute which points to a property that is validated via BeanValidation.
     * In this case the BeanValidator would get the ValueExpression #{cc.attrs.input}
     * which he does not need. He needs the actual ValueExpression, thus he has
     * to get it from the composite component. To accomplish this, he uses the
     * CompositeComponentExpressionHolder interface.
     * 
     * @throws IOException
     */
    @Test
    public void testCompositeComponentExpressionHolder() throws IOException
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleEditableValueHolder.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        UIComponent compositeComponent = form.getChildren().get(0);

        // "resolve" #{cc.attrs}
        CompositeComponentELResolver resolver = new CompositeComponentELResolver(MyfacesConfig.getCurrentInstance());
        Object attrs = resolver.getValue(facesContext.getELContext(), compositeComponent, "attrs");
        
        // the resolved value has to be a CompositeComponentExpressionHolder
        Assertions.assertTrue(attrs instanceof CompositeComponentExpressionHolder);
        
        // get the actual ValueExpression which is needed by the BeanValidator
        ValueExpression valueExpression 
                = ((CompositeComponentExpressionHolder) attrs).getExpression("input");
        
        // the expression String from the VE has to be #{myBean.input}
        Assertions.assertTrue("#{myBean.input}".equals(valueExpression.getExpressionString()));
    }
    
}
