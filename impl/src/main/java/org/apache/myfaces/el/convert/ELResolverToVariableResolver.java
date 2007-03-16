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

package org.apache.myfaces.el.convert;

import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

/**
 * Provides ELResolver wrapper so that legacy apps which rely on a 
 * VariableResolver can still work.
 *
 * @author Stan Silvert
 */
public class ELResolverToVariableResolver extends VariableResolver {
    
    private ELResolver elResolver;
    
    /**
     * Creates a new instance of ELResolverToVariableResolver
     */
    public ELResolverToVariableResolver(ELResolver elResolver) {
        if (elResolver == null) throw new NullPointerException();
        this.elResolver = elResolver;
    }

    public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException {
        
        try {
            return elResolver.getValue(facesContext.getELContext(), null, name);
        } catch (PropertyNotFoundException e) {
            throw new EvaluationException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }
    
}
