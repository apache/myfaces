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
package javax.faces.webapp.pdl.facelets;

import java.io.IOException;
import java.net.URL;

import javax.el.ELContext;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-12-10 18:39:00 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public abstract class FaceletContext extends ELContext
{
    // TODO: Report that aberration to the EG
    public static final String FACELET_CONTEXT_KEY = "com.sun.faces.facelets.FACELET_CONTEXT";
    
    public abstract String generateUniqueId(String base);
    
    public abstract Object getAttribute(String name);
    
    public abstract javax.el.ExpressionFactory getExpressionFactory();
    
    public abstract FacesContext getFacesContext();
    
    public abstract void includeFacelet(UIComponent parent, String relativePath) throws IOException;
    
    public abstract void includeFacelet(UIComponent parent, URL absolutePath) throws IOException;
    
    public abstract void setAttribute(String name, Object value);
    
    public abstract void setFunctionMapper(FunctionMapper mapper);
    
    public abstract void setVariableMapper(VariableMapper mapper);
}
