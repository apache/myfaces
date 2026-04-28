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
package org.apache.myfaces.view.facelets.tag;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4589: nested composites, user taglib {@code tst:panel} / {@code tst:icon}, and request-scoped
 * {@code #{icon.test}} / {@code #{color.blue}}.
 * <p>
 * Uses {@code t:tagAttribute} ({@link org.apache.myfaces.test.facelets.taghandler.TagAttributeHandler}) with template
 * parameters (no {@code VariableMapper} overlay on {@code FaceletContext}) so the test does not need OmniFaces.
 * {@code CompositeComponentResourceTagHandler} peels third-party {@code wrapped}-delegate overlays for
 * {@code cc:insertChildren} / {@code cc:insertFacet}, which covers real {@code o:tagAttribute} applications.
 * </p>
 * <p>
 * Concrete subclasses set {@link MyfacesConfig#STRICT_JSF_2_FACELETS_COMPATIBILITY} to exercise both the default
 * (non-strict) user-tag handler and the legacy strict Facelets path.
 * </p>
 */
public abstract class AbstractMyfaces4589FaceletsIntegrationTestCase extends AbstractFaceletTestCase
{

    /**
     * @return whether {@link MyfacesConfig#STRICT_JSF_2_FACELETS_COMPATIBILITY} is enabled for this run
     */
    protected abstract boolean strictJsf2FaceletsCompatibility();

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME,
                StateManager.StateSavingMethod.SERVER.name());
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.name());
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME,
                "/myfaces4589.taglib.xml;/test_tagattribute.taglib.xml");
        servletContext.addInitParameter(MyfacesConfig.STRICT_JSF_2_FACELETS_COMPATIBILITY,
                Boolean.toString(strictJsf2FaceletsCompatibility()));
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    /**
     * Asserts request beans {@code color} / {@code icon} are not shadowed inside nested composites (MYFACES-4589).
     */
    @Test
    public void testExternalReproducerTagAttributeShadowing4589() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("color", new ColorBean());
        facesContext.getExternalContext().getRequestMap().put("icon", new IconBean());
        facesContext.getExternalContext().getRequestMap().put("testView", new TestViewBean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "myfaces4589_test.xhtml");
        String out = render(root);

        Assertions.assertTrue(out.contains("color-blue-panel"), () -> out);
        Assertions.assertTrue(out.contains("pi pi-at pi-spin"), () -> out);
        Assertions.assertTrue(out.contains("color-blue"), () -> out);
        Assertions.assertTrue(out.contains("Development"), () -> out);
    }

    public static class ColorBean
    {
        public String getBlue()
        {
            return "color-blue";
        }
    }

    public static class IconBean
    {
        public String getTest()
        {
            return "pi pi-at pi-spin";
        }
    }

    public static class TestViewBean
    {
        private final DepartmentBean department = new DepartmentBean(1L, "Development");

        public DepartmentBean getDepartment()
        {
            return department;
        }
    }

    public static class DepartmentBean
    {
        private final Long id;
        private final String name;

        DepartmentBean(Long id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
    }
}
