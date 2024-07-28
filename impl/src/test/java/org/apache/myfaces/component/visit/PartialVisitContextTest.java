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
package org.apache.myfaces.component.visit;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.test.base.junit.AbstractFacesConfigurableMockTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PartialVisitContextTest extends AbstractFacesConfigurableMockTestCase {

    @Test
    public void test() {

        PartialVisitContext visitContext = new PartialVisitContext(facesContext,
                Arrays.asList("form", "form:input", "form"),
                EnumSet.of(VisitHint.EXECUTE_LIFECYCLE, VisitHint.SKIP_UNRENDERED));
        
        UIViewRoot viewRoot = new UIViewRoot();
        
        HtmlForm form = new HtmlForm();
        form.setId("form");
        form.setSubmitted(true);
        viewRoot.getChildren().add(form);

        AtomicBoolean processValidatorsOfInputInvoked = new AtomicBoolean(false);

        HtmlInputText inputText = new HtmlInputText()
        {
            @Override
            public void processValidators(FacesContext context)
            {
                processValidatorsOfInputInvoked.set(true);
            }
        };
        inputText.setId("input");
        form.getChildren().add(inputText);
        
        List<UIComponent> visitedComponents = new ArrayList<>();
        viewRoot.visitTree(visitContext, new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                visitedComponents.add(target);

                target.processValidators(facesContext);

                // Same as PhaseAwareVisitCallback:
                // Return VisitResult.REJECT as processDecodes/Validators/Updates already traverse sub tree
                return VisitResult.REJECT;
            }
        });

        // only the the form is visited and only once, it will itself visit the child input
        Assertions.assertEquals(1, visitedComponents.size());
        Assertions.assertTrue(visitedComponents.contains(form));

        Assertions.assertTrue(processValidatorsOfInputInvoked.get());
    }
}
