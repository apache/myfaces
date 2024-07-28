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
package org.apache.myfaces.config.impl;

import java.util.List;

import org.apache.myfaces.config.element.Application;
import org.apache.myfaces.config.element.ContractMapping;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.FacesConfigNameSlot;
import org.apache.myfaces.config.element.LocaleConfig;
import org.apache.myfaces.config.element.OrderSlot;
import org.apache.myfaces.config.impl.element.ConfigOthersSlotImpl;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesConfigUnmarshallerImplTest
{
    private FacesConfigUnmarshallerImpl _impl;

    @BeforeEach
    public void setUp() throws Exception
    {
        _impl = new FacesConfigUnmarshallerImpl(null);
    }

    @Test
    public void testEmptyConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "empty-config.xml"), "empty-config.xml");
        Assertions.assertNotNull(cfg);
        Assertions.assertTrue(cfg.getApplications().isEmpty());
        Assertions.assertTrue(cfg.getComponents().isEmpty());
        Assertions.assertTrue(cfg.getConverters().isEmpty());
        Assertions.assertTrue(cfg.getFactories().isEmpty());
        Assertions.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assertions.assertTrue(cfg.getNavigationRules().isEmpty());
        Assertions.assertTrue(cfg.getRenderKits().isEmpty());
        Assertions.assertTrue(cfg.getValidators().isEmpty());
    }

    @Test
    public void testApplicationConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "application-config.xml"), "application-config.xml");
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals(1, cfg.getApplications().size());
        Application app = cfg.getApplications().get(0);
        Assertions.assertEquals(2, app.getActionListener().size());
        Assertions.assertEquals("action-listener1", app.getActionListener().get(0));
        Assertions.assertEquals("action-listener2", app.getActionListener().get(1));
        Assertions.assertEquals(1, app.getDefaultRenderkitId().size());
        Assertions.assertEquals("default-render-kit-id", app.getDefaultRenderkitId()
                .get(0));
        assertLocaleConfig(app.getLocaleConfig());
        Assertions.assertEquals(1, app.getMessageBundle().size());
        Assertions.assertEquals("message-bundle", app.getMessageBundle().get(0));
        Assertions.assertEquals(1, app.getNavigationHandler().size());
        Assertions.assertEquals("navigation-handler", app.getNavigationHandler().get(0));
        Assertions.assertEquals(1, app.getPropertyResolver().size());
        Assertions.assertEquals("property-resolver", app.getPropertyResolver().get(0));

        Assertions.assertEquals(1, app.getStateManager().size());
        Assertions.assertEquals("state-manager", app.getStateManager().get(0));

        Assertions.assertEquals(1, app.getVariableResolver().size());
        Assertions.assertEquals("variable-resolver", app.getVariableResolver().get(0));

        Assertions.assertEquals(1, app.getViewHandler().size());
        Assertions.assertEquals("view-handler", app.getViewHandler().get(0));

        Assertions.assertEquals(1, app.getElResolver().size());
        Assertions.assertEquals("el-resolver", app.getElResolver().get(0));

        Assertions.assertEquals(1, app.getResourceBundle().size());
        Assertions.assertEquals("base-name", app.getResourceBundle().get(0).getBaseName());
        Assertions.assertEquals("var", app.getResourceBundle().get(0).getVar());
    }

    /**
     * @param localeConfig
     */
    private void assertLocaleConfig(List<LocaleConfig> localeConfig)
    {
        Assertions.assertEquals(1, localeConfig.size());
        LocaleConfig cfg = localeConfig.get(0);
        Assertions.assertEquals("aa", cfg.getDefaultLocale());
        Assertions.assertEquals(2, cfg.getSupportedLocales().size());
        Assertions.assertEquals("aa", cfg.getSupportedLocales().get(0));
        Assertions.assertEquals("bb", cfg.getSupportedLocales().get(1));
    }
    
    @Test
    public void testAbsoluteOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "absolute-ordering-config.xml"), "absolute-ordering-config.xml");
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals("true", cfg.getMetadataComplete());
        Assertions.assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getAbsoluteOrdering().getOrderList();
        
        Assertions.assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        Assertions.assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        Assertions.assertEquals(ConfigOthersSlotImpl.class, orderList.get(2).getClass());
        Assertions.assertEquals("d", ((FacesConfigNameSlot) orderList.get(3)).getName());
        
        Assertions.assertTrue(cfg.getApplications().isEmpty());
        Assertions.assertTrue(cfg.getComponents().isEmpty());
        Assertions.assertTrue(cfg.getConverters().isEmpty());
        Assertions.assertTrue(cfg.getFactories().isEmpty());
        Assertions.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assertions.assertTrue(cfg.getNavigationRules().isEmpty());
        Assertions.assertTrue(cfg.getRenderKits().isEmpty());
        Assertions.assertTrue(cfg.getValidators().isEmpty());
    }
    
    @Test
    public void testOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "ordering-config.xml"), "ordering-config.xml");
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getOrdering().getBeforeList();        
        Assertions.assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        Assertions.assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        Assertions.assertEquals(org.apache.myfaces.config.impl.element.ConfigOthersSlotImpl.class, orderList.get(2).getClass());
        
        orderList = cfg.getOrdering().getAfterList();        
        Assertions.assertEquals("d", ((FacesConfigNameSlot) orderList.get(0)).getName());
        
        Assertions.assertTrue(cfg.getApplications().isEmpty());
        Assertions.assertTrue(cfg.getComponents().isEmpty());
        Assertions.assertTrue(cfg.getConverters().isEmpty());
        Assertions.assertTrue(cfg.getFactories().isEmpty());
        Assertions.assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        Assertions.assertTrue(cfg.getNavigationRules().isEmpty());
        Assertions.assertTrue(cfg.getRenderKits().isEmpty());
        Assertions.assertTrue(cfg.getValidators().isEmpty());
    }
    
    @Test
    public void testFacesFlowConfig() throws Exception
    {/*
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "faces-flow.xml"), "faces-flow.xml");

        Assertions.assertNotNull(cfg);
        Assertions.assertEquals(1, cfg.getFacesFlowDefinitions().size());
        FacesFlowDefinition facesFlowDefinition = cfg.getFacesFlowDefinitions().get(0);
        
        Assertions.assertEquals("flow1", facesFlowDefinition.getId());
        Assertions.assertEquals("node1", facesFlowDefinition.getStartNode());
        Assertions.assertEquals("#{flowBean.init}", facesFlowDefinition.getInitializer());
        Assertions.assertEquals("#{flowBean.finalize}", facesFlowDefinition.getFinalizer());
        
        //view
        Assertions.assertEquals(1, facesFlowDefinition.getViewList().size());
        FacesFlowView facesFlowView = facesFlowDefinition.getViewList().get(0);
        Assertions.assertEquals("outcome2", facesFlowView.getId());
        Assertions.assertEquals("outcome-to-2.xhtml", facesFlowView.getVdlDocument());

        //switch
        Assertions.assertEquals(1, facesFlowDefinition.getSwitchList().size());
        FacesFlowSwitch facesFlowSwitch = facesFlowDefinition.getSwitchList().get(0);
        Assertions.assertEquals("switch1", facesFlowSwitch.getId());
        Assertions.assertEquals("outcome2", facesFlowSwitch.getDefaultOutcome().getFromOutcome());
        NavigationCase swNavigationCase = facesFlowSwitch.getNavigationCaseList().get(0);
        Assertions.assertEquals("#{flowBean.token > 0}", swNavigationCase.getIf());
        Assertions.assertEquals("outcome2", swNavigationCase.getFromOutcome());

        //flow return
        Assertions.assertEquals(1, facesFlowDefinition.getReturnList().size());
        FacesFlowReturn facesFlowReturn = facesFlowDefinition.getReturnList().get(0);
        Assertions.assertEquals("flowReturn1", facesFlowReturn.getId());
        Assertions.assertEquals("/outcome1", facesFlowReturn.getNavigationCase().getFromOutcome());
        
        //navigation rule
        Assertions.assertEquals(1, facesFlowDefinition.getNavigationRuleList().size());
        NavigationRule navigationRule = facesFlowDefinition.getNavigationRuleList().get(0);
        Assertions.assertEquals("/x.xhtml", navigationRule.getFromViewId());
        Assertions.assertEquals(1, navigationRule.getNavigationCases().size());
        NavigationCase navigationCase = navigationRule.getNavigationCases().get(0);
        Assertions.assertEquals("go", navigationCase.getFromOutcome());
        Assertions.assertEquals("#{test.true}", navigationCase.getIf());
        Assertions.assertEquals("/y.xhtml", navigationCase.getToViewId());

        //flow call
        Assertions.assertEquals(1, facesFlowDefinition.getFlowCallList().size());
        FacesFlowCall facesFlowCall = facesFlowDefinition.getFlowCallList().get(0);
        Assertions.assertEquals("flowCall", facesFlowCall.getId());
        Assertions.assertEquals("flow2", facesFlowCall.getFlowReference().getFlowId());
        Assertions.assertEquals(1, facesFlowCall.getOutboundParameterList().size());
        FacesFlowParameter facesFlowOutboundParameter = facesFlowCall.getOutboundParameterList().get(0);
        Assertions.assertEquals("name1", facesFlowOutboundParameter.getName());
        Assertions.assertEquals("value1", facesFlowOutboundParameter.getValue());
        
        //method call
        Assertions.assertEquals(1, facesFlowDefinition.getMethodCallList().size());
        FacesFlowMethodCall facesFlowMethodCall = facesFlowDefinition.getMethodCallList().get(0);
        Assertions.assertEquals("method1", facesFlowMethodCall.getId());
        Assertions.assertEquals("#{flowBean.doSomething}", facesFlowMethodCall.getMethod());
        Assertions.assertEquals("outcome2", facesFlowMethodCall.getDefaultOutcome());
        
        //inbound param
        Assertions.assertEquals(1, facesFlowDefinition.getInboundParameterList().size());
        FacesFlowParameter facesFlowParameter = facesFlowDefinition.getInboundParameterList().get(0);
        Assertions.assertEquals("name1", facesFlowParameter.getName());
        Assertions.assertEquals("value1", facesFlowParameter.getValue());*/
    }
    
    @Test
    public void testCsrf() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "csrf-and-contracts.xml"), "csrf-and-contracts.xml");
        
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals(2, cfg.getProtectedViewsUrlPatternList().size());
        Assertions.assertEquals("/files/*.xhtml", cfg.getProtectedViewsUrlPatternList().get(0));
        Assertions.assertEquals("/files2/*.xhtml", cfg.getProtectedViewsUrlPatternList().get(1));
        
        
    }
    
    @Test
    public void testContracts() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "csrf-and-contracts.xml"), "csrf-and-contracts.xml");
        
        Assertions.assertNotNull(cfg);

        Application app = cfg.getApplications().get(0);
        Assertions.assertNotNull(app);
        Assertions.assertEquals(1, app.getResourceLibraryContractMappings().size());
        
        ContractMapping mapping = app.getResourceLibraryContractMappings().get(0);
        Assertions.assertEquals("/files/*.xhtml", mapping.getUrlPatternList().get(0));
        Assertions.assertEquals("contractA contractB", mapping.getContractList().get(0));
    }
    
    @Test
    public void testContracts2() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "contracts2.xml"), "contracts2.xml");
        
        Assertions.assertNotNull(cfg);

        Application app = cfg.getApplications().get(0);
        Assertions.assertNotNull(app);
        Assertions.assertEquals(1, app.getResourceLibraryContractMappings().size());
        
        ContractMapping mapping = app.getResourceLibraryContractMappings().get(0);
        Assertions.assertTrue(mapping.getUrlPatternList().contains("/files/*.xhtml"));
        Assertions.assertTrue(mapping.getUrlPatternList().contains("/files2/*.xhtml"));
        Assertions.assertTrue(mapping.getContractList().contains("contractA"));
        Assertions.assertTrue(mapping.getContractList().contains("contractB"));
    }    
}
