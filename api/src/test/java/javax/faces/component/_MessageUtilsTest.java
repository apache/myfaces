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

package javax.faces.component;

import javax.faces.FacesException;

import junit.framework.TestCase;

public class _MessageUtilsTest extends TestCase
{

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(_MessageUtilsTest.class);
  }

  public _MessageUtilsTest(String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
  }

  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  public void testGetCause() throws Exception
  {
	  IllegalStateException e1 = new IllegalStateException("e1");
	  FacesException e2 = new FacesException("e2");
	  FacesException e3 = new FacesException("e3", e1);
	  
	  Throwable t1 = _MessageUtils.getCause(e1);
	  assertNull(t1);
	  
	  Throwable t2 = _MessageUtils.getCause(e2);
	  assertNull(t2);
	  
	  Throwable t3 = _MessageUtils.getCause(e3);
	  assertSame(e1, t3);
  }
}
