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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4589: external-reproducer–style tree (strict Facelets = legacy user tag handler): nested composites, user taglib
 * {@code tst:panel} / {@code tst:icon}, and EL {@code #{icon.test}}, {@code #{color.blue}} from request beans.
 * Taglib sources use {@code t:tagAttribute}, a copy of OmniFaces {@code o:tagAttribute} ({@link
 * org.apache.myfaces.test.facelets.taghandler.TagAttributeHandler} + {@link
 * org.apache.myfaces.test.facelets.taghandler.DelegatingVariableMapper}) so tests exercise the same VariableMapper
 * behaviour without pulling in the full OmniFaces {@code faces-config.xml}.
 * <p>
 * With {@code t:tagAttribute} applied, unfixed MyFaces hits MYFACES-4589 during render. The test below therefore
 * <strong>fails</strong> until the implementation is fixed; it should <strong>pass</strong> once request beans
 * {@code color} / {@code icon} are not shadowed by taglib/composite attribute names.
 * </p>
 * <p>
 * {@code @Disabled} so the suite stays green until MYFACES-4589 production support is restored (same pattern as
 * {@link Myfaces4585FaceletsIntegrationTestCase}).
 * </p>
 */
@Disabled("MYFACES-4589: fails until taglib/composite VariableMapper behaviour is fixed (t:tagAttribute reproducer)")
public class Myfaces4589FaceletsIntegrationTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(MyfacesConfig.STRICT_JSF_2_FACELETS_COMPATIBILITY, "true");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME,
                StateManager.StateSavingMethod.SERVER.name());
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.name());
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME,
                "/myfaces4589.taglib.xml;/test_tagattribute.taglib.xml");
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    /**
     * External-reproducer shape + {@code t:tagAttribute} on user taglib sources. Fails until MYFACES-4589 is fixed:
     * {@code #{color.blue}} and {@code #{icon.test}} must resolve request beans inside nested composites.
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
