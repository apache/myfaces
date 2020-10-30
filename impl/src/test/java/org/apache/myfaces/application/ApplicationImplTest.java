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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

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

import junit.framework.TestCase;
import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.TestRunner;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.apache.myfaces.test.mock.MockFacesContext12;
import org.junit.Assert;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ApplicationImplTest extends TestCase
{
    //TODO: need mock objects for VDL/VDLFactory
    //remove from excludes list in pom.xml after complete
    
    protected ApplicationImpl application;
    protected MockFacesContext facesContext;

    public void setUp() throws Exception
    {
        application = new ApplicationImpl(new RuntimeConfig());
        facesContext = new MockFacesContext();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetResourceBundleNPE()
    {
        MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run()
            {
                application.getResourceBundle(null, "xxx");
            }
        });
        MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run()
            {
                application.getResourceBundle(facesContext, null);
            }
        });
    }

    /**
     * <p>
     * Test if a {@link FacesException} is thrown if the specified resource bundle can not be found.
     * </p>
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetResourceBundleFacesException()
    {
        final ApplicationImpl myApp = new ApplicationImpl(new RuntimeConfig())
        {
            @Override
            String getBundleName(FacesContext facesContext, String name)
            {
                return "bundleName";
            }
        };
        MyFacesAsserts.assertException(FacesException.class, new TestRunner()
        {
            public void run()
            {
                myApp.getResourceBundle(facesContext, "xxx");
            }
        });
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetResourceBundleWithDefaultLocale()
    {
        assertGetResourceBundleWithLocale(Locale.getDefault());
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.ApplicationImpl#getResourceBundle(jakarta.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetResourceBundleWithUIViewRootLocale()
    {
        Locale locale = new Locale("xx");
        UIViewRoot viewRoot = new UIViewRoot();
        facesContext.setViewRoot(viewRoot);
        viewRoot.setLocale(locale);
        assertGetResourceBundleWithLocale(locale);
    }

    public void testCreateComponentCallSetValueOnExpressionIfValueNull() throws Exception
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
        Assert.assertTrue(UIOutput.class.isAssignableFrom(application.createComponent(expr, context, "testComponent").getClass()));
    }

    public void testCreateComponentExpressionFacesExceptionTest() throws Exception
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
            Assert.fail("FacesException expected: " + e.getMessage());
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
                Assert.assertEquals(var, name);
                return bundleName;
            }

            @Override
            ResourceBundle getResourceBundle(String name, Locale locale, ClassLoader loader)
            {
                Assert.assertEquals(Thread.currentThread().getContextClassLoader(), loader);
                Assert.assertEquals(bundleName, name);
                Assert.assertEquals(expectedLocale, locale);
                return bundle;
            }
        };
        assertSame(bundle, myapp.getResourceBundle(facesContext, var));
    }

    private enum MyEnum {VALUE1, VALUE2}; 

    /**
     * Test method for
     * {@link jakarta.faces.application.Application#createConverter(java.lang.Class)}.
     */
    public void testCreateEnumConverter() throws Exception
    {
        application.addConverter(Enum.class, EnumConverter.class.getName());

        Converter converter = application.createConverter(MyEnum.class);
        Assert.assertNotNull(converter);
        Assert.assertEquals(converter.getClass(), EnumConverter.class);
    }    
  

    private interface EnumCoded { public int getCode(); }
    private enum AnotherEnum implements EnumCoded { 
    	VALUE1, VALUE2;
		public int getCode() {return 0;}
	};
	
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
    public void testCreateConverterForInterface() throws Exception 
    {
        application.addConverter(Enum.class, EnumConverter.class.getName());
    	application.addConverter(EnumCoded.class, EnumCodedTestConverter.class.getName());
    	
    	Converter converter = application.createConverter(AnotherEnum.class);
    	assertNotNull(converter);
        Assert.assertEquals(converter.getClass(), EnumCodedTestConverter.class);
    }
}
