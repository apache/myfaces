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
package org.apache.myfaces.config.impl.digister;

import java.util.List;

import junit.framework.TestCase;

import org.apache.myfaces.config.impl.FacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.element.Application;
import org.apache.myfaces.config.element.ContractMapping;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.FacesConfigNameSlot;
import org.apache.myfaces.config.element.LocaleConfig;
import org.apache.myfaces.config.element.OrderSlot;
import org.apache.myfaces.config.impl.element.ConfigOthersSlotImpl;
import org.junit.Assert;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesConfigUnmarshallerImplTest extends TestCase
{
    private FacesConfigUnmarshallerImpl _impl;

    public void setUp() throws Exception
    {
        _impl = new FacesConfigUnmarshallerImpl(null);
    }

    public void testEmptyConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "empty-config.xml"), "empty-config.xml");
        Assert.assertNotNull(cfg);
        Assert.assertTrue(cfg.getApplications().isEmpty());
        Assert.assertTrue(cfg.getComponents().isEmpty());
        Assert.assertTrue(cfg.getConverters().isEmpty());
        Assert.assertTrue(cfg.getFactories().isEmpty());
        Assert.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assert.assertTrue(cfg.getNavigationRules().isEmpty());
        Assert.assertTrue(cfg.getRenderKits().isEmpty());
        Assert.assertTrue(cfg.getValidators().isEmpty());
    }

    public void testApplicationConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "application-config.xml"), "application-config.xml");
        Assert.assertNotNull(cfg);
        Assert.assertEquals(1, cfg.getApplications().size());
        Application app = cfg.getApplications().get(0);
        Assert.assertEquals(2, app.getActionListener().size());
        Assert.assertEquals("action-listener1", app.getActionListener().get(0));
        Assert.assertEquals("action-listener2", app.getActionListener().get(1));
        Assert.assertEquals(1, app.getDefaultRenderkitId().size());
        Assert.assertEquals("default-render-kit-id", app.getDefaultRenderkitId()
                .get(0));
        assertLocaleConfig(app.getLocaleConfig());
        Assert.assertEquals(1, app.getMessageBundle().size());
        Assert.assertEquals("message-bundle", app.getMessageBundle().get(0));
        Assert.assertEquals(1, app.getNavigationHandler().size());
        Assert.assertEquals("navigation-handler", app.getNavigationHandler().get(0));
        Assert.assertEquals(1, app.getPropertyResolver().size());
        Assert.assertEquals("property-resolver", app.getPropertyResolver().get(0));

        Assert.assertEquals(1, app.getStateManager().size());
        Assert.assertEquals("state-manager", app.getStateManager().get(0));

        Assert.assertEquals(1, app.getVariableResolver().size());
        Assert.assertEquals("variable-resolver", app.getVariableResolver().get(0));

        Assert.assertEquals(1, app.getViewHandler().size());
        Assert.assertEquals("view-handler", app.getViewHandler().get(0));

        Assert.assertEquals(1, app.getElResolver().size());
        Assert.assertEquals("el-resolver", app.getElResolver().get(0));

        Assert.assertEquals(1, app.getResourceBundle().size());
        Assert.assertEquals("base-name", app.getResourceBundle().get(0).getBaseName());
        Assert.assertEquals("var", app.getResourceBundle().get(0).getVar());
    }

    /**
     * @param localeConfig
     */
    private void assertLocaleConfig(List<LocaleConfig> localeConfig)
    {
        Assert.assertEquals(1, localeConfig.size());
        LocaleConfig cfg = localeConfig.get(0);
        Assert.assertEquals("aa", cfg.getDefaultLocale());
        Assert.assertEquals(2, cfg.getSupportedLocales().size());
        Assert.assertEquals("aa", cfg.getSupportedLocales().get(0));
        Assert.assertEquals("bb", cfg.getSupportedLocales().get(1));
    }
    
    public void testAbsoluteOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "absolute-ordering-config.xml"), "absolute-ordering-config.xml");
        Assert.assertNotNull(cfg);
        Assert.assertEquals("true", cfg.getMetadataComplete());
        Assert.assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getAbsoluteOrdering().getOrderList();
        
        Assert.assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        Assert.assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        Assert.assertEquals(ConfigOthersSlotImpl.class, orderList.get(2).getClass());
        Assert.assertEquals("d", ((FacesConfigNameSlot) orderList.get(3)).getName());
        
        Assert.assertTrue(cfg.getApplications().isEmpty());
        Assert.assertTrue(cfg.getComponents().isEmpty());
        Assert.assertTrue(cfg.getConverters().isEmpty());
        Assert.assertTrue(cfg.getFactories().isEmpty());
        Assert.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assert.assertTrue(cfg.getNavigationRules().isEmpty());
        Assert.assertTrue(cfg.getRenderKits().isEmpty());
        Assert.assertTrue(cfg.getValidators().isEmpty());
    }
    
    public void testOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "ordering-config.xml"), "ordering-config.xml");
        Assert.assertNotNull(cfg);
        Assert.assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getOrdering().getBeforeList();        
        Assert.assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        Assert.assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        Assert.assertEquals(org.apache.myfaces.config.impl.element.ConfigOthersSlotImpl.class, orderList.get(2).getClass());
        
        orderList = cfg.getOrdering().getAfterList();        
        Assert.assertEquals("d", ((FacesConfigNameSlot) orderList.get(0)).getName());
        
        Assert.assertTrue(cfg.getApplications().isEmpty());
        Assert.assertTrue(cfg.getComponents().isEmpty());
        Assert.assertTrue(cfg.getConverters().isEmpty());
        Assert.assertTrue(cfg.getFactories().isEmpty());
        Assert.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assert.assertTrue(cfg.getNavigationRules().isEmpty());
        Assert.assertTrue(cfg.getRenderKits().isEmpty());
        Assert.assertTrue(cfg.getValidators().isEmpty());
    }
    
    public void testFacesFlowConfig() throws Exception
    {/*
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "faces-flow.xml"), "faces-flow.xml");

        Assert.assertNotNull(cfg);
        Assert.assertEquals(1, cfg.getFacesFlowDefinitions().size());
        FacesFlowDefinition facesFlowDefinition = cfg.getFacesFlowDefinitions().get(0);
        
        Assert.assertEquals("flow1", facesFlowDefinition.getId());
        Assert.assertEquals("node1", facesFlowDefinition.getStartNode());
        Assert.assertEquals("#{flowBean.init}", facesFlowDefinition.getInitializer());
        Assert.assertEquals("#{flowBean.finalize}", facesFlowDefinition.getFinalizer());
        
        //view
        Assert.assertEquals(1, facesFlowDefinition.getViewList().size());
        FacesFlowView facesFlowView = facesFlowDefinition.getViewList().get(0);
        Assert.assertEquals("outcome2", facesFlowView.getId());
        Assert.assertEquals("outcome-to-2.xhtml", facesFlowView.getVdlDocument());

        //switch
        Assert.assertEquals(1, facesFlowDefinition.getSwitchList().size());
        FacesFlowSwitch facesFlowSwitch = facesFlowDefinition.getSwitchList().get(0);
        Assert.assertEquals("switch1", facesFlowSwitch.getId());
        Assert.assertEquals("outcome2", facesFlowSwitch.getDefaultOutcome().getFromOutcome());
        NavigationCase swNavigationCase = facesFlowSwitch.getNavigationCaseList().get(0);
        Assert.assertEquals("#{flowBean.token > 0}", swNavigationCase.getIf());
        Assert.assertEquals("outcome2", swNavigationCase.getFromOutcome());

        //flow return
        Assert.assertEquals(1, facesFlowDefinition.getReturnList().size());
        FacesFlowReturn facesFlowReturn = facesFlowDefinition.getReturnList().get(0);
        Assert.assertEquals("flowReturn1", facesFlowReturn.getId());
        Assert.assertEquals("/outcome1", facesFlowReturn.getNavigationCase().getFromOutcome());
        
        //navigation rule
        Assert.assertEquals(1, facesFlowDefinition.getNavigationRuleList().size());
        NavigationRule navigationRule = facesFlowDefinition.getNavigationRuleList().get(0);
        Assert.assertEquals("/x.xhtml", navigationRule.getFromViewId());
        Assert.assertEquals(1, navigationRule.getNavigationCases().size());
        NavigationCase navigationCase = navigationRule.getNavigationCases().get(0);
        Assert.assertEquals("go", navigationCase.getFromOutcome());
        Assert.assertEquals("#{test.true}", navigationCase.getIf());
        Assert.assertEquals("/y.xhtml", navigationCase.getToViewId());

        //flow call
        Assert.assertEquals(1, facesFlowDefinition.getFlowCallList().size());
        FacesFlowCall facesFlowCall = facesFlowDefinition.getFlowCallList().get(0);
        Assert.assertEquals("flowCall", facesFlowCall.getId());
        Assert.assertEquals("flow2", facesFlowCall.getFlowReference().getFlowId());
        Assert.assertEquals(1, facesFlowCall.getOutboundParameterList().size());
        FacesFlowParameter facesFlowOutboundParameter = facesFlowCall.getOutboundParameterList().get(0);
        Assert.assertEquals("name1", facesFlowOutboundParameter.getName());
        Assert.assertEquals("value1", facesFlowOutboundParameter.getValue());
        
        //method call
        Assert.assertEquals(1, facesFlowDefinition.getMethodCallList().size());
        FacesFlowMethodCall facesFlowMethodCall = facesFlowDefinition.getMethodCallList().get(0);
        Assert.assertEquals("method1", facesFlowMethodCall.getId());
        Assert.assertEquals("#{flowBean.doSomething}", facesFlowMethodCall.getMethod());
        Assert.assertEquals("outcome2", facesFlowMethodCall.getDefaultOutcome());
        
        //inbound param
        Assert.assertEquals(1, facesFlowDefinition.getInboundParameterList().size());
        FacesFlowParameter facesFlowParameter = facesFlowDefinition.getInboundParameterList().get(0);
        Assert.assertEquals("name1", facesFlowParameter.getName());
        Assert.assertEquals("value1", facesFlowParameter.getValue());*/
    }
    
    public void testCsrf() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "csrf-and-contracts.xml"), "csrf-and-contracts.xml");
        
        Assert.assertNotNull(cfg);
        Assert.assertEquals(2, cfg.getProtectedViewsUrlPatternList().size());
        Assert.assertEquals("/files/*.xhtml", cfg.getProtectedViewsUrlPatternList().get(0));
        Assert.assertEquals("/files2/*.xhtml", cfg.getProtectedViewsUrlPatternList().get(1));
        
        
    }
    
    public void testContracts() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "csrf-and-contracts.xml"), "csrf-and-contracts.xml");
        
        Assert.assertNotNull(cfg);

        Application app = cfg.getApplications().get(0);
        Assert.assertNotNull(app);
        Assert.assertEquals(1, app.getResourceLibraryContractMappings().size());
        
        ContractMapping mapping = app.getResourceLibraryContractMappings().get(0);
        Assert.assertEquals("/files/*.xhtml", mapping.getUrlPatternList().get(0));
        Assert.assertEquals("contractA contractB", mapping.getContractList().get(0));
    }
    
    public void testContracts2() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "contracts2.xml"), "contracts2.xml");
        
        Assert.assertNotNull(cfg);

        Application app = cfg.getApplications().get(0);
        Assert.assertNotNull(app);
        Assert.assertEquals(1, app.getResourceLibraryContractMappings().size());
        
        ContractMapping mapping = app.getResourceLibraryContractMappings().get(0);
        Assert.assertTrue(mapping.getUrlPatternList().contains("/files/*.xhtml"));
        Assert.assertTrue(mapping.getUrlPatternList().contains("/files2/*.xhtml"));
        Assert.assertTrue(mapping.getContractList().contains("contractA"));
        Assert.assertTrue(mapping.getContractList().contains("contractB"));
    }    
}
