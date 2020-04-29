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
package org.apache.myfaces.el.unified;

import jakarta.el.CompositeELResolver;
import jakarta.faces.context.FacesContext;

/**
 * The ELResolverBuilder is responsible to build the el resolver which is used by the application through
 * {@link jakarta.faces.application.Application#getELResolver()} according to 1.2 spec
 * section 5.6.2 or to be used as the el resolver for jsp
 * according to 1.2 spec section 5.6.1
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public interface ELResolverBuilder
{
    void build(CompositeELResolver elResolver);
    
    void build(FacesContext facesContext, CompositeELResolver elResolver);
}
