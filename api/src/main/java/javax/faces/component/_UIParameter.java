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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * 
 */
@JSFComponent
(clazz = "javax.faces.component.UIParameter",template=true)
abstract class _UIParameter extends UIComponentBase
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.Parameter";
  static public final String COMPONENT_TYPE =
    "javax.faces.Parameter";

  /**
   * Disable this property; although this class extends a base-class that
   * defines a read/write rendered property, this particular subclass
   * does not support setting it. Yes, this is broken OO design: direct
   * all complaints to the JSF spec group.
   */
  @JSFProperty(tagExcluded=true)
  public void setRendered(boolean state) {
      throw new UnsupportedOperationException();
  }
  
  /**
   * The value of this component.
   *
   * @return  the new value value
   */
  @JSFProperty
  public abstract Object getValue();

  /**
   * The name under which the value is stored.
   *
   * @return  the new name value
   */
  @JSFProperty
  public abstract String getName();

}
