/*
 * Copyright 2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.renderkit.html.util;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponentMock;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.component.search.*;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.mock.MockApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AjaxScriptBuilderTest extends AbstractFacesTestCase
{
    @BeforeEach
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        FactoryFinder.setFactory(FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY,
                SearchExpressionContextFactoryImpl.class.getName());

        MockApplication mockApplication = (MockApplication) FacesContext.getCurrentInstance().getApplication();
        mockApplication.setSearchExpressionHandler(new SearchExpressionHandlerImpl());

        CompositeSearchKeywordResolver baseResolver = new CompositeSearchKeywordResolver();
        baseResolver.add(new ThisSearchKeywordResolver());
        baseResolver.add(new ParentSearchKeywordResolver());
        baseResolver.add(new ChildSearchKeywordResolver());
        baseResolver.add(new CompositeComponentParentSearchKeywordResolver());
        baseResolver.add(new FormSearchKeywordResolver());
        baseResolver.add(new NamingContainerSearchKeywordResolver());
        baseResolver.add(new NextSearchKeywordResolver());
        baseResolver.add(new NoneSearchKeywordResolver());
        baseResolver.add(new PreviousSearchKeywordResolver());
        baseResolver.add(new RootSearchKeywordResolver());
        baseResolver.add(new IdSearchKeywordResolver());
        baseResolver.add(new AllSearchKeywordResolver());
        mockApplication.setSearchKeywordResolver(baseResolver);
    }

    @Test
    public void test()
    {
        UIComponentMock component = new UIComponentMock();
        component.setId("test");

        List<ClientBehaviorContext.Parameter> params = new ArrayList<>();
        params.add(new ClientBehaviorContext.Parameter("var1", "NEW VALUE"));

        StringBuilder sb = new StringBuilder();
        AjaxScriptBuilder.build(FacesContext.getCurrentInstance(),
                sb,
                component,
                "j_id_i",
                "click",
                Arrays.asList("@this"),
                null,
                null,
                false,
                null,
                "testJs",
                params,
                null);

        String script = sb.toString();

        Assertions.assertFalse(script.contains("':testJsparams:{"));
        Assertions.assertTrue(script.contains("':testJs,'params':{"));
        Assertions.assertEquals("myfaces.ab('j_id_i',event,'click','test','',{'onevent':testJs,'params':{'var1':'NEW VALUE'}})", script);
    }
}
