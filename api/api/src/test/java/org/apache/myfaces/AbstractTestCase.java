/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.myfaces;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

/**
 * Base class that unit tests can extend to ensure the current FacesContext
 * is corrects set up and torn down around tests.
 * <p>
 * When this class is used as a test case's base class, the setup method
 * verifies before each test that the current FacesContext is null, so that
 * a badly-written preceding test won't mysteriously break the test. In
 * addition, after the test is complete the current FacesContext is set back
 * to null, so that the test won't break following ones.
 */
public abstract class AbstractTestCase extends TestCase {

  public AbstractTestCase(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    if (FacesContext.getCurrentInstance() != null) {
    	throw new IllegalStateException("faces context not null");
    }
    
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
	FacesContextHelper.setCurrentInstance(null);
  }
}
