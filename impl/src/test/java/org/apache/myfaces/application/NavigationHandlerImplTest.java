package org.apache.myfaces.application;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.NavigationCase;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockApplication;
import org.xml.sax.SAXException;

public class NavigationHandlerImplTest extends AbstractJsfTestCase
{

    private DigesterFacesConfigUnmarshallerImpl _digesterFacesConfigUnmarshaller;

    public NavigationHandlerImplTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        //Set myfaces application instance instead mock
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                "org.apache.myfaces.application.ApplicationFactoryImpl");
        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder
                .getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (MockApplication) applicationFactory.getApplication();
        facesContext.setApplication(application);
        FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
                "org.apache.myfaces.context.PartialViewContextFactoryImpl");
        FactoryFinder.setFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY,
                "org.apache.myfaces.context.ExceptionHandlerFactoryImpl");

        _digesterFacesConfigUnmarshaller = new DigesterFacesConfigUnmarshallerImpl(
                externalContext);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    private void loadTextFacesConfig(String file) throws SAXException,
            IOException
    {
        RuntimeConfig runtimeConfig = RuntimeConfig
                .getCurrentInstance(externalContext);

        org.apache.myfaces.config.impl.digester.elements.FacesConfig config = _digesterFacesConfigUnmarshaller
                .getFacesConfig(getClass().getResourceAsStream(file), file);

        for (NavigationRule rule : config.getNavigationRules())
        {
            runtimeConfig.addNavigationRule(rule);
        }
    }

    public void testGetSimpleExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    public void testHandleSimpleExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }

    public void testGetSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    public void testHandleSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    public void testGetSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/c.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    public void testHandleSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        assertEquals("/c.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    public void testGetSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/cars/b.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/cars/c.jsp", nc.getToViewId(facesContext));
    }

    public void testHandleSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        assertEquals("/cars/b.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        assertEquals("/cars/c.jsp", facesContext.getViewRoot().getViewId());
    }
    
    public void testGetSimpleELExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    public void testGetSimpleELExactMatchRuleFailNullOutcome() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        assertNull(nc);
    }
    
    public static class TestBean
    {
        public boolean isTrue()
        {
            return true;
        }
        
        public boolean isFalse()
        {
            return false;
        }
    }
    
    public void testGetSimpleIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    public void testGetSimpleNotIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/b.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/d.jsp", nc.getToViewId(facesContext));
    }
    
    public void testGetSimplePreemptiveIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/go.jsp", nc.getToViewId(facesContext));
    }    
    
    public void testGetSimpleGlobalPreemptiveMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-preemptive-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        assertEquals("/a.jsp", nc.getToViewId(facesContext));
    }
    
    public void testGetSimpleELNoCondMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    public void testGetSimpleELNoCondNullOutcomeMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        assertNull(nc);
    }
}