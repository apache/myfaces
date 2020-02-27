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

package org.apache.myfaces.test.util;

import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;

public class Jsf12Utils
{

  public static void initView(FacesContext facesContext, ViewHandler viewHandler)
  {
    viewHandler.initView(facesContext);
  }

  public static void throwViewExpiredException(String viewId)
  {
    throw new ViewExpiredException(
        "The expected view was not returned for the view identifier: " + viewId, viewId);
  }
}
