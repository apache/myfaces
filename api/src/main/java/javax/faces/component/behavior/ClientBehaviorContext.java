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
package javax.faces.component.behavior;

import java.util.Collection;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 15:23:50 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class ClientBehaviorContext
{
    public static ClientBehaviorContext createBehaviorContext(FacesContext context, UIComponent component, String eventName,
                                                        String sourceId, Collection<Parameter> parameters)
    {
        // TODO: IMPLEMENT HERE
        // This method is weird... Creating a dummy impl class seems stupid, yet I don't see any other way...
        return null;
    }

    public abstract UIComponent getComponent();

    public abstract String getEventName();

    public abstract FacesContext getFacesContext();

    public abstract Collection<Parameter> getParameters();

    public abstract String getSourceId();

    /**
     * @author Simon Lessard (latest modification by $Author: slessard $)
     * @version $Revision: 696523 $ $Date: 2009-03-14 15:15:41 -0400 (mer., 17 sept. 2008) $
     * 
     * @since 2.0
     */
    public static class Parameter
    {
        private String _name;
        private Object _value;

        public Parameter(String name, Object value)
        {
            if (name == null)
            {
                throw new NullPointerException("name");
            }

            _name = name;
            _value = value;
        }

        public String getName()
        {
            return _name;
        }

        public Object getValue()
        {
            return _value;
        }
    }
}
