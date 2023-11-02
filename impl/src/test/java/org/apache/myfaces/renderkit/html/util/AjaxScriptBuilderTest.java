package org.apache.myfaces.renderkit.html.util;

import org.apache.myfaces.component.search.*;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockApplication;
import org.junit.Assert;
import org.junit.Test;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponentMock;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AjaxScriptBuilderTest extends AbstractJsfTestCase
{
    @Override
    public void setUp() throws Exception {
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
                params);

        String script = sb.toString();

        Assert.assertFalse(script.contains("':testJsparams:{"));
        Assert.assertTrue(script.contains("':testJs,'params':{"));
    }
}
