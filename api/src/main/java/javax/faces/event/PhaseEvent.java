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
package javax.faces.event;

import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import java.util.EventObject;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class PhaseEvent extends EventObject
{
    private static final long serialVersionUID = -7235692965954486239L;
    // FIELDS
    private FacesContext _facesContext;
    private PhaseId _phaseId;

	// CONSTRUCTORS
	public PhaseEvent(FacesContext facesContext, PhaseId phaseId, Lifecycle lifecycle)
	{
		super(lifecycle);
        if (facesContext == null) throw new NullPointerException("facesContext");
        if (phaseId == null) throw new NullPointerException("phaseId");
        if (lifecycle == null) throw new NullPointerException("lifecycle");

        _facesContext = facesContext;
        _phaseId = phaseId;
	}

	// METHODS
	public FacesContext getFacesContext()
	{
		return _facesContext;
	}

	public PhaseId getPhaseId()
	{
		return _phaseId;
	}

}
