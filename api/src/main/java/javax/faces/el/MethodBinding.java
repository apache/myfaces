/*
 * Copyright 2004 The Apache Software Foundation.
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
package javax.faces.el;

import javax.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @deprecated
 */
public abstract class MethodBinding
{

	// FIELDS

	// CONSTRUCTORS
        /**
         * @deprecated
         */
	public MethodBinding()
	{
	}

	// METHODS
	
	/**
	 * returns the <code>String</code> of your expression like <code>#{bean.foo}</code>.
         * @deprecated
	 */
	public String getExpressionString()
	{
        return null;
	}
    
    /**
     * @deprecated
     */
    public abstract Class getType(FacesContext facescontext) throws MethodNotFoundException;

    /**
     * @deprecated
     */
    public abstract Object invoke(FacesContext facescontext, Object aobj[])
        throws EvaluationException, MethodNotFoundException;
}
