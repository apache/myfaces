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

import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.ConfigOthersSlot;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigNameSlot;
import org.apache.myfaces.config.impl.digester.elements.LocaleConfig;
import org.apache.myfaces.config.impl.digester.elements.OrderSlot;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DigesterFacesConfigUnmarshallerImplTest extends TestCase
{
    private DigesterFacesConfigUnmarshallerImpl _impl;

    protected void setUp() throws Exception
    {
        _impl = new DigesterFacesConfigUnmarshallerImpl(null);
    }

    public void testEmptyConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "empty-config.xml"), "empty-config.xml");
        assertNotNull(cfg);
        assertTrue(cfg.getApplications().isEmpty());
        assertTrue(cfg.getComponents().isEmpty());
        assertTrue(cfg.getConverters().isEmpty());
        assertTrue(cfg.getFactories().isEmpty());
        assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        assertTrue(cfg.getManagedBeans().isEmpty());
        assertTrue(cfg.getNavigationRules().isEmpty());
        assertTrue(cfg.getRenderKits().isEmpty());
        assertTrue(cfg.getValidators().isEmpty());
    }

    public void testApplicationConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "application-config.xml"), "application-config.xml");
        assertNotNull(cfg);
        assertEquals(1, cfg.getApplications().size());
        Application app = cfg.getApplications().get(0);
        assertEquals(2, app.getActionListener().size());
        assertEquals("action-listener1", app.getActionListener().get(0));
        assertEquals("action-listener2", app.getActionListener().get(1));
        assertEquals(1, app.getDefaultRenderkitId().size());
        assertEquals("default-render-kit-id", app.getDefaultRenderkitId()
                .get(0));
        assertLocaleConfig(app.getLocaleConfig());
        assertEquals(1, app.getMessageBundle().size());
        assertEquals("message-bundle", app.getMessageBundle().get(0));
        assertEquals(1, app.getNavigationHandler().size());
        assertEquals("navigation-handler", app.getNavigationHandler().get(0));
        assertEquals(1, app.getPropertyResolver().size());
        assertEquals("property-resolver", app.getPropertyResolver().get(0));

        assertEquals(1, app.getStateManager().size());
        assertEquals("state-manager", app.getStateManager().get(0));

        assertEquals(1, app.getVariableResolver().size());
        assertEquals("variable-resolver", app.getVariableResolver().get(0));

        assertEquals(1, app.getViewHandler().size());
        assertEquals("view-handler", app.getViewHandler().get(0));

        assertEquals(1, app.getElResolver().size());
        assertEquals("el-resolver", app.getElResolver().get(0));

        assertEquals(1, app.getResourceBundle().size());
        assertEquals("base-name", app.getResourceBundle().get(0).getBaseName());
        assertEquals("var", app.getResourceBundle().get(0).getVar());
    }

    /**
     * @param localeConfig
     */
    private void assertLocaleConfig(List<LocaleConfig> localeConfig)
    {
        assertEquals(1, localeConfig.size());
        LocaleConfig cfg = localeConfig.get(0);
        assertEquals("aa", cfg.getDefaultLocale());
        assertEquals(2, cfg.getSupportedLocales().size());
        assertEquals("aa", cfg.getSupportedLocales().get(0));
        assertEquals("bb", cfg.getSupportedLocales().get(1));
    }
    
    public void testAbsoluteOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "absolute-ordering-config.xml"), "absolute-ordering-config.xml");
        assertNotNull(cfg);
        assertEquals("true", cfg.getMetadataComplete());
        assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getAbsoluteOrdering().getOrderList();
        
        assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        assertEquals(ConfigOthersSlot.class, orderList.get(2).getClass());
        assertEquals("d", ((FacesConfigNameSlot) orderList.get(3)).getName());
        
        assertTrue(cfg.getApplications().isEmpty());
        assertTrue(cfg.getComponents().isEmpty());
        assertTrue(cfg.getConverters().isEmpty());
        assertTrue(cfg.getFactories().isEmpty());
        assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        assertTrue(cfg.getManagedBeans().isEmpty());
        assertTrue(cfg.getNavigationRules().isEmpty());
        assertTrue(cfg.getRenderKits().isEmpty());
        assertTrue(cfg.getValidators().isEmpty());
    }
    
    public void testOrderingConfig() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
                "ordering-config.xml"), "ordering-config.xml");
        assertNotNull(cfg);
        assertEquals("a",cfg.getName());

        List<OrderSlot> orderList = cfg.getOrdering().getBeforeList();        
        assertEquals("b", ((FacesConfigNameSlot) orderList.get(0)).getName());
        assertEquals("c", ((FacesConfigNameSlot) orderList.get(1)).getName());
        assertEquals(ConfigOthersSlot.class, orderList.get(2).getClass());
        
        orderList = cfg.getOrdering().getAfterList();        
        assertEquals("d", ((FacesConfigNameSlot) orderList.get(0)).getName());
        
        assertTrue(cfg.getApplications().isEmpty());
        assertTrue(cfg.getComponents().isEmpty());
        assertTrue(cfg.getConverters().isEmpty());
        assertTrue(cfg.getFactories().isEmpty());
        assertTrue(cfg.getLifecyclePhaseListener().isEmpty());
        assertTrue(cfg.getManagedBeans().isEmpty());
        assertTrue(cfg.getNavigationRules().isEmpty());
        assertTrue(cfg.getRenderKits().isEmpty());
        assertTrue(cfg.getValidators().isEmpty());
    }    
}
