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
package jakarta.faces.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockValueExpression;

public class _SharedRendererUtilsTest extends AbstractJsfTestCase
{
    /**
     * A POJO used for testing. 
     */
    public static class POJO
    {
        private int id;
        private String name;
        
        public POJO(int id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public int getId()
        {
            return id;
        }
        
        public void setId(int id)
        {
            this.id = id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof POJO && ((POJO) obj).id == this.id;
        }

        @Override
        public int hashCode()
        {
            return id;
        }
    }
    
    /**
     * Converter for class POJO.
     */
    public static class POJOConverter implements Converter
    {

        public Object getAsObject(FacesContext context, UIComponent component,
                                  String value) throws ConverterException
        {
            return new POJO(new Integer(value), value);
        }

        public String getAsString(FacesContext context, UIComponent component,
                Object value) throws ConverterException
        {
            return ((POJO) value).name;
        }
        
    }
    
    /**
     * A Bean used for testing.
     */
    public static class Bean
    {
        private int[] intArrayValue;
        private Float[] floatArrayValue;
        private POJO[] pojoArrayValue;
        private Collection<POJO> pojoCollectionValue;

        public int[] getIntArrayValue()
        {
            if (intArrayValue == null)
            {
                intArrayValue = new int[0];
            }
            return intArrayValue;
        }

        public void setIntArrayValue(int[] intArrayValue)
        {
            this.intArrayValue = intArrayValue;
        }

        public Float[] getFloatArrayValue()
        {
            if (floatArrayValue == null)
            {
                floatArrayValue = new Float[0];
            }
            return floatArrayValue;
        }

        public void setFloatArrayValue(Float[] floatArrayValue)
        {
            this.floatArrayValue = floatArrayValue;
        }

        public POJO[] getPojoArrayValue()
        {
            if (pojoArrayValue == null)
            {
                pojoArrayValue = new POJO[0];
            }
            return pojoArrayValue;
        }

        public void setPojoArrayValue(POJO[] pojoArrayValue)
        {
            this.pojoArrayValue = pojoArrayValue;
        }

        public Collection<POJO> getPojoCollectionValue()
        {
            if (pojoCollectionValue == null)
            {
                pojoCollectionValue = new HashSet<POJO>();
            }
            return pojoCollectionValue;
        }

        public void setPojoCollectionValue(Collection<POJO> pojoCollectionValue)
        {
            this.pojoCollectionValue = pojoCollectionValue;
        }
    }

    private UISelectMany uiSelectMany;
    private String[] submittedValue;
    private Converter pojoConverter = new POJOConverter();
    
    public _SharedRendererUtilsTest(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        uiSelectMany = new UISelectMany();
        submittedValue = new String[]{"1", "2"};
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        uiSelectMany = null;
        submittedValue = null;
    }

    /**
     * Test case for the case that UISelectMany has neither a Converter nor a ValueExpression for value.
     * In this scenario it stores the values of submittedValue in a new object array.
     */
    public void testGetConvertedUISelectManyValueNoConverterNoValueExpression()
    {
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof Object[]);
        assertTrue(Arrays.deepEquals((Object[]) target, submittedValue));
    }
    
    /**
     * Test case for the case that UISelectMany has a ValueExpression pointing to a primitive
     * integer array and uses a standard jsf converter.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValuePrimitiveIntArray()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.intArrayValue}", int[].class);
        uiSelectMany.setValueExpression("value", expr);
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof int[]);
        int[] array = (int[]) target;
        assertTrue(new Integer(submittedValue[0]).equals(array[0]));
        assertTrue(new Integer(submittedValue[0]).equals(array[0]));
        
    }
    
    /**
     * Test case for the case that UISelectMany has a ValueExpression pointing to a non-primitive
     * Float array and uses a standard jsf converter.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValueNonPrimitiveFloatArray()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.floatArrayValue}", Float[].class);
        uiSelectMany.setValueExpression("value", expr);
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof Float[]);
        Float[] array = (Float[]) target;
        assertTrue(new Float(submittedValue[0]).equals(array[0]));
        assertTrue(new Float(submittedValue[1]).equals(array[1]));
    }
    
    /**
     * Test case for the case that UISelectMany has a ValueExpression pointing to a 
     * POJO array and uses a provided converter.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValuePOJOArray()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.pojoArrayValue}", POJO[].class);
        uiSelectMany.setValueExpression("value", expr);
        uiSelectMany.setConverter(pojoConverter);
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof POJO[]);
        POJO[] array = (POJO[]) target;
        assertTrue(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[0]).equals(array[0]));
        assertTrue(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[1]).equals(array[1]));
    }
    
    /**
     * Test case for the case that UISelectMany has a ValueExpression pointing to a 
     * Collection of POJOs and uses a provided converter. 
     * In this scenario the method does not get a hint for the right collection type, so it
     * retrieves #{bean.pojoCollectionValue} and clones that Collection.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValuePOJOCollectionWithoutCollectionType()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.pojoCollectionValue}", Collection.class);
        uiSelectMany.setValueExpression("value", expr);
        uiSelectMany.setConverter(pojoConverter);
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof Collection);
        Collection collection = (Collection) target;
        assertTrue(collection.contains(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[0])));
        assertTrue(collection.contains(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[1])));
    }
    
    /**
     * Test case for the case that UISelectMany has a ValueExpression pointing to a 
     * Collection of POJOs and uses a provided converter. 
     * In this scenario the method gets a hint for the right collection type in form
     * of the fully qualified class name of the target Collection, which it uses to
     * create the right Collection.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValuePOJOCollectionWithCollectionType()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.pojoCollectionValue}", Collection.class);
        uiSelectMany.setValueExpression("value", expr);
        uiSelectMany.setConverter(pojoConverter);
        uiSelectMany.getAttributes().put("collectionType", "java.util.HashSet");
        Object target = _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
        assertTrue(target instanceof HashSet);
        HashSet hashSet = (HashSet) target;
        assertTrue(hashSet.contains(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[0])));
        assertTrue(hashSet.contains(pojoConverter.getAsObject(facesContext, uiSelectMany, submittedValue[1])));
    }
    
    /**
     * In this test case the method can not proceed, because it is not able to
     * obtain a converter. So it throws a ConverterException.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValueNoConverter()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.pojoArrayValue}", POJO[].class);
        uiSelectMany.setValueExpression("value", expr);
        try
        {
            _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
            fail();
        }
        catch (ConverterException ce)
        {
            // success
        }
    }
    
    /**
     * In this test case the method gets a wrong hint for a collection type
     * (java.util.Collection can not be instantiated). So it throws a FacesException.
     */
    @SuppressWarnings("unchecked")
    public void testGetConvertedUISelectManyValueWrongCollectionType()
    {
        externalContext.getApplicationMap().put("bean", new Bean());
        ValueExpression expr = new MockValueExpression("#{bean.pojoCollectionValue}", Collection.class);
        uiSelectMany.setValueExpression("value", expr);
        uiSelectMany.setConverter(pojoConverter);
        uiSelectMany.getAttributes().put("collectionType", "java.util.Collection");
        try
        {
            _SharedRendererUtils.getConvertedUISelectManyValue(facesContext, uiSelectMany, submittedValue);
            fail();
        }
        catch (FacesException fe)
        {
            // success
        }
    }
}
