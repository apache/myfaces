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
package org.apache.myfaces.application;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.EnumConverter;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class ApplicationImplTest
{
    //TODO: need mock objects for VDL/VDLFactory
    //remove from excludes list in pom.xml after complete
    
    protected ApplicationImpl application;
    protected MockFacesContext facesContext;

    @BeforeEach
    void setUp() throws Exception
    {
        application = new ApplicationImpl(new RuntimeConfig());
        facesContext = new MockFacesContext();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    @Test
    void getResourceBundleNPE()
    {
        assertThrows(NullPointerException.class,
                () ->  application.getResourceBundle(null, "xxx"));

        assertThrows(NullPointerException.class,
                () ->  application.getResourceBundle(facesContext, null));
    }

    /**
     * <p>
     * Test if a {@link FacesException} is thrown if the specified resource bundle can not be found.
     * </p>
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    @Test
    void getResourceBundleFacesException()
    {
        final ApplicationImpl myApp = new ApplicationImpl(new RuntimeConfig())
        {
            @Override
            String getBundleName(FacesContext facesContext, String name)
            {
                return "bundleName";
            }
        };

        assertThrows(FacesException.class,
                () ->  myApp.getResourceBundle(facesContext, "xxx"));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    @Test
    void getResourceBundleWithDefaultLocale()
    {
        assertGetResourceBundleWithLocale(Locale.getDefault());
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    @Test
    void getResourceBundleWithUIViewRootLocale()
    {
        Locale locale = new Locale("xx");
        UIViewRoot viewRoot = new UIViewRoot();
        facesContext.setViewRoot(viewRoot);
        viewRoot.setLocale(locale);
        assertGetResourceBundleWithLocale(locale);
    }

    @Test
    void createComponentCallSetValueOnExpressionIfValueNull() throws Exception
    {
        ValueExpression expr = createMock(ValueExpression.class);
        FacesContext context = createMock(FacesContext.class);
        ELContext elcontext = createMock(ELContext.class);
        expect(context.getELContext()).andReturn(elcontext);
        expect(expr.getValue(elcontext)).andReturn(null);
        expr.setValue(eq(elcontext), isA(UIOutput.class));
        application.addComponent("testComponent", UIOutput.class.getName());
        replay(context);
        replay(expr);
        assertTrue(UIOutput.class.isAssignableFrom(application.createComponent(expr, context, "testComponent").getClass()));
    }

    @Test
    void createComponentExpressionFacesExceptionTest() throws Exception
    {
        ValueExpression expr = createMock(ValueExpression.class);
        FacesContext context = createMock(FacesContext.class);
        ELContext elcontext = createMock(ELContext.class);
        expect(context.getELContext()).andReturn(elcontext);
        expect(expr.getValue(elcontext)).andThrow(new IllegalArgumentException());
        replay(context);
        replay(expr);
        try
        {
            application.createComponent(expr, context, "testComponent");
        }
        catch (FacesException e)
        {
            // ok
        }
        catch (Throwable e)
        {
            fail("FacesException expected: " + e.getMessage());
        }
    }

    private void assertGetResourceBundleWithLocale(final Locale expectedLocale)
    {
        final String var = "test";
        final String bundleName = "bundleName";
        final ResourceBundle bundle = new ListResourceBundle()
        {
            @Override
            protected Object[][] getContents()
            {
                return null;
            }
        };
        ApplicationImpl myapp = new ApplicationImpl(new RuntimeConfig())
        {
            @Override
            String getBundleName(FacesContext facesContext, String name)
            {
                assertEquals(var, name);
                return bundleName;
            }

            @Override
            ResourceBundle getResourceBundle(String name, Locale locale, ClassLoader loader)
            {
                assertEquals(Thread.currentThread().getContextClassLoader(), loader);
                assertEquals(bundleName, name);
                assertEquals(expectedLocale, locale);
                return bundle;
            }
        };
        assertSame(bundle, myapp.getResourceBundle(facesContext, var));
    }

    private enum MyEnum {VALUE1, VALUE2}

    /**
     * Test method for
     * {@link jakarta.faces.application.Application#createConverter(java.lang.Class)}.
     */
    @Test
    void createEnumConverter() throws Exception
    {
        application.addConverter(Enum.class, EnumConverter.class.getName());

        Converter converter = application.createConverter(MyEnum.class);
        assertNotNull(converter);
        assertEquals(EnumConverter.class, converter.getClass());
    }    
  

    private interface EnumCoded { public int getCode(); }

    private enum AnotherEnum implements EnumCoded { 
    	VALUE1, VALUE2;
		public int getCode() {return 0;}
	}

    public static class EnumCodedTestConverter implements Converter
	{

        public EnumCodedTestConverter()
        {
        }

        public Object getAsObject(FacesContext context, UIComponent component,
                String value) throws ConverterException
        {
            return null;
        }

        public String getAsString(FacesContext context, UIComponent component,
                Object value) throws ConverterException
        {
            return null;
        }
	}

    /**
     * Test method for
     * {@link jakarta.faces.application.Application#createConverter(java.lang.Class)}.
     * <p>
     * Tests the situation when a object is both, an enum and an implementor of an
     * interface for which we have a specific converter registered. 
     * The interface should take precedence over the fact that our object is also
     * an enum.
     */
    @Test
    void createConverterForInterface() throws Exception 
    {
        application.addConverter(Enum.class, EnumConverter.class.getName());
    	application.addConverter(EnumCoded.class, EnumCodedTestConverter.class.getName());
    	
    	Converter converter = application.createConverter(AnotherEnum.class);
    	assertNotNull(converter);
        assertEquals(EnumCodedTestConverter.class, converter.getClass());
    }
}
