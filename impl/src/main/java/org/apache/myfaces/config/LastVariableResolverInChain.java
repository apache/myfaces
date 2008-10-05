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
package org.apache.myfaces.config;

import javax.faces.el.VariableResolver;
import javax.faces.el.EvaluationException;
import javax.faces.context.FacesContext;

/**
 * Represents the last Variable resolver in the chain - is added to be able to
 * through an Evaluation Exception, even if any third-party Variable Resolver is
 * added to the mix.
 *
 * @author Martin Marinschek (latest modification by $Author: dennisbyrne $)
 * @version $Revision: 375880 $ $Date: 2006-02-08 08:27:18 +0100 (Mi, 08 Feb 2006) $
 */
public class LastVariableResolverInChain extends VariableResolver
{
    private VariableResolver delegate;

    public LastVariableResolverInChain(VariableResolver variableResolver)
    {
        delegate = variableResolver;
    }

    // METHODS
    @Override
    public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException
    {
        return delegate.resolveVariable(facesContext, name);
    }
}
