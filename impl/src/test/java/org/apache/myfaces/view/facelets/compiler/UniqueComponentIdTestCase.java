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
package org.apache.myfaces.view.facelets.compiler;

import java.util.HashSet;
import java.util.Set;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.renderkit.html.HtmlResponseStateManager;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.util.DefaultSerialFactory;
import org.apache.myfaces.test.mock.MockFacesContext20;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.apache.myfaces.view.facelets.FaceletMultipleRequestsTestCase;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.junit.Assert;
import org.junit.Test;

public class UniqueComponentIdTestCase extends FaceletMultipleRequestsTestCase
{

    @Override
    protected void setUpApplication() throws Exception
    {
        super.setUpApplication();
        
        application.setStateManager(new StateManagerImpl());
    }
    
    @Override
    protected void setUpServletContextAndSession() throws Exception
    {
        super.setUpServletContextAndSession();
        
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "true");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, "-1");
        servletContext.setAttribute(MyfacesConfig.class.getName(), new MyfacesConfig());
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdCif() throws Exception
    {
        /* For this example:
         * 
         *   <h:outputText value="-A-"/>
         *   <c:if test="#{condition}">
         *   <h:outputText value="-B-"/>
         *   </c:if>
         *   <h:outputText value="-C-"/>
         *
         * the same tag-component unique id and component id should be generated
         * without taking into account the value on the condition.
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("condition", true);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdCif.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdCif.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("condition", false);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdCif.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdCif.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }
    
    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdCifSaveCondition() throws Exception
    {
        /* For this example:
         * 
         *   <h:outputText value="-A-"/>
         *   <c:if test="#{condition}">
         *   <h:outputText value="-B-"/>
         *   </c:if>
         *   <h:outputText value="-C-"/>
         *
         * When the view is restored, the condition should not be evaluated. Instead,
         * the value should be retrieved from the state, to ensure PSS initial conditions.
         * 
         * Then, try to refresh the view and check if the condition is evaluated.
         * Again the ids should be the same, but -B- component is removed from view
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("condition", true);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdCif.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdCif.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            request.setAttribute("condition", false);
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            UIViewRoot root = application.getStateManager().restoreView(facesContext, "/testUniqueComponentIdCif.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);

            Assert.assertNotNull(root);
            
            boolean restoredB = false;
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
            
            //Now let's refresh the view on render response, to reflect the value on the condition
            tagUniqueIdSet.remove(tagBId);
            componentIdSet.remove(componentBid);
            
            //Refresh!
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            vdl.buildView(facesContext, root, "/testUniqueComponentIdCif.xhtml");
            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
        }
        finally
        {
            tearDownRequest();
        }
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdInclude1_1() throws Exception
    {
        /* For this example:
         * 
         * <h:outputText value="-A-"/>
         * <ui:include src="#{pageSelected}"/>
         * <h:outputText value="-C-"/>
         * 
         * included page
         * 
         * <h:outputText value="-B-"/
         *
         * the same tag-component unique id and component id should be generated
         * without taking into account the components on the included page.
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_0.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }

    @Test
    public void testUniqueComponentIdInclude1_2() throws Exception
    {
        /* For this example:
         * 
         * <h:outputText value="-A-"/>
         * <ui:include src="#{pageSelected}"/>
         * <h:outputText value="-C-"/>
         * 
         * included page 1
         * 
         * <h:outputText value="-B-"/>
         * 
         * included page 2
         * 
         * <h:outputText value="-B-"/>
         *
         * the same tag-component unique id and component id should be generated
         * without taking into account the components on the included page.
         * 
         * Since the component -B- was defined on a different page, is a different
         * component and its values should not be related (but its component ids
         * could be the same). This condition ensure refreshing algorithm will
         * not mix components between two views.
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_2.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String componentId = child.getId();
                
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (componentBid.equals(componentId))
                {
                    Assert.assertFalse(tagBId.equals(id));
                }
                else
                {
                    if (!tagUniqueIdSet.contains(id))
                    {
                        Assert.fail();
                    }
                    if (!componentIdSet.contains(componentId))
                    {
                        Assert.fail();
                    }
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdIncludeSaveCondition() throws Exception
    {
        /* For this example:
         * 
         * <h:outputText value="-A-"/>
         * <ui:include src="#{pageSelected}"/>
         * <h:outputText value="-C-"/>
         * 
         * included page
         * 
         * <h:outputText value="-B-"/
         *
         * the same tag-component unique id and component id should be generated
         * without taking into account the components on the included page.
         *
         * When the view is restored, the pageSelected EL should not be evaluated. Instead,
         * the value should be retrieved from the state, to ensure PSS initial conditions.
         * 
         * Then, try to refresh the view and check if the pageSelected EL is evaluated.
         * Again the ids should be the same, but -B- component is removed from view
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                        child.getAttributes().put("test", "test");
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_0.xhtml");
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            UIViewRoot root = application.getStateManager().restoreView(facesContext, "/testUniqueComponentIdInclude1.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);

            Assert.assertNotNull(root);
            
            boolean restoredB = false;
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                        Assert.assertNotNull(child.getAttributes().get("test"));
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
            
            //Now let's refresh the view on render response, to reflect the value on the condition
            tagUniqueIdSet.remove(tagBId);
            componentIdSet.remove(componentBid);
            
            //Refresh!
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");
            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
        }
        finally
        {
            tearDownRequest();
        }
    }
    
    @Test
    public void testUniqueComponentIdIncludeSaveCondition2() throws Exception
    {
        /* For this example:
         * 
         * <h:outputText value="-A-"/>
         * <ui:include src="#{pageSelected}"/>
         * <h:outputText value="-C-"/>
         * 
         * included page
         * 
         * <h:outputText value="-B-"/
         *
         * the same tag-component unique id and component id should be generated
         * without taking into account the components on the included page.
         *
         * When the view is restored, the pageSelected EL should not be evaluated. Instead,
         * the value should be retrieved from the state, to ensure PSS initial conditions.
         * 
         * Then, try to refresh the view and check if the pageSelected EL is evaluated.
         * Again the ids should be the same, but -B- component is removed from view
         * Since testUniqueComponentIdInclude1_1 and testUniqueComponentIdInclude1_2 are
         * identical copies of the same file, we should check if c:if add/remove algorithm
         * can detect the difference.
         */
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdInclude1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                        child.getAttributes().put("test", "test");
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            request.setAttribute("pageSelected", "testUniqueComponentIdInclude1_2.xhtml");
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            UIViewRoot root = application.getStateManager().restoreView(facesContext, "/testUniqueComponentIdInclude1.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);

            Assert.assertNotNull(root);
            
            boolean restoredB = false;
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                        Assert.assertNotNull(child.getAttributes().get("test"));
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
            
            //Now let's refresh the view on render response, to reflect the value on the condition
            tagUniqueIdSet.remove(tagBId);
            componentIdSet.remove(componentBid);
            
            //Refresh!
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            vdl.buildView(facesContext, root, "/testUniqueComponentIdInclude1.xhtml");
            
            restoredB = false;
            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                String componentId = child.getId();
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                        //Check if the B component is not the same as before. 
                        //It should have different tag-component id and its state is different.
                        Assert.assertFalse(tagBId.equals(id));
                        Assert.assertNull(child.getAttributes().get("test"));
                    }
                }
                else
                {
                    if (!tagUniqueIdSet.contains(id))
                    {
                        Assert.fail();
                    }
                    if (!componentIdSet.contains(componentId))
                    {
                        Assert.fail();
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
        }
        finally
        {
            tearDownRequest();
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdDecorate1_1() throws Exception
    {
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        String componentDid = null;
        String tagDId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdDecorate1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                    if ("-D-".equals(((UIOutput)child).getValue()))
                    {
                        componentDid = componentId;
                        tagDId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            Assert.assertNotNull(componentDid);
            Assert.assertNotNull(tagDId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);
        tagUniqueIdSet.remove(tagDId);
        componentIdSet.remove(componentDid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_0.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdDecorate1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                String componentId = child.getId();
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-C-".equals(((UIOutput)child).getValue()))
                    {
                        //This is a different C, because it the template is different
                        //the final component id could be the same or different, but the
                        //important here is refreshing algorithm should not mix both 
                        Assert.assertTrue(!tagUniqueIdSet.contains(id));
                    }
                    else
                    {
                        if (!tagUniqueIdSet.contains(id))
                        {
                            Assert.fail();
                        }
                        if (!componentIdSet.contains(componentId))
                        {
                            Assert.fail();
                        }
                    }
                }
                else
                {
                    if (!tagUniqueIdSet.contains(id))
                    {
                        Assert.fail();
                    }
                    if (!componentIdSet.contains(componentId))
                    {
                        Assert.fail();
                    }
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testUniqueComponentIdDecorate1_2() throws Exception
    {
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        String componentDid = null;
        String tagDId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdDecorate1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                    if ("-D-".equals(((UIOutput)child).getValue()))
                    {
                        componentDid = componentId;
                        tagDId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            Assert.assertNotNull(componentDid);
            Assert.assertNotNull(tagDId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);
        tagUniqueIdSet.remove(tagDId);
        componentIdSet.remove(componentDid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_2.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdDecorate1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                String componentId = child.getId();
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-C-".equals(((UIOutput)child).getValue()) ||
                        "-B-".equals(((UIOutput)child).getValue()) ||
                        "-D-".equals(((UIOutput)child).getValue()))
                    {
                        //This is a different C,B or D, because it the template is different
                        //the final component id could be the same or different, but the
                        //important here is refreshing algorithm should not mix both 
                        Assert.assertTrue(!tagUniqueIdSet.contains(id));
                    }
                    else
                    {
                        if (!tagUniqueIdSet.contains(id))
                        {
                            Assert.fail();
                        }
                        if (!componentIdSet.contains(componentId))
                        {
                            Assert.fail();
                        }
                    }
                }
                else
                {
                    if (!tagUniqueIdSet.contains(id))
                    {
                        Assert.fail();
                    }
                    if (!componentIdSet.contains(componentId))
                    {
                        Assert.fail();
                    }
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }

    @Test
    public void testUniqueComponentIdDecorateSaveCondition() throws Exception
    {
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_1.xhtml");
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdDecorate1.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                        child.getAttributes().put("test", "test");
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            request.setAttribute("pageSelected", "testUniqueComponentIdDecorate1_0.xhtml");
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            UIViewRoot root = application.getStateManager().restoreView(facesContext, "/testUniqueComponentIdDecorate1.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);

            Assert.assertNotNull(root);
            
            boolean restoredB = false;
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                String componentId = child.getId();
                
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                        Assert.assertNotNull(child.getAttributes().get("test"));
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
            
            //Now let's refresh the view on render response, to reflect the value on the condition
            tagUniqueIdSet.remove(tagBId);
            componentIdSet.remove(componentBid);
            
            //Refresh!
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            vdl.buildView(facesContext, root, "/testUniqueComponentIdDecorate1.xhtml");
            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                String componentId = child.getId();
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput
                        && "-C-".equals(((UIOutput)child).getValue()))
                {
                    //This is a different C, because it the template is different
                    //the final component id could be the same or different, but the
                    //important here is refreshing algorithm should not mix both 
                    Assert.assertTrue(!tagUniqueIdSet.contains(id));
                }
                else
                {
                    if (!tagUniqueIdSet.contains(id))
                    {
                        Assert.fail();
                    }
                    if (!componentIdSet.contains(componentId))
                    {
                        Assert.fail();
                    }
                }
            }
        }
        finally
        {
            tearDownRequest();
        }
    }

    @Test
    public void testUniqueComponentIdChoose() throws Exception
    {
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("selectionC", true);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdChoose.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdChoose.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-C-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        tagUniqueIdSet.remove(tagBId);
        componentIdSet.remove(componentBid);

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("selectionX", true);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdChoose.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdChoose.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

    }

    @Test
    public void testUniqueComponentIdChooseSaveCondition() throws Exception
    {
        String viewStateParam = null;
        String componentBid = null;
        String tagBId = null;
        Set<String> tagUniqueIdSet = new HashSet<String>();
        Set<String> componentIdSet = new HashSet<String>();
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());
        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            request.setAttribute("selectionB", true);
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/testUniqueComponentIdChoose.xhtml");
            vdl.buildView(facesContext, root, "/testUniqueComponentIdChoose.xhtml");

            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                tagUniqueIdSet.add(id);
                String componentId = child.getId();
                if (componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                componentIdSet.add(componentId);
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        componentBid = componentId;
                        tagBId = id;
                    }
                }
            }
            
            Assert.assertNotNull(componentBid);
            Assert.assertNotNull(tagBId);
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
            request.setAttribute("selectionX", true);
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            UIViewRoot root = application.getStateManager().restoreView(facesContext, "/testUniqueComponentIdChoose.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);

            Assert.assertNotNull(root);
            
            boolean restoredB = false;
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
                if (componentId.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) && child instanceof UIOutput)
                {
                    if ("-B-".equals(((UIOutput)child).getValue()))
                    {
                        restoredB = true;
                    }
                }
            }
            
            Assert.assertTrue(restoredB);
            
            //Now let's refresh the view on render response, to reflect the value on the condition
            tagUniqueIdSet.remove(tagBId);
            componentIdSet.remove(componentBid);
            
            //Refresh!
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            vdl.buildView(facesContext, root, "/testUniqueComponentIdChoose.xhtml");
            
            //1. test unique MARK_CREATED id
            for (UIComponent child : root.getChildren())
            {
                String id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (!tagUniqueIdSet.contains(id))
                {
                    Assert.fail();
                }
                String componentId = child.getId();
                if (!componentIdSet.contains(componentId))
                {
                    Assert.fail();
                }
            }
        }
        finally
        {
            tearDownRequest();
        }
    }

}
