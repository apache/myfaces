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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4585: {@code MethodNotFoundException} when EL treats {@code converter} as {@link String} instead of the
 * actual {@link Converter} for method calls ({@code getDisplayValue}, {@code suggest}), see
 * <a href="https://issues.apache.org/jira/browse/MYFACES-4585">MYFACES-4585</a>.
 * <p>
 * This mirrors the <a href="https://github.com/mkomko/primefaces-test/tree/expression-error-method-not-found">
 * primefaces-test</a> setup: same taglib namespace {@code http://at.test.test/jsf-taglib}, same taglib metadata,
 * {@code t:tagAttribute} for {@code value}/{@code converter} (OmniFaces {@code o:tagAttribute}), nested composites,
 * and the same EL bindings as {@code p:autoComplete} ({@code value}, {@code converter}, {@code suggest} via
 * {@code ui:repeat}, {@code var="item"}, {@code itemLabel}/{@code itemValue} stand-ins). PrimeFaces is not on the
 * classpath; {@code h:inputText} and {@code h:outputText} evaluate the same expressions.
 * </p>
 * <p>
 * Concrete subclasses set {@link MyfacesConfig#STRICT_JSF_2_FACELETS_COMPATIBILITY} to exercise both the default
 * (non-strict) user-tag handler and the legacy strict Facelets path (previously could mask taglib {@code converter}
 * shadowing).
 * </p>
 */
public abstract class AbstractMyfaces4585FaceletsIntegrationTestCase extends AbstractFaceletTestCase
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
                "/myfaces4585.taglib.xml;/test_tagattribute.taglib.xml");
        servletContext.addInitParameter(MyfacesConfig.STRICT_JSF_2_FACELETS_COMPATIBILITY,
                Boolean.toString(strictJsf2FaceletsCompatibility()));
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testTagAttributeConverterMethodNotLost4585() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("autoCompleteDepartmentConverter",
                new DepartmentConverter());
        facesContext.getExternalContext().getRequestMap().put("testView", new TestViewBean());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "myfaces4585_test.xhtml");
        String out = render(root);

        Assertions.assertTrue(out.contains("Development"), () -> out);
        Assertions.assertTrue(out.contains("Sales"), () -> out);
        Assertions.assertTrue(out.contains("departmentName"), () -> out);
    }

    public static class TestViewBean
    {
        private final Department department = new Department(1L, "Development");

        public Department getDepartment()
        {
            return department;
        }
    }

    public static class Department implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final Long id;
        private final String name;

        public Department(Long id, String name)
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

        @Override
        public String toString()
        {
            return name != null ? name : "";
        }
    }

    /**
     * Converter with extra methods used from Facelets (same pattern as primefaces-test {@code AutoCompleteBaseConverter}).
     */
    public static class DepartmentConverter implements Converter<Department>
    {
        private final List<Department> allDepartments = new ArrayList<>();

        DepartmentConverter()
        {
            allDepartments.add(new Department(1L, "Development"));
            allDepartments.add(new Department(2L, "Sales"));
            allDepartments.add(new Department(3L, "Logistics"));
        }

        @Override
        public Department getAsObject(FacesContext context, UIComponent component, String value)
        {
            if (value == null || value.isEmpty())
            {
                return null;
            }
            try
            {
                Long id = Long.parseLong(value);
                return getEntity(id);
            }
            catch (NumberFormatException e)
            {
                throw new ConverterException("Invalid id: " + value, e);
            }
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Department value)
        {
            if (value == null)
            {
                return null;
            }
            return String.valueOf(value.getId());
        }

        public String getDisplayValue(Department entity)
        {
            if (entity == null)
            {
                return "";
            }
            return displayValueOf(entity);
        }

        public List<Department> suggest(String query)
        {
            if (query == null)
            {
                return Collections.emptyList();
            }
            String q = query.toLowerCase(Locale.ROOT);
            return allDepartments.stream()
                    .filter(dep -> dep.getName().toLowerCase(Locale.ROOT).contains(q))
                    .collect(Collectors.toList());
        }

        private String displayValueOf(Department entity)
        {
            return entity.getName();
        }

        private Department getEntity(Long id)
        {
            if (id == null)
            {
                return null;
            }
            return allDepartments.stream()
                    .filter(dep -> id.equals(dep.getId()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
