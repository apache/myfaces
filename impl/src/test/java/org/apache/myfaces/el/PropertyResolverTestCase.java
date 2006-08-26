/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
package org.apache.myfaces.el;

import javax.faces.el.PropertyResolver;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.el.data.B;
import org.apache.shale.test.base.AbstractJsfTestCase;

public class PropertyResolverTestCase extends AbstractJsfTestCase
{

  public PropertyResolverTestCase(String arg0) {
    super(arg0);
  }

  public void setUp() 
  {
    super.setUp();
    PropertyResolver pr = new PropertyResolverImpl();
    facesContext.getApplication().setPropertyResolver(pr);
  }

  public void tearDown() 
  {
    // TODO Auto-generated method stub
    super.tearDown();
  }
  
  public static Test suite()
  {
    return new TestSuite(PropertyResolverTestCase.class);
  }
  
  public void testComplexBean() throws Exception
  {
    B b = new B();
    facesContext.getExternalContext().getSessionMap().put("b", b);
    B o = (B) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "b");
    assertEquals(o.getFirst(), "First");
    facesContext.getApplication().getPropertyResolver().setValue(b, "first", "LALA");
    o = (B) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "b");
    assertEquals(o.getFirst(), "LALA");
    
  }
}