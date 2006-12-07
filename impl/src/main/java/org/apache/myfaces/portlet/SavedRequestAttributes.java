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

package org.apache.myfaces.portlet;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

/**
 * This class saves the request attributes at the end of processAction.
 *
 * The attributes saved will be restored at the beginning of each render or 
 * until the session times out.  Some portlet containers don't do this
 * automatically, so it is done here.
 *
 * @author Stan Silvert
 */
public class SavedRequestAttributes implements Serializable
{
   private Map reqAttribs;
   
   /** Creates a new instance of SavedRequestAttributes */
   public SavedRequestAttributes()
   {
   }
   
   public synchronized void saveRequestAttributes(ActionRequest request)
   {
      this.reqAttribs = new HashMap();
      for (Enumeration i = request.getAttributeNames(); i.hasMoreElements();)
      {
         String name = (String)i.nextElement();
         reqAttribs.put(name, request.getAttribute(name));
      }
   }
   
   public synchronized void resotreRequestAttributes(RenderRequest request)
   {
      for (Iterator i = this.reqAttribs.keySet().iterator(); i.hasNext();)
      {
         String name = (String)i.next();
         Object attrib = request.getAttribute(name);
         
         // if somebody already set this attribute, don't overwrite
         if (attrib == null)
         {
            request.setAttribute(name, this.reqAttribs.get(name));
         }
      }
   }
}
